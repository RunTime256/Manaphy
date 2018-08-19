import java.io.Console;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class MainRunner
{
    public static void main(String[] args)
    {
        String url;
        String user;
        String pass;
        String passToHash;
        Console console = System.console();
        //If console is invalid, end execution
        if (console == null)
        {
            System.out.println("Couldn't get Console instance");
            return;
        }
        console.printf("Booting up Manaphy...%n");

        //Set variables for login
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
            //Select bot token to log in
            String sql = "SELECT Entry FROM DiscordDB.Utils WHERE EntryDesc = 'Manaphy Token'";
            List<Object> params = new ArrayList<>();
            ResultSet set = JDBCConnection.getStatement(sql, params).executeQuery();
            if (set.next())
            {
                token = set.getString("Entry");
            }
            else
            {
                System.out.println("No valid token entry");
                return;
            }
            JDBCConnection.disconnect();

            //Sets password and creates client. Runs until shutdown
            BotUtils.setPassHash(passToHash);
            DiscordUtils.start(token);
        }
        catch (SQLException e)
        {
            System.out.println("Couldn't process result set");
            e.printStackTrace();
        }
    }
}