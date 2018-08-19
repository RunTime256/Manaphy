import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class OwnerCommands
{
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String prefix;
    public OwnerCommands(@SuppressWarnings("unused") Map<String, Command> map, String p)
    {
        prefix = p;
        //No current commands
    }
}