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
        UtilityCommands utility = new UtilityCommands();
        HelpCommands help = new HelpCommands();

        owner.addCommands(commands);
        test.addCommands(commands);
        utility.addCommands(commands);
        help.addCommands(commands);
    }

    //Updates playing text when starting up
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
        //Reads the guilds and adds them to a JsonObject
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

        //Goes through each channel id to see if it has joined a new guild
        //(Required due to event firing when connecting to a guild as well)
        for (int i = 0; i < guilds.size(); i++)
        {
            JsonObject guildO = guilds.getJsonObject(i);
            //Since JsonObjects cannot be changed, a new one must be constructed to write to the file. This prepares Objects as they are read
            arr.add(Json.createObjectBuilder().add("id", guildO.getJsonNumber("id").longValue()));
            if (guildO.getJsonNumber("id").longValue() == event.getGuild().getLongID())
            {
                return;
            }
        }
        //Add the new guild because it was not in the json file
        arr.add(Json.createObjectBuilder().add("id", event.getGuild().getLongID()));
        build.add("guilds", arr);
        JsonObject w = build.build();
        //Overwrites the original json to add new guild
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

    //Performs a command if the message received triggers one
    @EventSubscriber
    public void OnMessageReceived(MessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();
        String lMessage = message.toLowerCase();
        String[] args = lMessage.split(" ");
        int numArgs = args.length - 1;

        if (numArgs == -1)
        {
            return;
        }

        //Checks to see if there is a passive command to run
        if (!args[0].startsWith(BotUtils.BOT_PREFIX))
        {
            //Run passive commands
            return;
        }

        String comStr = args[0].substring(1);

        List<String> argsList = new ArrayList<String>(Arrays.asList(args));
        argsList.remove(0);

        //Checks to see if there is a command with the given key
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