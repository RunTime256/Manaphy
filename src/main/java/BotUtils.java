import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings({"WeakerAccess", "StringConcatenationInLoop", "ConstantConditions"})
class BotUtils
{
    public static final String DEFAULT_PREFIX = "+";
    public static final String TEST_PREFIX = "-";
    public static final String BOT_PREFIX = "=";
    public static Random RAND = new Random();
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);
    //public static final Color EXCEPTION_COLOR = new Color(255, 0, 0);
    //public static final Color WRONG_PERMISSION_COLOR = new Color(255, 255, 0);
    private static final String[] letters = {"A", "B", "C", "D", "E", "F"};

    private static String passHash;

    //Sets the password hash for encrypting data for DB
    public static void setPassHash(String pass)
    {
        if (passHash == null)
        {
            try
            {
                //Using SHA-512 Encryption method
                MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
                passHash = new String(sha512.digest(pass.getBytes()));
            }
            catch (NoSuchAlgorithmException ignored)
            {
            }
        }
    }

    //If there is an exception, this error message will be displayed in chat
    public static void sendCommandError(IChannel channel)
    {
        RequestBuffer.RequestFuture<IMessage> rf = RequestBuffer.request(() -> {
            try
            {
                return channel.sendMessage("There was an error running this command.");
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
        });

        rf.get();
    }

    //Sends the requested message
    public static IMessage sendMessage(IChannel channel, String message)
    {
        RequestBuffer.RequestFuture<IMessage> rf = RequestBuffer.request(() ->
        {
            try
            {
                return channel.sendMessage(message);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
        });

        return rf.get();
    }

    //Sends the requested embedded message
    public static IMessage sendMessage(IChannel channel, EmbedObject message)
    {
        RequestBuffer.RequestFuture<IMessage> rf = RequestBuffer.request(() ->
        {
            try
            {
                return channel.sendMessage(message);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
        });

        return rf.get();
    }

    //Adds the requested reaction
    public static void addReaction(IMessage message, ReactionEmoji emoji)
    {
        RequestBuffer.request(() ->
        {
            try
            {
                message.addReaction(emoji);
            }
            catch (DiscordException e)
            {
                System.err.println("Reaction could not be added with error: ");
                e.printStackTrace();
            }
        });
    }

    //Sends the requested File
    @SuppressWarnings("UnusedReturnValue")
    public static IMessage sendFile(IChannel channel, File file)
    {
        RequestBuffer.RequestFuture<IMessage> rf = RequestBuffer.request(() ->
        {
            try
            {
                return channel.sendFile(file);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
            catch (FileNotFoundException e)
            {
                return null;
            }
        });

        return rf.get();
    }

    //Checks if the user is a member of the Pokemon discord to run the command
    public static boolean isPokemon(MessageReceivedEvent event) throws SQLException
    {
        long c;
        String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Pokemon'";
        List<Object> params = new ArrayList<>();
        ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
            c = set.getLong("Entry");
        else
            return false;

        //If the user is a member of the Pokemon discord
        return event.getClient().getGuildByID(c).getUserByID(event.getAuthor().getLongID()) != null;
    }

    //Checks if the user is a member of the Fort Wort discord to run the command
    public static boolean isWort(MessageReceivedEvent event) throws SQLException
    {
        long c;
        String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Fort Wort'";
        List<Object> params = new ArrayList<>();
        ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
            c = set.getLong("Entry");
        else
            return false;

        //If the user is a member of the Fort Wort discord
        return event.getClient().getGuildByID(c).getUserByID(event.getAuthor().getLongID()) != null;
    }

    //Breaks username and discriminator into two separate indexes in an array
    public static String[] separateTag(String text)
    {
        String[] temp = new String[2];
        //If there is a potential valid discriminator at the end of the username (#0000) separate them
        if (text.length() > 5 && text.substring(text.length() - 5, text.length() - 4).equals("#"))
        {
            //Try parsing the end to see if it is a valid number
            try
            {
                String t = text.substring(text.length() - 4);
                Integer.parseInt(t);
                temp[0] = text.substring(0, text.length() - 5);
                temp[1] = t;
            }
            catch (NumberFormatException e)
            {
                temp[0] = text;
                temp[1] = "";
            }
        }
        else
        {
            temp[0] = text;
            temp[1] = "";
        }
        return temp;
    }

    //Returns the user's id, or -1 if it is not correct
    public static long getID(String text)
    {
        //Checks if it's a mention or channel
        if (text.startsWith("<@") || text.startsWith("<#"))
        {
            int start;
            //Checks if it starts with a nickname or role
            if (text.startsWith("<@!") || text.startsWith("<@&"))
                start = 3;
            else
                start = 2;

            if (text.endsWith(">"))
            {
                try
                {
                    return Long.parseLong(text.substring(start, text.length() - 1));
                }
                catch(NumberFormatException e)
                {
                    return -1;
                }
            }
            else
                return -1;
        }
        //Checks whole string if there is no @ mention
        try
        {
            return Long.parseLong(text);
        }
        catch(NumberFormatException e)
        {
            return -1;
        }
    }

    //Returns a date in a standard format, as well as in CST
    public static String formatDate(Instant instant)
    {
        return DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").format(instant.atZone(ZoneId.of("UTC-6")));
    }

    //Combines arguments including and after "start" into one String
    public static String combineArgs(List<String> args, int start)
    {
        String message = "";
        for (int i = start; i < args.size(); i++)
            message += args.get(i) + " ";
        //Removes trailing space at end
        return message.substring(0, message.length() - 1);
    }

    //Easily formatted help message to be called on an incorrect command
    public static void help(Map<String, Command> map, MessageReceivedEvent event, List<String> args, String mainCommand) throws SQLException
    {
        args.add(0, mainCommand);
        map.get("help").execute(event, args);
    }

    //Gets the time in the correct timezone (CST)
    public static ZonedDateTime now()
    {
        return LocalDateTime.now().atZone(ZoneId.of("UTC-6"));
    }

    //Gets hex from a string
    public static Color getHex(String code)
    {
        if (code.startsWith("#"))
            code = code.substring(1);
        if (code.length() != 6)
            return null;

        //Sets ints as rgb values of the color
        int one, two, three;
        one = getHexNum(code.substring(0, 2));
        two = getHexNum(code.substring(2, 4));
        three = getHexNum(code.substring(4, 6));
        //If at least one value is invalid, return null, else return the color
        if (one < 0 || two < 0 || three < 0)
            return null;
        else
            return new Color(one, two, three);
    }

    //Determines the hex value of a single number "code"
    private static int getHexNum(String code)
    {
        int one = -1, two = -1;
        //Try parsing the first value
        try
        {
            one = Integer.parseInt(code.substring(0, 1));
        }
        //If it's not a number, check to see if it is a hex letter
        catch (NumberFormatException e)
        {
            for (int i = 0; i < letters.length; i++)
            {
                if (code.substring(0, 1).toUpperCase().equals(letters[i]))
                {
                    one = 10 + i;
                    break;
                }
            }
            //If not valid, return -1
            if (one < 0)
                return -1;
        }

        //Try parsing the second value
        try
        {
            two = Integer.parseInt(code.substring(1, 2));
        }
        //If it's not a number, check to see if it is a hex letter
        catch (NumberFormatException e)
        {
            for (int i = 0; i < letters.length; i++)
            {
                if (code.substring(1, 2).toUpperCase().equals(letters[i]))
                {
                    two = 10 + i;
                    break;
                }
            }
            //If not valid, return -1
            if (two < 0)
                return -1;
        }
        return one * 16 + two;
    }

    /*
    static void logException(MessageReceivedEvent event, Exception exception, String data)
    {
        long c;
        //Retrieve logging channel
        try
        {
            String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryID = ?";
            List<Object> params = new ArrayList<>();
            params.add(2);
            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
            if (set == null)
                return;
            else if (set.next())
            {
                c = Long.parseLong(set.getString("Entry"));
            }
            else
            {
                System.out.println("No valid channel entry");
                return;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Couldn't process result set");
            e.printStackTrace();
            return;
        }
        catch (Exception e)
        {
            System.out.println("Not a long");
            e.printStackTrace();
            return;
        }
        IChannel channel = event.getClient().getChannelByID(c);
        ZonedDateTime time = event.getMessage().getTimestamp().atZone(ZoneId.of("UTC-6"));
        //TODO prepare encryption for storage
        /*
        String sql = "INSERT INTO DiscordDB.ErrorLog VALUES (?, ?, ?, AES_ENCRYPT(?, ?), ?, ?)";
        List<Object> params = new ArrayList<>();
        params.add(event.getGuild());
        params.add(event.getAuthor());
        params.add(exception.getClass().getSimpleName());

        //Encrypt this:
        params.add(event.getMessage().getContent());
        params.add(passHash);

        params.add(time);
        params.add(data);
        JDBCConnection.insert(sql, params);

        EmbedBuilder builder = new EmbedBuilder();
        builder.withDescription("");
        if (event.getGuild() != null)
        {
            builder.withAuthorName(event.getGuild().getName());
            builder.withAuthorIcon(event.getGuild().getIconURL());
            builder.appendDescription("Guild ID: " + event.getGuild().getLongID() + " | ");
        }
        builder.withTitle(event.getAuthor().getName());
        builder.withThumbnail(event.getAuthor().getAvatarURL());
        builder.appendDescription("User ID: " + event.getAuthor().getLongID());
        builder.withColor(EXCEPTION_COLOR);
        builder.withFooterText("Received at " + time.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a")) + " CST");

        //Since it's not stored, content can be sent through discord without encryption
        builder.appendField(event.getMessage().getContent(), exception.getClass().getSimpleName(), false);

        EmbedObject message = builder.build();

        RequestBuffer.request(() ->
        {
            try
            {
                IMessage mess = channel.sendMessage(message);
                return mess;
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
                return null;
            }
        });
    }
    */
}