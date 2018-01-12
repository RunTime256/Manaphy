import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CommandHandler
{
    private static Map<String, Command> commands = new HashMap<String, Command>();
    public static Random RAND = new Random();

    static
    {
        //Add all commands to the map
        OwnerCommands owner = new OwnerCommands();
        TestCommands test = new TestCommands();

        owner.addCommands(commands);
        test.addCommands(commands);
    }

    /*
    @EventSubscriber
    public void handle(ReadyEvent event)
    {
        event.getClient().changeUsername("Project Phi");
        event.getClient().changePlayingText("with my sourcecode");
    }
    */

    @EventSubscriber
    public void OnMessageReceived(MessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();
        String lMessage = message.toLowerCase();
        String[] args = lMessage.split(" ");
        int numArgs = args.length - 1;
        IChannel channel = event.getChannel();

        if (numArgs == -1)
        {
            return;
        }

        if (!args[0].startsWith(BotUtils.BOT_PREFIX))
        {
            //Run passive commands
            return;
        }

        String comStr = args[0].substring(1);

        List<String> argsList = new ArrayList<String>(Arrays.asList(args));
        argsList.remove(0);

        if (commands.containsKey(comStr))
        {
            //Run active command
            commands.get(comStr).execute(event, argsList, event.getAuthor().getLongID(), event.getGuild().getLongID());
            return;
        }
    }
}