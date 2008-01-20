/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import nu.rydin.kom.constants.Activities;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserListItem implements Serializable
{
    private final int m_session;
    
    public final NameAssociation m_user;
	
	public final short m_action;
	
	public final NameAssociation m_conference;
	
	public final boolean m_inMailbox;
	
	public final long m_loginTime;
	
	public final long m_lastHeartbeat;
    
    public final short m_clientType;
    
    public final String m_freeActivityText;
    
    public final long m_lastObject;
    
    public final short m_activity;
	
	public UserListItem(int session, NameAssociation user, short clientType, short action, NameAssociation conference, boolean inMailbox,
	        long loginTime, long lastHeartbeat, short activity, String freeActivityText, long lastObject)
	{
        m_session           = session;
		m_user 				= user;
        m_clientType        = clientType;
		m_action 			= action;
		m_conference 		= conference;
		m_inMailbox			= inMailbox;
		m_loginTime			= loginTime;
		m_lastHeartbeat		= lastHeartbeat;
        m_activity          = activity;
        m_freeActivityText  = freeActivityText;
        m_lastObject        = lastObject;
	}
	
    public int getSessionId()
    {
        return m_session;
    }
    
	public short getAction()
	{
		return m_action;
	}

	public NameAssociation getConference()
	{
		return m_conference;
	}

	public NameAssociation getUser()
	{
		return m_user;
	}
	
	public boolean isInMailbox()
	{
		return m_inMailbox;
	}
	
	public long getLoginTime()
	{
	    return m_loginTime;
	}
	
	public long getLastHeartbeat()
	{
	    return m_lastHeartbeat;
	}
    
    public short getClientType()
    {
        return m_clientType;
    }
    
    public short getActivity()
    {
        return m_activity;
    }
    
    public String getActivityText()
    {
        return m_freeActivityText;
    }
    
    public long getLastObject()
    {
        return m_lastObject;
    }
}
