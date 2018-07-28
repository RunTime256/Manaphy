import sx.blah.discord.handle.obj.IChannel;

import java.util.Map;

public class TestCommands
{

    public TestCommands(Map<String, Command> map)
    {
        //Test message to ensure bot is running and receiving commands
        map.put("test", new Command("test", "Provides a test message", BotUtils.BOT_PREFIX + "test", AccessLevel.MANAGER, (event, args) ->
        {
            BotUtils.sendMessage(event.getChannel(), "Test complete!");
        }));
    }
}