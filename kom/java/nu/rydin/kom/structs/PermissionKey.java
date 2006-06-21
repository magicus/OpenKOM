package nu.rydin.kom.structs;

public class PermissionKey
{
    private final long conference;
    private final long user;
    
    public PermissionKey(long conference, long user)
    {
        this.conference = conference;
        this.user = user;
    }
    
    public long getConference()
    {
        return conference;
    }
    
    public long getUser()
    {
        return user;
    }
    
    public int hashCode()
    {
        return (int) ((conference ^ user) & 0xffffffff);
    }
    
    public boolean equals(Object o)
    {
        if(o == this)
            return true;
        if(o == null)
            return false;
        if(!(o instanceof PermissionKey))
            return false;
        PermissionKey p = (PermissionKey) o;
        return p.conference == conference && p.user == user;
    }
}
