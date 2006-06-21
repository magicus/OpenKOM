/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

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
	
	public UserListItem(int session, NameAssociation user, short clientType, short action, NameAssociation conference, boolean inMailbox,
	        long loginTime, long lastHeartbeat)
	{
        m_session           = session;
		m_user 				= user;
        m_clientType        = clientType;
		m_action 			= action;
		m_conference 		= conference;
		m_inMailbox			= inMailbox;
		m_loginTime			= loginTime;
		m_lastHeartbeat		= lastHeartbeat;
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
}
