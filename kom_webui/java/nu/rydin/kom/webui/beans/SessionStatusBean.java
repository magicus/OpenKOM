package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.MembershipListItem;
import nu.rydin.kom.structs.UserInfo;

public class SessionStatusBean
{
    private UserInfoBean user;
    
    private int totalUnread;
    
    private int loggedInUsers;
    
    public SessionStatusBean()
    throws UnexpectedException
    {
        ServerSession session = KOMContext.getSession();
        user = new UserInfoBean();
        MembershipListItem[] news = session.listNews();
        int total = 0;
        for (int idx = 0; idx < news.length; idx++)
            total += news[idx].getUnread();
        totalUnread = total;
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

    public UserInfoBean getUser()
    {
        return user;
    }

    public void setUser(UserInfoBean user)
    {
        this.user = user;
    }    
}


