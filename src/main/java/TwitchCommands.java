import me.philippheuer.twitch4j.exceptions.ChannelDoesNotExistException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TwitchCommands
{
    public TwitchCommands(Map<String, Command> map)
    {
        //Commands for league betting in Pokemon discord
        map.put("twitch", new Command("twitch", "Twitch integration", BotUtils.BOT_PREFIX + "twitch", AccessLevel.MOD, false, new Command[]
                {
                        new Command("add", "Add a streamer to notify users when they are live. Stream link will be added automatically.", "add <twitch username> <channel> [message]", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (args.size() >= 3)
                            {
                                long c = BotUtils.getID(args.get(2));
                                //Checks if the channel is a valid channel in the current guild
                                if (c >= 0 && event.getGuild().getChannelByID(c) != null)
                                {
                                    String sql = "SELECT * FROM DiscordDB.TwitchLive WHERE GuildID = ? AND TwitchID = ?";
                                    List<Object> params = new ArrayList<>();
                                    params.add(event.getGuild().getLongID());
                                    params.add(TwitchUtils.getTwitchClient().getChannelEndpoint().getChannel(args.get(1)).getId());
                                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                    if (set == null)
                                        return;
                                    else if (set.next())
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "This twitch user already has a notification set! Please use the command `" + BotUtils.BOT_PREFIX + "twitch update` to update their notification.");
                                        return;
                                    }
                                    else
                                    {
                                        sql = "INSERT INTO DiscordDB.TwitchLive (GuildID, TwitchID, ChannelID, Message) VALUES (?, ?, ?, ?)";
                                        params.add(c);
                                    }

                                    String message;
                                    if (args.size() > 3)
                                    {
                                        message = BotUtils.combineArgs(args, 3);
                                    }
                                    else
                                    {
                                        String defaultSQL = "SELECT Entry FROM DiscordDB.Utils WHERE EntryID = ?";
                                        List<Object> defaultParams = new ArrayList<>();
                                        defaultParams.add(6);
                                        ResultSet defaultSet = JDBCConnection.getStatement(defaultSQL, defaultParams).executeQuery();
                                        if (defaultSet == null)
                                            return;
                                        else if (defaultSet.next())
                                        {
                                            message = defaultSet.getString("Entry");
                                        }
                                        else
                                        {
                                            return;
                                        }
                                    }
                                    params.add(message);

                                    JDBCConnection.getStatement(sql, params).executeUpdate();

                                    BotUtils.sendMessage(event.getChannel(), "Stream notification for " + args.get(1) + " added!");
                                }
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "twitch");
                            }
                        })),

                        new Command("update_channel", "Update a live notification channel", "update <twitch username> <channel>", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (args.size() == 3)
                            {
                                long c = BotUtils.getID(args.get(2));
                                //Checks if the channel is a valid channel in the current guild
                                if (c >= 0 && event.getGuild().getChannelByID(c) != null)
                                {
                                    try
                                    {
                                        String sql = "SELECT * FROM DiscordDB.TwitchLive WHERE GuildID = ? AND TwitchID = ?";
                                        List<Object> params = new ArrayList<>();
                                        params.add(event.getGuild().getLongID());
                                        params.add(TwitchUtils.getTwitchClient().getChannelEndpoint().getChannel(args.get(1)).getId());
                                        ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                        if (set == null)
                                            return;
                                        else if (set.next())
                                        {
                                            sql = "UPDATE DiscordDB.TwitchLive SET ChannelID = ? WHERE GuildID = ? AND TwitchID = ?";
                                            params.add(0, c);
                                            JDBCConnection.getStatement(sql, params).executeUpdate();
                                        }
                                        else
                                        {
                                            BotUtils.sendMessage(event.getChannel(), "This twitch user does not have a notification yet! Add one with the command `" + BotUtils.BOT_PREFIX + "twitch add`");
                                            return;
                                        }
                                    }
                                    catch (ChannelDoesNotExistException e)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "Please provide a valid channel name");
                                        return;
                                    }
                                }
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "twitch");
                            }
                        })),

                        new Command("update_message", "Update a live notification message", "update <twitch username> <message>", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (args.size() >= 3)
                            {
                                try
                                {
                                    String sql = "SELECT * FROM DiscordDB.TwitchLive WHERE GuildID = ? AND TwitchID = ?";
                                    List<Object> params = new ArrayList<>();
                                    params.add(event.getGuild().getLongID());
                                    params.add(TwitchUtils.getTwitchClient().getChannelEndpoint().getChannel(args.get(1)).getId());
                                    ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();

                                    if (set == null)
                                        return;
                                    else if (set.next())
                                    {
                                        String message = BotUtils.combineArgs(args, 2);
                                        sql = "UPDATE DiscordDB.TwitchLive SET Message = ? WHERE GuildID = ? AND TwitchID = ?";
                                        params.add(0, message);
                                        JDBCConnection.getStatement(sql, params).executeUpdate();
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "This twitch user does not have a notification yet! Add one with the command `" + BotUtils.BOT_PREFIX + "twitch add`");
                                        return;
                                    }
                                }
                                catch (ChannelDoesNotExistException e)
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please provide a valid channel name");
                                    return;
                                }
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "twitch");
                            }
                        })),

                        new Command("remove", "Remove a live notification", "remove <twitch username>", AccessLevel.MOD, false, ((event, args) ->
                        {
                            if (args.size() == 2)
                            {
                                try
                                {
                                    String sql = "DELETE FROM DiscordDB.TwitchLive WHERE GuildID = ? AND TwitchID = ?";
                                    List<Object> params = new ArrayList<>();
                                    params.add(event.getGuild().getLongID());
                                    params.add(TwitchUtils.getTwitchClient().getChannelEndpoint().getChannel(args.get(1)).getId());
                                    int num = JDBCConnection.getStatement(sql, params).executeUpdate();
                                    if (num >= 1)
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "Notifications for " + args.get(1) + " removed.");
                                    }
                                    else
                                    {
                                        BotUtils.sendMessage(event.getChannel(), "This twitch user does not have any notifications to remove");
                                    }
                                }
                                catch (ChannelDoesNotExistException e)
                                {
                                    BotUtils.sendMessage(event.getChannel(), "Please provide a valid channel name");
                                    return;
                                }
                            }
                            else
                            {
                                BotUtils.help(map, event, args, "twitch");
                            }
                        }))
                },

                (event, args) ->
                {

                    BotUtils.help(map, event, args, "twitch");
                })
        );
    }
}