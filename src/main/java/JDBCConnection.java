import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Driver;

import java.time.ZonedDateTime;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class JDBCConnection
{
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static Connection conn;
    private static String url;
    private static String username;
    private static String password;

    public static void initialize(String u, String user, String pass)
    {
        url = u;
        username = user;
        password = pass;
    }

    //Create connection to DB
    public static void connect()
    {
        try
        {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(url, username, password);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //Create a prepared statement with given sql and parameters
    public static PreparedStatement getStatement(String sql, List<Object> params)
    {
        try
        {
            //Uses PreparedStatement to help prevent SQL Injection
            PreparedStatement statement = conn.prepareStatement(sql);
            setParameters(statement, params);
            return statement;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //Disconnect from DB
    public static void disconnect()
    {
        try
        {
            if (conn != null)
                conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Set parameters for statement based on class type
    private static void setParameters(PreparedStatement statement, List<Object> params) throws SQLException
    {
        //Inserts all parameters
        for (int i = 0; i < params.size(); i++)
        {
            if (params.get(i).getClass() == Long.class)
                statement.setLong(i + 1, (Long)params.get(i));
            else if (params.get(i).getClass() == String.class)
                statement.setString(i + 1, (String)params.get(i));
            else if (params.get(i).getClass() == Integer.class)
                statement.setInt(i + 1, (Integer)params.get(i));
            else if (params.get(i).getClass() == Double.class)
                statement.setDouble(i + 1, (Double)params.get(i));
            else if (params.get(i).getClass() == Boolean.class)
                statement.setBoolean(i + 1, (Boolean)params.get(i));
            else if (params.get(i).getClass() == ZonedDateTime.class)
                statement.setTimestamp(i + 1, new Timestamp(((ZonedDateTime)params.get(i)).toInstant().toEpochMilli()));
        }
    }
}