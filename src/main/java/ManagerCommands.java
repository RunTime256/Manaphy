import com.univocity.parsers.fixed.FixedWidthFields;
import com.univocity.parsers.fixed.FixedWidthWriter;
import com.univocity.parsers.fixed.FixedWidthWriterSettings;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "ConstantConditions", "StringConcatenationInLoop"})
public class ManagerCommands
{
    private static IChannel mimicSend;
    private static IChannel mimicReceive;
    private static boolean mimicActive;

    private final String YES = "\u2705";
    private final String NO = "\u274C";
    @SuppressWarnings("FieldCanBeLocal")
    private String prefix;

    public ManagerCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Shuts down the bot and ends the connection
        map.put("shutdown", new Command("shutdown", "Shuts down the bot", prefix + "shutdown", AccessLevel.MANAGER, false, (event, args) ->
        {
            BotUtils.sendMessage(event.getChannel(), "Shutting down...");
            event.getClient().logout();
        }));

        //Sets the presence and text of the bot
        map.put("presence", new Command("presence", "Sets the presence", prefix + "presence <status> [activity] [text]", AccessLevel.MANAGER, true, (event, args) ->
        {
            StatusType status;
            ActivityType activity;
            //Set status without text
            if (args.size() == 1)
            {
                //Sets the status type (color it appears as)
                switch (args.get(0))
                {
                    case "invisible":
                        status = StatusType.INVISIBLE;
                        break;
                    case "online":
                        status = StatusType.ONLINE;
                        break;
                    case "idle":
                        status = StatusType.IDLE;
                        break;
                    case "dnd":
                        status = StatusType.DND;
                        break;
                    default:
                        BotUtils.help(map, event, args, "presence");
                        return;
                }
                event.getClient().changePresence(status);
                BotUtils.sendMessage(event.getChannel(), "Presence has been updated to `" + args.get(0) + "`");
            }
            //Set status with text
            else if (args.size() >= 3)
            {
                //Sets the status type (color it appears as)
                switch (args.get(0))
                {
                    case "online":
                        status = StatusType.ONLINE;
                        break;
                    case "idle":
                        status = StatusType.IDLE;
                        break;
                    case "dnd":
                        status = StatusType.DND;
                        break;
                    default:
                        BotUtils.help(map, event, args, "presence");
                        return;
                }
                //Sets the activity type (what the bot is doing)
                switch (args.get(1))
                {
                    case "playing":
                        activity = ActivityType.PLAYING;
                        break;
                    case "watching":
                        activity = ActivityType.WATCHING;
                        break;
                    case "listening":
                        activity = ActivityType.LISTENING;
                        break;
                    default:
                        BotUtils.help(map, event, args, "presence");
                        return;
                }

                //Remove trailing space
                String message = BotUtils.combineArgs(args, 2);

                //Update presence
                event.getClient().changePresence(status, activity, message);
                BotUtils.sendMessage(event.getChannel(), "Presence has been updated to `" + args.get(0) + "` with message `" + message + "`");
            }
            else
            {
                BotUtils.help(map, event, args, "presence");
            }
        }));

        //Replicates messages from the manager's DMs and sends them to a channel
        map.put("mimic", new Command("mimic", "Replicates DMs to selected channel", prefix + "mimic <start/stop> <channel>", AccessLevel.MANAGER, true, (event, args) ->
        {
            if (args.size() == 1)
            {
                //If not stop, command is invalid
                if (!args.get(0).equals("stop"))
                {
                    BotUtils.help(map, event, args, "mimic");
                    return;
                }

                //Set variables to stop mimicking
                long id = mimicSend.getLongID();
                mimicSend = null;
                mimicReceive = null;
                mimicActive = false;
                BotUtils.sendMessage(event.getChannel(), "Mimicking ended in <#" + id + ">");
            }
            else if (args.size() == 2)
            {
                //If not start, command is invalid
                if (!args.get(0).equals("start"))
                    BotUtils.help(map, event, args, "mimic");

                try
                {
                    //Set variables to start mimicking in selected channel
                    mimicSend = event.getClient().getChannelByID(Long.parseLong(args.get(1)));
                    mimicReceive = event.getChannel();
                    mimicActive = true;
                    BotUtils.sendMessage(event.getChannel(), "Mimicking started in <#" + mimicSend.getLongID() + ">");
                }
                catch (NumberFormatException e)
                {
                    BotUtils.help(map, event, args, "mimic");
                }
            }
            else
                BotUtils.help(map, event, args, "mimic");
        }));

        //Verifies validity and executes an sql statement from DMs
        map.put("sql", new Command("sql", "Perform a MySQL statement", prefix + "sql <statement>", AccessLevel.MANAGER, true, (event, args) ->
        {
            //If invalid arguments, send help message
            if (args.size() < 1)
            {
                BotUtils.help(map, event, args, "sql");
                return;
            }

            //Loops through statement to determine if altering keywords are mentioned
            for (int i = 0; i < args.size(); i++)
            {
                //If drop keyword is mentioned
                if (args.get(i).toUpperCase().equals("DROP"))
                {
                    //Select count of affected rows in table
                    String sql = "SELECT Count(*) AS Count FROM " + BotUtils.combineArgs(args, i + 2);
                    List<Object> params = new ArrayList<>();
                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                    if (set.next())
                    {
                        //Send verification message if user wants to execute statement
                        IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to drop table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
                        BotUtils.addReaction(message, ReactionEmoji.of(YES));
                        BotUtils.addReaction(message, ReactionEmoji.of(NO));

                        //Adds verification listener
                        event.getClient().getDispatcher().registerListener(new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0)));
                        return;
                    }
                }
                //If truncate keyword is mentioned
                if (args.get(i).toUpperCase().equals("TRUNCATE"))
                {
                    //Select count of affected rows in table
                    String sql = "SELECT Count(*) AS Count FROM " + BotUtils.combineArgs(args, i + 2);
                    List<Object> params = new ArrayList<>();
                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                    if (set.next())
                    {
                        //Send verification message if user wants to execute statement
                        IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to truncate table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
                        BotUtils.addReaction(message, ReactionEmoji.of(YES));
                        BotUtils.addReaction(message, ReactionEmoji.of(NO));

                        //Adds verification listener
                        event.getClient().getDispatcher().registerListener(new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0)));
                        return;
                    }
                }
                //If delete keyword is mentioned
                if (args.get(i).toUpperCase().equals("DELETE"))
                {
                    //Select count of affected rows in table
                    String sql = "SELECT Count(*) AS Count " + BotUtils.combineArgs(args, i + 1);
                    List<Object> params = new ArrayList<>();
                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                    if (set.next())
                    {
                        //Send verification message if user wants to execute statement
                        IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to delete " + set.getLong("Count") + " row(s) in table + `" + args.get(i + 2) + "`");
                        BotUtils.addReaction(message, ReactionEmoji.of(YES));
                        BotUtils.addReaction(message, ReactionEmoji.of(NO));

                        //Adds verification listener
                        event.getClient().getDispatcher().registerListener(new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0)));
                        return;
                    }
                }
                //If alter keyword is mentioned
                if (args.get(i).toUpperCase().equals("ALTER"))
                {
                    //Select count of affected rows in table
                    String sql = "SELECT Count(*) AS Count FROM " + args.get(i + 2);
                    List<Object> params = new ArrayList<>();
                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                    if (set.next())
                    {
                        //Send verification message if user wants to execute statement
                        IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to alter table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
                        BotUtils.addReaction(message, ReactionEmoji.of(YES));
                        BotUtils.addReaction(message, ReactionEmoji.of(NO));

                        //Adds verification listener
                        event.getClient().getDispatcher().registerListener(new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0)));
                        return;
                    }
                }
                //If select keyword is mentioned
                if (args.get(i).equals("SELECT"))
                {
                    //Selects rows and gets MetaData for column names
                    String sql = BotUtils.combineArgs(args, 0);
                    List<Object> params = new ArrayList<>();
                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                    ResultSetMetaData data = set.getMetaData();

                    String[] cols = new String[data.getColumnCount()];
                    int[] maxLength = new int[cols.length];
                    //Loop through column names and set max length for the column for easy formatting
                    for (int j = 0; j < cols.length; j++)
                    {
                        cols[j] = data.getColumnName(j+1);
                        maxLength[j] = cols[j].length() + 1;
                    }

                    List<String[]> rows = new ArrayList<>();
                    //Loop through rows
                    while (set.next())
                    {
                        String[] temp = new String[cols.length];
                        //Loop through columns
                        for (int j = 0; j < cols.length; j++)
                        {
                            //Add column string to array
                            temp[j] = set.getString(j+1);
                            //If null, make text be null
                            if (temp[j] == null)
                                temp[j] = "null";

                            //If column length is greater than the max, increase the max
                            if (temp[j].length() >= maxLength[j])
                                maxLength[j] = temp[j].length() + 1;
                        }

                        //Add array to rows
                        rows.add(temp);
                    }
                    //Create a writer to ensure all columns line up in printing
                    FixedWidthWriter writer = new FixedWidthWriter(new StringWriter(), new FixedWidthWriterSettings(new FixedWidthFields(cols, maxLength)));
                    String message = "`" + writer.writeHeadersToString().trim();
                    //If the column length maxes out limit, send error message
                    if (message.length() + 1 > 2000)
                    {
                        BotUtils.sendMessage(event.getChannel(), "Columns are too long to create a table. Select statement cancelled");
                        return;
                    }

                    //Loop through rows
                    for (String[] row : rows)
                    {
                        //Create a trimmed message to not use up max character count for message
                        String temp = writer.writeRowToString(row).trim().replace("`", "\\`");
                        //If combined message maxes out limit, send current message and start a new message. Otherwise add it to current message
                        if (message.length() + temp.length() + 1 > 2000)
                        {
                            BotUtils.sendMessage(event.getChannel(), message + "`");
                            //If row maxes out limit, send error row. Otherwise create a new message including it
                            if (temp.length() + 2 > 2000)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Row is too long to be included");
                                message += "`";
                            } else
                                message = "`" + temp;
                        } else
                        {
                            message += "\n" + temp;
                        }
                    }
                    //Send trailing message rows
                    BotUtils.sendMessage(event.getChannel(), message + "`");
                    return;
                }
            }

            String sql = BotUtils.combineArgs(args, 0);
            List<Object> params = new ArrayList<>();
            JDBCConnection.getStatement(sql, params).execute();

            BotUtils.sendMessage(event.getChannel(), "Statement executed successfully!");
        }));
    }

    //Adds a listener that checks if the user wants to execute an sql statement
    //TODO store listener info in a table to remove them if a new one is created and one still exists
    private class VerificationReactionListener implements IListener<ReactionAddEvent>
    {
        private long messageID;
        private long userID;
        private String sql;
        public VerificationReactionListener(long m, long u, String s)
        {
            messageID = m;
            userID = u;
            sql = s;
        }

        public void handle(ReactionAddEvent reactionAddEvent)
        {
            //If a reaction was added to the wrong message or the wrong user reacted, do nothing
            if (reactionAddEvent.getMessageID() != messageID || reactionAddEvent.getUser().getLongID() != userID)
                return;

            //Check if the emoji was a yes or no
            if (reactionAddEvent.getReaction().getEmoji().getName().equals(YES))
            {
                //Try executing statement
                try
                {
                    List<Object> params = new ArrayList<>();
                    JDBCConnection.getStatement(sql, params).executeUpdate();
                    BotUtils.sendMessage(reactionAddEvent.getChannel(), "Statement executed successfully!");
                }
                catch (Exception e)
                {
                    BotUtils.sendCommandError(reactionAddEvent.getChannel());
                    System.out.println("\nNew " + e.getClass().getSimpleName() + " at " + BotUtils.formatDate(BotUtils.now().toInstant()) + "\n");
                    e.printStackTrace();
                }
                //Deletes this listener even if it fails
                reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
            }
            else if (reactionAddEvent.getReaction().getEmoji().getName().equals(NO))
            {
                BotUtils.sendMessage(reactionAddEvent.getChannel(), "Statement cancelled.");
                //Deletes this listener upon valid execution
                reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
            }
        }
    }

    //Get commands for CommandHandler to read mimic variables and send messages if mimicking is active
    public static IChannel getMimicSend()
    {
        return mimicSend;
    }

    public static IChannel getMimicReceive()
    {
        return mimicReceive;
    }

    public static boolean isMimicActive()
    {
        return mimicActive;
    }
}