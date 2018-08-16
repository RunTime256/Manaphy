import java.util.Map;

public class TestCommands
{
    private String prefix;
    public TestCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Test message to ensure bot is running and receiving commands
        map.put("test", new Command("test", "Provides a test message", prefix + "test", AccessLevel.MANAGER, false, (event, args) ->
        {
            BotUtils.sendMessage(event.getChannel(), "Test complete!");
        }));
    }
}