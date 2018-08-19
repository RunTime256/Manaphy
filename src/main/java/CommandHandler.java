import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandHandler
{
    private Map<String, Command> commands;
    private Map<String, Command> hiddenCommands;
    private Map<String, Command> secretCommands;
    private Map<String, Command> botCommands;

/*
    //Main constructor during publish
    public CommandHandler()
    {
        commands = new HashMap<>();
        hiddenCommands = new HashMap<>();
        secretCommands = new HashMap<>();
        botCommands = new HashMap<>();

        //Add all commands to the map
        new ManagerCommands(commands, BotUtils.DEFAULT_PREFIX);
        new OwnerCommands(commands, BotUtils.DEFAULT_PREFIX);
        new AdminCommands(commands, BotUtils.DEFAULT_PREFIX);
        new ModeratorCommands(commands, BotUtils.DEFAULT_PREFIX);
        new TestCommands(commands, BotUtils.DEFAULT_PREFIX);
        new UtilityCommands(commands, BotUtils.DEFAULT_PREFIX);
        new HelpCommands(commands, BotUtils.DEFAULT_PREFIX);
        new EventCommands(commands, BotUtils.DEFAULT_PREFIX);
        new GameCommands(commands, BotUtils.DEFAULT_PREFIX);

        //Commands that are secret (normal command but hidden from help)
        new SecretCommands(secretCommands, BotUtils.DEFAULT_PREFIX);

        //Commands that are only executable by bots
        new BotCommands(botCommands, BotUtils.BOT_PREFIX);
    }
*/


    //Test constructor for specific command improvements
    public CommandHandler()
    {
        commands = new HashMap<>();
        hiddenCommands = new HashMap<>();
        secretCommands = new HashMap<>();
        botCommands = new HashMap<>();

        new GameCommands(hiddenCommands, BotUtils.TEST_PREFIX);
        new HelpCommands(hiddenCommands, BotUtils.TEST_PREFIX);
    }


    //Updates playing text when starting up
    @EventSubscriber
    public void handle(ReadyEvent event)
    {
        //event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "underwater");
        event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "+help");
        //event.getClient().changePresence(StatusType.ONLINE, ActivityType.WATCHING, "from afar");
        //event.getClient().changePresence(StatusType.INVISIBLE);
    }

    @EventSubscriber
    public void onUserJoin(UserJoinEvent event)
    {
        try
        {
            JDBCConnection.connect();
            String sql;
            List<Object> params = new ArrayList<>();
            sql = "SELECT RoleID FROM DiscordDB.Roles WHERE GuildID = ? AND Auto = true";
            params.add(event.getGuild().getLongID());
            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

            while (set.next())
            {
                event.getUser().addRole(event.getGuild().getRoleByID(set.getLong("RoleID")));
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
            }
            JDBCConnection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    //Sends a welcome message upon joining a new server

    @EventSubscriber
    public void onGuildCreate(GuildCreateEvent event)
    {
        try
        {
            JDBCConnection.connect();
            String sql;
            List<Object> params = new ArrayList<>();
            sql = "SELECT * FROM DiscordDB.Guilds WHERE GuildID = ?";
            params.add(event.getGuild().getLongID());
            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

            if (!set.next())
            {
                //Insert guild into table
                sql = "INSERT INTO DiscordDB.Guilds (GuildID) VALUES (?)";
                params.clear();
                params.add(event.getGuild().getLongID());
                JDBCConnection.getStatement(sql, params).executeUpdate();

                //Send new guild message to guild owner
                sql = "SELECT DiscordDB.EntryDescription FROM MessageEntry WHERE EntryDesc = 'Guild Join Message'";
                params.clear();
                set = JDBCConnection.getStatement(sql, params).executeQuery();
                if (set.next())
                {
                    BotUtils.sendMessage(event.getGuild().getOwner().getOrCreatePMChannel(), set.getString("EntryDescription"));
                }
            }
            JDBCConnection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }


    //Performs a command if the message received triggers one
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();
        String[] args = message.split(" ");

        if (args.length == 0)
        {
            return;
        }

        //Checks to see if there is a passive command to run
        if (!(args[0].startsWith(BotUtils.DEFAULT_PREFIX) || args[0].startsWith(BotUtils.TEST_PREFIX) || args[0].startsWith(BotUtils.BOT_PREFIX)))
        {
            //Replicate DMs if mimicking is enabled
            if (event.getGuild() == null && ManagerCommands.isMimicActive() && ManagerCommands.getMimicReceive().getLongID() == event.getChannel().getLongID())
            {
                BotUtils.sendMessage(ManagerCommands.getMimicSend(), event.getMessage().getContent());
            }
            return;
        }

        String comStr = args[0].substring(1);

        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        Map<String, Command> map;
        boolean secret = false;
        boolean bot = false;
        //Determines command map based on prefix
        if (args[0].startsWith(BotUtils.DEFAULT_PREFIX))
        {
            map = commands;
            secret = true;
        }
        else if (args[0].startsWith(BotUtils.TEST_PREFIX))
        {
            map = hiddenCommands;
        }
        else if (args[0].startsWith(BotUtils.BOT_PREFIX))
        {
            map = botCommands;
            bot = true;
        }
        else
        {
            return;
        }

        //Checks to see if there is a command with the given key
        if (map.containsKey(comStr))
        {
            if (bot && event.getAuthor().isBot() || !bot && !event.getAuthor().isBot())
            {
                try
                {
                    event.getChannel().setTypingStatus(true);
                    map.get(comStr).execute(event, argsList);
                } catch (Exception e)
                {
                    BotUtils.sendCommandError(event.getChannel());
                    System.out.println("\nNew " + e.getClass().getSimpleName() + " at " + BotUtils.formatDate(BotUtils.now().toInstant()) + "\n");
                    e.printStackTrace();
                } finally
                {
                    event.getChannel().setTypingStatus(false);
                }
            }
        }
        else if (secret && secretCommands.containsKey(comStr))
        {
            map = secretCommands;
            try
            {
                event.getChannel().setTypingStatus(true);
                map.get(comStr).execute(event, argsList);
            }
            catch (Exception e)
            {
                BotUtils.sendCommandError(event.getChannel());
                System.out.println("\nNew " + e.getClass().getSimpleName() + " at " + BotUtils.formatDate(BotUtils.now().toInstant()) + "\n");
                e.printStackTrace();
            }
            finally
            {
                event.getChannel().setTypingStatus(false);
            }
        }
    }
}