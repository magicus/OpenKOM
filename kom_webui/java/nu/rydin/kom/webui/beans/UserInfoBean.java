package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.UserInfo;

public class UserInfoBean
{
    private String login;
    
    private long id;
    
    private String name;
    
    private String locale;
    
    private String timeZone;
    
    private long flags1;
    
    private long flags2;
    
    private long flags3;
    
    private long flags4;
    
    private long rights;

    public UserInfoBean()
    {
        ServerSession session = KOMContext.getSession();
        UserInfo ui = session.getLoggedInUser();
        login = ui.getUserid();
        id = ui.getId();
        name = ui.getName().getName();
        locale = ui.getLocale();
        timeZone = ui.getTimeZone().getID();
        flags1 = ui.getFlags1();
        flags2 = ui.getFlags2();
        flags3 = ui.getFlags3();
        flags4 = ui.getFlags4();
        rights = ui.getRights();
    }

    public long getFlags1()
    {
        return flags1;
    }

    public void setFlags1(long flags1)
    {
        this.flags1 = flags1;
    }

    public long getFlags2()
    {
        return flags2;
    }

    public void setFlags2(long flags2)
    {
        this.flags2 = flags2;
    }

    public long getFlags3()
    {
        return flags3;
    }

    public void setFlags3(long flags3)
    {
        this.flags3 = flags3;
    }

    public long getFlags4()
    {
        return flags4;
    }

    public void setFlags4(long flags4)
    {
        this.flags4 = flags4;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getRights()
    {
        return rights;
    }

    public void setRights(long rights)
    {
        this.rights = rights;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }
}
