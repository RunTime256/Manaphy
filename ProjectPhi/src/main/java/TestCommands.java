import sx.blah.discord.handle.obj.IChannel;
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
            if (Command.hasChannelPerms(comName, event.getGuild().getLongID(), channel.getLongID()))
            {
                BotUtils.sendMessage(channel, "Test complete!");
            }
        }));
    }
}