import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

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

    public void execute(MessageReceivedEvent event, List<String> argsList, long u, long g)
    {
        boolean a = false;
        JsonObject jsonO;
        try {
            JsonReader reader = Json.createReader(new FileReader("../PrivateResources/users.json"));
            jsonO = (JsonObject)reader.read();
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

        if (a)
        {
            execute.runCommand(event, argsList);
        }
        else
        {
            BotUtils.sendMessage(event.getChannel(), "You do not have permission to use this command!\nYou need " + access.getName() + " access or higher to do so!");
        }
    }

    public static boolean hasChannelPerms(String comName, long g, long c)
    {
        JsonObject jsonO;
        try {
            if (comName == TEST) {
                JsonReader reader = Json.createReader(new FileReader(TestCommands.PERMS));
                jsonO = (JsonObject)reader.read();
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
            if (guildO.getJsonNumber("id").longValue() == g)
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