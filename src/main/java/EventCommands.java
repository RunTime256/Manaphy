import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventCommands
{

    public EventCommands(Map<String, Command> map)
    {
        //Commands for league betting in Pokemon discord
        map.put("league", new Command("league", "Activities for the Mod League", BotUtils.BOT_PREFIX + "league", AccessLevel.DEACTIVATED, new Command[]
                {
                        //Sub-command to bet
                        new Command("bet", "Bet on a placing (1st, 2nd, or 3rd) for the Mod League Champion Tournament (Use ", "bet <place> <user code>", AccessLevel.DEACTIVATED, ((event, args) ->
                        {
                            if (event.getGuild() == null && BotUtils.isPokemon(event))
                            {
                                if (args.size() == 3)
                                {
                                    try
                                    {
                                        int place = Integer.parseInt(args.get(1));
                                        String code = args.get(2);

                                        if (place >= 1 && place <= 3)
                                        {
                                            String name;
                                            try
                                            {
                                                //Selects username from given code
                                                String sql = "SELECT Username FROM DiscordDB.LeagueChampions WHERE Code = ?";
                                                List<Object> params = new ArrayList<>();
                                                params.add(code);
                                                PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                                                ResultSet set = statement.executeQuery();
                                                if (set == null)
                                                    return;
                                                else if (set.next())
                                                {
                                                    name = set.getString("Username");
                                                }
                                                else
                                                {
                                                    BotUtils.sendMessage(event.getChannel(), "Invalid user code. Use `" + BotUtils.BOT_PREFIX + "league list` to view Champions and their codes");
                                                    statement.close();
                                                    return;
                                                }
                                                statement.close();

                                                //Checks if there is already a bet for the current placing
                                                sql = "SELECT * FROM DiscordDB.LeagueBet WHERE UserID = ? AND Place = ?";
                                                params = new ArrayList<>();
                                                params.add(event.getAuthor().getLongID());
                                                params.add(place);
                                                statement = JDBCConnection.getStatement(sql, params);
                                                set = statement.executeQuery();
                                                if (set == null)
                                                    return;
                                                //If there is a bet, update it
                                                else if (set.next())
                                                {
                                                    sql = "UPDATE DiscordDB.LeagueBet SET Code = ? WHERE UserID = ? AND Place = ?";
                                                    params.add(0, code);
                                                }
                                                //If not, insert it
                                                else
                                                {
                                                    sql = "INSERT INTO DiscordDB.LeagueBet (UserId, Place, Code) VALUES(?, ?, ?)";
                                                    params.add(code);
                                                }
                                                statement.close();

                                                statement = JDBCConnection.getStatement(sql, params);
                                                statement.executeUpdate();
                                            }
                                            catch (SQLException e)
                                            {
                                                System.out.println("Couldn't process result set");
                                                e.printStackTrace();
                                                return;
                                            }

                                            //Set emoji based on given place
                                            String emoji = "";
                                            String num = "";
                                            if (place == 1)
                                            {
                                                emoji = ":first_place:";
                                                num = "1st";
                                            }
                                            else if (place == 2)
                                            {
                                                emoji = ":second_place:";
                                                num = "2nd";
                                            }
                                            else if (place == 3)
                                            {
                                                emoji = ":third_place:";
                                                num = "3rd";
                                            }
                                            String response = "You have chosen " + name + " to place " + num + " in the Mod League Champion Tournament. " + emoji;
                                            BotUtils.sendMessage(event.getChannel(), response);
                                        }
                                        else
                                        {
                                            BotUtils.help(map, event, args, "league");
                                        }
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        BotUtils.help(map, event, args, "league");
                                    }
                                }
                                else
                                {
                                    BotUtils.help(map, event, args, "league");
                                }
                            }
                        })),

                        //Sub-command to list all champions
                        new Command("list", "List the users to bet on for the Mod League Champion Tournament", "list", AccessLevel.DEACTIVATED, ((event, args) ->
                        {
                            if (event.getGuild() == null && BotUtils.isPokemon(event))
                            {
                                try
                                {
                                    //Select and all champions and codes
                                    String sql = "SELECT Username, Code FROM DiscordDB.LeagueChampions";
                                    List<Object> params = new ArrayList<>();
                                    PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                                    ResultSet set = statement.executeQuery();
                                    String message = "The current Mod League Champions that will compete are:\n\n";

                                    if (set == null)
                                        return;
                                    else if (set.next())
                                    {
                                        set.previous();
                                        //List all champions to vote for
                                        while (set.next())
                                        {
                                            message += ":small_orange_diamond: " + set.getString("Username") + " (" + set.getString("Code") + ")" + "\n";
                                        }
                                    }
                                    else
                                    {
                                        message += "`There are currently no champions to select`";
                                    }
                                    statement.close();

                                    BotUtils.sendMessage(event.getChannel(), message);
                                }
                                catch (SQLException e)
                                {
                                    System.out.println("Couldn't process result set");
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        })),

                        //List current bets for the current user
                        new Command("my_bets", "List your bets for the Mod League Champion Tournament", "my_bets", AccessLevel.DEACTIVATED, ((event, args) ->
                        {
                            if (event.getGuild() == null && BotUtils.isPokemon(event))
                            {
                                try
                                {
                                    //Select info for rankings
                                    String sql = "SELECT Place, Code FROM DiscordDB.LeagueBet WHERE UserID = ? ORDER BY Place ASC";
                                    List<Object> params = new ArrayList<>();
                                    params.add(event.getAuthor().getLongID());
                                    PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                                    ResultSet set = statement.executeQuery();
                                    String message = "Your bets for the Mod League Champions are:\n\n";
                                    params.remove(0);

                                    if (set == null)
                                        return;
                                    else if (set.next())
                                    {
                                        //Select names for each place
                                        sql = "SELECT Username FROM DiscordDB.LeagueChampions WHERE Code = ?";
                                        params.add(set.getString("Code"));
                                        PreparedStatement userStatement = JDBCConnection.getStatement(sql, params);
                                        ResultSet userSet = userStatement.executeQuery();
                                        userSet.next();
                                        message += get_medal(set.getInt("Place")) + " " + userSet.getString("Username");
                                        userStatement.close();
                                        if (set.next())
                                        {
                                            sql = "SELECT Username FROM DiscordDB.LeagueChampions WHERE Code = ?";
                                            params.remove(0);
                                            params.add(set.getString("Code"));
                                            userStatement = JDBCConnection.getStatement(sql, params);
                                            userSet = userStatement.executeQuery();
                                            userSet.next();
                                            message += "\n" + get_medal(set.getInt("Place")) + " " + userSet.getString("Username");
                                            userStatement.close();
                                            if (set.next())
                                            {
                                                sql = "SELECT Username FROM DiscordDB.LeagueChampions WHERE Code = ?";
                                                params.remove(0);
                                                params.add(set.getString("Code"));
                                                userStatement = JDBCConnection.getStatement(sql, params);
                                                userSet = userStatement.executeQuery();
                                                userSet.next();
                                                message += "\n" + get_medal(set.getInt("Place")) + " " + userSet.getString("Username");
                                                userStatement.close();
                                            }
                                        }
                                    }
                                    else
                                    {
                                        message += "`No current bets`";
                                    }
                                    statement.close();

                                    BotUtils.sendMessage(event.getChannel(), message);
                                } catch (SQLException e)
                                {
                                    System.out.println("Couldn't process result set");
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        })),

                        //List all bets for all users (moderation purposes)
                        new Command("all_bets", "List all bets for the Mod League Champion Tournament", "all_bets", AccessLevel.DEACTIVATED, ((event, args) ->
                        {
                            if (event.getGuild() == null && BotUtils.isPokemon(event))
                            {
                                try
                                {
                                    //Select all info and sort them
                                    String sql = "SELECT UserID, Place, Code FROM DiscordDB.LeagueBet ORDER BY UserID ASC, Place ASC";
                                    List<Object> params = new ArrayList<>();
                                    PreparedStatement statement = JDBCConnection.getStatement(sql, params);
                                    ResultSet set = statement.executeQuery();
                                    String message = "The bets for the Mod League Champions are:\n";

                                    if (set == null)
                                        return;
                                    else if (set.next())
                                    {
                                        set.previous();
                                        //List each user and their bets
                                        while (set.next())
                                        {
                                            //Select username for current code
                                            sql = "SELECT Username FROM DiscordDB.LeagueChampions WHERE Code = ?";
                                            params.add(set.getString("Code"));
                                            PreparedStatement userStatement = JDBCConnection.getStatement(sql, params);
                                            ResultSet userSet = userStatement.executeQuery();
                                            params.remove(0);
                                            userSet.next();
                                            message += "\n" + set.getLong("UserID") + ": " + set.getInt("Place") + " " + userSet.getString("Username");
                                            userStatement.close();
                                        }
                                    }
                                    else
                                    {
                                        message += "`No bets`";
                                    }
                                    statement.close();

                                    BotUtils.sendMessage(event.getChannel(), message);
                                } catch (SQLException e)
                                {
                                    System.out.println("Couldn't process result set");
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        }))
                },

                (event, args) ->
                {
                    if (BotUtils.isPokemon(event) && Command.hasChannelPerms(event, Command.EVENT, "league"))
                    {
                        BotUtils.help(map, event, args, "league");
                    }
                })
        );
    }

    //Gets emote name for place
    private String get_medal(int place)
    {
        if (place == 1)
        {
            return ":first_place:";
        }
        else if (place == 2)
        {
            return ":second_place:";
        }
        else if (place == 3)
        {
            return ":third_place:";
        }
        return "";
    }
}