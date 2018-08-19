import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private Map<String, ReactionEmoji> stoneMap2;
    private Map<String, String> emojiMap;
    private Map<String, String> emojiMap2;
    private final String[] PEGS = {"F", "W", "L", "T", "M", "S", "Y", "I", "K", "N"};
    private final String[] EMOTES = {"\uD83D\uDD25", "\uD83D\uDCA7", "\uD83C\uDF43", "\u26A1", "\uD83C\uDF15", "\u2600", "\u2728", "\u2744", "\u2601", "\uD83D\uDD05"};
    private final Emote[] STONES = {new Emote("firestone", 468167664775331850L), new Emote("waterstone", 468167718483525643L), new Emote("leafstone", 468167673214271488L), new Emote("thunderstone", 468167707934720011L), new Emote("moonstone", 468167683846832169L), new Emote("sunstone",468167696857563146L),
            new Emote("shinystone", 476133483966758925L), new Emote("icestone", 476133973261680641L), new Emote("duskstone", 476133462206578698L), new Emote("dawnstone", 476133450756128768L)};
    private final Emote[] WHITE_CUPS = {new Emote("w1", 469335051121983498L), new Emote("w2", 469335058889572352L), new Emote("w3", 469335070126112768L), new Emote("w4", 469335081752854541L)};
    private final Emote[] RED_CUPS = {new Emote("r1", 469335015113752596L), new Emote("r2", 469335023712075776L), new Emote("r3", 469335032809390090L), new Emote("r4", 469335041017905165L)};
    private final Emote[] GREEN_CUPS = {new Emote("g1", 469334976274366474L), new Emote("g2", 469334987490197514L), new Emote("g3", 469334994985287681L), new Emote("g4", 469335004049047561L)};
    private final Emote[] TAN_CUPS = {new Emote("t1", 480168147853246485L), new Emote("t2", 480168178413207553L), new Emote("t3", 480168186877181962L), new Emote("t4", 480168195802791936L)};
    private final Emote[] BLUE_CUPS = {new Emote("b1", 480168269265764353L), new Emote("b2", 480168278581575680L), new Emote("b3", 480168287653855235L), new Emote("b4", 480168298592469013L)};
    private final Color GREEN = new Color(1, 181, 83);
    private final Color BLUE = new Color(50, 121, 210);
    private final Color RED = new Color(243, 74, 74);
    private String prefix;

    public GameCommands(Map<String, Command> map, String p)
    {
        prefix = p;
        //Create map to find codes and emojis
        stoneMap = new HashMap<>();
        stoneMap2 = new HashMap<>();
        emojiMap = new HashMap<>();
        emojiMap2 = new HashMap<>();
        stoneMap.put(PEGS[0], ReactionEmoji.of(STONES[0].name, STONES[0].id));
        stoneMap.put(PEGS[1], ReactionEmoji.of(STONES[1].name, STONES[1].id));
        stoneMap.put(PEGS[2], ReactionEmoji.of(STONES[2].name, STONES[2].id));
        stoneMap.put(PEGS[3], ReactionEmoji.of(STONES[3].name, STONES[3].id));
        stoneMap.put(PEGS[4], ReactionEmoji.of(STONES[4].name, STONES[4].id));
        stoneMap.put(PEGS[5], ReactionEmoji.of(STONES[5].name, STONES[5].id));
        stoneMap2.put(PEGS[0], ReactionEmoji.of(STONES[0].name, STONES[0].id));
        stoneMap2.put(PEGS[1], ReactionEmoji.of(STONES[1].name, STONES[1].id));
        stoneMap2.put(PEGS[2], ReactionEmoji.of(STONES[2].name, STONES[2].id));
        stoneMap2.put(PEGS[3], ReactionEmoji.of(STONES[3].name, STONES[3].id));
        stoneMap2.put(PEGS[4], ReactionEmoji.of(STONES[4].name, STONES[4].id));
        stoneMap2.put(PEGS[5], ReactionEmoji.of(STONES[5].name, STONES[5].id));
        stoneMap2.put(PEGS[6], ReactionEmoji.of(STONES[6].name, STONES[6].id));
        stoneMap2.put(PEGS[7], ReactionEmoji.of(STONES[7].name, STONES[7].id));
        stoneMap2.put(PEGS[8], ReactionEmoji.of(STONES[8].name, STONES[8].id));
        stoneMap2.put(PEGS[9], ReactionEmoji.of(STONES[9].name, STONES[9].id));
        emojiMap.put(EMOTES[0], PEGS[0]);
        emojiMap.put(EMOTES[1], PEGS[1]);
        emojiMap.put(EMOTES[2], PEGS[2]);
        emojiMap.put(EMOTES[3], PEGS[3]);
        emojiMap.put(EMOTES[4], PEGS[4]);
        emojiMap.put(EMOTES[5], PEGS[5]);
        emojiMap2.put(EMOTES[0], PEGS[0]);
        emojiMap2.put(EMOTES[1], PEGS[1]);
        emojiMap2.put(EMOTES[2], PEGS[2]);
        emojiMap2.put(EMOTES[3], PEGS[3]);
        emojiMap2.put(EMOTES[4], PEGS[4]);
        emojiMap2.put(EMOTES[5], PEGS[5]);
        emojiMap2.put(EMOTES[6], PEGS[6]);
        emojiMap2.put(EMOTES[7], PEGS[7]);
        emojiMap2.put(EMOTES[8], PEGS[8]);
        emojiMap2.put(EMOTES[9], PEGS[9]);

        map.put("stones", new Command("stones", "Stones and Cups is a code-breaking game. Guess the stones in the correct order to win! You have 7 turns to guess.", prefix + "stones", AccessLevel.EVERYONE, false, new Command[]
                {
                        new Command("start", "Starts a game", "start [code]", AccessLevel.EVERYONE, true, ((event, args) ->
                        {
                            if (args.size() >= 1 && args.size() <= 2)
                            {
                                boolean isEvent1 = false;
                                boolean isEvent2 = false;
                                boolean isEvent3 = false;
                                //If there's an event code
                                if (args.size() > 1)
                                {
                                    String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 1'";
                                    List<Object> params = new ArrayList<>();
                                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        if (args.get(1).equals(set.getString("Entry")))
                                        {
                                            if (BotUtils.isPokemon(event))
                                            {
                                                isEvent1 = true;
                                            }
                                            else
                                            {
                                                BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                return;
                                            }
                                        }
                                        else
                                        {
                                            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 2'";
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next())
                                            {
                                                if (args.get(1).equals(set.getString("Entry")))
                                                {
                                                    if (BotUtils.isPokemon(event))
                                                    {
                                                        isEvent2 = true;
                                                    }
                                                    else
                                                    {
                                                        BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                        return;
                                                    }
                                                }
                                                else
                                                {
                                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 3'";
                                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                    if (set.next())
                                                    {
                                                        if (args.get(1).equals(set.getString("Entry")))
                                                        {
                                                            if (BotUtils.isWort(event))
                                                            {
                                                                isEvent3 = true;
                                                            }
                                                            else
                                                            {
                                                                BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                                return;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                            return;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                //TODO temporary else for event
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "This command could not be run at the time.");
                                    //return;
                                }

                                //Find if there exists an active game, otherwise get stats from it
                                String sql = "SELECT StartTime, EndTime FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                if (set.next())
                                {
                                    //If there is a current game (no end time) send error message
                                    if (set.getTimestamp("EndTime") == null)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "There is already an active game. Complete it before starting another!");
                                        return;
                                    }
                                    //Cooldown of 3 minutes, checks if next command is before that
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

                                int streak = 0;
                                int gameNum = 1;
                                sql = "SELECT Streak, GameNumber FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = ? AND Event2 = ? AND Event3 = ? ORDER BY StartTime DESC LIMIT 1";
                                params.add(isEvent1);
                                params.add(isEvent2);
                                params.add(isEvent3);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    streak = set.getInt("Streak");
                                    gameNum = set.getInt("GameNumber") + 1;
                                }

                                sql = "INSERT INTO DiscordDB.Stones (UserID, StartTime, Solution, Tries, Event1, Event2, Event3, Win, Streak, GameNumber) VALUES (?, ?, ?, 0, ?, ?, ?, false, ?, ?)";
                                params.clear();
                                params.add(event.getAuthor().getLongID());
                                params.add(BotUtils.now());
                                String solution = "";
                                for (int i = 0; i < 4; i++)
                                {
                                    if (isEvent2)
                                        solution += PEGS[Generator.getNext(event, i)];
                                    else
                                        solution += PEGS[BotUtils.RAND.nextInt(6)];
                                }
                                params.add(solution);
                                params.add(isEvent1);
                                params.add(isEvent2);
                                params.add(isEvent3);
                                params.add(streak);
                                params.add(gameNum);
                                JDBCConnection.getStatement(sql, params).executeUpdate();

                                if (isEvent1)
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Start Event 1'";
                                else if (isEvent2)
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Start Event 2'";
                                else
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Start'";
                                params.clear();
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String phrase;
                                if (set.next())
                                {
                                    phrase = set.getString("Entry");
                                }
                                else
                                {
                                    return;
                                }

                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess'";
                                if (isEvent2)
                                {
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess Event 2'";
                                }
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String url = "";
                                if (set.next())
                                {
                                    url = set.getString("Entry");
                                }

                                String key = "";
                                for (int i = 0; i < PEGS.length && (isEvent2 || i < 6); i++)
                                {
                                    key += stoneMap2.get(PEGS[i]) + " - " + EMOTES[i] + " (" + PEGS[i] + ")";
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

                                sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = ? AND Event2 = ? AND Event3 = ? ORDER BY Streak DESC LIMIT 1";
                                params.clear();
                                params.add(event.getAuthor().getLongID());
                                params.add(isEvent1);
                                params.add(isEvent2);
                                params.add(isEvent3);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                int best = 0;
                                if (set.next())
                                {
                                    best = set.getInt("Streak");
                                }

                                String cups;
                                if (!isEvent2)
                                    cups = ReactionEmoji.of(GREEN_CUPS[0].name, GREEN_CUPS[0].id) + " - Correct stone and correct position\n" + ReactionEmoji.of(RED_CUPS[0].name, RED_CUPS[0].id) + " - Correct stone but incorrect position\n" + ReactionEmoji.of(WHITE_CUPS[0].name, WHITE_CUPS[0].id) + " - Incorrect stone";
                                else
                                    cups = ReactionEmoji.of(BLUE_CUPS[0].name, BLUE_CUPS[0].id) + " - Correct stone and correct position\n" + ReactionEmoji.of(TAN_CUPS[0].name, TAN_CUPS[0].id) + " - Correct stone but incorrect position\n" + ReactionEmoji.of(WHITE_CUPS[0].name, WHITE_CUPS[0].id) + " - Incorrect stone";

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withAuthorName("Stones and Cups");
                                builder.withTitle("Starting game " + gameNum);
                                builder.withDescription(phrase);
                                builder.withImage(url);
                                if (isEvent2)
                                    builder.withColor(BLUE);
                                else if (isEvent3)
                                    builder.withColor(RED);
                                else
                                    builder.withColor(GREEN);
                                builder.appendField("Guessing Key", key, false);
                                builder.appendField("Response Key", cups, false);
                                builder.appendField("Guess Command", prefix + "stones g \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\t\tor\t\t" + prefix + "stones g FFFF", false);
                                if (!isEvent2)
                                {
                                    builder.appendField("Current Streak", "" + streak, false);
                                    builder.appendField("Best Streak", "" + best, false);
                                }

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
                                String sql = "SELECT Solution, GameNumber, EndTime, Event1, Event2, Event3 FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                if (set.next())
                                {
                                    if (set.getTimestamp("EndTime") == null)
                                    {
                                        boolean isEvent1 = set.getBoolean("Event1");
                                        boolean isEvent2 = set.getBoolean("Event2");
                                        boolean isEvent3 = set.getBoolean("Event3");

                                        sql = "UPDATE DiscordDB.Stones SET EndTime = ? WHERE UserID = ? AND GameNumber = ? AND Event1 = ? AND Event2 = ? AND Event3 = ?";
                                        params.clear();
                                        params.add(0, BotUtils.now());
                                        params.add(event.getAuthor().getLongID());
                                        params.add(set.getInt("GameNumber"));
                                        params.add(isEvent1);
                                        params.add(isEvent2);
                                        params.add(isEvent3);
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

                                        if (!isEvent2)
                                            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess'";
                                        else
                                            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Guess Event 2'";
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
                                        if (isEvent2)
                                            builder.withColor(BLUE);
                                        else if (isEvent3)
                                            builder.withColor(RED);
                                        else
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
                                String sql = "SELECT GameNumber, EndTime, Solution, Tries, Streak, Event1, Event2, Event3 FROM DiscordDB.Stones WHERE UserID = ? ORDER BY StartTime DESC LIMIT 1";
                                List<Object> params = new ArrayList<>();
                                params.add(event.getAuthor().getLongID());
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                String solution;
                                int tries;
                                int streak;
                                int num;
                                boolean isEvent1;
                                boolean isEvent2;
                                boolean isEvent3;
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
                                    isEvent1 = set.getBoolean("Event1");
                                    isEvent2 = set.getBoolean("Event2");
                                    isEvent3 = set.getBoolean("Event3");
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "There is no active game. Start one using the command `" + prefix + "stones start`");
                                    return;
                                }

                                Map<String, ReactionEmoji> stones;
                                Map<String, String> emojis;
                                if (isEvent2)
                                {
                                    stones = stoneMap2;
                                    emojis = emojiMap2;
                                }
                                else
                                {
                                    stones = stoneMap;
                                    emojis = emojiMap;
                                }

                                String guess = "";
                                if (args.size() == 2)
                                {
                                    String temp = args.get(1);
                                    for (int i = 0; i < temp.length(); i++)
                                    {
                                        if (emojis.containsKey(temp.substring(i, i + 1)))
                                        {
                                            guess += emojis.get(temp.substring(i, i + 1));
                                        }
                                        else if (i < temp.length() - 1 && emojis.containsKey(temp.substring(i, i + 2)))
                                        {
                                            guess += emojis.get(temp.substring(i, i + 2));
                                            i++;
                                        }
                                        else if (stones.containsKey(temp.substring(i, i + 1)))
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
                                        if (emojis.containsKey(temp))
                                        {
                                            temp = emojis.get(temp);
                                        }
                                        if (stones.containsKey(temp))
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

                                if (!(isEvent1 || isEvent2 || isEvent3))
                                {
                                    for (int i = 0; i < result.length; i++)
                                    {
                                        int max = i;
                                        for (int j = i + 1; j < result.length; j++)
                                        {
                                            if (result[j] > result[max])
                                            {
                                                max = j;
                                            }
                                        }
                                        int temp = result[i];
                                        result[i] = result[max];
                                        result[max] = temp;
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
                                        if (!isEvent2)
                                            emotes[i] = RED_CUPS[i];
                                        else
                                            emotes[i] = TAN_CUPS[i];
                                        correct = false;
                                    }
                                    else if (result[i] == 2)
                                    {
                                        if (!isEvent2)
                                            emotes[i] = GREEN_CUPS[i];
                                        else
                                            emotes[i] = BLUE_CUPS[i];
                                    }
                                    guessEmote += stones.get(guess.substring(i, i + 1));
                                }

                                String response1, response2 = "", url = "", thumb = "";
                                if (correct)
                                {
                                    streak++;
                                    if (!isEvent2)
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Win'";
                                    else
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Win Event 2'";
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

                                    boolean changed = false;
                                    int current = 1;
                                    if (isEvent1)
                                    {
                                        sql = "SELECT CurrentStreak, MaxStreak FROM DiscordDB.StonesEventStreaks WHERE UserID = ?";
                                        params.clear();
                                        params.add(event.getAuthor().getLongID());
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        if (set.next())
                                        {
                                            current = set.getInt("CurrentStreak") + 1;
                                            int max = set.getInt("MaxStreak");
                                            if (max < current)
                                            {
                                                max = current;
                                                changed = true;
                                            }
                                            int level = 7;

                                            sql = "SELECT * FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = false AND Event2 = true AND Event3 = false AND Win = true";
                                            params.clear();
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next())
                                            {
                                                level = 11;
                                            }
                                            if (max <= level  && streak >= current)
                                            {
                                                sql = "UPDATE DiscordDB.StonesEventStreaks SET CurrentStreak = ?, MaxStreak = ? WHERE UserID = ?";
                                                params.clear();
                                                params.add(current);
                                                params.add(max);
                                                params.add(event.getAuthor().getLongID());
                                                JDBCConnection.getStatement(sql, params).executeUpdate();
                                            }
                                        }
                                        else
                                        {
                                            sql = "INSERT INTO DiscordDB.StonesEventStreaks (UserID, CurrentStreak, MaxStreak) VALUES (?, 1, 1)";
                                            JDBCConnection.getStatement(sql, params).executeUpdate();
                                        }
                                    }

                                    sql = "UPDATE DiscordDB.Stones SET EndTime = ?, Win = true, Streak = ? WHERE UserID = ? AND GameNumber = ? AND Event1 = ? AND Event2 = ? AND Event3 = ?";
                                    params.clear();
                                    params.add(BotUtils.now());
                                    params.add(streak);
                                    params.add(event.getAuthor().getLongID());
                                    params.add(num);
                                    params.add(isEvent1);
                                    params.add(isEvent2);
                                    params.add(isEvent3);
                                    JDBCConnection.getStatement(sql, params).executeUpdate();

                                    if (!isEvent2)
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Win'";
                                    else
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Win Event 2'";
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

                                    if (isEvent1)
                                    {
                                        if (current == 2 && changed)
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
                                        else if (current == 4 && changed)
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
                                        else if (current == 7 && changed)
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
                                        else if (current == 9 && changed)
                                        {
                                            sql = "SELECT * FROM DiscordDB.Stones WHERE UserID = ? AND Event2 = true AND Win = true";
                                            params.clear();
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next())
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak 9'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    response2 = set.getString("Entry");
                                                } else
                                                {
                                                    return;
                                                }

                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak Image 9'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    thumb = set.getString("Entry");
                                                } else
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
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #29");
                                                        builder.appendField("User ID", "" + author.getLongID(), false);
                                                        builder.appendField("Streak Number", "9", false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }
                                        }
                                        else if (current == 11 && changed)
                                        {
                                            sql = "SELECT * FROM DiscordDB.Stones WHERE UserID = ? AND Event2 = true AND Win = true";
                                            params.clear();
                                            params.add(event.getAuthor().getLongID());
                                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                                            if (set.next())
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Streak 11'";
                                                params.clear();
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    response2 = set.getString("Entry");
                                                } else
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
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #29");
                                                        builder.appendField("User ID", "" + author.getLongID(), false);
                                                        builder.appendField("Streak Number", "11", false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else if (isEvent2)
                                    {
                                        sql = "SELECT Count(*) AS Count FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = false AND Event2 = true AND Event3 = false AND Win = true";
                                        params.clear();
                                        params.add(event.getAuthor().getLongID());
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();

                                        if (set.next())
                                        {
                                            if (set.getLong("Count") <= 1)
                                            {
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
                                                        builder.withTitle(author.getName() + "#" + author.getDiscriminator() + " received intel drop #29 (Empoleon)");
                                                        builder.appendField("User ID", "" + author.getLongID(), false);

                                                        BotUtils.sendMessage(event.getClient().getGuildByID(guild).getChannelByID(channel), builder.build());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else if ((isEvent1 || isEvent3) && tries == 7 || isEvent2 && tries == 3 || tries == 7)
                                {
                                    if (!isEvent2)
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Loss'";
                                    else
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Loss Event 2'";
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

                                    sql = "UPDATE DiscordDB.Stones SET EndTime = ?, Streak = 0 WHERE UserID = ? AND GameNumber = ? AND Event1 = ? AND Event2 = ? AND Event3 = ?";
                                    params.clear();
                                    params.add(BotUtils.now());
                                    params.add(event.getAuthor().getLongID());
                                    params.add(num);
                                    params.add(isEvent1);
                                    params.add(isEvent2);
                                    params.add(isEvent3);
                                    JDBCConnection.getStatement(sql, params).executeUpdate();

                                    if (!isEvent2)
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Loss'";
                                    else
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Image Loss Event 2'";
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
                                    if (!isEvent1 || BotUtils.RAND.nextInt(100 / max) != 0)
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

                                sql = "UPDATE DiscordDB.Stones SET Tries = ? WHERE UserID = ? AND GameNumber = ? AND Event1 = ? AND Event2 = ? AND Event3 = ?";
                                params.clear();
                                params.add(tries);
                                params.add(event.getAuthor().getLongID());
                                params.add(num);
                                params.add(isEvent1);
                                params.add(isEvent2);
                                params.add(isEvent3);
                                JDBCConnection.getStatement(sql, params).executeUpdate();

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.withAuthorName("Stones and Cups");
                                if (isEvent1 || isEvent3)
                                    builder.withTitle("Guess " + (tries) + " of 7");
                                else if (isEvent2)
                                    builder.withTitle("Guess " + (tries) + " of 3");
                                else
                                    builder.withTitle("Guess " + (tries) + " of 10");
                                builder.withDescription(response1);
                                if (isEvent2)
                                    builder.withColor(BLUE);
                                else if (isEvent3)
                                    builder.withColor(RED);
                                else
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
                                if (isEvent2)
                                    builder.withColor(BLUE);
                                else if (isEvent3)
                                    builder.withColor(RED);
                                else
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
                                    sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = ? AND Event2 = ? AND Event3 = ? ORDER BY Streak DESC LIMIT 1";
                                    params.clear();
                                    params.add(event.getAuthor().getLongID());
                                    params.add(isEvent1);
                                    params.add(isEvent2);
                                    params.add(isEvent3);
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    int best = 0;
                                    if (set.next())
                                    {
                                        best = set.getInt("Streak");
                                    }

                                    if (!isEvent2)
                                    {
                                        builder.appendField("Current Streak", "" + streak, false);
                                        builder.appendField("Best Streak", "" + best, false);
                                    }
                                    builder.withDescription(response2);

                                    if (!(isEvent1 || isEvent2))
                                        updateLeaderboard(event, isEvent3);

                                    BotUtils.sendMessage(event.getChannel(), builder.build());
                                }
                                else if ((isEvent1 || isEvent3) && tries == 7 || isEvent2 && tries == 3 || tries == 10)
                                {
                                    sql = "SELECT Streak FROM DiscordDB.Stones WHERE UserID = ? AND Event1 = ? AND Event2 = ? AND Event3 = ? ORDER BY Streak DESC LIMIT 1";
                                    params.clear();
                                    params.add(event.getAuthor().getLongID());
                                    params.add(isEvent1);
                                    params.add(isEvent2);
                                    params.add(isEvent3);
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
                                    if (!isEvent2)
                                    {
                                        builder.appendField("Solution", solutionEmote, false);
                                        builder.appendField("Best Streak", "" + best, false);
                                    }
                                    builder.withDescription(response2);

                                    if (!(isEvent1 || isEvent2))
                                        updateLeaderboard(event, isEvent3);

                                    BotUtils.sendMessage(event.getChannel(), builder.build());
                                }
                            }
                            else
                            {
                                BotUtils.sendMessage(event.getChannel(), "Invalid guess");
                            }
                        })),

                        new Command("stats", "Calculate your statistics", "stats [code]", AccessLevel.EVERYONE, false, ((event, args) ->
                        {
                            int wins, games, streak;
                            double score = 0;
                            boolean isEvent1 = false;
                            boolean isEvent2 = false;
                            boolean isEvent3 = false;

                            if (args.size() > 1)
                            {
                                String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 1'";
                                List<Object> params = new ArrayList<>();
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    if (args.get(1).equals(set.getString("Entry")))
                                    {
                                        if (BotUtils.isPokemon(event))
                                        {
                                            isEvent1 = true;
                                        }
                                        else
                                        {
                                            BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                            return;
                                        }
                                    }
                                    else
                                    {
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 2'";
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        if (set.next())
                                        {
                                            if (args.get(1).equals(set.getString("Entry")))
                                            {
                                                if (BotUtils.isPokemon(event))
                                                {
                                                    isEvent2 = true;
                                                }
                                                else
                                                {
                                                    BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                    return;
                                                }
                                            }

                                            else
                                            {
                                                sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 3'";
                                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                                if (set.next())
                                                {
                                                    if (args.get(1).equals(set.getString("Entry")))
                                                    {
                                                        if (BotUtils.isWort(event))
                                                        {
                                                            isEvent3 = true;
                                                        }
                                                        else
                                                        {
                                                            BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                            return;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            //TODO temporary else for event
                            else
                            {
                                BotUtils.sendMessage(event.getChannel(), "This command could not be run at the time.");
                                return;
                            }

                            String sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event1 = ? AND Event2 = ? AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                                    "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event1 = ? AND Event2 = ? AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                                    "(SELECT Streak FROM DiscordDB.Stones WHERE Event1 = ? AND Event2 = ? AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
                            List<Object> params = new ArrayList<>();
                            params.add(isEvent1);
                            params.add(isEvent2);
                            params.add(isEvent3);
                            params.add(event.getAuthor().getLongID());
                            params.add(isEvent1);
                            params.add(isEvent2);
                            params.add(isEvent3);
                            params.add(event.getAuthor().getLongID());
                            params.add(isEvent1);
                            params.add(isEvent2);
                            params.add(isEvent3);
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

                            if (!isEvent2)
                                sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event1 = ? AND Event2 = ? AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?";
                            else
                                sql = "SELECT Tries FROM DiscordDB.Stones WHERE Event1 = ? AND Event2 = ? AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?";
                            params.clear();
                            params.add(isEvent1);
                            params.add(isEvent2);
                            params.add(isEvent3);
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

                            if (games != 0)
                            {
                                score = streak * 20.0 / ((sum * 1.0 + 15 * (games - wins)) / games);
                            }
                            score = ((int)(score * 1000)) / 1000.0;

                            EmbedBuilder builder = new EmbedBuilder();

                            if (isEvent1)
                            {
                                builder.withTitle("Stones and Cups (" + args.get(1) + ")");
                                builder.withColor(GREEN);
                                builder.appendField("Total Wins", "" + wins, true);
                                builder.appendField("Total Games", "" + games, true);
                                builder.appendField("Best Streak", "" + streak, true);
                                builder.appendField("Win Percentage", percent + "%", true);
                                builder.appendField("Average Win Guess Count", "" + tries, true);
                            }
                            else if (isEvent2)
                            {
                                if (wins >= 1)
                                {
                                    builder.withTitle("Stones and Cups (" + args.get(1) + ")");
                                    builder.withColor(BLUE);
                                    builder.appendField("Total Games", "" + games, true);
                                    builder.appendField("Total Guesses", "" + sum, true);
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "There are no stats available at this time");
                                    return;
                                }
                            }
                            else
                            {
                                int trophy = wins / 5;
                                if (trophy > 0)
                                {
                                    if (trophy > 28)
                                        trophy = 28;
                                    sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = ?";
                                    params.clear();
                                    params.add("Stones Trophy " + trophy);
                                    set = JDBCConnection.getStatement(sql, params).executeQuery();
                                    if (set.next())
                                    {
                                        builder.withImage(set.getString("Entry"));
                                    }
                                }

                                sql = "SELECT UserID FROM DiscordDB.StonesScores WHERE Event3 = ? ORDER BY Score DESC LIMIT 10";
                                params.clear();
                                params.add(isEvent3);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                while (set.next())
                                {
                                    if (event.getAuthor().getLongID() == set.getLong("UserID"))
                                    {
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Top'";
                                        params.clear();
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        if (set.next())
                                            builder.withThumbnail(set.getString("Entry"));
                                        break;
                                    }
                                }

                                if (isEvent3)
                                {
                                    builder.withTitle("Stones and Cups (" + args.get(1) + ")");
                                    builder.withColor(RED);
                                }
                                else
                                {
                                    builder.withTitle("Stones and Cups");
                                    builder.withColor(GREEN);
                                }
                                builder.appendField("Total Wins", "" + wins, true);
                                builder.appendField("Total Games", "" + games, true);
                                builder.appendField("Best Streak", "" + streak, true);
                                builder.appendField("Win Percentage", percent + "%", true);
                                builder.appendField("Average Win Guess Count", "" + tries, true);
                                builder.appendField("Score", "" + score, true);
                            }

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        new Command("leaderboard", "Generate a leaderboard of top players", "leaderboard", AccessLevel.TESTER, false, ((event, args) ->
                        {
                            boolean isEvent3 = false;
                            String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 3'";
                            List<Object> params = new ArrayList<>();
                            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                            if (args.size() > 1 && set.next())
                            {
                                if (args.get(1).equals(set.getString("Entry")))
                                {
                                    if (BotUtils.isWort(event))
                                    {
                                        isEvent3 = true;
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                        return;
                                    }
                                }
                                else
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                    return;
                                }
                            }

                            sql = "SELECT DISTINCT(UserID) FROM DiscordDB.Stones WHERE Event1 = false AND Event2 = false AND Event3 = ?";
                            params.add(isEvent3);
                            ResultSet userSet = JDBCConnection.getStatement(sql, params).executeQuery();
                            while (userSet.next())
                            {
                                long id = userSet.getLong("UserID");
                                int wins, games, streak;
                                sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                                        "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                                        "(SELECT Streak FROM DiscordDB.Stones WHERE Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
                                params.clear();
                                params.add(isEvent3);
                                params.add(id);
                                params.add(isEvent3);
                                params.add(id);
                                params.add(isEvent3);
                                params.add(id);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();

                                if (set.next())
                                {
                                    wins = set.getInt("Wins");
                                    games = set.getInt("Games");
                                    streak = set.getInt("Streak");
                                } else
                                {
                                    wins = 0;
                                    games = 0;
                                    streak = 0;
                                }

                                sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?";
                                params.clear();
                                params.add(isEvent3);
                                params.add(id);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();

                                int sum = 0;
                                while (set.next())
                                {
                                    sum += set.getInt("Tries");
                                }

                                double score = 0;
                                if (games != 0)
                                {
                                    score = streak * 20.0 / ((sum * 1.0 + 15 * (games - wins)) / games);
                                }
                                score = ((int) (score * 1000)) / 1000.0;

                                sql = "SELECT * FROM DiscordDB.StonesScores WHERE UserID = ? AND Event3 = ?";
                                params.clear();
                                params.add(event.getAuthor().getLongID());
                                params.add(isEvent3);
                                set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    sql = "UPDATE DiscordDB.StonesScores SET Score = ? WHERE UserID = ? AND Event3 = ?";
                                    params.add(0, score);
                                }
                                else
                                {
                                    sql = "INSERT INTO DiscordDB.StonesScores (UserID, Event3, Score) VALUES (?, ?, ?)";
                                    params.add(score);
                                }
                                JDBCConnection.getStatement(sql, params).executeUpdate();
                            }

                            EmbedBuilder builder = new EmbedBuilder();
                            builder.withTitle("Stones and Cups Leaderboard");
                            if (isEvent3)
                                builder.withColor(RED);
                            else
                                builder.withColor(GREEN);

                            sql = "SELECT UserID, Score FROM DiscordDB.StonesScores WHERE Event3 = ? ORDER BY Score DESC LIMIT 10";
                            params.clear();
                            params.add(isEvent3);
                            set = JDBCConnection.getStatement(sql, params).executeQuery();
                            int count = 1;
                            while (set.next())
                            {
                                IUser user = event.getClient().getUserByID(set.getLong("UserID"));
                                builder.appendField(count + ". " + user.getName() + "#" + user.getDiscriminator(), "" + ((int)(set.getDouble("Score") * 1000)) / 1000.0, false);
                                count++;
                            }

                            BotUtils.sendMessage(event.getChannel(), builder.build());
                        })),

                        new Command("all_stats", "Create graph of all statistics", "all_stats", AccessLevel.TESTER, false, ((event, args) ->
                        {
                            boolean isEvent1 = false;
                            boolean isEvent3 = false;

                            if (args.size() > 1)
                            {
                                String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 1'";
                                List<Object> params = new ArrayList<>();
                                ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
                                if (set.next())
                                {
                                    if (args.get(1).equals(set.getString("Entry")))
                                    {
                                        if (BotUtils.isPokemon(event))
                                        {
                                            isEvent1 = true;
                                        }
                                        else
                                        {
                                            BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                            return;
                                        }
                                    }
                                    else
                                    {
                                        sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Secret 3'";
                                        set = JDBCConnection.getStatement(sql, params).executeQuery();
                                        if (set.next())
                                        {
                                            if (args.get(1).equals(set.getString("Entry")))
                                            {
                                                if (BotUtils.isWort(event))
                                                {
                                                    isEvent3 = true;
                                                }
                                                else
                                                {
                                                    BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                    return;
                                                }
                                            }
                                            else
                                            {
                                                BotUtils.sendMessage(event.getChannel(), "Please provide a valid event code");
                                                return;
                                            }
                                        }
                                    }
                                }
                            }

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
                                int wins, games, streak;
                                sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event1 = ? AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                                        "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event1 = ? AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                                        "(SELECT Streak FROM DiscordDB.Stones WHERE Event1 = ? AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
                                params.clear();
                                params.add(isEvent1);
                                params.add(isEvent3);
                                params.add(set.getLong("UserID"));
                                params.add(isEvent1);
                                params.add(isEvent3);
                                params.add(set.getLong("UserID"));
                                params.add(isEvent1);
                                params.add(isEvent3);
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

                                sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event1 = ? AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?";
                                params.clear();
                                params.add(isEvent1);
                                params.add(isEvent3);
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

    private void updateLeaderboard(MessageReceivedEvent event, boolean isEvent3) throws SQLException
    {
        int wins, games, streak;
        String sql = "SELECT (SELECT COUNT(*) FROM DiscordDB.Stones WHERE Win = true AND Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Wins, " +
                "(SELECT COUNT(*) FROM DiscordDB.Stones WHERE Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?) AS Games, " +
                "(SELECT Streak FROM DiscordDB.Stones WHERE Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ? ORDER BY Streak DESC LIMIT 1) AS Streak";
        List<Object> params = new ArrayList<>();
        params.add(isEvent3);
        params.add(event.getAuthor().getLongID());
        params.add(isEvent3);
        params.add(event.getAuthor().getLongID());
        params.add(isEvent3);
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

        sql = "SELECT Tries FROM DiscordDB.Stones WHERE Win = true AND Event1 = false AND Event2 = false AND Event3 = ? AND EndTime IS NOT NULL AND UserID = ?";
        params.clear();
        params.add(isEvent3);
        params.add(event.getAuthor().getLongID());
        set = JDBCConnection.getStatement(sql, params).executeQuery();

        int sum = 0;
        while (set.next())
        {
            sum += set.getInt("Tries");
        }

        double score = 0;
        if (games != 0)
        {
            score = streak * 20.0 / ((sum * 1.0 + 15 * (games - wins)) / games);
        }
        score = ((int)(score * 1000)) / 1000.0;

        sql = "SELECT * FROM DiscordDB.StonesScores WHERE UserID = ? AND Event3 = ?";
        params.clear();
        params.add(event.getAuthor().getLongID());
        params.add(isEvent3);
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            sql = "UPDATE DiscordDB.StonesScores SET Score = ? WHERE UserID = ? AND Event3 = ?";
            params.add(0, score);
        }
        else
        {
            sql = "INSERT INTO DiscordDB.StonesScores (UserID, Event3, Score) VALUES (?, ?, ?)";
            params.add(score);
        }
        JDBCConnection.getStatement(sql, params).executeUpdate();

        long channel;
        if (isEvent3)
            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Leaderboard Channel Event 3'";
        else
            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Leaderboard Channel'";
        params.clear();
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            channel = set.getLong("Entry");
        }
        else
            return;

        long message;
        if (isEvent3)
            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Leaderboard Message Event 3'";
        else
            sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Stones Leaderboard Message'";
        params.clear();
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        if (set.next())
        {
            message = set.getLong("Entry");
        }
        else
            return;

        EmbedBuilder builder = new EmbedBuilder();
        builder.withTitle("Stones and Cups Leaderboard");
        if (!isEvent3)
            builder.withColor(GREEN);
        else
            builder.withColor(RED);

        sql = "SELECT UserID, Score FROM DiscordDB.StonesScores WHERE Event3 = ? ORDER BY Score DESC LIMIT 10";
        params.clear();
        params.add(isEvent3);
        set = JDBCConnection.getStatement(sql, params).executeQuery();
        int count = 1;
        while (set.next())
        {
            IUser user = event.getClient().getUserByID(set.getLong("UserID"));
            builder.appendField(count + ". " + user.getName() + "#" + user.getDiscriminator(), "" + ((int)(set.getDouble("Score") * 1000)) / 1000.0, false);
            count++;
        }

        IMessage m = event.getClient().getChannelByID(channel).getMessageHistoryIn(message, message).getEarliestMessage();
        m.edit(builder.build());
    }
}