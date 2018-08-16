import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.PermissionUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UtilityCommands
{
    private final String YES = "\u2705";
    private final String NO = "\u274C";
    private String prefix;

    public UtilityCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Give user info
        map.put("userinfo", new Command("userinfo", "Gathers the requester's info", prefix + "userinfo [@user]", AccessLevel.EVERYONE, false, (event, args) ->
        {
            EmbedBuilder builder = new EmbedBuilder();
            IUser user;
            //Checks which user in the server to get info of
            if (args.size() == 0)
            {
                user = event.getAuthor();
            }
            else
            {
                long id = BotUtils.getID(args.get(0));
                //Finds the user given
                if (id >= 0)
                {
                    if (event.getGuild() == null)
                    {
                        BotUtils.sendMessage(event.getChannel(), "That user does not exist here!");
                        return;
                    }
                    user = event.getGuild().getUserByID(id);
                    if (user == null)
                    {
                        BotUtils.sendMessage(event.getChannel(), "That user does not exist here!");
                        return;
                    }
                }
                else
                {
                    map.get("help").execute(event, args);
                    return;
                }
            }

            //Checks for DM
            if (event.getGuild() == null)
            {
                builder.appendField("ID", "" + user.getLongID(), false);
                builder.appendField("Account Creation", BotUtils.formatDate(user.getCreationDate()), false);

                builder.withColor(BotUtils.DEFAULT_COLOR);
            }
            else
            {
                IGuild g = event.getGuild();

                //Add details about user
                if (user.getNicknameForGuild(g) != null)
                {
                    builder.appendField("Nickname", user.getNicknameForGuild(g), false);
                }
                builder.appendField("ID", "" + user.getLongID(), false);
                builder.appendField("Account Creation", BotUtils.formatDate(user.getCreationDate()), false);
                builder.appendField(g.getName() + " Join Date", BotUtils.formatDate(g.getJoinTimeForUser(user)), false);
                List<IRole> roles = user.getRolesForGuild(g);

                roles = orderRoles(roles, g.getRoles());

                //Lists all roles except @everyone
                String r = "";
                for (int i = 0; i < roles.size() - 1; i++)
                {
                    r += roles.get(i) + ", ";
                }
                if (roles.size() > 1)
                    builder.appendField("Roles", r.substring(0, r.length() - 2), false);
                else
                    builder.appendField("Roles", "None", false);

                builder.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
            }

            builder.withTitle(user.getName() + "#" + user.getDiscriminator());
            builder.withThumbnail(user.getAvatarURL());
            BotUtils.sendMessage(event.getChannel(), builder.build());
        }));

        map.put("role", new Command("role", "Add or remove a role", prefix + "role", AccessLevel.EVERYONE, false, new Command[]
                {
                        new Command("set", "Sets a selectable role to be added with a code. Code cannot have spaces. Add auto to have the role added upon join.", "set [auto] <role id> <role code>", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (event.getGuild() != null && (args.size() == 3 || args.size() == 4))
                            {
                                boolean auto = false;
                                if (args.size() == 4 && args.get(1).equals("auto"))
                                {
                                    args.remove(1);
                                    auto = true;
                                }

                                long id = BotUtils.getID(args.get(1));
                                IRole role = event.getGuild().getRoleByID(id);
                                if (role == null)
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please enter a valid role");
                                    return;
                                }
                                List<IRole> check = new ArrayList<>();
                                check.add(role);
                                if (!PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), check))
                                {
                                    BotUtils.sendMessage(event.getChannel(),"You do not have permissions to grant that role");
                                    return;
                                }

                                String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ?";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getGuild().getLongID());
                                params.add(args.get(2));
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    BotUtils.sendMessage(event.getChannel(), "There is already a role with this code. Please use `" + prefix + "set " + set.getLong("RoleID") + " <code>` with a new code to use this code.");
                                    return;
                                }

                                sql = "SELECT * FROM DiscordDB.Roles WHERE GuildID = ? AND RoleID = ?";
                                params.clear();
                                params.add(event.getGuild().getLongID());
                                params.add(id);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    if (auto)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "You cannot set a requestable role as automatic.");
                                        return;
                                    }
                                    sql = "UPDATE DiscordDB.Roles SET Code = ? WHERE GuildID = ? AND RoleID = ?";
                                    params.add(0, args.get(2));
                                    JDBCConnection.getStatement(sql, params).executeUpdate();
                                }
                                else
                                {
                                    if (auto)
                                        sql = "INSERT INTO DiscordDB.Roles (GuildID, RoleID, Code, Auto) VALUES (?, ?, ?, true)";
                                    else
                                        sql = "INSERT INTO DiscordDB.Roles (GuildID, RoleID, Code, Auto) VALUES (?, ?, ?, false)";
                                    params.add(args.get(2));
                                    JDBCConnection.getStatement(sql, params).executeUpdate();
                                }
                                BotUtils.sendMessage(event.getChannel(), role.getName() + " was updated with code " + args.get(2));
                            }
                        })),

                        new Command("delete", "Deletes a selectable role, no longer making it selectable", "delete <role code>", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (event.getGuild() != null && args.size() == 2)
                            {
                                String sql = "DELETE FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ?";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getGuild().getLongID());
                                params.add(args.get(1));
                                JDBCConnection.getStatement(sql, params).executeUpdate();
                                BotUtils.sendMessage(event.getChannel(), "Requestable role deleted.");
                            }
                        })),

                        new Command("add", "Adds a role", "add <role>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            if (event.getGuild() != null && args.size() == 2)
                            {
                                String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ? AND Auto is false";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getGuild().getLongID());
                                params.add(args.get(1));
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    long id = set.getLong("RoleID");
                                    IRole role = event.getGuild().getRoleByID(id);
                                    if (role != null && event.getAuthor().hasRole(role))
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "You already have this role.");
                                        return;
                                    }

                                    List<IChannel> channels = event.getGuild().getChannels();
                                    for (int i = 0; i < channels.size(); i++)
                                    {
                                        IChannel channel = channels.get(i);
                                        if (channel.isNSFW() && channel.getRoleOverrides().get(id) != null && channel.getRoleOverrides().get(id).allow().contains(Permissions.READ_MESSAGES))
                                        {
                                            IMessage message = BotUtils.sendMessage(event.getChannel(), "This role grants access to NSFW channels. Do you still wish to add it?");
                                            message.addReaction(ReactionEmoji.of(YES));
                                            boolean x = true;
                                            do
                                            {
                                                try
                                                {
                                                    Thread.sleep(250);
                                                    x = false;
                                                }
                                                catch (InterruptedException e)
                                                {
                                                }
                                            }
                                            while (x);
                                            message.addReaction(ReactionEmoji.of(NO));
                                            x = true;
                                            do
                                            {
                                                try
                                                {
                                                    Thread.sleep(250);
                                                    x = false;
                                                }
                                                catch (InterruptedException e)
                                                {
                                                }
                                            }
                                            while (x);
                                            event.getClient().getDispatcher().registerListener(new NSFWListener(message.getLongID(), event.getAuthor().getLongID(), id));
                                            return;
                                        }
                                    }
                                    if (role != null)
                                    {
                                        event.getAuthor().addRole(role);
                                        BotUtils.sendMessage(event.getChannel(), role.getName() + " was added!");
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "Please enter a valid role");
                                    }
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please enter a valid role.");
                                }
                            }
                        })),

                        new Command("remove", "Removes a role", "remove <role>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            if (event.getGuild() != null && args.size() == 2)
                            {
                                String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ? AND Auto is false";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getGuild().getLongID());
                                params.add(args.get(1));
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    long id = set.getLong("RoleID");
                                    IRole role = event.getGuild().getRoleByID(id);
                                    if (role != null && !event.getAuthor().hasRole(role))
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "You do not have this role.");
                                        return;
                                    }
                                    if (role != null)
                                    {
                                        event.getAuthor().removeRole(role);
                                        BotUtils.sendMessage(event.getChannel(), role.getName() + " was removed!");
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "Please enter a valid role");
                                    }
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please enter a valid role.");
                                }
                            }
                        })),

                        new Command("list", "List requestable roles", "list", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            if (event.getGuild() != null && args.size() == 1)
                            {
                                String sql = "SELECT RoleID, Code, Auto FROM DiscordDB.Roles WHERE GuildID = ?";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getGuild().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withTitle("Requestable roles");
                                if (set.next())
                                {
                                    set.previous();
                                    while (set.next())
                                    {
                                        IRole role = event.getGuild().getRoleByID(set.getLong("RoleID"));
                                        boolean auto = set.getBoolean("Auto");
                                        String title = set.getString("Code");
                                        if (auto)
                                            title = "[auto] " + title;
                                        if (role == null)
                                        {
                                            builder.appendField(title, "Invalid Role", true);
                                        }
                                        else
                                        {
                                            builder.appendField(title, role.mention(),true);
                                        }
                                    }
                                }
                                else
                                {
                                    builder.withDescription("No available requestable roles");
                                }
                                BotUtils.sendMessage(event.getChannel(), builder.build());
                            }
                        }))
                },
                (event, args) ->
                {
                    BotUtils.help(map, event, args, "role");
                })
        );

        map.put("get", new Command("get", "Get info", prefix + "get", AccessLevel.EVERYONE, false, new Command[]
                {
                        new Command("guild", "Guild info", "guild [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IGuild guild;
                            if (args.size() >= 2)
                            {
                                String g = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(g);
                                if (id >= 0)
                                {
                                    guild = event.getClient().getGuildByID(id);
                                }
                                else
                                {
                                    List<IGuild> guilds = event.getClient().getGuilds();
                                    guild = null;
                                    for (int i = 0; i < guilds.size(); i++)
                                    {
                                        if (guilds.get(i).getName().equals(g))
                                        {
                                            guild = guilds.get(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                guild = event.getGuild();
                            }
                            if (guild == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Guild is not available");
                                return;
                            }

                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle(guild.getName());
                            builder.withThumbnail(guild.getIconURL());
                            builder.withColor(BotUtils.DEFAULT_COLOR);
                            builder.appendField("Owner", guild.getOwner().getName() + "#" + guild.getOwner().getDiscriminator(), true);
                            builder.appendField("ID", "" + guild.getLongID(), true);
                            builder.appendField("Creation Date", BotUtils.formatDate(guild.getCreationDate()), true);
                            builder.appendField("Members", "" + guild.getTotalMemberCount(), true);

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        new Command("role", "Role info", "role <id/name>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IRole role = null;
                            IGuild guild = event.getGuild();
                            if (args.size() >= 2 && guild != null)
                            {
                                String r = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(r);
                                if (id >= 0)
                                {
                                    role = guild.getRoleByID(id);
                                }
                                else
                                {
                                    List<IRole> roles = guild.getRoles();
                                    for (int i = 0; i < roles.size(); i++)
                                    {
                                        if (roles.get(i).getName().equals(r))
                                        {
                                            role = roles.get(i);
                                            break;
                                        }
                                    }
                                }

                                if (role == null)
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Role is not available");
                                    return;
                                }

                                int count =  0;
                                List<IUser> users = guild.getUsers();
                                for (int i = 0; i < users.size(); i++)
                                {
                                    if (users.get(i).hasRole(role))
                                        count++;
                                }
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withTitle(role.getName());
                                builder.withColor(role.getColor());
                                builder.appendField("ID", "" + role.getLongID(), true);
                                builder.appendField("Creation Date", BotUtils.formatDate(role.getCreationDate()), true);
                                builder.appendField("Users", "" + count, true);

                                BotUtils.sendMessage(event.getChannel(), builder.build());
                            }
                            else
                            {
                                BotUtils.sendMessage(event.getChannel(), "Role is not available");
                                return;
                            }
                        })),

                        new Command("channel", "Channel info", "channel [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IChannel channel = null;
                            IGuild guild = event.getGuild();
                            if (args.size() >= 2 && guild != null)
                            {
                                String c = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(c);
                                if (id >= 0)
                                {
                                    channel = guild.getChannelByID(id);
                                }
                                else
                                {
                                    List<IChannel> channels = guild.getChannels();
                                    for (int i = 0; i < channels.size(); i++)
                                    {
                                        if (channels.get(i).getName().equals(c))
                                        {
                                            channel = channels.get(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                channel = event.getChannel();
                            }
                            if (channel == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Channel is not available");
                                return;
                            }

                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle("#" + channel.getName());
                            builder.withColor(BotUtils.DEFAULT_COLOR);
                            if (guild != null)
                            {
                                builder.appendField("ID", "" + channel.getLongID(), true);
                                if (channel.getCategory() != null)
                                    builder.appendField("Category", channel.getCategory().getName(), true);
                                builder.appendField("Creation Date", BotUtils.formatDate(channel.getCreationDate()), true);
                            }
                            else
                            {
                                builder.appendField("ID", "" + channel.getLongID(), true);
                                builder.appendField("Creation Date", BotUtils.formatDate(channel.getCreationDate()), true);
                            }

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        new Command("user", "User info", "user [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IUser user = null;
                            IGuild guild = null;
                            if (args.size() >= 2)
                            {
                                String u = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(u);
                                if (id >= 0)
                                {
                                    if (event.getGuild() != null)
                                        user = event.getGuild().getUserByID(id);
                                    if (user == null)
                                    {
                                        user = event.getClient().getUserByID(id);
                                    }
                                    else
                                    {
                                        guild = event.getGuild();
                                    }
                                }
                                else if (event.getGuild() != null)
                                {
                                    String[] separate = BotUtils.separateTag(u);
                                    List<IUser> users = event.getGuild().getUsers();
                                    user = null;
                                    for (int i = 0; i < users.size(); i++)
                                    {
                                        if (users.get(i).getName().equals(separate[0]) && (separate[1].equals("") || users.get(i).getDiscriminator().equals(separate[1])))
                                        {
                                            user = users.get(i);
                                            guild = event.getGuild();
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                user = event.getAuthor();
                                guild = event.getGuild();
                            }
                            if (user == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "User is not available");
                                return;
                            }

                            EmbedBuilder builder = new EmbedBuilder();

                            if (guild != event.getGuild() || event.getGuild() == null)
                            {
                                builder.appendField("ID", "" + user.getLongID(), false);
                                builder.appendField("Account Creation", BotUtils.formatDate(user.getCreationDate()), false);

                                builder.withColor(BotUtils.DEFAULT_COLOR);
                            }
                            else
                            {
                                //Add details about user
                                if (user.getNicknameForGuild(guild) != null)
                                {
                                    builder.appendField("Nickname", user.getNicknameForGuild(guild), false);
                                }
                                builder.appendField("ID", "" + user.getLongID(), false);
                                builder.appendField("Account Creation", BotUtils.formatDate(user.getCreationDate()), false);
                                builder.appendField(guild.getName() + " Join Date", BotUtils.formatDate(guild.getJoinTimeForUser(user)), false);
                                List<IRole> roles = user.getRolesForGuild(guild);

                                roles = orderRoles(roles, guild.getRoles());

                                //Lists all roles except @everyone
                                String r = "";
                                for (int i = 0; i < roles.size() - 1; i++)
                                {
                                    r += roles.get(i) + ", ";
                                }
                                if (roles.size() > 1)
                                    builder.appendField("Roles", r.substring(0, r.length() - 2), false);
                                else
                                    builder.appendField("Roles", "None", false);

                                builder.withColor(event.getAuthor().getColorForGuild(event.getGuild()));
                            }

                            builder.withTitle(user.getName() + "#" + user.getDiscriminator());
                            builder.withThumbnail(user.getAvatarURL());
                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        }))
                },
                (event, args) ->
                {

                })
        );
    }

    //Orders user's roles in order of the server roles
    private List<IRole> orderRoles(List<IRole> roles1, List<IRole> roles2)
    {
        List<IRole> r = new ArrayList<>();
        for (int i = roles2.size() - 1; i >= 0; i--)
        {
            if (roles1.contains(roles2.get(i)))
            {
                r.add(roles2.get(i));
            }
        }

        return r;
    }

    private class NSFWListener implements IListener<ReactionAddEvent>
    {
        private long messageID, userID, roleID;
        private boolean triggered;
        public NSFWListener(long m, long u, long r)
        {
            messageID = m;
            userID = u;
            roleID = r;
            triggered = false;
        }

        public void handle(ReactionAddEvent reactionAddEvent)
        {
            if (triggered)
            {
                reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
                return;
            }
            if (reactionAddEvent.getMessageID() == messageID && reactionAddEvent.getUser().getLongID() == userID)
            {
                if (reactionAddEvent.getReaction().getEmoji().getName().equals(YES))
                {
                    IRole role = reactionAddEvent.getGuild().getRoleByID(roleID);
                    reactionAddEvent.getUser().addRole(role);
                    BotUtils.sendMessage(reactionAddEvent.getChannel(), role.getName() + " was added!");
                    triggered = true;
                    reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
                }
                else if (reactionAddEvent.getReaction().getEmoji().getName().equals(NO))
                {
                    BotUtils.sendMessage(reactionAddEvent.getChannel(), reactionAddEvent.getGuild().getRoleByID(roleID).getName() + " was not added.");
                    triggered = true;
                    reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
                }
            }
        }
    }
}