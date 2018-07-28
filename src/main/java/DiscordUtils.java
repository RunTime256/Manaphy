import sx.blah.discord.api.IDiscordClient;

public class DiscordUtils
{
    private static IDiscordClient cli;
    public static void start(String token)
    {
        cli = BotUtils.getBuiltDiscordClient(token);

        //Register a listener to perform commands on events
        cli.getDispatcher().registerListener(new CommandHandler());

        cli.login();
    }

    //Returns the client for listeners outside of discord
    public static IDiscordClient getDiscordClient()
    {
        return cli;
    }
}