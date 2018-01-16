import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UtilityCommands
{
    public static final String PERMS = "../PrivateResources/utility_commands.json";
    private static String comName = Command.UTILITY;

    public UtilityCommands()
    {

    }

    public static void addCommands(Map<String, Command> map)
    {
        map.put("userinfo", new Command("userinfo", "Gathers the requester's info", BotUtils.BOT_PREFIX + "userinfo [optional: @user]", AccessLevel.EVERYONE, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
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
                    long id = BotUtils.getPing(args.get(0));
                    //Finds the user given
                    if (id >= 0)
                    {
                        if (event.getGuild() == null)
                        {
                            BotUtils.sendMessage(channel, "That user does not exist here!");
                            return;
                        }
                        user = event.getGuild().getUserByID(id);
                        if (user == null)
                        {
                            BotUtils.sendMessage(channel, "That user does not exist here!");
                            return;
                        }
                    }
                    //If not correctly formatted, send help info
                    else
                    {
                        ArrayList<String> list = new ArrayList<>();
                        list.add("userinfo");
                        user = event.getAuthor();
                        map.get("help").execute(event, list, user.getLongID());
                        return;
                    }
                }

                //Checks for DM
                if (event.getGuild() == null)
                {
                    builder.appendField("ID", "" + user.getLongID(), false);
                    builder.appendField("Account Creation", user.getCreationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy KK:mm a")), false);

                    builder.withColor(BotUtils.DEFAULT_COLOR);
                }
                else
                {
                    IGuild g = event.getGuild();

                    if (user.getNicknameForGuild(g) != null)
                    {
                        builder.appendField("Nickname", user.getNicknameForGuild(g), false);
                    }
                    builder.appendField("ID", "" + user.getLongID(), false);
                    builder.appendField("Account Creation", user.getCreationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy KK:mm a")), false);
                    builder.appendField(g.getName() + " Join Date", g.getJoinTimeForUser(user).format(DateTimeFormatter.ofPattern("MM/dd/yyyy KK:mm a")), false);
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

                    builder.withColor(roles.get(0).getColor());
                }

                builder.withAuthorName(user.getName() + "#" + user.getDiscriminator());
                builder.withAuthorIcon(user.getAvatarURL());
                builder.withThumbnail(user.getAvatarURL());
                BotUtils.sendMessage(channel, builder.build());
            }
        }));
    }

    //Orders user's roles in order of the server roles
    private static List<IRole> orderRoles(List<IRole> roles1, List<IRole> roles2)
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
}