import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HelpCommands
{
    private String prefix;

    public HelpCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        map.put("help", new Command("help", "Lists all available commands and what they do", prefix + "help [command]", AccessLevel.EVERYONE, false, (event, args) ->
        {
            IChannel channel = event.getChannel();
            EmbedBuilder builder = new EmbedBuilder();

            String sql;
            List<Object> params = new ArrayList<>();
            //Checks if user has widespread permissions
            //(Manager gets all access)
            sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID IS NULL";
            params.add(event.getAuthor().getLongID());
            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

            //Default access level of everyone, so not every user needs to be stored
            int accessLevel = AccessLevel.EVERYONE.getLevel();
            if (set.next())
            {
                accessLevel = set.getInt("AccessLevel");
            }

            //Checks if user has guild specific permissions
            if (event.getGuild() != null)
            {
                sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID = ?";
                params.clear();
                params.add(event.getAuthor().getLongID());
                params.add(event.getGuild().getLongID());
            }
            set = JDBCConnection.getStatement(sql, params).executeQuery();

            //Guild permissions override widespread permissions
            if (set.next())
            {
                accessLevel = set.getInt("AccessLevel");
            }

            //Set iterator to the last correct command, or list all main commands
            Iterator<Command> iter;
            if (args.size() > 0 && map.containsKey(args.get(0)))
            {
                Command current = map.get(args.get(0));
                int i;
                for (i = 1; i < args.size(); i++)
                {
                    if (current.getSubComms().containsKey(args.get(i)))
                    {
                        current = current.getSubComms().get(args.get(i));
                    }
                    else
                    {
                        break;
                    }
                }
                iter = current.getSubComms().values().iterator();
                current = map.get(args.get(0));
                String title = "";
                for (int j = 0; j < i - 1; j++)
                {
                    title += current.getSyntax() + " ";
                    current = current.getSubComms().get(args.get(j));
                }
                title += current.getSyntax();
                builder.withTitle(title);
            }
            else
            {
                iter = map.values().iterator();
            }

            boolean b = false;
            //Add commands to embed builder
            while (iter.hasNext())
            {
                Command c = iter.next();
                if (c.getAccess().isAccessible(accessLevel) && !(event.getGuild() != null && c.getDM()))
                {
                    builder.appendField(c.getSyntax(), c.getDescription(), b);
                    b = true;
                }
            }

            builder.withAuthorName("Help Manual");
            builder.withAuthorIcon(event.getClient().getApplicationIconURL());
            builder.withColor(BotUtils.DEFAULT_COLOR);

            BotUtils.sendMessage(channel, builder.build());
        }));
    }
}