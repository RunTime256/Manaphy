import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command
{
    private String name;
    private String description;
    private String syntax;
    private AccessLevel access;
    private Map<String, Command> subComms;
    private CommandExecutor execute;

    //Useful Strings for commands
    public static final String MANAGER = "Manager";
    public static final String TESTER = "Tester";
    public static final String OWNER = "Owner";
    public static final String ADMIN = "Admin";
    public static final String MOD = "Mod";
    public static final String HELP = "Help";
    public static final String TEST = "Test";
    public static final String UTILITY = "Utility";
    public static final String EVENT = "Event";

    //Create a standard command
    public Command(String n, String d, String x, AccessLevel a, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = new HashMap<>();
        execute = e;
    }

    //Create a command with an array of sub-commands
    public Command(String n, String d, String x, AccessLevel a, Command[] s, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = new HashMap<>();
        //Add sub-commands to a map
        for (int i = 0; i < s.length; i++)
        {
            subComms.put(s[i].getName(), s[i]);
        }
        execute = e;
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
        JDBCConnection.connect();
        String sql;
        List<Object> params = new ArrayList<>();
        //Checks for DMs or if the command is a manager command
        //(Manager gets all access)
        if (event.getGuild() == null || access == AccessLevel.MANAGER)
        {
            sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID IS NULL";
            params.add(event.getAuthor().getLongID());
        }
        else
        {
            sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID = ?";
            params.add(event.getAuthor().getLongID());
            params.add(event.getGuild().getLongID());
        }
        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
        ResultSet set = statement.executeQuery();

        //Default access level of everyone, so not every user needs to be stored
        int accessLevel = AccessLevel.EVERYONE.getLevel();
        if (set.next())
        {
            accessLevel = set.getInt("AccessLevel");
        }
        statement.close();

        if (access.hasAccess(accessLevel))
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
        //Checks for DMs or if the command is a manager command
        //(Manager gets all access)
        if (event.getGuild() == null || access == AccessLevel.MANAGER)
        {
            sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID IS NULL";
            params.add(event.getAuthor().getLongID());
        }
        else
        {
            sql = "SELECT AccessLevel FROM DiscordDB.Users WHERE UserID = ? AND GuildID = ?";
            params.add(event.getAuthor().getLongID());
            params.add(event.getGuild().getLongID());
        }
        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
        ResultSet set = statement.executeQuery();

        //Default access level of everyone, so not every user needs to be stored
        int accessLevel = AccessLevel.EVERYONE.getLevel();
        if (set.next())
        {
            accessLevel = set.getInt("AccessLevel");
        }
        statement.close();

        //Executes a sub command if it has permission
        if (pos < argsList.size())
        {
            if (subComms.containsKey(argsList.get(pos)))
            {
                Command command = subComms.get(argsList.get(pos));
                if (command.getAccess().hasAccess(accessLevel))
                {
                    command.execute(event, argsList, pos + 1);
                }
                return;
            }
        }
        if (access.hasAccess(accessLevel))
        {
            execute.runCommand(event, argsList);
        }
    }

    //Checks if the bot has permission to use the command in the current channel
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
        PreparedStatement statement = JDBCConnection.getStatement(sql, params);
        ResultSet set = statement.executeQuery();
        if (set.next())
        {
            statement.close();
            return false;
        }
        statement.close();
        //Checks blacklist for single command
        sql = "SELECT * FROM DiscordDB.BlacklistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name = ? AND Active = FALSE";
        params.add(comName);
        statement = JDBCConnection.getStatement(sql, params);
        set = statement.executeQuery();
        if (set.next())
        {
            statement.close();
            return false;
        }
        statement.close();
        //Checks whitelist for category
        sql = "SELECT * FROM DiscordDB.WhitelistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name IS NULL AND Active = FALSE";
        params.remove(params.size() - 1);
        statement = JDBCConnection.getStatement(sql, params);
        set = statement.executeQuery();
        if (set.next())
        {
            statement.close();
            return true;
        }
        statement.close();
        //Checks whitelist for single command
        sql = "SELECT * FROM DiscordDB.WhitelistCommands WHERE GuildID = ? AND ChannelID = ? AND Type = ? AND Name = ? AND Active = FALSE";
        params.add(comName);
        statement = JDBCConnection.getStatement(sql, params);
        set = statement.executeQuery();
        if (set.next())
        {
            statement.close();
            return true;
        }
        statement.close();
        return true;
    }
}