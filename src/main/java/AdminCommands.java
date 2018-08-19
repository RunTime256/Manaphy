import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class AdminCommands
{
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String prefix;

    public AdminCommands(@SuppressWarnings("unused") Map<String, Command> map, String p)
    {
        prefix = p;
        //No current commands
    }
}