import junit.framework.Test;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class Command
{
    private String name;
    private String description;
    private String syntax;
    private AccessLevel access;
    private SubCommand[] subComms;
    private CommandExecutor execute;
    public static final String TEST = "Test";
    public static final String OWNER = "Owner";
    public static final String HELP = "Help";
    public static final String UTILITY = "Utility";
    private static Gson gson;

    public Command(String n, String d, String x, AccessLevel a, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = new SubCommand[] {};
        execute = e;
        gson = new Gson();
    }

    public Command(String n, String d, String x, AccessLevel a, SubCommand[] s, CommandExecutor e)
    {
        name = n;
        description = d;
        syntax = x;
        access = a;
        subComms = s;
        execute = e;
        gson = new Gson();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public AccessLevel getAccess()
    {
        return access;
    }

    public SubCommand[] getSubComms()
    {
        return subComms;
    }

    public boolean hasAccess(AccessLevel request)
    {
        return access.hasAccess(request);
    }

    //Executes the command from a dm
    public void execute(MessageReceivedEvent event, List<String> argsList, long u)
    {
        JSONDMCommands dmCommands;
        String file = "dm_permissions.json";
        try
        {
            dmCommands = gson.fromJson(new FileReader("resources/" + file), JSONDMCommands.class);
        }
        catch(FileNotFoundException e)
        {
            System.out.println(file + " not found");
            return;
        }

        for (int i = 0; i < dmCommands.users.length; i++)
        {
            JSONUserAccess userAccess = dmCommands.users[i];
            if (userAccess.id == u)
            {
                //Executes the command if the user has permission
                if (access.hasAccess(new AccessLevel(userAccess.access_level)))
                {
                    execute.runCommand(event, argsList);
                }
                else
                {
                    BotUtils.sendMessage(event.getChannel(), "You do not have permission to use this command!\nYou need " + access.getName() + " access or higher to do so!");
                }
                return;
            }
        }
    }

    //Executes the command from a guild
    public void execute(MessageReceivedEvent event, List<String> argsList, long u, long g)
    {
        JSONUserCommands userCommands;
        String file = "user_permissions.json";
        try
        {
            userCommands = gson.fromJson(new FileReader("resources/" + file), JSONUserCommands.class);
        }
        catch(FileNotFoundException e)
        {
            System.out.println(file + " not found");
            return;
        }

        for (int i = 0; i < userCommands.guilds.length; i++)
        {
            JSONGuildUserAccess guild = userCommands.guilds[i];
            if (guild.id == g)
            {
                for (int j = 0; j < guild.users.length; j++)
                {
                    JSONUserAccess userAccess = guild.users[j];
                    if (userAccess.id == u)
                    {
                        //Executes the command if the user has permission
                        if (access.hasAccess(new AccessLevel(userAccess.access_level)))
                        {
                            execute.runCommand(event, argsList);
                        }
                        else
                        {
                            BotUtils.sendMessage(event.getChannel(), "You do not have permission to use this command!\nYou need " + access.getName() + " access or higher to do so!");
                        }
                        return;
                    }
                }
                return;
            }
        }
    }

    //Checks if the bot has permission to use the command in the current channel
    public static boolean hasChannelPerms(String comName, IGuild g, long c)
    {
        //If it's in a dm, it has permission
        if (g == null)
            return true;

        JSONChannelPermissions channelPermissions;
        String file = "";
        try
        {
            if (comName == TEST)
            {
                file = TestCommands.PERMS;
                channelPermissions = gson.fromJson(new FileReader("resources/" + file), JSONChannelPermissions.class);
            }
            else if (comName == UTILITY)
            {
                file = UtilityCommands.PERMS;
                channelPermissions = gson.fromJson(new FileReader("resources/" + file), JSONChannelPermissions.class);
            }
            else if (comName == OWNER)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println(file + " not found");
            return false;
        }

        for (int i = 0; i < channelPermissions.guilds.length; i++)
        {
            JSONGuild guild = channelPermissions.guilds[i];
            if (guild.id == g.getLongID())
            {
                for (int j = 0; j < guild.channels.length; j++)
                {
                    JSONChannel channel = guild.channels[j];
                    if (channel.id == c)
                    {
                        //Checks if the channel has permission, and if so, checks if the specific command was blocked
                        if (channel.active)
                        {
                            for (int k = 0; k < channel.blocked_commands.length; k++)
                            {
                                if (comName.equals(channel.blocked_commands[k]))
                                {
                                    return false;
                                } else
                                {
                                    return true;
                                }
                            }
                        }
                        else
                        {
                            return false;
                        }
                    }
                    break;
                }
                break;
            }
        }

        return true;
    }
}