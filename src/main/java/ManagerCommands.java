import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManagerCommands
{
    private static IChannel mimicSend;
    private static IChannel mimicReceive;
    private static boolean mimicActive;

    private final String YES = "\u2705";
    private final String NO = "\u274C";

    public ManagerCommands(Map<String, Command> map)
    {
        //Shuts down the bot and ends the connection
        map.put("shutdown", new Command("shutdown", "Shuts down the bot", BotUtils.BOT_PREFIX + "shutdown", AccessLevel.MANAGER, (event, args) ->
        {
            BotUtils.sendMessage(event.getChannel(), "Shutting down...");
            event.getClient().logout();
        }));

        //Sets the presence and text of the bot
        map.put("presence", new Command("presence", "Sets the presence", BotUtils.BOT_PREFIX + "presence <status> [activity] [text]", AccessLevel.MANAGER, (event, args) ->
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
            }
            //Set status with text
            if (args.size() >= 3)
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

                event.getClient().changePresence(status, activity, message);
            }
        }));

        //Replicates messages from the manager's DMs and sends them to a channel
        map.put("mimic", new Command("mimic", "Replicates DMs to selected channel", BotUtils.BOT_PREFIX + "mimic <start/stop> <guild> <channel>", AccessLevel.MANAGER, (event, args) ->
        {
            if (event.getGuild() == null)
            {
                if (args.size() == 1)
                {
                    if (args.get(0).equals("stop"))
                    {
                        mimicSend = null;
                        mimicReceive = null;
                        mimicActive = false;
                    }
                    else
                    {
                        BotUtils.help(map, event, args, "mimic");
                    }
                }
                if (args.size() >= 4)
                {
                    if (args.get(0).equals("start"))
                    {
                        try
                        {
                            //Get the guild to get the channel
                            mimicSend = event.getClient().getGuildByID(Long.parseLong(args.get(1))).getChannelByID(Long.parseLong(args.get(2)));
                            mimicReceive = event.getChannel();
                            mimicActive = true;
                        }
                        catch (NumberFormatException e)
                        {
                            BotUtils.help(map, event, args, "mimic");
                        }
                    }
                    else
                    {
                        BotUtils.help(map, event, args, "mimic");
                    }
                }
            }
        }));

        map.put("sql", new Command("sql", "Perform a MySQL statement", BotUtils.BOT_PREFIX + "sql <statement>", AccessLevel.MANAGER, (event, args) ->
        {
            if (event.getGuild() == null)
            {
                if (args.size() < 1)
                {
                    BotUtils.help(map, event, args, "sql");
                    return;
                }
                
                for (int i = 0; i < args.size(); i++)
                {
                    if (args.get(i).toUpperCase().equals("DROP"))
                    {
                        String sql = "SELECT Count(*) AS Count FROM " + BotUtils.combineArgs(args, i + 2);
                        List<Object> params = new ArrayList<>();
                        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                        ResultSet set = statement.executeQuery();
                        if (set.next())
                        {
                            IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to drop table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
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

                            VerificationReactionListener verification = new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0));
                            event.getClient().getDispatcher().registerListener(verification);
                            return;
                        }
                    }
                    if (args.get(i).toUpperCase().equals("TRUNCATE"))
                    {
                        String sql = "SELECT Count(*) AS Count FROM " + BotUtils.combineArgs(args, i + 2);
                        List<Object> params = new ArrayList<>();
                        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                        ResultSet set = statement.executeQuery();
                        if (set.next())
                        {
                            IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to truncate table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
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
                            x = true;
                            message.addReaction(ReactionEmoji.of(NO));
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

                            VerificationReactionListener verification = new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0));
                            event.getClient().getDispatcher().registerListener(verification);
                            return;
                        }
                    }
                    if (args.get(i).toUpperCase().equals("DELETE"))
                    {
                        String sql = "SELECT Count(*) AS Count " + BotUtils.combineArgs(args, i + 1);
                        List<Object> params = new ArrayList<>();
                        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                        ResultSet set = statement.executeQuery();
                        if (set.next())
                        {
                            IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to delete " + set.getLong("Count") + " row(s) in table + `" + args.get(i + 2) + "`");
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
                            x = true;
                            message.addReaction(ReactionEmoji.of(NO));
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

                            VerificationReactionListener verification = new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0));
                            event.getClient().getDispatcher().registerListener(verification);
                            return;
                        }
                    }
                    if (args.get(i).toUpperCase().equals("ALTER"))
                    {
                        String sql = "SELECT Count(*) AS Count FROM " + args.get(i + 2);
                        List<Object> params = new ArrayList<>();
                        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                        ResultSet set = statement.executeQuery();
                        if (set.next())
                        {
                            IMessage message = BotUtils.sendMessage(event.getChannel(), "Are you sure you want to alter table `" + args.get(i + 2) + "` with " + set.getLong("Count") + " row(s)?");
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

                            VerificationReactionListener listener = new VerificationReactionListener(message.getLongID(), event.getAuthor().getLongID(), BotUtils.combineArgs(args, 0));
                            event.getClient().getDispatcher().registerListener(listener);
                            return;
                        }
                    }
                }

                String sql = BotUtils.combineArgs(args, 0);
                List<Object> params = new ArrayList<>();
                PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                statement.execute();

                BotUtils.sendMessage(event.getChannel(), "Statement executed successfully!");
            }
        }));
    }

    private class VerificationReactionListener implements IListener<ReactionAddEvent>
    {
        private long messageID;
        private long userID;
        private String sql;
        private boolean triggered;
        public VerificationReactionListener(long m, long u, String s)
        {
            messageID = m;
            userID = u;
            sql = s;
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
                    try
                    {
                        List<Object> params = new ArrayList<>();
                        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                        statement.executeUpdate();
                        BotUtils.sendMessage(reactionAddEvent.getChannel(), "Statement executed successfully!");
                        triggered = true;
                        reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
                    }
                    catch (Exception e)
                    {
                        BotUtils.sendCommandError(reactionAddEvent.getChannel());
                        System.out.println("\nNew " + e.getClass().getSimpleName() + " at " + BotUtils.formatDate(BotUtils.now().toInstant()) + "\n");
                        e.printStackTrace();
                    }
                }
                else if (reactionAddEvent.getReaction().getEmoji().getName().equals(NO))
                {
                    BotUtils.sendMessage(reactionAddEvent.getChannel(), "Statement cancelled.");
                    triggered = true;
                    reactionAddEvent.getClient().getDispatcher().unregisterListener(this);
                }
            }
        }
    }

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