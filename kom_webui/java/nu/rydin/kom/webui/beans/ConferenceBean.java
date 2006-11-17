package nu.rydin.kom.webui.beans;

public class ConferenceBean
{
    private long id;
    
    private String name;
    
    private int unread;
    
    private int order;

    public ConferenceBean(long id, String name, int unread, int order)
    {
        this.id = id;
        this.name = name;
        this.unread = unread;
        this.order = order;
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

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }    
}
