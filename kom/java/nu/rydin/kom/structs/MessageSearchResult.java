/*
 * Created on Jul 15, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author Henrik Schröder
 */
public class MessageSearchResult implements Serializable 
{
	private final long m_id;
	private final int m_localnum;
	private final long m_user;
	private final String m_username;
	private final String m_subject;
	 
	
	public MessageSearchResult(long id, int localnum, long user, String username, String subject)
	{
		m_id = id;
		m_localnum = localnum;
		m_user = user;
		m_username = username;
		m_subject = subject;
	}
	
	public long getId() 
	{
		return m_id;
	}
	
	public int getLocalnum() 
	{
		return m_localnum;
	}

	public long getUser() 
	{
		return m_user;
	}
	
	public String getUsername() 
	{
		return m_username;
	}
	
	public String getSubject() 
	{
		return m_subject;
	}
}
