import sx.blah.discord.handle.obj.IChannel;

import java.util.Map;

public class TestCommands
{
    public static final String PERMS = "test_commands.json";
    private String comName = Command.TEST;

    public TestCommands(Map<String, Command> map)
    {
        map.put("test", new Command("test", "Provides a test message", BotUtils.BOT_PREFIX + "test", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
            {
                BotUtils.sendMessage(channel, "Test complete!");
            }
        }));

        map.put("dm", new Command("dm", "DMs a test message", BotUtils.BOT_PREFIX + "dm", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
            {
                IChannel dm;
                //If the command was not sent in a dm, dm the user that sent the command
                if (event.getGuild() != null)
                {
                    dm = event.getClient().getOrCreatePMChannel(event.getGuild().getOwner());
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