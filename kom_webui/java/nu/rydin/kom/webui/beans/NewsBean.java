package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.MembershipListItem;

public class NewsBean extends ConferenceListBean
{
    public NewsBean()
    throws UnexpectedException
    { 
        super();
    }
    
    protected void initialize()
    throws UnexpectedException
    {
        ServerSession session = KOMContext.getSession();
        MembershipListItem[] items = session.listNews();
        news = new ConferenceBean[items.length]; 
        for (int idx = 0; idx < items.length; idx++)
            news[idx] = new ConferenceBean(items[idx].getConference().getId(), 
                    items[idx].getConference().getName().getName(), items[idx].getUnread(), idx + 1);
    }
}
