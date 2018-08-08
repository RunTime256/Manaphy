public class AccessLevel
{
    //Command priority
    public static final AccessLevel DEACTIVATED = new AccessLevel(-1, "Deactivated");
    public static final AccessLevel MANAGER = new AccessLevel(0, "Manager");
    public static final AccessLevel TESTER = new AccessLevel(1, "Tester");
    public static final AccessLevel OWNER = new AccessLevel(2, "Owner");
    public static final AccessLevel ADMIN = new AccessLevel(3, "Admin");
    public static final AccessLevel MOD = new AccessLevel(4, "Moderator");
    public static final AccessLevel EVERYONE = new AccessLevel(8, "Everyone");

    private int level;
    private String name;

    private AccessLevel(int l, String n)
    {
        level = l;
        name = n;
    }

    public int getLevel()
    {
        return level;
    }

    public String getName()
    {
        return name;
    }

    //Checks if the requester level is less than the command level
    //(i.e. Admin is 3, which is less than Moderator (4),
    // so Admins can run Moderator commands)
    public boolean isAccessible(int request)
    {
        return request <= level;
    }
}