package nu.rydin.kom.webui.beans;

public class ConferenceBean
{
    private long id;
    
    private String name;
    
    private int unread;

    public ConferenceBean(long id, String name, int unread)
    {
        this.id = id;
        this.name = name;
        this.unread = unread;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getUnread()
    {
        return unread;
    }

    public void setUnread(int unread)
    {
        this.unread = unread;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }    
}
