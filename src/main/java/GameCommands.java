import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameCommands
{
    private Map<String, ReactionEmoji> stoneMap;
    private Map<String, String> emojiMap;
    private final String[] PEGS = {"F", "W", "L", "T", "M", "S"};
    private final String[] EMOTES = {"\uD83D\uDD25", "\uD83D\uDCA7", "\uD83C\uDF43", "\u26A1", "\uD83C\uDF15", "\u2600"};
    private final Emote[] STONES = {new Emote("firestone", 468167664775331850L), new Emote("waterstone", 468167718483525643L), new Emote("leafstone", 468167673214271488L), new Emote("thunderstone", 468167707934720011L), new Emote("moonstone", 468167683846832169L), new Emote("sunstone",468167696857563146L)};
    private final Emote[] WHITE_CUPS = {new Emote("w1", 469335051121983498L), new Emote("w2", 469335058889572352L), new Emote("w3", 469335070126112768L), new Emote("w4", 469335081752854541L)};
    private final Emote[] RED_CUPS = {new Emote("r1", 469335015113752596L), new Emote("r2", 469335023712075776L), new Emote("r3", 469335032809390090L), new Emote("r4", 469335041017905165L)};
    private final Emote[] GREEN_CUPS = {new Emote("g1", 469334976274366474L), new Emote("g2", 469334987490197514L), new Emote("g3", 469334994985287681L), new Emote("g4", 469335004049047561L)};
    private final Color GREEN = new Color(1, 181, 83);
    private String prefix;

    public GameCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Create map to find codes and emojis
        stoneMap = new HashMap<>();
        emojiMap = new HashMap<>();
        stoneMap.put(PEGS[0], ReactionEmoji.of(STONES[0].name, STONES[0].id));
        stoneMap.put(PEGS[1], ReactionEmoji.of(STONES[1].name, STONES[1].id));
        stoneMap.put(PEGS[2], ReactionEmoji.of(STONES[2].name, STONES[2].id));
        stoneMap.put(PEGS[3], ReactionEmoji.of(STONES[3].name, STONES[3].id));
        stoneMap.put(PEGS[4], ReactionEmoji.of(STONES[4].name, STONES[4].id));
        stoneMap.put(PEGS[5], ReactionEmoji.of(STONES[5].name, STONES[5].id));
        emojiMap.put(EMOTES[0], PEGS[0]);
        emojiMap.put(EMOTES[1], PEGS[1]);
        emojiMap.put(EMOTES[2], PEGS[2]);
        emojiMap.put(EMOTES[3], PEGS[3]);
        emojiMap.put(EMOTES[4], PEGS[4]);
        emojiMap.put(EMOTES[5], PEGS[5]);

        map.put("stones", new Command("stones", "Stones and Cups is a code-breaking game. Guess the stones in the correct order to win! You have 7 turns to guess.", prefix + "stones", AccessLevel.EVERYONE, true, new Command[]
                {
                        new Command("start", "Starts a game", "start", AccessLevel.EVERYONE, true, ((event, args) ->
                        {
                            if (args.size() >= 1 && args.size() <= 2)
                            {
                                boolean isEvent = false;
                                //If there's an event code
                                if (args.size() > 1)
                                {
                                    String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret'";
                                    List<Object> params = new ArrayList<>();
                                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        if (args.get(1).equals(set.getString("Entry")))
                                        {
                                            if (BotUtils.isPokemon(event))
                                            {
                                                isEvent = true;
                                            }
                                            else
                                            {
                                                BotUtils.help(map, event, args, "stones");
                                                return;
                                            }
                                        }
                                        else
                                        {
                                            BotUtils.help(map, event, args, "stones");
                                            return;
                                        }
                                    }
                                }
                                //TODO temporary else for event
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "This command could not be run at the time.");
                                    return;
                                }

                                //Find if there exists an active game, otherwise get stats from it
                                String sql = "SELECT StartTime, EndTime, Streak, GameNumber FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                int gameNum = 1;
                                int streak = 0;

                                if (set.next())
                                {
                                    streak = set.getInt("Streak");
                                    gameNum = set.getInt("GameNumber") + 1;
                                    //If there is a current game (no end time) send error message
                                    if (set.getTimestamp("EndTime") == null)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "There is already an active game. Complete it before starting another!");
                                        return;
                                    }
                                    //Cooldown of 1 hour, checks if next command is before that
                                    else if (set.getTimestamp("StartTime").toInstant().isAfter(BotUtils.now().minusMinutes(3).toInstant()))
                                    {
                                        ZonedDateTime start = set.getTimestamp("StartTime").toInstant().atZone(ZoneId.of("UTC-6"));
                                        ZonedDateTime now = BotUtils.now();
                                        int mins;
                                        int secs;
                                        if (now.getMinute() < start.getMinute())
                                        {
                                            mins = start.getMinute() - now.getMinute() - 57;
                                        }
                                        else
                                        {
                                            mins = 3 - now.getMinute() + start.getMinute();
                                        }
                                        if (now.getSecond() < start.getSecond())
                                        {
                                            secs = start.getSecond() - now.getSecond();
                                        }
                                        else
                                        {
                                            secs = 59 - now.getSecond() + start.getSecond();
                                            mins--;
                                        }

                                        BotUtils.sendMessage(event.getChannel(), "There is a 3 minute cooldown to play. Please wait " + mins + " minutes and " + secs + " seconds to try again.");
                                        return;
                                    }
                                }

                                sql = "INSERT INTO DiscordDB.Stones (UserID, ChannelID, StartTime, Solution, Tries, Event, Win, Streak, GameNumber) VALUES (?, ?, ?, ?, 0, ?, false, ?, ?)";
                                params.add(event.getChannel().getLongID());
                                params.add(BotUtils.now());
                                String solution = "";
                                for (int i = 0; i < 4; i++)
                                {
                                    solution += PEGS[BotUtils.RAND.nextInt(PEGS.length)];
                                }
                                params.add(solution);
                                params.add(isEvent);
                                params.add(streak);
                                params.add(gameNum);
                                JDBCConnection.getStatement(sql, params).executeUpdate();

                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Start'";
                                params.clear();
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String phrase = "";
                                if (set.next())
                                {
                                    int count = 1;
                                    while (set.next())
                                    {
                                        count++;
                                    }
                                    int rand = BotUtils.RAND.nextInt(count);
                                    set.beforeFirst();
                                    for (int i = 0; i <= rand; i++)
                                    {
                                        set.next();
                                    }
                                    phrase = set.getString("Entry");
                                }

                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess'";
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String url = "";
                                if (set.next())
                                {
                                    url = set.getString("Entry");
                                }

                                String key = "";
                                for (int i = 0; i < PEGS.length; i++)
                                {
                                    key += stoneMap.get(PEGS[i]) + " - " + EMOTES[i] + " (" + PEGS[i] + ")";
                                    if (i % 2 == 0)
                                    {
                                        key += "\t\t";
                                    }
                                    else
                                    {
                                        key += "\n";
                                    }
                                }
                                key = key.substring(0, key.length() - 1);

                                sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? ORDER BY Streak DESC LIMIT 1";
                                params.clear();
                                params.add(event.getAuthor().getLongID());
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                int best = 0;
                                if (set.next())
                                {
                                    best = set.getInt("Streak");
                                }

                                String cups = ReactionEmoji.of(GREEN_CUPS[0].name, GREEN_CUPS[0].id) + " - Correct stone and correct position\n" + ReactionEmoji.of(RED_CUPS[0].name, RED_CUPS[0].id) + " - Correct stone but incorrect position\n" + ReactionEmoji.of(WHITE_CUPS[0].name, WHITE_CUPS[0].id) + " - Incorrect stone";

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withAuthorName("Stones and Cups");
                                builder.withTitle("Starting game " + gameNum);
                                builder.withDescription(phrase);
                                builder.withImage(url);
                                builder.withColor(GREEN);
                                builder.appendField("Guessing Key", key, false);
                                builder.appendField("Response Key", cups, false);
                                builder.appendField("Guess Command", prefix + "stones g \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\t\tor\t\t" + prefix + "stones g FFFF", false);
                                builder.appendField("Current Streak", "" + streak, false);
                                builder.appendField("Best Streak", "" + best, false);

                                BotUtils.sendMessage(event.getChannel(), builder.build());
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "stones");
                            }
                        })),

                        new Command("quit", "Quits a game", "quit", AccessLevel.TESTER, true, ((event, args) ->
                        {
                            if (args.size() == 1)
                            {
                                String sql = "SELECT Solution, GameNumber, EndTime FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                if (set.next())
                                {
                                    if (set.getTimestamp("EndTime") == null)
                                    {
                                        sql = "UPDATE DiscordDB.Stones SET EndTime = ? WHERE UserID = ? AND GameNumber = ?";
                                        params.clear();
                                        params.add(0, BotUtils.now());
                                        params.add(event.getAuthor().getLongID());
                                        params.add(set.getInt("GameNumber"));
                                        JDBCConnection.getStatement(sql, params).executeUpdate();

                                        String solution = set.getString("Solution");
                                        String message = "";
                                        for (int i = 0; i < solution.length(); i++)
                                        {
                                            message += stoneMap.get(solution.substring(i, i + 1));
                                        }

                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Quit'";
                                        params.clear();
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        String phrase = "";
                                        if (set.next())
                                        {
                                            int count = 1;
                                            while (set.next())
                                            {
                                                count++;
                                            }
                                            int rand = BotUtils.RAND.nextInt(count);
                                            set.beforeFirst();
                                            for (int i = 0; i <= rand; i++)
                                            {
                                                set.next();
                                            }
                                            phrase = set.getString("Entry");
                                        }

                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess'";
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        String url = "";
                                        if (set.next())
                                        {
                                            url = set.getString("Entry");
                                        }

                                        EmbedBuilder builder = new EmbedBuilder();
                                        builder.withAuthorName("Stones and Cups");
                                        builder.withTitle("Ending game...");
                                        builder.withDescription(phrase);
                                        builder.withImage(url);
                                        builder.withColor(GREEN);
                                        builder.appendField("Solution", message, false);

                                        BotUtils.sendMessage(event.getChannel(), builder.build());
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "There is no active game. Start one using the command `" + prefix + "stones start`");
                                        return;
                                    }
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "There is no active game. Start one using the command `" + prefix + "stones start`");
                                    return;
                                }
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "stones");
                            }
                        })),

                        new Command("g", "Make a guess", "g <order>", AccessLevel.EVERYONE, true, ((event, args) ->
                        {
                            if (args.size() == 2 || args.size() == 5)
                            {
                                String guess = "";
                                if (args.size() == 2)
                                {
                                    String temp = args.get(1);
                                    for (int i = 0; i < temp.length(); i++)
                                    {
                                        if (emojiMap.containsKey(temp.substring(i, i + 1)))
                                        {
                                            guess += emojiMap.get(temp.substring(i, i + 1));
                                        }
                                        else if (i < temp.length() - 1 && emojiMap.containsKey(temp.substring(i, i + 2)))
                                        {
                                            guess += emojiMap.get(temp.substring(i, i + 2));
                                            i++;
                                        }
                                        else if (stoneMap.containsKey(temp.substring(i, i + 1)))
                                        {
                                            guess += temp.substring(i, i + 1);
                                        }
                                        else
                                        {
                                            BotUtils.sendMessage(event.getChannel(), "Invalid Guess (Check the key!)");
                                            return;
                                        }
                                    }
                                }
                                else
                                {
                                    for (int i = 1; i < args.size(); i++)
                                    {
                                        String temp = args.get(i);
                                        if (emojiMap.containsKey(temp))
                                        {
                                            temp = emojiMap.get(temp);
                                        }
                                        if (stoneMap.containsKey(temp))
                                        {
                                            guess += temp;
                                        }
                                        else
                                        {
                                            BotUtils.sendMessage(event.getChannel(), "Invalid Guess (Check the key!)");
                                            return;
                                        }
                                    }
                                }
                                if (guess.length() != 4)
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Invalid Guess (Check the key!)");
                                    return;
                                }

                                String sql = "SELECT GameNumber, EndTime, Solution, Tries, Streak, Event FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String solution;
                                int tries;
                                int streak;
                                int num;
                                boolean isEvent;
                                if (set.next())
                                {
                                    if (set.getTimestamp("EndTime") != null)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "There is no active game. Start one using the command `" + prefix + "stones start`");
                                        return;
                                    }

                                    solution = set.getString("Solution");
                                    tries = set.getInt("Tries") + 1;
                                    streak = set.getInt("Streak");
                                    num = set.getInt("GameNumber");
                                    isEvent = set.getBoolean("Event");
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "There is no active game. Start one using the command `" + prefix + "stones start`");
                                    return;
                                }

                                int[] result = new int[4];
                                boolean[] found = new boolean[4];
                                for (int i = 0; i < solution.length(); i++)
                                {
                                    if (solution.substring(i, i + 1).equals(guess.substring(i, i + 1)))
                                    {
                                        result[i] = 2;
                                        found[i] = true;
                                    }
                                }
                                for (int i = 0; i < guess.length(); i++)
                                {
                                    for (int j = 0; j < solution.length(); j++)
                                    {
                                        if (guess.substring(i, i + 1).equals(solution.substring(j, j + 1)) && result[i] == 0 && !found[j])
                                        {
                                            result[i] = 1;
                                            found[j] = true;
                                            break;
                                        }
                                    }
                                }

                                Emote[] emotes = new Emote[4];
                                String guessEmote = "";
                                boolean correct = true;
                                for (int i = 0; i < result.length; i++)
                                {
                                    if (result[i] == 0)
                                    {
                                        emotes[i] = WHITE_CUPS[i];
                                        correct = false;
                                    }
                                    else if (result[i] == 1)
                                    {
                                        emotes[i] = RED_CUPS[i];
                                        correct = false;
                                    }
                                    else if (result[i] == 2)
                                    {
                                        emotes[i] = GREEN_CUPS[i];
                                    }
                                    guessEmote += stoneMap.get(guess.substring(i, i + 1));
                                }

                                String response1, response2 = "", url = "", thumb = "";
                                if (correct)
                                {
                                    streak++;
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Win'";
                                    params.clear();
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        response2 = set.getString("Entry");
                                    }
                                    else
                                    {
                                        return;
                                    }

                                    sql = "UPDATE DiscordDB.Stones SET EndTime = ?, Win = true, Streak = ? WHERE UserID = ? AND GameNumber = ?";
                                    params.clear();
                                    params.add(BotUtils.now());
                                    params.add(streak);
                                    params.add(event.getAuthor().getLongID());
                                    params.add(num);
                                    JDBCConnection.getStatement(sql, params).executeUpdate();

                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Win'";
                                    params.clear();
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        url = set.getString("Entry");
                                    }
                                    else
                                    {
                                        return;
                                    }

                                    if (isEvent)
                                    {
                                        if (streak == 2)
                                        {
                                            sql = "SELECT Count(*) AS Count FROM DiscordDB.Stones WHERE UserID = ? AND STREAK >= 2";
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next() && set.getLong("Count") == 1)
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak 2'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    response2 = set.getString("Entry").replace("\\n", "\n");
                                                }
                                                else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'War'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    long guild = set.getLong("Entry");
                                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Logs'";
                                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                    if (set.next())
                                                    {
                                                        long channel = set.getLong("Entry");

                                                        EmbedBuilder builder = new EmbedBuilder();
                                                        IUser author = event.getAuthor();
                                                        builder.withAuthorIcon(author.getAvatarURL());
                                                        builder.withAuthorName(author.getName());
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #22");
                                                        builder.appendField("User ID", "" +  author.getLongID(), false);
                                                        builder.appendField("Streak Number", "2", false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }

                                        }
                                        else if (streak == 4)
                                        {
                                            sql = "SELECT Count(*) AS Count FROM DiscordDB.Stones WHERE UserID = ? AND STREAK >= 4";
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next() && set.getLong("Count") == 1)
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak 4'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    response2 = set.getString("Entry");
                                                }
                                                else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak Image 4'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    thumb = set.getString("Entry");
                                                }
                                                else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'War'";
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    long guild = set.getLong("Entry");
                                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Logs'";
                                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                    if (set.next())
                                                    {
                                                        long channel = set.getLong("Entry");

                                                        EmbedBuilder builder = new EmbedBuilder();
                                                        IUser author = event.getAuthor();
                                                        builder.withAuthorIcon(author.getAvatarURL());
                                                        builder.withAuthorName(author.getName());
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #22");
                                                        builder.appendField("User ID", "" +  author.getLongID(), false);
                                                        builder.appendField("Streak Number", "4", false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }
                                        }
                                        else if (streak == 7)
                                        {
                                            sql = "SELECT Count(*) AS Count FROM DiscordDB.Stones WHERE UserID = ? AND STREAK >= 7";
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next() && set.getLong("Count") == 1)
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak 7'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    response2 = set.getString("Entry");
                                                }
                                                else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak Image 7'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    thumb = set.getString("Entry");
                                                }
                                                else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'War'";
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    long guild = set.getLong("Entry");
                                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Logs'";
                                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                    if (set.next())
                                                    {
                                                        long channel = set.getLong("Entry");

                                                        EmbedBuilder builder = new EmbedBuilder();
                                                        IUser author = event.getAuthor();
                                                        builder.withAuthorIcon(author.getAvatarURL());
                                                        builder.withAuthorName(author.getName());
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #22");
                                                        builder.appendField("User ID", "" +  author.getLongID(), false);
                                                        builder.appendField("Streak Number", "7", false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else if (tries == 7)
                                {
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Loss'";
                                    params.clear();
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        response2 = set.getString("Entry");
                                    }
                                    else
                                    {
                                        return;
                                    }

                                    sql = "UPDATE DiscordDB.Stones SET EndTime = ?, Streak = 0 WHERE UserID = ? AND GameNumber = ?";
                                    params.clear();
                                    params.add(BotUtils.now());
                                    params.add(event.getAuthor().getLongID());
                                    params.add(num);
                                    JDBCConnection.getStatement(sql, params).executeUpdate();

                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Loss'";
                                    params.clear();
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    url = "";
                                    if (set.next())
                                    {
                                        url = set.getString("Entry");
                                    }
                                }
                                sql = "SELECT Count(*) AS Count FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Guess'";
                                params.clear();
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                int max;
                                if (set.next())
                                {
                                    max = Math.toIntExact(set.getLong("Count"));
                                }
                                else
                                {
                                    return;
                                }

                                int rand = BotUtils.RAND.nextInt(max);
                                if (rand == 0)
                                {
                                    if (BotUtils.RAND.nextInt(100 / max) != 0)
                                    {
                                        rand = BotUtils.RAND.nextInt(max - 1) + 1;
                                    }
                                }
                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Guess' ORDER BY EntryID ASC";
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    if (rand != 0)
                                    {
                                        for (int i = 0; i < rand; i++)
                                        {
                                            set.next();
                                        }
                                    }
                                    else
                                    {
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'War'";
                                        ResultSet warSet = JDBCConnection.getStatement(sql, params).executeQuery();
                                        if (warSet.next())
                                        {
                                            long guild = warSet.getLong("Entry");
                                            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Logs'";
                                            warSet = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (warSet.next())
                                            {
                                                long channel = warSet.getLong("Entry");

                                                EmbedBuilder builder = new EmbedBuilder();
                                                IUser author = event.getAuthor();
                                                builder.withAuthorIcon(author.getAvatarURL());
                                                builder.withAuthorName(author.getName());
                                                builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #23 (Secret message)");
                                                builder.appendField("User ID", "" +  author.getLongID(), false);

                                                BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                            }
                                        }
                                    }
                                    response1 = set.getString("Entry");
                                }
                                else
                                {
                                    return;
                                }

                                sql = "UPDATE DiscordDB.Stones SET Tries = ? WHERE UserID = ? AND GameNumber = ?";
                                params.clear();
                                params.add(tries);
                                params.add(event.getAuthor().getLongID());
                                params.add(num);
                                JDBCConnection.getStatement(sql, params).executeUpdate();

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withAuthorName("Stones and Cups");
                                builder.withTitle("Guess " + (tries) + " of 7");
                                builder.withDescription(response1);
                                builder.withColor(GREEN);
                                builder.appendField("Your guess", guessEmote, false);

                                IMessage message = BotUtils.sendMessage(event.getChannel(), builder.build());

                                for (int i = 0; i < emotes.length; i++)
                                {
                                    BotUtils.addReaction(message, ReactionEmoji.of(emotes[i].name, emotes[i].id));
                                    boolean x = true;
                                    do
                                    {
                                        try
                                        {
                                            Thread.sleep(400);
                                            x = false;
                                        }
                                        catch (InterruptedException e)
                                        {
                                        }
                                    }
                                    while (x);
                                }

                                builder = new EmbedBuilder();
                                builder.withAuthorName("Stones and Cups");
                                builder.withColor(GREEN);
                                if (!url.equals(""))
                                {
                                    builder.withImage(url);
                                }
                                if (!thumb.equals(""))
                                {
                                    builder.withThumbnail(thumb);
                                }
                                if (correct)
                                {
                                    sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? ORDER BY Streak DESC LIMIT 1";
                                    params.clear();
                                    params.add(event.getAuthor().getLongID());
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    int best = 0;
                                    if (set.next())
                                    {
                                        best = set.getInt("Streak");
                                    }

                                    builder.appendField("Current Streak", "" + streak, false);
                                    builder.appendField("Best Streak", "" + best, false);
                                    builder.withDescription(response2);

                                    BotUtils.sendMessage(event.getChannel(), builder.build());
                                }
                                else if (tries == 7)
                                {
                                    sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? ORDER BY Streak DESC LIMIT 1";
                                    params.clear();
                                    params.add(event.getAuthor().getLongID());
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    int best = 0;
                                    if (set.next())
                                    {
                                        best = set.getInt("Streak");
                                    }

                                    String solutionEmote = "";
                                    for (int i = 0; i < solution.length(); i++)
                                    {
                                        solutionEmote += stoneMap.get(solution.substring(i, i + 1));
                                    }
                                    builder.appendField("Solution", solutionEmote, false);
                                    builder.appendField("Best Streak", "" + best, false);
                                    builder.withDescription(response2);

                                    BotUtils.sendMessage(event.getChannel(), builder.build());
                                }
                            }
                            else
                            {
                                BotUtils.sendMessage(event.getChannel(), "Invalid guess");
                            }
                        })),

                        new Command("stats", "Calculate your statistics", "stats", AccessLevel.EVERYONE, true, ((event, args) ->
                        {
                            int wins, games, streak;
                            //TODO change "Event = true"
                            String sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event = true AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                                    "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event = true AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                                    "(SELECT Streak FROM DiscordDB.Stones WHERE EVENT = true AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
                            List<Object> params = new ArrayList<>();
                            params.add(event.getAuthor().getLongID());
                            params.add(event.getAuthor().getLongID());
                            params.add(event.getAuthor().getLongID());
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                            if (set.next())
                            {
                                wins = set.getInt("Wins");
                                games = set.getInt("Games");
                                streak = set.getInt("Streak");
                            }
                            else
                            {
                                wins = 0;
                                games = 0;
                                streak = 0;
                            }

                            sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event = true AND EndTime IS NOT NULL AND UserID = ?";
                            params.clear();
                            params.add(event.getAuthor().getLongID());
                            set = JDBCConnection.getStatement(sql, params).executeQuery();

                            int sum = 0;
                            while (set.next())
                            {
                                sum += set.getInt("Tries");
                            }

                            double percent, tries;
                            if (games != 0)
                            {
                                percent = ((int)(wins * 1.0 / games * 10000)) / 100.0;
                            }
                            else
                            {
                                percent = 0;
                            }
                            if (wins != 0)
                            {
                                tries = ((int)(sum * 1.0 / wins * 100)) / 100.0;
                            }
                            else
                            {
                                tries = 0;
                            }

                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle("Stones and Cups");
                            builder.withColor(GREEN);
                            builder.appendField("Total Wins", "" + wins, false);
                            builder.appendField("Total Games", "" + games, false);
                            builder.appendField("Best Streak", "" + streak, false);
                            builder.appendField("Win Percentage", percent + "%", false);
                            builder.appendField("Average Win Guess Count", "" + tries, false);

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        new Command("all_stats", "Create graph of all statistics", "all_stats", AccessLevel.TESTER, true, ((event, args) ->
                        {
                            String sql = "SELECT DISTINCT UserID FROM DiscordDB.Stones";
                            List<Object> params = new ArrayList<>();
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                            List<Integer> allWins = new ArrayList<>();
                            List<Integer> allStreaks = new ArrayList<>();
                            Integer[] allTries = new Integer[12];
                            Integer[] allPercentage = new Integer[20];
                            for (int i = 0; i < allTries.length; i++)
                            {
                                allTries[i] = 0;
                            }
                            for (int i = 0; i < allPercentage.length; i++)
                            {
                                allPercentage[i] = 0;
                            }
                            while (set.next())
                            {
                                //TODO change "Event = true"
                                int wins, games, streak;
                                sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event = true AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                                        "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event = true AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                                        "(SELECT Streak FROM DiscordDB.Stones WHERE EVENT = true AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
                                params.clear();
                                params.add(set.getLong("UserID"));
                                params.add(set.getLong("UserID"));
                                params.add(set.getLong("UserID"));
                                ResultSet userSet = JDBCConnection.getStatement(sql, params).executeQuery();

                                if (userSet.next())
                                {
                                    wins = userSet.getInt("Wins");
                                    games = userSet.getInt("Games");
                                    streak = userSet.getInt("Streak");
                                }
                                else
                                {
                                    wins = 0;
                                    games = 0;
                                    streak = 0;
                                }

                                if (games == 0)
                                    continue;

                                sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event = true AND EndTime IS NOT NULL AND UserID = ?";
                                params.clear();
                                params.add(set.getLong("UserID"));
                                userSet = JDBCConnection.getStatement(sql, params).executeQuery();

                                int sum = 0;
                                while (userSet.next())
                                {
                                    sum += userSet.getInt("Tries");
                                }

                                int percent, tries;
                                if (games != 0)
                                {
                                    percent = (int)(wins * 1.0 / games * 100) / 5;
                                }
                                else
                                {
                                    percent = 0;
                                }
                                if (percent == 20)
                                {
                                    percent--;
                                }
                                allPercentage[percent]++;
                                if (wins != 0)
                                {
                                    tries = sum * 2 / wins - 2;
                                }
                                else
                                {
                                    tries = 0;
                                }
                                allTries[tries]++;
                            }

                            CategoryChart chart = new CategoryChartBuilder().width(1500).height(500).title("Stones and Cups Average Win Guesses").xAxisTitle("Average Win Guesses").yAxisTitle("Users").build();

                            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
                            chart.getStyler().setHasAnnotations(true);

                            String[] tries = new String[12];
                            for (int i = 0; i < tries.length; i++)
                            {
                                tries[i] = ((i + 2) / 2.0) + " - " + ((i + 3) / 2.0);
                            }

                            chart.addSeries("Tries Data", Arrays.asList(tries), Arrays.asList(allTries));

                            try
                            {
                                BitmapEncoder.saveBitmap(chart, "./TriesChart", BitmapEncoder.BitmapFormat.PNG);
                            }
                            catch (IOException e)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Error creating chart");
                                return;
                            }

                            File file = new File("./TriesChart.png");
                            BotUtils.sendFile(event.getChannel(), file);
                            file.delete();

                            chart = new CategoryChartBuilder().width(1500).height(500).title("Stones and Cups Win Percentage").xAxisTitle("Win Percentage").yAxisTitle("Users").build();

                            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
                            chart.getStyler().setHasAnnotations(true);

                            String[] percents = new String[20];
                            for (int i = 0; i < percents.length; i++)
                            {
                                percents[i] = (i * 5) + "% - " + ((i + 1) * 5) + "%";
                            }

                            chart.addSeries("Win Data", Arrays.asList(percents), Arrays.asList(allPercentage));

                            try
                            {
                                BitmapEncoder.saveBitmap(chart, "./PercentageChart", BitmapEncoder.BitmapFormat.PNG);
                            }
                            catch (IOException e)
                            {
                                BotUtils.sendMessage(event.getChannel(), "Error creating chart");
                                return;
                            }

                            file = new File("./PercentageChart.png");
                            BotUtils.sendFile(event.getChannel(), file);
                            file.delete();
                        }))
                },

                (event, args) ->
                {
                    BotUtils.help(map, event, args, "stones");
                })
        );
    }

    private class Emote
    {
        private String name;
        private long id;
        public Emote(String n, long i)
        {
            name = n;
            id = i;
        }
    }
}