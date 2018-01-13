import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import javax.json.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    @EventSubscriber
    public void handle(ReadyEvent event)
    {
        //event.getClient().changeUsername("Project Phi");
        event.getClient().changePlayingText("with my sourcecode");
    }

    //Sends a welcome message upon joining a new server
    @EventSubscriber
    public void OnGuildCreate(GuildCreateEvent event)
    {
        IUser owner = event.getGuild().getOwner();
        JsonObject jsonO;
        try {
            JsonReader reader = Json.createReader(new FileReader("../PrivateResources/guilds.json"));
            jsonO = (JsonObject)reader.read();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("User file not found");
            return;
        }
        JsonArray guilds = jsonO.getJsonArray("guilds");
        JsonObjectBuilder build = Json.createObjectBuilder();
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (int i = 0; i < guilds.size(); i++)
        {
            JsonObject guildO = guilds.getJsonObject(i);
            arr.add(Json.createObjectBuilder().add("id", guildO.getJsonNumber("id").longValue()));
            if (guildO.getJsonNumber("id").longValue() == event.getGuild().getLongID())
            {
                return;
            }
        }
        arr.add(Json.createObjectBuilder().add("id", event.getGuild().getLongID()));
        build.add("guilds", arr);
        JsonObject w = build.build();
        try {
            JsonWriter writer = Json.createWriter(new FileWriter("../PrivateResources/guilds.json"));
            writer.writeObject(w);
            writer.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("User file not found");
            return;
        }
        catch(IOException e)
        {
            System.out.println("IO Error");
            return;
        }
        IChannel dm = MainRunner.getClient().getOrCreatePMChannel(owner);
        BotUtils.sendMessage(dm, "test");
    }

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
            //DM
            if (event.getGuild() == null)
            {
                commands.get(comStr).execute(event, argsList, event.getAuthor().getLongID());
            }
            //Run command
            else
            {
                commands.get(comStr).execute(event, argsList, event.getAuthor().getLongID(), event.getGuild().getLongID());
            }
            return;
        }
    }
}