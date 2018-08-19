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

@SuppressWarnings({"WeakerAccess", "StringConcatenationInLoop", "ConstantConditions", "CodeBlock2Expr"})
public class UtilityCommands
{
    private final String YES = "\u2705";
    private final String NO = "\u274C";
    private String prefix;

    public UtilityCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Give user info
        map.put("userinfo", new Command("userinfo", "Gathers the requester's info", prefix + "userinfo [@user]", AccessLevel.DEACTIVATED, false, (event, args) ->
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

        //Manages selectable and auto-assigned roles
        map.put("role", new Command("role", "Add or remove a role", prefix + "role", AccessLevel.EVERYONE, false, new Command[]
                {
                        //Create a new selectable or auto role, or alter one
                        new Command("set", "Sets a selectable/auto-assigned role to be added with a code. Code cannot have spaces. Add `auto` argument to have the role added upon join.", "set [auto] <role id> <role code>", AccessLevel.MODERATOR, false, ((event, args) ->
                        {
                            //If in DMs or invalid arguments, do nothing
                            if (event.getGuild() == null)
                            {
                                return;
                            }
                            if (args.size() < 3 || args.size() > 4)
                            {
                                BotUtils.help(map, event, args, "role");
                                return;
                            }

                            //Checks if auto argument is included
                            boolean auto = false;
                            if (args.size() == 4 && args.get(1).equals("auto"))
                            {
                                args.remove(1);
                                auto = true;
                            }

                            //Get id from arguments
                            long id = BotUtils.getID(args.get(1));
                            IRole role = event.getGuild().getRoleByID(id);
                            //If invalid id/role, send error message
                            if (role == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Please enter a valid role");
                                return;
                            }
                            List<IRole> check = new ArrayList<>();
                            check.add(role);
                            //If user cannot grant role themselves, do not make it assignable
                            if (!PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), check))
                            {
                                BotUtils.sendMessage(event.getChannel(),"You do not have permissions to grant that role");
                                return;
                            }

                            //Find if there is already a role with the same code
                            String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ?";
                            List<Object> params = new ArrayList<>();
                            params.add(event.getGuild().getLongID());
                            params.add(args.get(2));
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                            //If there exists a role with this code already, do not add it
                            if (set.next())
                            {
                                BotUtils.sendMessage(event.getChannel(), "There is already a role with this code. Please use `" + prefix + "set " + set.getLong("RoleID") + " <code>` with a new code to use this code.");
                                return;
                            }

                            //Select role to check if it's already assigned a code
                            sql = "SELECT * FROM DiscordDB.Roles WHERE GuildID = ? AND RoleID = ?";
                            params.clear();
                            params.add(event.getGuild().getLongID());
                            params.add(id);
                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                            //If role already exists, update the code
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
                                sql = "INSERT INTO DiscordDB.Roles (GuildID, RoleID, Code, Auto) VALUES (?, ?, ?, ?)";
                                params.add(args.get(2));
                                params.add(auto);
                                JDBCConnection.getStatement(sql, params).executeUpdate();
                            }
                            BotUtils.sendMessage(event.getChannel(), role.getName() + " was updated with code " + args.get(2));
                        })),

                        new Command("delete", "Deletes a selectable/auto-assigned role, no longer making it available", "delete <role code>", AccessLevel.MODERATOR, false, ((event, args) ->
                        {
                            //If in DMs or invalid arguments, do nothing
                            if (event.getGuild() == null)
                            {
                                return;
                            }
                            if (args.size() != 2)
                            {
                                BotUtils.help(map, event, args, "role");
                                return;
                            }

                            //Delete role with code
                            String sql = "DELETE FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ?";
                            List<Object> params = new ArrayList<>();
                            params.add(event.getGuild().getLongID());
                            params.add(args.get(1));
                            JDBCConnection.getStatement(sql, params).executeUpdate();
                            BotUtils.sendMessage(event.getChannel(), "Requestable role deleted.");
                        })),

                        //Add a selectable role
                        new Command("add", "Adds a role", "add <role>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            //If in DMs or invalid arguments, do nothing
                            if (event.getGuild() == null)
                            {
                                return;
                            }
                            if (args.size() != 2)
                            {
                                BotUtils.help(map, event, args, "role");
                                return;
                            }

                            //Select role to assign
                            String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ? AND Auto is false";
                            List<Object> params = new ArrayList<>();
                            params.add(event.getGuild().getLongID());
                            params.add(args.get(1));
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                            //If role is not in table
                            if (!set.next())
                            {
                                BotUtils.sendMessage(event.getChannel(), "Please enter a valid role.");
                                return;
                            }

                            long id = set.getLong("RoleID");
                            IRole role = event.getGuild().getRoleByID(id);
                            if (role == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "This role does not exist.");
                                return;
                            }
                            //Check if user has role
                            if (event.getAuthor().hasRole(role))
                            {
                                BotUtils.sendMessage(event.getChannel(), "You already have this role.");
                                return;
                            }

                            List<IChannel> channels = event.getGuild().getChannels();
                            //Loop through channels to determine if role grants read permissions to an NSFW channel
                            for (IChannel channel: channels)
                            {
                                //Checks if NSFW and read permissions
                                if (channel.isNSFW() && channel.getRoleOverrides().get(id) != null && channel.getRoleOverrides().get(id).allow().contains(Permissions.READ_MESSAGES))
                                {
                                    //Send verification message
                                    IMessage message = BotUtils.sendMessage(event.getChannel(), "This role grants access to NSFW channels. Do you still wish to add it?");
                                    BotUtils.addReaction(message, ReactionEmoji.of(YES));
                                    BotUtils.addReaction(message, ReactionEmoji.of(NO));

                                    //Adds message verification listener
                                    event.getClient().getDispatcher().registerListener(new NSFWListener(message.getLongID(), event.getAuthor().getLongID(), id));
                                    return;
                                }
                            }
                            event.getAuthor().addRole(role);
                            BotUtils.sendMessage(event.getChannel(), role.getName() + " was added!");
                        })),

                        new Command("remove", "Removes a role", "remove <role>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            //If in DMs or invalid arguments, do nothing
                            if (event.getGuild() == null)
                            {
                                return;
                            }
                            if (args.size() != 2)
                            {
                                BotUtils.help(map, event, args, "role");
                                return;
                            }

                            //Select role to remove
                            String sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Code = ? AND Auto is false";
                            List<Object> params = new ArrayList<>();
                            params.add(event.getGuild().getLongID());
                            params.add(args.get(1));
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                            //If role is not in table
                            if (!set.next())
                            {
                                BotUtils.sendMessage(event.getChannel(), "Please enter a valid role.");
                                return;
                            }

                            long id = set.getLong("RoleID");
                            IRole role = event.getGuild().getRoleByID(id);
                            if (role == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "This role does not exist.");
                                return;
                            }
                            //Check if user has role
                            if (!event.getAuthor().hasRole(role))
                            {
                                BotUtils.sendMessage(event.getChannel(), "You do not have this role.");
                                return;
                            }

                            event.getAuthor().removeRole(role);
                            BotUtils.sendMessage(event.getChannel(), role.getName() + " was removed!");
                        })),

                        new Command("list", "List requestable roles", "list", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            //If in DMs or invalid arguments, do nothing
                            if (event.getGuild() == null)
                            {
                                return;
                            }
                            if (args.size() != 1)
                            {
                                BotUtils.help(map, event, args, "role");
                                return;
                            }

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
                        }))
                },

                (event, args) ->
                {
                    BotUtils.help(map, event, args, "role");
                })
        );

        //Gets info on different details about a user or guild
        map.put("get", new Command("get", "Get info", prefix + "get", AccessLevel.EVERYONE, false, new Command[]
                {
                        //Gets guild details
                        new Command("guild", "Guild info", "guild [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IGuild guild;
                            //If provided a guild name or id, get info for that guild. Otherwise get current guild
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
                                    for (IGuild guild1 : guilds)
                                    {
                                        if (guild1.getName().equals(g))
                                        {
                                            guild = guild1;
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                                guild = event.getGuild();
                            //If the selected guild does not exist, it is not available
                            if (guild == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Guild is not available");
                                return;
                            }

                            //Create embed with guild details
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

                        //Gets role details for current guild
                        new Command("role", "Role info", "role <id/name>", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IRole role = null;
                            IGuild guild = event.getGuild();
                            //If invalid arguments or in DMs, send error
                            if (guild == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Please perform this command in a valid guild.");
                                return;
                            }
                            if (args.size() < 2)
                            {
                                BotUtils.help(map, event, args, "get");
                                return;
                            }

                            //If id, get role. Otherwise get role by name
                            String r = BotUtils.combineArgs(args, 1);
                            long id = BotUtils.getID(r);
                            if (id >= 0)
                                role = guild.getRoleByID(id);
                            else
                            {
                                //If name is everyone, get everyone role. Otherwise find matching role name
                                if (r.equals("everyone"))
                                    role = guild.getEveryoneRole();
                                else
                                {
                                    List<IRole> roles = guild.getRoles();
                                    for (IRole role1 : roles)
                                    {
                                        if (role1.getName().equals(r))
                                        {
                                            role = role1;
                                            break;
                                        }
                                    }
                                }
                            }

                            //If role is invalid, send error message
                            if (role == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Role is not available");
                                return;
                            }

                            //Create embed with role details
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle(role.getName());
                            builder.withColor(role.getColor());
                            builder.appendField("ID", "" + role.getLongID(), true);
                            builder.appendField("Creation Date", BotUtils.formatDate(role.getCreationDate()), true);
                            builder.appendField("Users", "" + guild.getUsersByRole(role).size(), true);

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        //Gets channel details
                        new Command("channel", "Channel info", "channel [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IChannel channel = null;
                            IGuild guild = event.getGuild();
                            //If in a guild and given a specific channel, get that channel. Otherwise, get the current channel
                            if (args.size() >= 2 && guild != null)
                            {
                                //If id, get channel. Otherwise get channel by name
                                String c = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(c);
                                if (id >= 0)
                                    channel = guild.getChannelByID(id);
                                else
                                {
                                    List<IChannel> channels = guild.getChannels();
                                    for (IChannel channel1 : channels)
                                    {
                                        if (channel1.getName().equals(c))
                                        {
                                            channel = channel1;
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                                channel = event.getChannel();

                            if (channel == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Channel is not available");
                                return;
                            }

                            //Create embed with channel details
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle("#" + channel.getName());
                            builder.withColor(BotUtils.DEFAULT_COLOR);
                            builder.appendField("ID", "" + channel.getLongID(), true);
                            //Add category info if the guild exists and the channel is part of a category
                            if (guild != null && channel.getCategory() != null)
                                builder.appendField("Category", channel.getCategory().getName(), true);
                            builder.appendField("Creation Date", BotUtils.formatDate(channel.getCreationDate()), true);

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        //Gets user details
                        new Command("user", "User info", "user [id/name]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            IUser user = null;
                            IGuild guild = null;
                            //If given arguments, get that user by name or id. Otherwise get current user
                            if (args.size() >= 2)
                            {
                                String u = BotUtils.combineArgs(args, 1);
                                long id = BotUtils.getID(u);
                                if (id >= 0)
                                {
                                    if (event.getGuild() != null)
                                        user = event.getGuild().getUserByID(id);
                                    if (user == null)
                                        user = event.getClient().getUserByID(id);
                                    else
                                        guild = event.getGuild();
                                }
                                else if (event.getGuild() != null)
                                {
                                    String[] separate = BotUtils.separateTag(u);
                                    List<IUser> users = event.getGuild().getUsers();
                                    user = null;
                                    for (IUser user1 : users)
                                    {
                                        if (user1.getName().equals(separate[0]) && (separate[1].equals("") || user1.getDiscriminator().equals(separate[1])))
                                        {
                                            user = user1;
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

                            //If user is invalid, send an error message
                            if (user == null)
                            {
                                BotUtils.sendMessage(event.getChannel(), "User is not available");
                                return;
                            }

                            //Ceate embed with user details
                            EmbedBuilder builder = new EmbedBuilder();

                            //If not in current guild or in DMs, get basic details. Otherwise get guild specific details
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

    //Adds a listener that checks if the user wants to add a role that grants access to an NSFW channel
    //TODO store listener info in a table to remove them if a new one is created and one still exists
    private class NSFWListener implements IListener<ReactionAddEvent>
    {
        private long messageID, userID, roleID;
        public NSFWListener(long m, long u, long r)
        {
            messageID = m;
            userID = u;
            roleID = r;
        }

        public void handle(ReactionAddEvent reactionAddEvent)
        {
            //If a reaction was added to the wrong message or the wrong user reacted, do nothing
            if (reactionAddEvent.getMessageID() != messageID || reactionAddEvent.getUser().getLongID() != userID)
                return;

            //Check if the emoji was a yes or no
            if (reactionAddEvent.getReaction().getEmoji().getName().equals(YES))
            {
                //Adds role to user if yes was selected
                IRole role = reactionAddEvent.getGuild().getRoleByID(roleID);
                reactionAddEvent.getUser().addRole(role);
                BotUtils.sendMessage(reactionAddEvent.getChannel(), role.getName() + " was added!");
                //Deletes this listener upon valid execution
                reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
            }
            else if (reactionAddEvent.getReaction().getEmoji().getName().equals(NO))
            {
                //Does not add role if no was selected
                BotUtils.sendMessage(reactionAddEvent.getChannel(), reactionAddEvent.getGuild().getRoleByID(roleID).getName() + " was not added.");
                //Deletes this listener upon valid execution
                reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
            }
        }
    }
}