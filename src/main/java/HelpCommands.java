import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HelpCommands
{
    public HelpCommands(Map<String, Command> map)
    {
        map.put("help", new Command("help", "Lists all available commands and what they do", BotUtils.BOT_PREFIX + "help", AccessLevel.EVERYONE, (event, args) ->
        {
            IChannel channel = event.getChannel();
            EmbedBuilder builder = new EmbedBuilder();

            String sql;
            List<Object> params = new ArrayList<>();
            //DM command options
            if (event.getGuild() == null)
            {
                sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID IS NULL";
                params.add(event.getAuthor().getLongID());
            }
            //Guild command options
            else
            {
                sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID = ?";
                params.add(event.getAuthor().getLongID());
                params.add(event.getGuild().getLongID());
            }
            PreparedStatement statement = JDBCConnection.getStatement(sql, params);
            ResultSet set = statement.executeQuery();
            int accessLevel = AccessLevel.EVERYONE.getLevel();
            if (set == null)
                return;
            else if (set.next())
            {
                accessLevel = set.getInt("AccessLevel");
            }
            statement.close();

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
                if (c.getAccess().hasAccess(accessLevel))
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
