import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.JsonArray;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class Command
{
    private String name;
    private String description;
    private AccessLevel access;
    private SubCommand[] subComms;
    private CommandExecutor execute;
    public static final String TEST = "Test";

    public Command(String n, String d, AccessLevel a, CommandExecutor e)
    {
        name = n;
        description = d;
        access = a;
        subComms = new SubCommand[] {};
        execute = e;
    }

    public Command(String n, String d, AccessLevel a, SubCommand[] s, CommandExecutor e)
    {
        name = n;
        description = d;
        access = a;
        subComms = s;
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
        boolean a = false;
        JsonObject jsonO;
        //Checks the user's permissions from the json
        try {
            JsonReader reader = Json.createReader(new FileReader("../PrivateResources/dm_commands.json"));
            jsonO = (JsonObject)reader.read();
            reader.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("User file not found");
            return;
        }
        JsonArray guilds = jsonO.getJsonArray("users");
        for (int i = 0; i < guilds.size(); i++)
        {
            JsonObject userO = guilds.getJsonObject(i);
            if (userO.getJsonNumber("id").longValue() == u)
            {
                AccessLevel level = new AccessLevel(userO.getJsonNumber("access_level").intValue());
                a = access.hasAccess(level);
            }
        }

        //Executes the command if the user has permission
        if (a)
        {
            execute.runCommand(event, argsList);
        }
        else
        {
            BotUtils.sendMessage(event.getChannel(), "You do not have permission to use this command!\nYou need " + access.getName() + " access or higher to do so!");
        }
    }

    //Executes the command from a guild
    public void execute(MessageReceivedEvent event, List<String> argsList, long u, long g)
    {
        boolean a = false;
        JsonObject jsonO;
        //Checks the user's guild permissions from the json
        try {
            JsonReader reader = Json.createReader(new FileReader("../PrivateResources/users.json"));
            jsonO = (JsonObject)reader.read();
            reader.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("User file not found");
            return;
        }
        JsonArray guilds = jsonO.getJsonArray("guilds");
        for (int i = 0; i < guilds.size(); i++)
        {
            JsonObject guildO = guilds.getJsonObject(i);
            if (guildO.getJsonNumber("id").longValue() == g)
            {
                JsonArray users = guildO.getJsonArray("users");
                for (int j = 0; j < users.size(); j++)
                {
                    JsonObject channelO = users.getJsonObject(j);
                    if (channelO.getJsonNumber("id").longValue() == u)
                    {
                        AccessLevel level = new AccessLevel(channelO.getJsonNumber("access_level").intValue());
                        a = access.hasAccess(level);
                    }
                }
            }
        }

        //Executes the command if the user has permission
        if (a)
        {
            execute.runCommand(event, argsList);
        }
        else
        {
            BotUtils.sendMessage(event.getChannel(), "You do not have permission to use this command!\nYou need " + access.getName() + " access or higher to do so!");
        }
    }

    //Checks if the bot has permission to use the command in the current channel
    public static boolean hasChannelPerms(String comName, IGuild g, long c)
    {
        //If it's in a dm, it has permission
        if (g == null)
            return true;


        JsonObject jsonO;
        try {
            if (comName == TEST) {
                JsonReader reader = Json.createReader(new FileReader(TestCommands.PERMS));
                jsonO = (JsonObject)reader.read();
                reader.close();
            }
            else
            {
                return false;
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Perms file not found");
            return false;
        }
        JsonArray guilds = jsonO.getJsonArray("guilds");
        for (int i = 0; i < guilds.size(); i++)
        {
            JsonObject guildO = guilds.getJsonObject(i);
            if (guildO.getJsonNumber("id").longValue() == g.getLongID())
            {
                JsonArray channels = guildO.getJsonArray("channels");
                for (int j = 0; j < channels.size(); j++)
                {
                    JsonObject channelO = channels.getJsonObject(j);
                    if (channelO.getJsonNumber("id").longValue() == c)
                    {
                        return channelO.getBoolean("active");
                    }
                }
            }
        }
        return false;
    }
}