/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageRange;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferenceManager // extends NameManager
{
	public static final short CONFERENCE_KIND = 1;
	
	private final NameManager m_nameManager;
	
	private final PreparedStatement m_addConfStmt;
	private final PreparedStatement m_loadConfStmt;
	private final PreparedStatement m_loadRangeStmt;
	
	public ConferenceManager(Connection conn, NameManager nameManager)
	throws SQLException
	{
		m_nameManager = nameManager;
		m_addConfStmt = conn.prepareStatement(
			"INSERT INTO conferences(id, administrator, permissions, replyConf) VALUES(?, ?, ?, ?)");
		m_loadConfStmt = conn.prepareStatement(
			"SELECT n.fullname, c.administrator, c.permissions, c.replyConf " +
			"FROM names n, conferences c " +
			"WHERE c.id = ? AND n.id = c.id");
		m_loadRangeStmt = conn.prepareStatement(	
			"SELECT MIN(localnum), MAX(localnum) FROM messageoccurrences WHERE conference = ?");
	}
	
	public void close()
	{
		try
		{
			if(m_addConfStmt != null)
				m_addConfStmt.close();
			if(m_loadConfStmt != null)
				m_loadConfStmt.close();
			if(m_loadRangeStmt != null)
				m_loadRangeStmt.close();
		}
		catch(SQLException e)
		{
			// Not much we can do here...
			//
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new conference
	 * 
	 * @throws DuplicateNameException
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 */
	public long addConference(String fullname, long administrator, int permissions, short visibility, long replyConf)
		throws DuplicateNameException, SQLException, AmbiguousNameException
	{
		if(m_nameManager.nameExists(fullname))
			throw new DuplicateNameException(fullname);
			
		// First, add the name
		//
		long nameId = m_nameManager.addName(fullname, CONFERENCE_KIND, visibility);
		
		// Now, add the user
		//
		m_addConfStmt.clearParameters();
		m_addConfStmt.setLong(1, nameId);
		m_addConfStmt.setLong(2, administrator);
		m_addConfStmt.setInt(3, permissions);
		if(replyConf == -1)
			m_addConfStmt.setNull(4, Types.BIGINT);
		else
			m_addConfStmt.setLong(4, replyConf);			
		m_addConfStmt.executeUpdate();
		return nameId;
	}
	
	/**
	 * Adds a personal mailbox
	 * 
	 * @throws DuplicateNameException
	 * @throws SQLException
	 */
	public void addMailbox(long user, String userName, int permissions)
		throws DuplicateNameException, SQLException, AmbiguousNameException
	{		
		// Mailboxes are conferences with the same id as the user
		//
		m_addConfStmt.clearParameters();
		m_addConfStmt.setLong(1, user);
		m_addConfStmt.setLong(2, user);
		m_addConfStmt.setInt(3, permissions);
		m_addConfStmt.setNull(4, Types.BIGINT);					
		m_addConfStmt.executeUpdate();
	}
	
		
	public ConferenceInfo loadConference(long id)
	throws ObjectNotFoundException, SQLException
	{
		m_loadConfStmt.clearParameters();
		m_loadConfStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_loadConfStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Conference id=" + id);
			
			// Load the message range
			//
			MessageRange r = this.getMessageRange(id);
			return new ConferenceInfo(
				id,							// Id
				rs.getString(1),			// Name
				rs.getLong(2),				// Admin
				rs.getInt(3),				// Permissions
				rs.getObject(4) != null ? rs.getLong(4) : -1, // Reply conference
				r.getMin(),					// First text
				r.getMax()					// Last text
				);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	public MessageRange getMessageRange(long id)
	throws ObjectNotFoundException, SQLException
	{
		m_loadRangeStmt.clearParameters();
		m_loadRangeStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_loadRangeStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Conference id=" + id);
			return new MessageRange(rs.getInt(1), rs.getInt(2));
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	

	/**
	 * Returns a list of user names based on a search pattern
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public String[] getConferenceNamesByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getNamesByPatternAndKind(pattern, CONFERENCE_KIND);
	}
	
	/**
	 * Returns a list of user ids based on a search pattern
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long[] getConferenceIdsByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getIdsByPatternAndKind(pattern, CONFERENCE_KIND);
	}
}
