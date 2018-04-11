public class AccessLevel
{
    private int level;
    private String name;
    public static final AccessLevel OWNER = new AccessLevel(0, "Owner");
    public static final AccessLevel ADMIN = new AccessLevel(1, "Admin");
    public static final AccessLevel MOD = new AccessLevel(2, "Moderator");
    public static final AccessLevel EVERYONE = new AccessLevel(8, "Everyone");

    public AccessLevel(int l, String n)
    {
        level = l;
        name = n;
    }

    public AccessLevel(int l)
    {
        level = l;
        name = "temp";
    }

    public int getLevel()
    {
        return level;
    }

    public String getName()
    {
        return name;
    }

    //Ensures the requesting user has access to use the command
    public boolean hasAccess(AccessLevel request)
    {
        return level >= request.getLevel();
    }

    //Returns true if the requester has a higher level than the base
    public boolean compareAccess(AccessLevel base, AccessLevel request)
    {
        return base.getLevel() >= request.getLevel();
    }
}
