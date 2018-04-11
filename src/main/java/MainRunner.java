import sx.blah.discord.api.IDiscordClient;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class MainRunner
{
    public static void main(String[] args)
    {
        IDiscordClient cli;
        Gson gson = new Gson();
        JSONBotUtils botUtils;
        String file = "bot_utils.json";
        try
        {
            botUtils = gson.fromJson(new FileReader("resources/" + file), JSONBotUtils.class);
        }
        catch(FileNotFoundException e)
        {
            System.out.println(file + " not found");
            return;
        }

        cli = BotUtils.getBuiltDiscordClient(botUtils.token);

        // Register a listener via the EventSubscriber annotation which allows for organisation and delegation of events
        cli.getDispatcher().registerListener(new CommandHandler());

        // Only login after all events are registered otherwise some may be missed.
        cli.login();
    }
}