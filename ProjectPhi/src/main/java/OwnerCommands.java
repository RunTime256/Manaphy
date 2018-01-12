import sx.blah.discord.handle.obj.IChannel;
import java.util.Map;

public class OwnerCommands
{
    public static final String PERMS = "../PrivateResources/owner_commands.json";
    private static String comName = Command.TEST;

    public OwnerCommands()
    {

    }

    public static void addCommands(Map<String, Command> map)
    {
        map.put("shutdown", new Command("shutdown", "Shuts down the bot", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            BotUtils.sendMessage(channel, "Shutting down...");
            MainRunner.clientLogout();
        }));
    }
}