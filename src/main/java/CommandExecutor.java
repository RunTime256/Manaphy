import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface CommandExecutor
{
    // Interface for a command to be implemented in the command map
    void runCommand(MessageReceivedEvent event, List<String> args);
}