import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import java.util.Map;

public class TestCommands
{
    public static final String PERMS = "../PrivateResources/test_commands.json";
    private static String comName = Command.TEST;

    public TestCommands()
    {

    }

    public static void addCommands(Map<String, Command> map)
    {
        map.put("test", new Command("test", "Provides a test message", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
            {
                BotUtils.sendMessage(channel, "Test complete!");
            }
        }));

        map.put("dm", new Command("dm", "DMs a test message", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
            {
                IChannel dm;
                //If the command was not sent in a dm, dm the user that sent the command
                if (event.getGuild() != null)
                {
                    dm = MainRunner.getClient().getOrCreatePMChannel(event.getGuild().getOwner());
                }
                else
                {
                    dm = channel;
                }
                BotUtils.sendMessage(dm, "Test complete!");
            }
        }));
    }
}