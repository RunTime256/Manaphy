import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

public class HelpCommands
{
    public static final String PERMS = "owner_commands.json";
    private String comName = Command.TEST;

    public HelpCommands(Map<String, Command> map)
    {
        map.put("help", new Command("help", "Lists all available commands and what they do", BotUtils.BOT_PREFIX + "help", AccessLevel.EVERYONE, (event, args) ->
        {
            Gson gson = new Gson();
            IChannel channel = event.getChannel();
            Iterator<Command> iter = map.values().iterator();
            EmbedBuilder builder = new EmbedBuilder();

            AccessLevel access = null;
            if (event.getGuild() == null)
            {
                JSONDMCommands dmCommands;
                String file = "dm_permissions.json";
                try
                {
                    dmCommands = gson.fromJson(new FileReader("resources/" + file), JSONDMCommands.class);
                }
                catch(FileNotFoundException e)
                {
                    System.out.println(file + " not found");
                    return;
                }

                for (int i = 0; i < dmCommands.users.length; i++)
                {
                    JSONUserAccess userAccess = dmCommands.users[i];
                    if (userAccess.id == event.getAuthor().getLongID())
                    {
                        access = new AccessLevel(userAccess.access_level);
                    }
                }
            }
            else
            {
                JSONUserCommands userCommands;
                String file = "user_permissions.json";
                try
                {
                    userCommands = gson.fromJson(new FileReader("resources/" + file), JSONUserCommands.class);
                }
                catch(FileNotFoundException e)
                {
                    System.out.println(file + " not found");
                    return;
                }

                for (int i = 0; i < userCommands.guilds.length; i++)
                {
                    JSONGuildUserAccess guild = userCommands.guilds[i];
                    if (guild.id == event.getGuild().getLongID())
                    {
                        for (int j = 0; j < guild.users.length; j++)
                        {
                            JSONUserAccess userAccess = guild.users[j];
                            if (userAccess.id == event.getAuthor().getLongID())
                            {
                                access = new AccessLevel(userAccess.access_level);
                            }
                        }
                    }
                }
            }

            if (access == null)
            {
                return;
            }

            boolean b = false;
            while (iter.hasNext())
            {
                Command c = iter.next();
                if (c.getAccess().hasAccess(access))
                {
                    builder.appendField(c.getName(), c.getDescription(), b);
                    b = true;
                }
            }

            builder.withAuthorName("Help Manual");
            builder.withAuthorIcon(event.getClient().getApplicationIconURL());
            builder.withColor(BotUtils.DEFAULT_COLOR);

            BotUtils.sendMessage(channel, builder.build());
        }));
    }
}
