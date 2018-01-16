import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

public class HelpCommands
{
    public static final String PERMS = "../PrivateResources/owner_commands.json";
    private static String comName = Command.TEST;

    public HelpCommands()
    {

    }

    public static void addCommands(Map<String, Command> map)
    {
        map.put("help", new Command("help", "Lists all available commands and what they do", BotUtils.BOT_PREFIX + "help", AccessLevel.EVERYONE, (event, args) ->
        {
            IChannel channel = event.getChannel();
            Iterator<Command> iter = map.values().iterator();
            EmbedBuilder builder = new EmbedBuilder();

            AccessLevel access = null;
            if (event.getGuild() == null)
            {
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
                    if (userO.getJsonNumber("id").longValue() == event.getAuthor().getLongID())
                    {
                        access = new AccessLevel(userO.getJsonNumber("access_level").intValue());
                    }
                }
            }
            else
            {
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
                    if (guildO.getJsonNumber("id").longValue() == event.getGuild().getLongID())
                    {
                        JsonArray users = guildO.getJsonArray("users");
                        for (int j = 0; j < users.size(); j++)
                        {
                            JsonObject channelO = users.getJsonObject(j);
                            if (channelO.getJsonNumber("id").longValue() == event.getAuthor().getLongID())
                            {
                                access = new AccessLevel(channelO.getJsonNumber("access_level").intValue());
                            }
                        }
                    }
                }
            }

            if (access == null)
            {
                return;
            }

            boolean b = false;
            while (iter.hasNext())
            {
                Command c = iter.next();
                if (c.getAccess().hasAccess(access))
                {
                    builder.appendField(c.getName(), c.getDescription(), b);
                    b = true;
                }
            }

            builder.withAuthorName("Help Manual");
            builder.withAuthorIcon(MainRunner.getClient().getApplicationIconURL());
            builder.withColor(BotUtils.DEFAULT_COLOR);

            BotUtils.sendMessage(channel, builder.build());
        }));
    }
}