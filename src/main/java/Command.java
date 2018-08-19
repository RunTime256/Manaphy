import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public class Command
{
    private String name;
    private String description;
    private String syntax;
    private boolean dm;
    private AccessLevel access;
    private Map<String, Command> subComms;
    private CommandExecutor execute;

    //Create a standard command
    public Command(String n, String d, String x, AccessLevel a, boolean m, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = new HashMap<>();
        execute = e;
        dm = m;
    }

    //Create a command with an array of sub-commands
    public Command(String n, String d, String x, AccessLevel a, boolean m, Command[] s, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = new HashMap<>();
        //Add sub-commands to a map
        for (Command value : s)
            subComms.put(value.getName(), value);
        execute = e;
        dm = m;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSyntax()
    {
        return syntax;
    }

    public boolean getDM()
    {
        return dm;
    }

    public AccessLevel getAccess()
    {
        return access;
    }

    public Map<String, Command> getSubComms()
    {
        return subComms;
    }

    //Executes the command
    public void execute(MessageReceivedEvent event, List<String> argsList) throws SQLException
    {
        if (event.getGuild() != null && dm)
            return;
        JDBCConnection.connect();
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

        if (access.isAccessible(accessLevel))
        {
            //Checks args to see if there's a sub command available
            if (argsList.size() > 0)
            {
                execute(event, argsList, 0);
                return;
            }
            else
            {
                execute.runCommand(event, argsList);
            }
        }
        JDBCConnection.disconnect();
    }

    //Checks if there is a sub command with given args and executes that one
    public void execute(MessageReceivedEvent event, List<String> argsList, int pos) throws SQLException
    {
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

        //Executes a sub command if it has permission
        if (pos < argsList.size())
        {
            if (subComms.containsKey(argsList.get(pos)))
            {
                Command command = subComms.get(argsList.get(pos));
                if (command.getAccess().isAccessible(accessLevel))
                {
                    command.execute(event, argsList, pos + 1);
                }
                return;
            }
        }
        if (access.isAccessible(accessLevel))
        {
            execute.runCommand(event, argsList);
        }
    }

    //Checks if the bot has permission to use the command in the current channel
    @SuppressWarnings("unused")
    public static boolean hasChannelPerms(MessageReceivedEvent event, String type, String comName) throws SQLException
    {
        //If it's in a DM, it has permission
        if (event.getGuild() == null)
            return true;

        String sql;
        List<Object> params = new ArrayList<>();
        //Check blacklist for category
        sql = "SELECT * FROM DiscordDB.BlacklistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name IS NULL AND Active = FALSE";
        params.add(event.getGuild().getLongID());
        params.add(event.getChannel().getLongID());
        params.add(type);
        ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            return false;
        }
        //Checks blacklist for single command
        sql = "SELECT * FROM DiscordDB.BlacklistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name = ? AND Active = FALSE";
        params.add(comName);
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            return false;
        }
        //Checks whitelist for category
        sql = "SELECT * FROM DiscordDB.WhitelistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name IS NULL AND Active = FALSE";
        params.remove(params.size() - 1);
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            return true;
        }
        //Checks whitelist for single command
        sql = "SELECT * FROM DiscordDB.WhitelistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name = ? AND Active = FALSE";
        params.add(comName);
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            return true;
        }
        return true;
    }
}