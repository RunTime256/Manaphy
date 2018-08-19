import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.ClientBuilder;

@SuppressWarnings("WeakerAccess")
public class DiscordUtils
{
    public static void start(String token)
    {
        IDiscordClient cli = new ClientBuilder().withToken(token).build();

        //Register a listener to perform commands on events
        cli.getDispatcher().registerListener(new CommandHandler());

        cli.login();
    }
}