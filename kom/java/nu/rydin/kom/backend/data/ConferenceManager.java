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
import java.sql.Timestamp;
import java.sql.Types;

import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageRange;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:guru@slideware.com>Ulf Hedlund</a>
 */
public class ConferenceManager // extends NameManager
{
	public static final short CONFERENCE_KIND = 1;
	
	public static final short MAGIC_USERPRESENTATIONS = 0;
	public static final short MAGIC_CONFPRESENTATIONS = 1;
	public static final short MAGIC_NOTE = 2;
	
	private final NameManager m_nameManager;
	
	private final PreparedStatement m_addConfStmt;
	private final PreparedStatement m_loadConfStmt;
	private final PreparedStatement m_loadRangeStmt;
	private final PreparedStatement m_getMagicConfStmt;
	private final PreparedStatement m_setMagicConfStmt;
	private final PreparedStatement m_isMagicConfStmt;
	
	public ConferenceManager(Connection conn, NameManager nameManager)
	throws SQLException
	{
		m_nameManager = nameManager;
		m_addConfStmt = conn.prepareStatement(
			"INSERT INTO conferences(id, administrator, permissions, replyConf, created) VALUES(?, ?, ?, ?, ?)");
		m_loadConfStmt = conn.prepareStatement(
			"SELECT n.fullname, c.administrator, c.permissions, c.replyConf, c.created, c.lasttext " +
			"FROM names n, conferences c " +
			"WHERE c.id = ? AND n.id = c.id");
		m_loadRangeStmt = conn.prepareStatement(	
			"SELECT MIN(localnum), MAX(localnum) FROM messageoccurrences WHERE conference = ?");
		m_getMagicConfStmt = conn.prepareStatement(
			 "select conference from magicconferences where kind = ?");
		m_setMagicConfStmt = conn.prepareStatement(
			 "replace into magicconferences(conference, kind) values(?, ?)");
		m_isMagicConfStmt = conn.prepareStatement(
			 "select count(*) from magicconferences where conference = ?");
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
			if(m_getMagicConfStmt != null)
				m_getMagicConfStmt.close();
			if(m_setMagicConfStmt != null)
				m_setMagicConfStmt.close();
			if(m_isMagicConfStmt != null)
				m_isMagicConfStmt.close();
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
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		// Now, add the conference
		//
		m_addConfStmt.clearParameters();
		m_addConfStmt.setLong(1, nameId);
		m_addConfStmt.setLong(2, administrator);
		m_addConfStmt.setInt(3, permissions);
		if(replyConf == -1)
			m_addConfStmt.setNull(4, Types.BIGINT);
		else
			m_addConfStmt.setLong(4, replyConf);
		m_addConfStmt.setTimestamp(5, now);
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
		Timestamp now = new Timestamp(System.currentTimeMillis());

		m_addConfStmt.clearParameters();
		m_addConfStmt.setLong(1, user);
		m_addConfStmt.setLong(2, user);
		m_addConfStmt.setInt(3, permissions);
		m_addConfStmt.setNull(4, Types.BIGINT);					
		m_addConfStmt.setTimestamp(5, now);
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
				rs.getTimestamp(5),
				rs.getTimestamp(6),
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
	
	/**
	 * Returns the conference ID for a magic conference.
	 * 
	 * @param kind ConferenceManager.MAGIC_XXXXXXXX
	 * @return Conference ID.
	 */
	public long getMagicConference(short kind)
	throws SQLException
	{
		ResultSet rs = null;
		try
		{
			m_getMagicConfStmt.clearParameters();
			m_getMagicConfStmt.setShort(1, kind);
			rs = m_getMagicConfStmt.executeQuery();
			rs.first();
			return rs.getLong(1);
		}
		finally
		{
			if (null != rs)
			{
				rs.close();
			}
		}
	}
	
	public void setMagicConference(long conference, short kind)
	throws SQLException
	{
		m_setMagicConfStmt.clearParameters();
		m_setMagicConfStmt.setLong(1, conference);
		m_setMagicConfStmt.setShort(2, kind);
		m_setMagicConfStmt.execute();
	}
	
	public boolean isMagic(long conference)
	throws SQLException
	{
		ResultSet rs = null;
		try
		{
			m_isMagicConfStmt.clearParameters();
			m_isMagicConfStmt.setLong(1, conference);
			rs = m_isMagicConfStmt.executeQuery();
			rs.first();
			return (0 != rs.getInt(1)) ? true : false;
		}
		finally
		{
			if (null != rs)
			{
				rs.close();
			}
		}
	}
}
