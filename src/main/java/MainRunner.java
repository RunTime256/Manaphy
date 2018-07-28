import sx.blah.discord.api.IDiscordClient;

import java.io.Console;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainRunner
{
    public static void main(String[] args)
    {
        IDiscordClient cli;
        String url;
        String user;
        String pass;
        String passToHash;
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        console.printf("Booting up Manaphy...%n");
        url = "jdbc:mysql://" + console.readLine("Enter your DB url: ") + ":3306/?useSSL=false";
        user = console.readLine("Enter your DB username: ");
        char[] passwordArray = console.readPassword("Enter your DB password: ");
        pass = new String(passwordArray);
        passwordArray = console.readPassword("Enter your Hash password: ");
        passToHash = new String(passwordArray);
        JDBCConnection.initialize(url, user, pass);

        String token;
        try
        {
            JDBCConnection.connect();
            String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Manaphy Token'";
            List<Object> params = new ArrayList<>();
            PreparedStatement statement = JDBCConnection.getStatement(sql, params);
            ResultSet set = statement.executeQuery();
            if (set == null)
                return;
            else if (set.next())
            {
                token = set.getString("Entry");
            }
            else
            {
                System.out.println("No valid token entry");
                statement.close();
                return;
            }
            statement.close();
            JDBCConnection.disconnect();

            BotUtils.setPassHash(passToHash);
            cli = BotUtils.getBuiltDiscordClient(token);

            // Register a listener via the EventSubscriber annotation which allows for organisation and delegation of events
            cli.getDispatcher().registerListener(new CommandHandler());

            // Only login after all events are registered otherwise some may be missed.
            cli.login();
        }
        catch (SQLException e)
        {
            System.out.println("Couldn't process result set");
            e.printStackTrace();
            return;
        }
    }
}