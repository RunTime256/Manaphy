import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;

class BotUtils
{
    // Constants for use throughout the bot
    static final String BOT_PREFIX = "+";
    static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Handles the creation and getting of a IDiscordClient object for a token
    static IDiscordClient getBuiltDiscordClient(String token)
    {
        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .build();
    }

    // Helper functions to make certain aspects of the bot easier to use.
    static IMessage sendMessage(IChannel channel, String message)
    {
        // This might look weird but it'll be explained in another page.
        RequestBuffer.RequestFuture<IMessage> rf = RequestBuffer.request(() -> {
            try
            {
                IMessage mess = channel.sendMessage(message);
                return mess;
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
        });

        return rf.get();

        /*
        // The below example is written to demonstrate sending a message if you want to catch the RLE for logging purposes
        RequestBuffer.request(() ->
        {
            try
            {
                channel.sendMessage(message);
            }
            catch (RateLimitException e)
            {
                System.out.println("Do some logging");
                throw e;
            }
        });
        */
    }
}