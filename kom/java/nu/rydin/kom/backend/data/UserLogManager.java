/*
 * Created on Aug 24, 2004
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
import java.util.ArrayList;

import nu.rydin.kom.structs.UserLogItem;
import nu.rydin.kom.utils.Logger;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserLogManager
{
    private final PreparedStatement m_storeStmt;
    
    private final PreparedStatement m_getByDateStmt;
    
    private final PreparedStatement m_getByUserStmt;
    
    private final Connection m_conn;
    
    public UserLogManager(Connection conn)
    throws SQLException
    {
        m_conn = conn;
        m_storeStmt = m_conn.prepareStatement(
                "INSERT INTO userlog(user, logged_in, logged_out, num_posted, num_read, num_chat_messages, num_broadcasts, " +
                "num_copies) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        m_getByDateStmt = m_conn.prepareStatement(
                "SELECT l.user, n.fullname, l.logged_in, l.logged_out, l.num_posted, l.num_read, " +
                "l.num_chat_messages, l.num_broadcasts, l.num_copies " +
                "FROM userlog l, names n " +
                "WHERE l.user = n.id AND l.logged_in > ? AND l.logged_in < ? " +
                "ORDER BY l.logged_in DESC LIMIT ? OFFSET ?");
        m_getByUserStmt = m_conn.prepareStatement(
                "SELECT l.user, n.fullname, l.logged_in, l.logged_out, l.num_posted, l.num_read, " +
                "l.num_chat_messages, l.num_broadcasts, l.num_copies " +
                "FROM userlog l, names n " +
        		"WHERE l.user = n.id AND l.user = ? AND l.logged_in > ? AND l.logged_in < ? " +
        		"ORDER BY l.logged_in DESC LIMIT ? OFFSET ?");
    }
    
    public void close()
    throws SQLException
    {
        if(m_storeStmt != null)
            m_storeStmt.close();
        if(m_getByDateStmt != null)
            m_getByDateStmt.close();
        if(m_getByUserStmt != null)
            m_getByUserStmt.close();
    }
    
    public void finalize()
    {
        try
        {
            this.close();
        }
        catch(SQLException e)
        {
            Logger.error(this, "Exception in finalizer", e);
        }
    }
    
    public void store(UserLogItem ul)
    throws SQLException
    {
        m_storeStmt.clearParameters(); 
        m_storeStmt.setLong(1, ul.getUserId());
        m_storeStmt.setTimestamp(2, ul.getLoggedIn());
        m_storeStmt.setTimestamp(3, ul.getLoggedOut());
        m_storeStmt.setInt(4, ul.getNumPosted());
        m_storeStmt.setInt(5, ul.getNumRead());
        m_storeStmt.setInt(6, ul.getNumChats());
        m_storeStmt.setInt(7, ul.getNumBroadcasts());
        m_storeStmt.setInt(8, ul.getNumCopies());
        m_storeStmt.executeUpdate();
    }
    
    public UserLogItem[] getByDate(Timestamp start, Timestamp end, int offset, int limit)
    throws SQLException
    {
        m_getByDateStmt.clearParameters();
        m_getByDateStmt.setTimestamp(1, start);
        m_getByDateStmt.setTimestamp(2, end);
        m_getByDateStmt.setInt(3, limit);
        m_getByDateStmt.setInt(4, offset);
        ArrayList<UserLogItem> list = new ArrayList<UserLogItem>(limit);
        ResultSet rs = null;
        try
        {
            rs = m_getByDateStmt.executeQuery();
            while(rs.next())
                list.add(this.extractLogItem(rs));
            UserLogItem[] answer = new UserLogItem[list.size()];
            list.toArray(answer);
            return answer;
        }
        finally
        {
            if(rs != null)
                rs.close();
        }
    }
    
    public UserLogItem[] getByUser(long userId, Timestamp start, Timestamp end, int offset, int limit)
    throws SQLException
    {
        m_getByUserStmt.clearParameters();
        m_getByUserStmt.setLong(1, userId);
        m_getByUserStmt.setTimestamp(2, start);
        m_getByUserStmt.setTimestamp(3, end);
        m_getByUserStmt.setInt(4, limit);
        m_getByUserStmt.setInt(5, offset);
        ArrayList<UserLogItem> list = new ArrayList<UserLogItem>(limit);
        ResultSet rs = null;
        try
        {
            rs = m_getByUserStmt.executeQuery();
            while(rs.next())
                list.add(this.extractLogItem(rs));
            UserLogItem[] answer = new UserLogItem[list.size()];
            list.toArray(answer);
            return answer;
        }
        finally
        {
            if(rs != null)
                rs.close();
        }
    }
    
     
    private UserLogItem extractLogItem(ResultSet rs)
    throws SQLException
    {
        return new UserLogItem(
                rs.getLong(1),		// Userid 
                rs.getString(2),	// User name
                rs.getTimestamp(3),	// login
                rs.getTimestamp(4), // logout
                rs.getInt(5),		// num posted
                rs.getInt(6), 		// num read
                rs.getInt(7), 		// num chats
                rs.getInt(8), 		// num broadcasts
                rs.getInt(9));		// num copies
    }
}
