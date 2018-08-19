@SuppressWarnings("WeakerAccess")
public class AccessLevel
{
    //Command priority
    public static final AccessLevel DEACTIVATED = new AccessLevel(-1);
    public static final AccessLevel MANAGER = new AccessLevel(0);
    public static final AccessLevel TESTER = new AccessLevel(1);
    @SuppressWarnings("unused")
    public static final AccessLevel OWNER = new AccessLevel(2);
    @SuppressWarnings("unused")
    public static final AccessLevel ADMINISTRATOR = new AccessLevel(3);
    public static final AccessLevel MODERATOR = new AccessLevel(4);
    public static final AccessLevel BOT = new AccessLevel(5);
    public static final AccessLevel EVERYONE = new AccessLevel(99);

    private int level;

    private AccessLevel(int l)
    {
        level = l;
    }

    public int getLevel()
    {
        return level;
    }

    //Checks if the requester level is less than the command level
    //(i.e. Admin is 3, which is less than Moderator (4),
    // so Admins can run Moderator commands)
    public boolean isAccessible(int request)
    {
        return request <= level;
    }
}