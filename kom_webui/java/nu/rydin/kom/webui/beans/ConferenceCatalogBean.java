package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.ConferenceListItem;

public class ConferenceCatalogBean extends ConferenceListBean
{
    public ConferenceCatalogBean()
    throws UnexpectedException
    {
        super();
    }
    
    protected void initialize() throws UnexpectedException
    {
        ServerSession ss = KOMContext.getSession();
        try
        {            
            ConferenceListItem[] confs = ss.listConferencesByName();
            news = new ConferenceBean[confs.length];
            for (int idx = 0; idx < confs.length; idx++)
            {
                ConferenceListItem item = confs[idx];
                news[idx] = new ConferenceBean(item.getId(), item.getName().getName(), 
                        item.isMember() ? ss.countUnread(item.getId()) : 0, item.getOrder());
            }
        }
        catch(ObjectNotFoundException e)
        {
            throw new UnexpectedException(ss.getLoggedInUserId(), e);
        }
    }
}
