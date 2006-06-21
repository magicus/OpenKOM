/*
 * Created on Jul 12, 2004
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

import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageLogManager 
{
    private final Connection m_conn;
    
    private final PreparedStatement m_storeMessageStmt;
    private final PreparedStatement m_storeMessagePointerStmt;
    private final PreparedStatement m_listMessagesStmt;
    private final PreparedStatement m_listRecipientsStmt;
    
    public MessageLogManager(Connection conn) 
    throws SQLException
    {
        m_conn = conn;
        m_storeMessageStmt = m_conn.prepareStatement(
                "INSERT INTO messagelog(body, created, author, author_name) VALUES(?, ?, ?, ?)");
        m_storeMessagePointerStmt = m_conn.prepareStatement(
                "INSERT INTO messagelogpointers(recipient, logid, sent, kind) VALUES(?, ?, ?, ?)");
        m_listMessagesStmt = m_conn.prepareStatement(
                "SELECT ml.id, mlp.kind, ml.author, ml.author_name, ml.created, mlp.sent, ml.body " +
                "FROM messagelog ml, messagelogpointers mlp WHERE mlp.logid = ml.id AND " +
                "mlp.recipient = ? AND mlp.kind >= ? AND mlp.kind <= ? ORDER BY mlp.logid DESC LIMIT ? OFFSET 0");
        m_listRecipientsStmt = m_conn.prepareStatement(
                "SELECT mlp.recipient, n.fullname, n.visibility FROM messagelogpointers mlp, names n " +
                "WHERE n.id = mlp.recipient AND mlp.logid = ? AND mlp.sent = 0");
    }
    
    public void close()
    throws SQLException
    {
        if(m_storeMessageStmt != null)
            m_storeMessageStmt.close();
        if(m_storeMessagePointerStmt != null)
            m_storeMessagePointerStmt.close();
        if(m_listMessagesStmt != null)
            m_listMessagesStmt.close(); 
        if(m_listRecipientsStmt != null)
        	m_listRecipientsStmt.close();
    }
    
	public void finalize()
	{
		try
		{
			this.close();
		}
		catch(SQLException e)
		{
			// Not much we can do here...
			//
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores a message log item.
	 * 
	 * @param author The id of the author
	 * @param authorName The name of the author
	 * @param body Message body
	 * @return The log id
	 * @throws SQLException
	 */
	public long storeMessage(long author, String authorName, String body)
	throws SQLException
	{
	    m_storeMessageStmt.clearParameters();
	    m_storeMessageStmt.setString(1, body);
	    m_storeMessageStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
	    m_storeMessageStmt.setLong(3, author);
	    m_storeMessageStmt.setString(4, authorName);
	    m_storeMessageStmt.executeUpdate();
	    return ((com.mysql.jdbc.PreparedStatement) m_storeMessageStmt).getLastInsertID();
	}
	
	/**
	 * Stores a message log pointer, i.e. a link between a recipient and
	 * a message.
	 * 
	 * @param logid The is of the message
	 * @param recipient The id of the recipient
	 * @param sent True if this is a copy of a sent message
	 * @param kind The kind of message, e.g. private chat message or broadcast message.
	 * @throws SQLException
	 */
	public void storeMessagePointer(long logid, long recipient, boolean sent, short kind)
	throws SQLException
	{
	    m_storeMessagePointerStmt.clearParameters();
	    m_storeMessagePointerStmt.setLong(1, recipient);
	    m_storeMessagePointerStmt.setLong(2, logid);
	    m_storeMessagePointerStmt.setBoolean(3, sent);
	    m_storeMessagePointerStmt.setShort(4, kind);
	    m_storeMessagePointerStmt.executeUpdate();
	}
	
	public MessageLogItem[] listChatMessages(long user, int limit)
	throws SQLException
	{
	    return this.getMessages(user, limit, MessageLogKinds.CHAT, MessageLogKinds.CHAT);
	}
	
	public MessageLogItem[] listBroadcastMessages(long user, int limit)
	throws SQLException
	{
	    return this.getMessages(user, limit, MessageLogKinds.BROADCAST, 
	            MessageLogKinds.CONDENSED_BROADCAST);
	}
	
	public MessageLogItem[] listMulticastMessages(long user, int limit)
	throws SQLException
	{
	    return this.getMessages(user, limit, MessageLogKinds.MULTICAST, 
	            MessageLogKinds.MULTICAST);
	}
	
		
	public NameAssociation[] listRecipients(long logid)
	throws SQLException
	{
	    m_listRecipientsStmt.clearParameters();
	    m_listRecipientsStmt.setLong(1, logid);
	    ResultSet rs = null;
	    try
	    {
	        rs = m_listRecipientsStmt.executeQuery();
	        ArrayList<NameAssociation> list = new ArrayList<NameAssociation>();
	        while(rs.next())
	        {
	            list.add(new NameAssociation(
	                    rs.getLong(1),				// Id
	                    new Name(rs.getString(2), 
	                            rs.getShort(3), NameManager.CONFERENCE_KIND)));	// Name
	        }
	        NameAssociation[] answer = new NameAssociation[list.size()];
	        list.toArray(answer);
	        return answer;
	    }
	    finally
	    {
	        if(rs != null)
	            rs.close();
	    }
	}
	
	private MessageLogItem[] getMessages(long user, int limit,
	        short lowKind, short highKind)
	throws SQLException
	{
	    m_listMessagesStmt.clearParameters();
	    m_listMessagesStmt.setLong(1, user);
	    m_listMessagesStmt.setShort(2, lowKind);
	    m_listMessagesStmt.setShort(3, highKind);
	    m_listMessagesStmt.setInt(4, limit);
	    ResultSet rs = null;
	    try
	    {
	        ArrayList<MessageLogItem> list = new ArrayList<MessageLogItem>(limit);
	        rs = m_listMessagesStmt.executeQuery();
	        while(rs.next())
	        {
	            list.add(new MessageLogItem(
	                    rs.getShort(2),			// Kind
	                    rs.getLong(3),			// Author
	                    rs.getString(4),		// Author name
	                    rs.getTimestamp(5),		// Created
	                    rs.getBoolean(6),		// Sent
	                    rs.getString(7),		// Body
	                    this.listRecipients(rs.getLong(1)))); // Recipients		
	        }
	        MessageLogItem[] answer = new MessageLogItem[list.size()];
	        list.toArray(answer);
	        return answer;
	    }
	    finally
	    {
	        if(rs != null)
	            rs.close();
	    }
	}
}
