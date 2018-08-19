import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ModeratorCommands
{
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String prefix;

    public ModeratorCommands(@SuppressWarnings("unused") Map<String, Command> map, String p)
    {
        prefix = p;
        //No current commands
    }
}