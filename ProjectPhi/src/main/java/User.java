public class User
{
    private long id;
    private AccessLevel access;

    public User(long i, AccessLevel level)
    {
        id = i;
        access = level;
    }

    public long getId()
    {
        return id;
    }

    public AccessLevel getAccess()
    {
        return access;
    }
}
