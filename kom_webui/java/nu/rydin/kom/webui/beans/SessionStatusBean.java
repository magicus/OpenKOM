package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.UserInfo;

public class SessionStatusBean
{
    private String username;
    
    private long userId;
    
    private int totalUnread;
    
    private int loggedInUsers;
    
    public SessionStatusBean()
    throws UnexpectedException
    {
        ServerSession session = KOMContext.getSession();
        UserInfo ui = session.getLoggedInUser();
        username = ui.getName().getName();
        userId = ui.getId();
        totalUnread = session.listNews().length;
        loggedInUsers = session.listLoggedInUsers().length;
    }

    public int getLoggedInUsers()
    {
        return loggedInUsers;
    }

    public void setLoggedInUsers(int loggedInUsers)
    {
        this.loggedInUsers = loggedInUsers;
    }

    public int getTotalUnread()
    {
        return totalUnread;
    }

    public void setTotalUnread(int totalUnread)
    {
        this.totalUnread = totalUnread;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
    
    
}


