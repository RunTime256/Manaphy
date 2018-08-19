import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.TwitchClientBuilder;

@SuppressWarnings("WeakerAccess")
public class TwitchUtils
{
    private static TwitchClient cli;
    public static void start(String token)
    {
        cli = TwitchClientBuilder.init().connect();
    }

    //Returns the client for listeners outside of twitch
    public static TwitchClient getTwitchClient()
    {
        return cli;
    }
}