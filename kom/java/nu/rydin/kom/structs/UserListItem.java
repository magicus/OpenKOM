/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserListItem implements Serializable
{
	public long m_userId;
	
	public String m_userName;
	
	public short m_action;
	
	public String m_conferenceName;
	
	public boolean m_inMailbox;
	
	public UserListItem(long userId, String userName, short action, String conferenceName, boolean inMailbox)
	{
		m_userId 			= userId;
		m_userName 			= userName;
		m_action 			= action;
		m_conferenceName 	= conferenceName;
		m_inMailbox			= inMailbox;
	}
	
	public short getAction()
	{
		return m_action;
	}

	public String getConferenceName()
	{
		return m_conferenceName;
	}

	public long getUserId()
	{
		return m_userId;
	}

	public String getUserName()
	{
		return m_userName;
	}
	
	public boolean isInMailbox()
	{
		return m_inMailbox;
	}
}
