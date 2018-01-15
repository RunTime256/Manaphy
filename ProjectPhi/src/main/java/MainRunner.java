import sx.blah.discord.api.IDiscordClient;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class MainRunner
{
    private static IDiscordClient cli;
    public static void main(String[] args)
    {
        JsonStructure jsonst;
        try
        {
            //Receives json file containing bot token
            JsonReader reader = Json.createReader(new FileReader("../PrivateResources/botutils.json"));
            jsonst = reader.read();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Utils file not found");
            return;
        }

        cli = BotUtils.getBuiltDiscordClient(((JsonObject)jsonst).getString("token"));

        // Register a listener via the EventSubscriber annotation which allows for organisation and delegation of events
        cli.getDispatcher().registerListener(new CommandHandler());

        // Only login after all events are registered otherwise some may be missed.
        cli.login();
    }

    public static void clientLogout()
    {
        cli.logout();
    }

    public static IDiscordClient getClient()
    {
        return cli;
    }
}