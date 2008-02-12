/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import nu.rydin.kom.backend.CacheManager;
import nu.rydin.kom.backend.KOMCache;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferenceListItem;
import nu.rydin.kom.structs.MessageRange;
import nu.rydin.kom.structs.Name;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:guru@slideware.com>Ulf Hedlund</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ConferenceManager 
{	
	private final NameManager m_nameManager;
	
	private final PreparedStatement m_addConfStmt;
	private final PreparedStatement m_changeReplyToConfStmt;
	private final PreparedStatement m_loadConfStmt;
	private final PreparedStatement m_loadRangeStmt;
	private final PreparedStatement m_isMailboxStmt;
	private final PreparedStatement m_listByDateStmt;
	private final PreparedStatement m_listByNameStmt;
	private final PreparedStatement m_countStmt;
	private final PreparedStatement m_changePermissionsStmt;
    private final PreparedStatement m_getParentCountForConfStmt;
    private final PreparedStatement m_getMaxParentCountStmt;
	
	public ConferenceManager(Connection conn, NameManager nameManager)
	throws SQLException
	{
		m_nameManager = nameManager;
		m_addConfStmt = conn.prepareStatement(
			"INSERT INTO conferences(id, administrator, permissions, nonmember_permissions, replyConf, created) VALUES(?, ?, ?, ?, ?, ?)");
		m_changeReplyToConfStmt = conn.prepareStatement(
		    "UPDATE conferences SET replyConf = ? WHERE id = ?");
		m_loadConfStmt = conn.prepareStatement(
			"SELECT n.fullname, n.keywords, c.administrator, c.permissions, c.nonmember_permissions, n.visibility, c.replyConf, c.created, c.lasttext, n.emailalias " +
			"FROM names n, conferences c " +
			"WHERE c.id = ? AND n.id = c.id");
		m_loadRangeStmt = conn.prepareStatement(	
			"SELECT MIN(localnum), MAX(localnum) FROM messageoccurrences WHERE conference = ? AND kind != 3");
		m_isMailboxStmt = conn.prepareStatement(
		     "SELECT COUNT(*) FROM users WHERE id = ?");
		m_listByDateStmt = conn.prepareStatement(
		     "SELECT c.id, n.fullname, n.visibility, c.created, c.lasttext, m.permissions,  c.administrator, m.priority, m.active " +
		     "FROM conferences c " +
		     "LEFT OUTER JOIN memberships m ON c.id = m.conference AND m.user = ? " +
		     "JOIN names n ON n.id = c.id " +
		     "WHERE n.kind = " + NameManager.CONFERENCE_KIND + ' '+
		     "ORDER BY c.lasttext DESC");
		m_listByNameStmt = conn.prepareStatement(
		     "SELECT c.id, n.fullname, n.visibility, c.created, c.lasttext, m.permissions, c.administrator, m.priority, m.active " +
		     "FROM conferences c " +
		     "LEFT OUTER JOIN memberships m ON c.id = m.conference AND m.user = ? " +
		     "JOIN names n ON n.id = c.id " +
		     "WHERE n.kind = " + NameManager.CONFERENCE_KIND + ' '+
		     "ORDER BY n.norm_name");
		m_countStmt = conn.prepareStatement("SELECT count(*) FROM conferences");
		m_changePermissionsStmt = conn.prepareStatement(
		     "UPDATE conferences SET permissions = ?, nonmember_permissions = ? WHERE id = ?");
        m_getParentCountForConfStmt = conn.prepareStatement("select count(*) from conferences where replyConf = ?");
        m_getMaxParentCountStmt = conn.prepareStatement("select count(*) as count from conferences where replyConf is not null group by replyConf order by count desc limit 1");
	}
	
	public void close()
	{
		try
		{
			if(m_addConfStmt != null)
				m_addConfStmt.close();
			if(m_changeReplyToConfStmt != null)
			    m_changeReplyToConfStmt.close();
			if(m_loadConfStmt != null)
				m_loadConfStmt.close();
			if(m_loadRangeStmt != null)
				m_loadRangeStmt.close();
			if(m_isMailboxStmt != null)
			    m_isMailboxStmt.close();
			if(m_listByDateStmt != null)
			    m_listByDateStmt.close();
			if(m_listByNameStmt != null)
			    m_listByNameStmt.close();
			if(m_countStmt != null)
			    m_countStmt.close();
            if(m_getParentCountForConfStmt != null)
                m_getParentCountForConfStmt.close();
            if(m_getMaxParentCountStmt != null)
                m_getMaxParentCountStmt.close();
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
	public long addConference(String fullname, String keywords, long administrator, int permissions, int nonmemberPermissions, short visibility, long replyConf)
		throws DuplicateNameException, SQLException, AmbiguousNameException
	{
		if(m_nameManager.nameExists(fullname))
			throw new DuplicateNameException(fullname);
			
		// First, add the name
		//
		long nameId = m_nameManager.addName(fullname, NameManager.CONFERENCE_KIND, visibility, keywords);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		// Now, add the conference
		//
		m_addConfStmt.clearParameters();
		m_addConfStmt.setLong(1, nameId);
		m_addConfStmt.setLong(2, administrator);
		m_addConfStmt.setInt(3, permissions);
		m_addConfStmt.setInt(4, nonmemberPermissions);
		if(replyConf == -1)
			m_addConfStmt.setNull(5, Types.BIGINT);
		else
			m_addConfStmt.setLong(5, replyConf);
		m_addConfStmt.setTimestamp(6, now);
		m_addConfStmt.executeUpdate();
		return nameId;
	}
	
    /**
     * Change the replyto-conference for the given conference
     * 
     * @param originalConferenceId
     * @param newReplyToConferenceId
     * @throws SQLException
     */
    public void changeReplyToConference(long originalConferenceId, long newReplyToConferenceId) throws SQLException
    {
        CacheManager.instance().getConferenceCache().registerInvalidation(originalConferenceId);
        m_changeReplyToConfStmt.clearParameters();
        if (newReplyToConferenceId == -1)
            m_changeReplyToConfStmt.setNull(1, Types.BIGINT);
		else
            m_changeReplyToConfStmt.setLong(1, newReplyToConferenceId);
        m_changeReplyToConfStmt.setLong(2, originalConferenceId);
        m_changeReplyToConfStmt.executeUpdate();
    }
    
    public void changePermissions(long id, int permissions, int nonmemberpermissions, short visibility)
    throws SQLException
    {
        // Change conference permissions
        //
        m_changePermissionsStmt.clearParameters();
        m_changePermissionsStmt.setInt(1, permissions);
        m_changePermissionsStmt.setInt(2, nonmemberpermissions);
        m_changePermissionsStmt.setLong(3, id);
        m_changePermissionsStmt.executeUpdate();
        
        // Change visibility
        //
        m_nameManager.changeVisibility(id, visibility);
        
        // Update cache.
        // Scrap the entire permission cache, since changing the conference permission would
        // have consequences all over the place.
        //
        CacheManager.instance().getConferenceCache().registerInvalidation(id);
        CacheManager.instance().getPermissionCache().clear();
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
		m_addConfStmt.setInt(4, 0);
		m_addConfStmt.setNull(5, Types.BIGINT);					
		m_addConfStmt.setTimestamp(6, now);
		m_addConfStmt.executeUpdate();
	}
	
		
	public ConferenceInfo loadConference(long id)
	throws ObjectNotFoundException, SQLException
	{
		KOMCache cache = CacheManager.instance().getConferenceCache();
		Long key = new Long(id);
		ConferenceInfo cached = (ConferenceInfo) cache.get(key);
		if(cached != null)
			return cached;
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
			ConferenceInfo answer = new ConferenceInfo(
				id,							// Id
				new Name(rs.getString(1), rs.getShort(6), NameManager.CONFERENCE_KIND),	// Name
                rs.getString(2),            // Keywords
                rs.getString(10),           // Email alias
				rs.getLong(3),				// Admin
				rs.getInt(4),				// Permissions
				rs.getInt(5),				// Nonmember permissions
				rs.getShort(6),				// Visibility
				rs.getObject(7) != null ? rs.getLong(7) : -1, // Reply conference
				rs.getTimestamp(8),
				rs.getTimestamp(9),
				r.getMin(),					// First text
				r.getMax()					// Last text
				);
			cache.deferredPut(key, answer);
			return answer;
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
	public Name[] getConferenceNamesByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getNamesByPatternAndKind(pattern, NameManager.CONFERENCE_KIND);
	}
	
	/**
	 * Returns a list of user ids based on a search pattern
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long[] getConferenceIdsByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getIdsByPatternAndKind(pattern, NameManager.CONFERENCE_KIND);
	}
		
	public boolean isMailbox(long conference)
	throws SQLException, ObjectNotFoundException
	{
		ResultSet rs = null;
		try
		{
			m_isMailboxStmt.clearParameters();
			m_isMailboxStmt.setLong(1, conference);
			rs = m_isMailboxStmt.executeQuery();
			if(!rs.next())
			    throw new ObjectNotFoundException("Conf=" + conference);
			return 0 != rs.getInt(1);
		}
		finally
		{
			if (null != rs)
			{
				rs.close();
			}
		}	    
	}
	
	public ConferenceListItem[] listByDate(long user)
	throws SQLException
	{
	    m_listByDateStmt.clearParameters();
	    m_listByDateStmt.setLong(1, user);
	    ResultSet rs = null;
	    try
	    {
	        ArrayList<ConferenceListItem> list = new ArrayList<ConferenceListItem>();
	        rs = m_listByDateStmt.executeQuery();
	        while(rs.next())
	        {
	            long id = rs.getLong(1); 
	            list.add(new ConferenceListItem(
                    id,new Name(
                    rs.getString(2),
                    rs.getShort(3), NameManager.CONFERENCE_KIND),
                    rs.getTimestamp(4),
                    rs.getTimestamp(5),
                    rs.getObject(6) != null && rs.getBoolean(9),
                    rs.getLong(7) == user,
                    rs.getInt(8)));
	        }
	        ConferenceListItem[] answer = new ConferenceListItem[list.size()];
	        list.toArray(answer);
	        return answer;
	    }
		finally
		{
			if (null != rs)
			{
				rs.close();
			}
		}	    	    
	}
	
	public long countConferences()
	throws SQLException
	{
	    ResultSet rs = null;
	    try
	    {
	        rs = m_countStmt.executeQuery();
	        rs.first();
	        return rs.getLong(1);
	    }
	    finally
	    {
	        if(rs != null)
	            rs.close();
	    }
	}
	
    public long countParentsForConference(long conf)
    throws SQLException
    {
        ResultSet rs = null;
        try
        {
            m_getParentCountForConfStmt.clearParameters();
            m_getParentCountForConfStmt.setLong(1, conf);
            rs = m_getParentCountForConfStmt.executeQuery();
            rs.first();
            return rs.getLong(1);
        }
        finally
        {
            if (rs != null)
                rs.close();
        }
    }
    
    public long getMaxParentCount()
    throws SQLException
    {
        ResultSet rs = null;
        try
        {
            m_getMaxParentCountStmt.executeQuery();
            rs.first();
            return rs.getLong(1);
        }
        finally
        {
            if (rs != null)
                rs.close();
        }
    }
    
	public ConferenceListItem[] listByName(long user)
	throws SQLException
	{
	    m_listByNameStmt.clearParameters();
	    m_listByNameStmt.setLong(1, user);
	    ResultSet rs = null;
	    try
	    {
	        ArrayList<ConferenceListItem> list = new ArrayList<ConferenceListItem>();
	        rs = m_listByNameStmt.executeQuery();
	        while(rs.next())
	        {
	            list.add(new ConferenceListItem(
                    rs.getLong(1),new Name(
                    rs.getString(2),
                    rs.getShort(3), NameManager.CONFERENCE_KIND),
                    rs.getTimestamp(4),
                    rs.getTimestamp(5),
                    rs.getObject(6) != null && rs.getBoolean(9),
                    rs.getLong(7) == user,
                    rs.getInt(8)));
	        }
	        ConferenceListItem[] answer = new ConferenceListItem[list.size()];
	        list.toArray(answer);
	        return answer;
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
