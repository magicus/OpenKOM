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
	public final long m_userId;
	
	public final Name m_userName;
	
	public final short m_action;
	
	public final Name m_conferenceName;
	
	public final boolean m_inMailbox;
	
	public final long m_loginTime;
	
	public final long m_lastHeartbeat;
	
	public UserListItem(long userId, Name userName, short action, Name conferenceName, boolean inMailbox,
	        long loginTime, long lastHeartbeat)
	{
		m_userId 			= userId;
		m_userName 			= userName;
		m_action 			= action;
		m_conferenceName 	= conferenceName;
		m_inMailbox			= inMailbox;
		m_loginTime			= loginTime;
		m_lastHeartbeat		= lastHeartbeat;
	}
	
	public short getAction()
	{
		return m_action;
	}

	public Name getConferenceName()
	{
		return m_conferenceName;
	}

	public long getUserId()
	{
		return m_userId;
	}

	public Name getUserName()
	{
		return m_userName;
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
}
