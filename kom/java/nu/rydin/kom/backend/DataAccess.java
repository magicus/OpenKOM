/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.Connection;
import java.sql.SQLException;

import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.MembershipManager;
import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.data.UserManager;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class DataAccess 
{
	/**
	 * JDBC connection to use for all database operations
	 */
	private final Connection m_conn;
	
	private final NameManager m_nameManager;
	
	/**
	 * Toolkit object dealing with users
	 */
	private final UserManager m_userManager;
	
	/**
	 * Toolkit object dealing with conferences
	 */
	private final ConferenceManager m_conferenceManager;
		
	/**
	 * Toolkit object dealing with users
	 */	
	private final MembershipManager m_membershipManager;
	
	/**
	 * Toolkit object dealing with users
	 */	
	private final MessageManager m_messageManager;
	
	public DataAccess(Connection conn)
	throws UnexpectedException
	{
		m_conn				= conn;
		try
		{
			m_nameManager		= new NameManager(conn);
			m_userManager	 	= new UserManager(conn, CacheManager.instance(), m_nameManager);
			m_conferenceManager = new ConferenceManager(conn, m_nameManager);
			m_membershipManager = new MembershipManager(conn);
			m_messageManager 	= new MessageManager(conn);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, "Error while creating DataAccess", e);
		}
	}
	
	public NameManager getNameManager()
	{
		return m_nameManager;
	}
	
	public UserManager getUserManager()
	{
		return m_userManager;
	}
	
	public ConferenceManager getConferenceManager()
	{
		return m_conferenceManager;
	}
	
	public MembershipManager getMembershipManager()
	{
		return m_membershipManager;
	}
	
	public MessageManager getMessageManager()
	{
		return m_messageManager;
	}
	
	public void commit()
	throws UnexpectedException
	{
		try
		{
			m_conn.commit();
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, "Exception while committing transaction", e);
		}
	}
	
	public void rollback()
	throws UnexpectedException
	{
		try
		{
			m_conn.rollback();
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, "Exception while rolling back transaction", e);
		}
	}	
}
