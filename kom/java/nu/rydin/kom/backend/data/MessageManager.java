/*
 * Created on Oct 12, 2003
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
import java.util.List;

import nu.rydin.kom.backend.CacheManager;
import nu.rydin.kom.backend.SQLUtils;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.SelectionOverflowException;
import nu.rydin.kom.structs.GlobalMessageSearchResult;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 * @author Henrik Schröder
 */
public class MessageManager
{
	private final PreparedStatement m_loadMessageStmt;
	private final PreparedStatement m_loadMessageInConfStmt;
	private final PreparedStatement m_loadMessageHeaderStmt;
	private final PreparedStatement m_loadMessageOccurrenceStmt;
	private final PreparedStatement m_getNextNumStmt;
	private final PreparedStatement m_addMessageStmt;
	private final PreparedStatement m_addMessageSearchStmt;
	private final PreparedStatement m_addMessageOccurrenceStmt;
	private final PreparedStatement m_listOccurrencesStmt;
	private final PreparedStatement m_getFirstOccurrenceStmt;
	private final PreparedStatement m_getFirstOccurrenceInMyConfsStmt;
	private final PreparedStatement m_getOccurrenceInConferenceStmt;
	private final PreparedStatement m_getGlobalIdStmt;
	private final PreparedStatement m_getRepliesStmt;
	private final PreparedStatement m_getReplyIdsStmt;
	private final PreparedStatement m_loadOccurrenceStmt;
	private final PreparedStatement m_getVisibleOccurrencesStmt;
	private final PreparedStatement m_getMessageAttributesStmt;
	private final PreparedStatement m_addMessageAttributeStmt;
	private final PreparedStatement m_dropMessageAttributeStmt;
	private final PreparedStatement m_dropMessageOccurrenceStmt;
	private final PreparedStatement m_countMessageOccurrencesStmt;
	private final PreparedStatement m_dropMessageStmt;
	private final PreparedStatement m_dropMessageSearchStmt;
	private final PreparedStatement m_getLocalIdsInConfStmt;
	private final PreparedStatement m_dropConferenceStmt;
	private final PreparedStatement m_getGlobalBySubjectStmt;
	private final PreparedStatement m_getLocalBySubjectStmt;
	private final PreparedStatement m_findLastOccurrenceInConferenceWithAttrStmt;
	private final PreparedStatement m_getLatestMagicMessageStmt;
	private final PreparedStatement m_updateConferenceLasttext;
	private final PreparedStatement m_searchMessagesLocally;
    private final PreparedStatement m_grepMessagesLocally;
	private final PreparedStatement m_listAllMessagesLocally;
    private final PreparedStatement m_listMessagesLocallyByAuthor;
    private final PreparedStatement m_listMessagesGloballyByAuthor;
    private final PreparedStatement m_searchMessagesGlobally;
    private final PreparedStatement m_countStmt;
    private final PreparedStatement m_setThreadIdStmt;
    private final PreparedStatement m_selectByThreadStmt;
	
	private final Connection m_conn; 
	
	public static final short ACTION_CREATED 		= 0;
	public static final short ACTION_COPIED			= 1;
	public static final short ACTION_MOVED			= 2;
	
	public MessageManager(Connection conn)
	throws SQLException
	{
		m_conn = conn;
		m_loadMessageStmt = m_conn.prepareStatement(
			"SELECT created, author, author_name, reply_to, thread, subject, body FROM messages WHERE id = ?");
		m_loadMessageInConfStmt = m_conn.prepareStatement(
			"SELECT m.id, m.created, m.author, m.author_name, m.reply_to, m.thread, m.subject, m.body " +			"FROM messages m, messageoccurrences mo " +			"WHERE m.id = mo.message AND mo.conference = ? AND mo.localnum = ?");
		m_loadMessageHeaderStmt = m_conn.prepareStatement(
			"SELECT created, author, author_name, reply_to, thread, subject FROM messages WHERE id = ?");			
		m_loadMessageOccurrenceStmt = m_conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name FROM messages " +
			"WHERE conference = ? AND localNum = ?");
		m_getNextNumStmt = m_conn.prepareStatement(			"SELECT MAX(localnum) FROM messageoccurrences WHERE conference = ? FOR UPDATE");
		m_addMessageStmt = m_conn.prepareStatement(
			"INSERT INTO messages(created, author, author_name, reply_to, subject, body) " +			"VALUES(?, ?, ?, ?, ?, ?)");
		m_addMessageSearchStmt = m_conn.prepareStatement(
			"REPLACE INTO messagesearch(id, subject, body) " +
			"VALUES(?, ?, ?)");
		m_addMessageOccurrenceStmt = m_conn.prepareStatement(
			"INSERT INTO messageoccurrences(message, action_ts, kind, user, user_name, conference, localnum) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?)");
		m_updateConferenceLasttext = m_conn.prepareStatement(
			"UPDATE conferences SET lasttext=? WHERE id=?");
		m_listOccurrencesStmt = m_conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +			"WHERE message = ? ORDER BY action_ts");
		m_getOccurrenceInConferenceStmt = m_conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +
			"WHERE conference = ? AND message = ?");
		m_getFirstOccurrenceInMyConfsStmt = m_conn.prepareStatement(
				"SELECT mo.message, mo.action_ts, mo.kind, mo.user, mo.user_name, mo.conference, mo.localnum FROM messageoccurrences mo, memberships mbr " +
				"WHERE mbr.conference = mo.conference AND mbr.user = ? AND mo.message = ?");
		m_getFirstOccurrenceStmt = m_conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +
			"WHERE message = ? ORDER BY action_ts LIMIT 1");
		m_getGlobalIdStmt = m_conn.prepareStatement(
			"SELECT message FROM messageoccurrences WHERE conference = ? AND localnum = ?");
		m_getRepliesStmt = m_conn.prepareStatement(
			"SELECT id, created, author, author_name, reply_to, thread, subject FROM messages " +			"WHERE reply_to = ?");
		m_getReplyIdsStmt = m_conn.prepareStatement(
			"SELECT id FROM messages WHERE reply_to = ?");
		m_loadOccurrenceStmt = m_conn.prepareStatement(
			"SELECT message, action_ts, kind, user, conference, localnum FROM messageoccurrences " +
			"WHERE conference = ? AND localnum = ?");
		m_getVisibleOccurrencesStmt = m_conn.prepareStatement(
			"SELECT o.message, o.action_ts, o.kind, o.user, o.user_name, o.conference, o.localnum " +			"FROM messageoccurrences o, memberships m " +			"WHERE o.conference = m.conference AND m.user = ? AND o.message = ?");
		m_getMessageAttributesStmt = m_conn.prepareStatement(
		    "SELECT id, message, kind, created, value " +
		    "FROM messageattributes " +
		    "WHERE message = ? " +
			"order by created asc");
		m_addMessageAttributeStmt = m_conn.prepareStatement(
		    "INSERT INTO messageattributes (message, kind, created, value) " +
		    "VALUES (?, ?, ?, ?)");
		m_dropMessageAttributeStmt = m_conn.prepareStatement(
			"DELETE FROM messageattributes WHERE id = ? AND message = ?");
		m_dropMessageOccurrenceStmt = m_conn.prepareStatement(
			"delete from messageoccurrences " +
			"where localnum = ? and conference = ?");
		m_countMessageOccurrencesStmt = m_conn.prepareStatement(
			 "select count(*) from messageoccurrences " +
			 "where message = ?");
		m_dropMessageStmt = m_conn.prepareStatement(
			 "delete from messages " + 
			 "where id = ?");
		m_dropMessageSearchStmt = m_conn.prepareStatement(
			 "delete from messagesearch " + 
			 "where id = ?");
		
		// Ooooh! There's room for optimization here :-) I suggest we optimize by moving to an
		// RDBMS that supports indexed views (not to mention stored procedures..)
		//		
		m_getGlobalBySubjectStmt = m_conn.prepareStatement(
			 "select message " + 
			 "from messageoccurrences as mo " +
			 "join memberships as ms on mo.conference = ms.conference " +
			 "join messages on mo.message = messages.id " +
			 "where ms.user = ? and subject = ?");
		m_getLocalBySubjectStmt = m_conn.prepareStatement(
		     "select mo.localnum " +
		     "from messageoccurrences mo, messages m " +
		     "where m.id = mo.message and mo.conference = ? and m.subject = ?");
		
		// To speed things up, a special version to just retrieve the local ID.
		//
		m_getLocalIdsInConfStmt = m_conn.prepareStatement(
			 "select localnum from messageoccurrences where conference = ?");
		m_dropConferenceStmt = m_conn.prepareStatement(
			 "delete from conferences where id = ?");
		m_findLastOccurrenceInConferenceWithAttrStmt = m_conn.prepareStatement(
			 "select localnum " +
			 "from messageoccurrences as mo " +
			 "join messageattributes as ma on mo.message = ma.message " +
			 "where ma.kind = ? and mo.conference = ? " +
			 "order by mo.message desc " +
			 "limit 1 offset 0");
		m_getLatestMagicMessageStmt = m_conn.prepareStatement(
			 "select m.id " +
			 "from messages as m, messageattributes ma " +
			 "where ma.kind = ? and ma.value = ? and ma.message = m.id " +
			 "order by m.created desc " +
			 "limit 1 offset 0");
				
		m_searchMessagesLocally = m_conn.prepareStatement(
				"SELECT ms.id, mo.localnum, mo.user, m.author_name, ms.subject, m.reply_to " +
				"FROM messagesearch ms, messageoccurrences mo, messages m " +
				"WHERE ms.id = mo.message AND ms.id = m.id AND mo.conference = ? " +
				"AND MATCH(ms.subject, ms.body) AGAINST (? IN BOOLEAN MODE) " +
				"ORDER BY localnum DESC " +
				"LIMIT ? OFFSET ?");
		
		// Selecting from messagesearch is a couple of 1000 times faster. InnoDB...
		//
		m_countStmt = m_conn.prepareStatement(
		        "SELECT COUNT(*) FROM messagesearch");

		m_grepMessagesLocally = m_conn.prepareStatement(
				"SELECT ms.id, mo.localnum, mo.user, m.author_name, ms.subject, m.reply_to " +
				"FROM messagesearch ms, messageoccurrences mo, messages m " +
				"WHERE ms.id = mo.message AND ms.id = m.id AND mo.conference = ? " +
				"AND (ms.subject LIKE ? OR ms.body LIKE ?) " +
				"ORDER BY localnum DESC " +
				"LIMIT ? OFFSET ?");
		
		m_listAllMessagesLocally = m_conn.prepareStatement(
		        "SELECT m.id, mo.localnum, mo.user, m.author_name, m.subject, m.reply_to " +
		        "FROM messages m JOIN messageoccurrences mo " +
		        "ON m.id = mo.message " +
		        "WHERE mo.conference = ? " +
		        "ORDER BY localnum DESC " +
		        "LIMIT ? OFFSET ?");
		
		m_listMessagesLocallyByAuthor = m_conn.prepareStatement(
		        "SELECT m.id, mo.localnum, mo.user, m.author_name, m.subject, m.reply_to " +
		        "FROM messages m JOIN messageoccurrences mo " +
		        "ON m.id = mo.message " +
		        "WHERE mo.conference = ? AND m.author = ? " +
		        "ORDER BY localnum DESC " +
		        "LIMIT ? OFFSET ?");
		
		m_listMessagesGloballyByAuthor = m_conn.prepareStatement(
		        "SELECT m.id, mo.localnum, mo.conference, n.fullname, n.visibility, mo.user, m.author_name, m.subject, m.reply_to " +
		        "FROM messages m, messageoccurrences mo, names n " +
		        "WHERE m.id = mo.message AND n.id = mo.conference AND m.author = ? " +
		        "ORDER BY mo.action_ts DESC " +
		        "LIMIT ? OFFSET ?");
		
		m_searchMessagesGlobally = m_conn.prepareStatement(
		        "SELECT m.id, mo.localnum, mo.conference, n.fullname, n.visibility, mo.user, me.author_name, m.subject, me.reply_to " +
		        "FROM messagesearch m, messages me, messageoccurrences mo, names n " +
		        "WHERE m.id = mo.message AND n.id = mo.conference AND m.id = me.id " +
		        "AND MATCH(m.subject, m.body) AGAINST (? IN BOOLEAN MODE) " +
		        "ORDER BY mo.action_ts DESC " +
		        "LIMIT ? OFFSET ?");
		m_setThreadIdStmt = m_conn.prepareStatement(
		        "UPDATE messages SET thread = ? WHERE id = ?");
		m_selectByThreadStmt = m_conn.prepareStatement(
		        "SELECT id FROM messages WHERE thread = ?");
	}
	
	public void close()
	throws SQLException
	{
		if(m_loadMessageStmt != null)
			m_loadMessageStmt.close();
		if(m_loadMessageInConfStmt != null)
			m_loadMessageInConfStmt.close();			
		if(m_loadMessageOccurrenceStmt != null)
			m_loadMessageOccurrenceStmt.close();
		if(m_getNextNumStmt != null)
			m_getNextNumStmt.close();	
		if(m_addMessageStmt != null)
			m_addMessageStmt.close();
		if(m_addMessageOccurrenceStmt != null)
			m_addMessageOccurrenceStmt.close();
		if(m_listOccurrencesStmt != null)
			m_listOccurrencesStmt.close();
		if(m_getFirstOccurrenceStmt != null)
			m_getFirstOccurrenceStmt.close();
		if(m_getFirstOccurrenceInMyConfsStmt != null)
			m_getFirstOccurrenceInMyConfsStmt.close();
		if(m_getOccurrenceInConferenceStmt != null)
			m_getOccurrenceInConferenceStmt.close();
		if(m_getGlobalIdStmt != null)
			m_getGlobalIdStmt.close();																	
		if(m_getRepliesStmt != null)
			m_getRepliesStmt.close();
		if(m_getReplyIdsStmt != null)
			m_getReplyIdsStmt.close();				
		if(m_loadOccurrenceStmt != null)
			m_loadOccurrenceStmt.close();		
		if(m_getVisibleOccurrencesStmt != null)
			m_getVisibleOccurrencesStmt.close();
		if(m_dropMessageOccurrenceStmt != null)
			m_dropMessageOccurrenceStmt.close();
		if(m_countMessageOccurrencesStmt != null)
			m_countMessageOccurrencesStmt.close();
		if(m_dropMessageStmt != null)
			m_dropMessageStmt.close();
		if(m_listAllMessagesLocally != null)
			m_listAllMessagesLocally.close();
		if(m_getLocalIdsInConfStmt != null)
			m_getLocalIdsInConfStmt.close();
		if(m_dropConferenceStmt != null)
			m_dropConferenceStmt.close();
		if(m_getGlobalBySubjectStmt != null)
			m_getGlobalBySubjectStmt.close();
		if(m_findLastOccurrenceInConferenceWithAttrStmt != null)
			m_findLastOccurrenceInConferenceWithAttrStmt.close();
		if(m_getLatestMagicMessageStmt != null)
			m_getLatestMagicMessageStmt.close();
		if(m_countStmt != null)
		    m_countStmt.close();
		if(m_setThreadIdStmt != null)
		    m_setThreadIdStmt.close();
		if(m_selectByThreadStmt != null)
		    m_selectByThreadStmt.close();
	}
	
	public void finalize()
	{
		try
		{
			this.close();
		}
		catch(SQLException e)
		{
			// Not much we can do...
			//
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a message using its id
	 * 
	 * @param id The id
	 * @return The message
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public Message loadMessage(long id)
	throws MessageNotFoundException, SQLException
	{
		m_loadMessageStmt.clearParameters();
		m_loadMessageStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_loadMessageStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message id=" + id);
			return new Message(
				id,
				rs.getTimestamp(1),		// created
				rs.getLong(2), 			// author
				new Name(rs.getString(3), Visibilities.PUBLIC),	// Author name
				rs.getObject(4) != null ? rs.getLong(4) : -1, 	// reply to
				rs.getObject(5) != null ? rs.getLong(5) : -1, 	// thread
				rs.getString(6),		// subject
				rs.getString(7),		// body
				this.getOccurrences(id)
				);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Loads a message using a conference id and a local number
	 * 
	 * @return The message
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public Message loadMessage(long conf, int localNum)
	throws MessageNotFoundException, SQLException
	{
		m_loadMessageInConfStmt.clearParameters();
		m_loadMessageInConfStmt.setLong(1, conf);
		m_loadMessageInConfStmt.setInt(2, localNum);
		ResultSet rs = null;
		try
		{
			rs = m_loadMessageInConfStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message conf=" + conf + " localnum=" + localNum);
			long id = rs.getLong(1);
			return new Message(
				id,
				rs.getTimestamp(2),		// created
				rs.getLong(3), 			// author
				new Name(rs.getString(4), Visibilities.PUBLIC),	// author name				
				rs.getObject(5) != null ? rs.getLong(5) : -1, 	// reply to
				rs.getObject(6) != null ? rs.getLong(6) : -1, 	// thread
				rs.getString(7),		// subject
				rs.getString(8),		// body 
				this.getOccurrences(id)
				);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	
   /** Loads a message header (i.e. message, less the body) using its id
	* 
	* @param id The id
	* @return The message
	* @throws MessageNotFoundException
	* @throws SQLException
	*/
   	public MessageHeader loadMessageHeader(long id)
   	throws MessageNotFoundException, SQLException
   	{
	   m_loadMessageHeaderStmt.clearParameters();
	   m_loadMessageHeaderStmt.setLong(1, id);
	   ResultSet rs = null;
	   try
	   {
		   rs = m_loadMessageHeaderStmt.executeQuery();
		   if(!rs.next())
			   throw new MessageNotFoundException("Message id=" + id);
		   return new MessageHeader(
		   		id,
			   	rs.getTimestamp(1),		// created
			   	rs.getLong(2), 			// author
			   	new Name(rs.getString(3), Visibilities.PUBLIC),		// author name	
			   	rs.getObject(4) != null ? rs.getLong(4) : -1,		// reply to
			    rs.getObject(5) != null ? rs.getLong(5) : -1,		// reply to
			   	rs.getString(6)		
			   	);
			
	   }
	   finally
	   {
		   if(rs != null)
			   rs.close();
	   }
   	}
   	
   	public MessageOccurrence createMessageOccurrence(long globalId, short kind, long user, String userName, long conference)
   	throws MessageNotFoundException, SQLException
   	{
   		ResultSet rs = null;
   		try
   		{
			// Get next message number
			//
			m_getNextNumStmt.clearParameters();
			m_getNextNumStmt.setLong(1, conference);			
			rs = m_getNextNumStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("conference id=" + conference);
			int num = rs.getInt(1) + 1;
			Timestamp now = new Timestamp(System.currentTimeMillis());
	
			// Create message occurrence record
			//
			for(;;)
			{
				m_addMessageOccurrenceStmt.clearParameters();
				m_addMessageOccurrenceStmt.setLong(1, globalId);
				m_addMessageOccurrenceStmt.setTimestamp(2, now);
				m_addMessageOccurrenceStmt.setShort(3, kind);
				m_addMessageOccurrenceStmt.setLong(4, user);
				m_addMessageOccurrenceStmt.setString(5, userName);			
				m_addMessageOccurrenceStmt.setLong(6, conference);
				m_addMessageOccurrenceStmt.setInt(7, num);
				try
				{
				    m_addMessageOccurrenceStmt.executeUpdate();
				    break;
				}
				catch(SQLException e)
				{
				    // SELECT MAX(localnum) ... FOR UPDATE does not seem to
				    // put a lock on the entire table (IMHO, it should).
				    // Anyway... Check for index uniqueness violations.
				    //
				    if(e.getMessage().startsWith("Duplicate key"))
				    {
				        // Message number clash! Bump number and try again!
				        //
				        Logger.info(this, "Duplicate message number, trying again...");
				        ++num;
				        continue;
				    }
				    throw e;
				}
			}
						
			// Update conference records "last text" field
			//
			m_updateConferenceLasttext.clearParameters();
			m_updateConferenceLasttext.setTimestamp(1, now);
			m_updateConferenceLasttext.setLong(2,conference);
			m_updateConferenceLasttext.executeUpdate();
			
			// This changes the number of messages in a conference. 
			// 
			CacheManager.instance().getConferenceCache().registerInvalidation(new Long(conference));
			
			return new MessageOccurrence(globalId, now, kind, new NameAssociation(user, userName), conference, num);
   		}
   		finally
   		{
   			if(rs != null)
   				rs.close();
   		}

   	}
   
   	public MessageOccurrence addMessage(long author, String authorName, long conference, long replyTo, String subject, String body)
   	throws MessageNotFoundException, SQLException
   	{
   		ResultSet rs = null;
		try
		{
			// Create message
			//			
			Timestamp now = new Timestamp(System.currentTimeMillis());
			m_addMessageStmt.clearParameters();
			m_addMessageStmt.setTimestamp(1, now);
			m_addMessageStmt.setLong(2, author);
			m_addMessageStmt.setString(3, authorName);
			if(replyTo != -1L)
				m_addMessageStmt.setLong(4, replyTo);
			else
				m_addMessageStmt.setObject(4, null);
			m_addMessageStmt.setString(5, subject);
			m_addMessageStmt.setString(6, body);
			m_addMessageStmt.executeUpdate();
			
			// Get hold of id of newly created record
			//
			long id = ((com.mysql.jdbc.PreparedStatement) m_addMessageStmt).getLastInsertID();
			
			// Create message occurrence record
			//
			MessageOccurrence result = this.createMessageOccurrence(id, ACTION_CREATED, author, authorName, conference);
			
			// Add thread id if needed
			//
			long thread = -1;
			if(replyTo == -1) // We're the root of a thread, so use our own id
			    thread = id;
			else
			{
			    // This is a reply, so the thread id is the thread id
			    // of the message we're replying to.
			    //
			    MessageHeader mh = this.loadMessageHeader(replyTo);
			    thread = mh.getThread();
			}
			
			// Update with thread id if needed
			//
			if(thread != -1)
			{
			    m_setThreadIdStmt.clearParameters();
			    m_setThreadIdStmt.setLong(1, thread);
			    m_setThreadIdStmt.setLong(2, id);
			    m_setThreadIdStmt.executeUpdate();
			}
			// Duplicate into search table
			//
			m_addMessageSearchStmt.clearParameters();
			m_addMessageSearchStmt.setLong(1, id);
			m_addMessageSearchStmt.setString(2, subject);
			m_addMessageSearchStmt.setString(3, body);
			m_addMessageSearchStmt.executeUpdate();
			
			return result; 
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
   	}
   	
   	/**
   	 * Returns the occurrence records for a message.
   	 * 
   	 * @param messageId The global message id
   	 */
   	public MessageOccurrence[] getOccurrences(long messageId)
   	throws SQLException
   	{
   		m_listOccurrencesStmt.clearParameters();
   		m_listOccurrencesStmt.setLong(1, messageId);
   		ResultSet rs = null;
   		try
   		{
   			List list = new ArrayList();
   			rs = m_listOccurrencesStmt.executeQuery();
   			while(rs.next())
   			{
   				list.add(new MessageOccurrence(
   					rs.getLong(1),		// message id
   					rs.getTimestamp(2),	// Timestamp
   					rs.getShort(3),		// Kind,
   					new NameAssociation(rs.getLong(4),		// User
   					        rs.getString(5)),	// User name
   					rs.getLong(6),		// Conference
   					rs.getInt(7)		// Localnum
   					));
   			}
   			MessageOccurrence[] answer = new MessageOccurrence[list.size()];
   			list.toArray(answer);
   			return answer;
   		}
   		finally
   		{
   			if(rs != null)
   				rs.close();
   		}
   	}
   	
   	/**
   	 * Returns a <tt>MessageOccurrence</tt> record representing the
   	 * earliest message
   	 * 
   	 * @param messageId The message id
   	 * @throws MessageNotFoundException
   	 * @throws SQLException
   	 */
   	public MessageOccurrence getFirstOccurrence(long messageId)
   	throws MessageNotFoundException, SQLException
   	{
		m_getFirstOccurrenceStmt.clearParameters();
		m_getFirstOccurrenceStmt.setLong(1, messageId);
		ResultSet rs = null;
		try
		{
			rs = m_getFirstOccurrenceStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message id=" + messageId);

			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				new NameAssociation(rs.getLong(4),		// User
				rs.getString(5)),	// User name
				rs.getLong(6),		// Conference
				rs.getInt(7)		// Localnum
				);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
   	}
   	
   	/**
   	 * Returns a global message id based on a conference id and a local message number
   	 * 
   	 * @param conference The conference id
   	 * @param localnum The local message number
   	 * 
   	 * @throws MessageNotFoundException
   	 * @throws SQLException
   	 */
   	public long getGlobalMessageId(long conference, int localnum)
   	throws MessageNotFoundException, SQLException
   	{
		m_getGlobalIdStmt.clearParameters();
		m_getGlobalIdStmt.setLong(1, conference);
		m_getGlobalIdStmt.setInt(2, localnum);
		ResultSet rs = null;
		try
		{
			rs = m_getGlobalIdStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message conference=" + conference + " localnum=" + localnum);
			return rs.getLong(1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Loads a <tt>MessageOccurrence</tt> for a local message number and a conference
	 * 
	 * @param conference The conference
	 * @param localnum The local message number
	 * @return
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence loadMessageOccurrence(long conference, int localnum)
	throws MessageNotFoundException, SQLException
	{
		m_loadOccurrenceStmt.clearParameters();
		m_loadOccurrenceStmt.setLong(1, conference);
		m_loadOccurrenceStmt.setInt(2, localnum);
		ResultSet rs = null;
		try
		{
			rs = m_loadOccurrenceStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message conference=" + conference + " localnum=" + localnum);
			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				new NameAssociation(rs.getLong(4),		// User
				rs.getString(5)),	// User name
				conference,
				localnum
				); 
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
   	
   	/**
   	 * Reurns an occurrence of a message in a conference
   	 * 
   	 * @param conferenceId The conference
   	 * @param messageId The message
   	 * 
   	 * @throws MessageNotFoundException If the message didn't exist in this conference
   	 * @throws SQLException
   	 */
	public MessageOccurrence getOccurrenceInConference(long conferenceId, long messageId)
	throws MessageNotFoundException, SQLException
	{
		m_getOccurrenceInConferenceStmt.clearParameters();
		m_getOccurrenceInConferenceStmt.setLong(1, conferenceId);
		m_getOccurrenceInConferenceStmt.setLong(2, messageId);
		ResultSet rs = null;
		try
		{
			rs = m_getOccurrenceInConferenceStmt.executeQuery();
			if(!rs.next())
				throw new MessageNotFoundException("Message id=" + messageId);

			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				new NameAssociation(rs.getLong(4),		// User
				rs.getString(5)),	// User name
				rs.getLong(6),		// Conference
				rs.getInt(7)		// Localnum
				);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Given a message id and a conference, this method returns the message occurrence
	 * the user is most likely interested in. The occurrence is determined as follows:
	 * <br>1: If the message exists in the current conference, pick that one
	 * <br>2: Otherwise, pick the earliest occurrence
	 * @param user The id of the requestor 
	 * @param conference The conference id
	 * @param id The message id
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence getMostRelevantOccurrence(long user, long conference, long id)
	throws MessageNotFoundException, SQLException
	{
		try
		{
			return this.getOccurrenceInConference(conference, id);   	
		}
		catch(MessageNotFoundException e)
		{
		    ResultSet rs = null;
		    try
		    {
		        // Get first occurrence in a conference we're members of
		        //
		        m_getFirstOccurrenceInMyConfsStmt.clearParameters();
		        m_getFirstOccurrenceInMyConfsStmt.setLong(1, user);
		        m_getFirstOccurrenceInMyConfsStmt.setLong(2, id);
		        rs = m_getFirstOccurrenceInMyConfsStmt.executeQuery();
		        if(rs.next())
					return new MessageOccurrence(
							rs.getLong(1),		// Global id
							rs.getTimestamp(2),	// Timestamp
							rs.getShort(3),		// Kind,
							new NameAssociation(rs.getLong(4),		// User
							rs.getString(5)),	// User name
							rs.getLong(6),		// Conference
							rs.getInt(7)		// Localnum
							);
		    }
		    finally
		    {
		        if(rs != null)
		            rs.close();
		    }
		    // 
			// Does not exist in a conference we're members of. Pick the first occurrence!
			//
			return this.getFirstOccurrence(id);
		}
	}
	
	/**
	 * Returns the original occurrence of a message, which is defined as:
	 * 1) If there is one with kind=ACTION_CREATED, pick it, otherwise
	 * 2) Pick the one with kind=ACTION_MOVED, or if that also does not exist,
	 * 3) Throw MessageNotFoundException. 
	 * 
	 * @param messageId
	 * @return the original occurrence
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence getOriginalMessageOccurrence(long messageId)
	throws MessageNotFoundException, SQLException
	{
	    MessageOccurrence[] occurrences = getOccurrences(messageId);
	    for (int i = 0; i < occurrences.length; i++)
        {
	        // Note: In the highly unlikely event that there is more than one of either
	        // a CREATED or MOVED occurrence, we'll pick the first one. The only way 
	        // this can happen is if someone screws up, THERE CAN BE ONLY ONE!
	        //
            if (occurrences[i].getKind() == ACTION_CREATED || occurrences[i].getKind() == ACTION_MOVED)
            {
                return occurrences[i];
            }
        }
	    throw new MessageNotFoundException();
	}
	
	/**
	 * Returns the visible occurrences of a message, i.e. the occurrences
	 * that appear in conferences the specified user is a member of.
	 * 
	 * @param userId The user
	 * @param globalId Global message id
	 * @throws MessageNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence[] getVisibleOccurrences(long userId, long globalId)
	throws MessageNotFoundException, SQLException
	{
		List list = new ArrayList();
		m_getVisibleOccurrencesStmt.clearParameters();
		m_getVisibleOccurrencesStmt.setLong(1, userId);
		m_getVisibleOccurrencesStmt.setLong(2, globalId);
		ResultSet rs = null;
		try
		{
			rs = m_getVisibleOccurrencesStmt.executeQuery();
			while(rs.next())
			{
				list.add(new MessageOccurrence(
								rs.getLong(1),		// Global id
								rs.getTimestamp(2),	// Timestamp
								rs.getShort(3),		// Kind
								new NameAssociation(rs.getLong(4),		// User
								rs.getString(5)),	// User name
								rs.getLong(6),		// Conference
								rs.getInt(7)		// Localnum
								));
			}
			MessageOccurrence[] answer = new MessageOccurrence[list.size()];
			list.toArray(answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns the <tt>MessageHeaders</tt> for the replies to a message
	 * 
	 * @param originalId The message id of the original message
	 * @return
	 * @throws SQLException
	 */
	public MessageHeader[] getReplies(long originalId)
	throws SQLException
	{
		List list = new ArrayList();
		m_getRepliesStmt.clearParameters();
		m_getRepliesStmt.setLong(1, originalId);
		ResultSet rs = null;
		try
		{
			rs = m_getRepliesStmt.executeQuery();
			while(rs.next())
			{
				list.add(new MessageHeader(
					 rs.getLong(1),
					 rs.getTimestamp(2),	// created
					 rs.getLong(3), 		// author
					 new Name(rs.getString(4), Visibilities.PUBLIC),// author name	
					 rs.getObject(5) != null ? rs.getLong(5) : -1,	// reply to
					 rs.getObject(6) != null ? rs.getLong(6) : -1,	// reply to
					 rs.getString(7)		// subject	
					 ));
			}
			MessageHeader[] answer = new MessageHeader[list.size()];
			list.toArray(answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns the global ids of the replies to a message
	 * 
	 * @param originalId
	 * @throws SQLException
	 */
	public long[] getReplyIds(long originalId)
	throws SQLException
	{
		m_getReplyIdsStmt.clearParameters();
		m_getReplyIdsStmt.setLong(1, originalId);
		ResultSet rs = null;
		try
		{
			rs = m_getReplyIdsStmt.executeQuery();
			return SQLUtils.extractLongs(rs, 1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}

	}
	
	/**
	 * Returns the attributes to a message
	 * 
	 * @param messageId
	 * @throws SQLException
	 */
	public MessageAttribute[] getMessageAttributes(long messageId)
	throws SQLException
	{
	    List list = new ArrayList();
	    m_getMessageAttributesStmt.clearParameters();
	    m_getMessageAttributesStmt.setLong(1, messageId);
	    ResultSet rs = null;
	    try
	    {
	        rs = m_getMessageAttributesStmt.executeQuery();
	        while(rs.next())
	        {
	            list.add(new MessageAttribute(
	                    rs.getLong(1),		//id
	                    rs.getLong(2),		//message
	                    rs.getShort(3),		//kind
	                    rs.getTimestamp(4),	//created
	                    rs.getString(5)));	//value
	        }
	        MessageAttribute[] result = new MessageAttribute[list.size()];
	        list.toArray(result);
	        return result;
	    }
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	public void addMessageAttribute(long message, short kind, String value)
	throws SQLException
	{
		// Create messageattribute
		//			
		Timestamp now = new Timestamp(System.currentTimeMillis());
		m_addMessageAttributeStmt.clearParameters();
		m_addMessageAttributeStmt.setLong(1, message);
		m_addMessageAttributeStmt.setShort(2, kind);
		m_addMessageAttributeStmt.setTimestamp(3,now);
		m_addMessageAttributeStmt.setString(4, value);
		m_addMessageAttributeStmt.executeUpdate();
	}
	
	public void dropMessageAttribute(long attributeid, long messageid) 
	throws SQLException
	{
		m_dropMessageAttributeStmt.clearParameters();
		m_dropMessageAttributeStmt.setLong(1, attributeid);
		m_dropMessageAttributeStmt.setLong(2, messageid);
		m_dropMessageAttributeStmt.executeUpdate();
	}
	
	public int getMessageOccurrenceCount (long globalId)
	throws SQLException
	{
		ResultSet rs = null;
		try
		{
			this.m_countMessageOccurrencesStmt.clearParameters();
			this.m_countMessageOccurrencesStmt.setLong(1, globalId);
			rs = this.m_countMessageOccurrencesStmt.executeQuery();
			rs.first();
			return rs.getInt(1);
		}
		finally
		{
			if (rs != null)
				rs.close();
		}
	}
	
	public void dropMessageOccurrence(int localNum, long conference)
	throws SQLException, MessageNotFoundException
	{
		long globalNum = this.getGlobalMessageId(conference, localNum);
		
		this.m_dropMessageOccurrenceStmt.clearParameters();
		this.m_dropMessageOccurrenceStmt.setInt(1, localNum);
		this.m_dropMessageOccurrenceStmt.setLong(2, conference);
		this.m_dropMessageOccurrenceStmt.execute();
		
		if (0 == this.getMessageOccurrenceCount(globalNum))
		{
			// last occurrence deleted, so drop the message
			dropMessage (globalNum);
		}
	}
	
	public void dropMessage(long globalNum)
	throws SQLException
	{
		// Invalidate cache for all occurrences
		//
		MessageOccurrence[] occs = this.getOccurrences(globalNum);
		int top = occs.length;
		for(int idx = 0; idx < top; ++idx)
			CacheManager.instance().getConferenceCache().registerInvalidation(
				new Long(occs[idx].getConference()));
				
		// Add "original deleted" attribute to replies
		//
		try
        {
		    MessageHeader[] replies = getReplies(globalNum);
            MessageHeader original = loadMessageHeader(globalNum);
			for (int i = 0; i < replies.length; i++)
	        {
	            addMessageAttribute(replies[i].getId(), MessageAttributes.ORIGINAL_DELETED, MessageAttribute.constructUsernamePayload(original.getAuthor(), original.getAuthorName().getName()));
	        }
        } 
		catch (MessageNotFoundException e)
        {
		    // Well, uhh, if we got here it means we tried to delete a non-existing
		    // message, which will fail naturally anyway. No worries, mate.
        }
		
		// Delete the actual message.
		//
		this.m_dropMessageStmt.clearParameters();
		this.m_dropMessageStmt.setLong(1, globalNum);
		this.m_dropMessageStmt.execute();

		this.m_dropMessageSearchStmt.clearParameters();
		this.m_dropMessageSearchStmt.setLong(1, globalNum);
		this.m_dropMessageSearchStmt.execute();
	}
	
	public void deleteConference (long conference)
	throws SQLException
	{
		CacheManager.instance().getConferenceCache().registerInvalidation(new Long(conference));
		this.m_getLocalIdsInConfStmt.clearParameters();
		this.m_getLocalIdsInConfStmt.setLong(1, conference);
		ResultSet rs = this.m_getLocalIdsInConfStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				this.dropMessageOccurrence(rs.getInt(1), conference);
			}
			catch (MessageNotFoundException f)
			{
				// Ignore. This exception is probably due to someone deleting the message before
				// we got to it.
			}
		}
	}
	
	public long[] getMessagesBySubject (String subject, long user)
	throws SQLException
	{
		this.m_getGlobalBySubjectStmt.clearParameters();
		this.m_getGlobalBySubjectStmt.setLong(1, user);
		this.m_getGlobalBySubjectStmt.setString(2, subject);
		ResultSet rs = this.m_getGlobalBySubjectStmt.executeQuery();
		return SQLUtils.extractLongs(rs, 1);	
	}
	
	public int[] getLocalMessagesBySubject (String subject, long conference)
	throws SQLException
	{
	    this.m_getLocalBySubjectStmt.clearParameters();
	    this.m_getLocalBySubjectStmt.setLong(1, conference);
	    this.m_getLocalBySubjectStmt.setString(2, subject);
	    ResultSet rs = this.m_getLocalBySubjectStmt.executeQuery();
	    return SQLUtils.extractInts(rs, 1);
	    
	}
	
	public int findLastOccurrenceInConferenceWithAttrStmt (short attrKind, long conference)
	throws SQLException
	{
		this.m_findLastOccurrenceInConferenceWithAttrStmt.clearParameters();
		this.m_findLastOccurrenceInConferenceWithAttrStmt.setShort(1, attrKind);
		this.m_findLastOccurrenceInConferenceWithAttrStmt.setLong(2, conference);
		ResultSet rs = this.m_findLastOccurrenceInConferenceWithAttrStmt.executeQuery();
		rs.first();
		int i = rs.getInt(1);
		rs.close();
		rs = null;		
		return i;
	}
	
	public long getTaggedMessage(long objectId, short kind)
	throws SQLException, MessageNotFoundException
	{
		ResultSet rs = null;
		try
		{
			this.m_getLatestMagicMessageStmt.clearParameters();
			this.m_getLatestMagicMessageStmt.setShort(1, kind);
			this.m_getLatestMagicMessageStmt.setString(2, Long.toString(objectId));
			rs = this.m_getLatestMagicMessageStmt.executeQuery();
			if(!rs.next())
			    throw new MessageNotFoundException("object=" + objectId + ", kind=" + kind);
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
    
    public LocalMessageSearchResult[] searchMessagesLocally(long conference, String searchterm, int offset, int length)
    throws SQLException
    {
    	this.m_searchMessagesLocally.clearParameters();
    	this.m_searchMessagesLocally.setLong(1, conference);
    	this.m_searchMessagesLocally.setString(2, searchterm);
        this.m_searchMessagesLocally.setLong(3, length);
        this.m_searchMessagesLocally.setLong(4, offset);
        
        return innerLocalSearch(this.m_searchMessagesLocally);
    }

    public LocalMessageSearchResult[] grepMessagesLocally(long conference, String searchterm, int offset, int length)
    throws SQLException
    {
    	this.m_grepMessagesLocally.clearParameters();
    	this.m_grepMessagesLocally.setLong(1, conference);
    	this.m_grepMessagesLocally.setString(2, "%" + searchterm + "%");
    	this.m_grepMessagesLocally.setString(3, "%" + searchterm + "%");
        this.m_grepMessagesLocally.setLong(4, length);
        this.m_grepMessagesLocally.setLong(5, offset);
        
        return innerLocalSearch(this.m_grepMessagesLocally); 
    }
    
	public LocalMessageSearchResult[] listAllMessagesLocally(long conference,
            int offset, int length) throws SQLException
    {
        this.m_listAllMessagesLocally.clearParameters();
        this.m_listAllMessagesLocally.setLong(1, conference);
        this.m_listAllMessagesLocally.setInt(2, length);
        this.m_listAllMessagesLocally.setInt(3, offset);
        
        return innerLocalSearch(this.m_listAllMessagesLocally);        
    }
	
	public LocalMessageSearchResult[] listMessagesLocallyByAuthor(long conference, long user,
            int offset, int length) throws SQLException
    {
        this.m_listMessagesLocallyByAuthor.clearParameters();
        this.m_listMessagesLocallyByAuthor.setLong(1, conference);
        this.m_listMessagesLocallyByAuthor.setLong(2, user);
        this.m_listMessagesLocallyByAuthor.setInt(3, length);
        this.m_listMessagesLocallyByAuthor.setInt(4, offset);
        
        return innerLocalSearch(this.m_listMessagesLocallyByAuthor);        
    }
	
	private LocalMessageSearchResult[] innerLocalSearch(PreparedStatement localSearchStatement) throws SQLException
	{
        ResultSet rs = localSearchStatement.executeQuery();
        List l = new ArrayList();
        while (rs.next())
        {
            l.add(new LocalMessageSearchResult(
                    rs.getLong(1), // globalid
                    rs.getInt(2), // localid
                    new NameAssociation(rs.getLong(3), // authorid
                    new Name(rs.getString(4), Visibilities.PUBLIC)), // authorname
                    rs.getString(5), // subject
                    rs.getLong(6)) 	// Reply to
                    );
        }
        LocalMessageSearchResult[] lmsr = new LocalMessageSearchResult[l.size()];
        l.toArray(lmsr);
        return lmsr;
	}
	
	public GlobalMessageSearchResult[] listMessagesGloballyByAuthor(long user,
            int offset, int length) throws SQLException
    {
        this.m_listMessagesGloballyByAuthor.clearParameters();
        this.m_listMessagesGloballyByAuthor.setLong(1, user);
        this.m_listMessagesGloballyByAuthor.setInt(2, length);
        this.m_listMessagesGloballyByAuthor.setInt(3, offset);
        
        return innerGlobalSearch(this.m_listMessagesGloballyByAuthor);        
    }

	public GlobalMessageSearchResult[] searchMessagesGlobally(String searchterm,
            int offset, int length) throws SQLException
    {
        this.m_searchMessagesGlobally.clearParameters();
        this.m_searchMessagesGlobally.setString(1, searchterm);
        this.m_searchMessagesGlobally.setInt(2, length);
        this.m_searchMessagesGlobally.setInt(3, offset);
        
        return innerGlobalSearch(this.m_searchMessagesGlobally);        
    }
	
    private GlobalMessageSearchResult[] innerGlobalSearch(PreparedStatement globalSearchStatement) throws SQLException
	{
        ResultSet rs = globalSearchStatement.executeQuery();
        List l = new ArrayList();
        while (rs.next())
        {
            l.add(new GlobalMessageSearchResult(
                    rs.getLong(1), // globalid
                    rs.getInt(2), // localid
                    new NameAssociation(rs.getLong(3), // conferenceid
                            new Name(rs.getString(4), rs.getShort(5))),
                    new NameAssociation(rs.getLong(6), // authorid
                            new Name(rs.getString(7), Visibilities.PUBLIC)), // authorname
                    rs.getString(8), // subject
                    rs.getLong(9)) // reply to
                    );
        }
        GlobalMessageSearchResult[] lmsr = new GlobalMessageSearchResult[l.size()];
        l.toArray(lmsr);
        return lmsr;
	}
    
    public long countMessages()
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
    
    public long[] selectByThread(long threadId, int max)
    throws SQLException, SelectionOverflowException
    {
        ResultSet rs = null;
        long[] buffer = new long[max];
        try
        {
            m_selectByThreadStmt.clearParameters();
            m_selectByThreadStmt.setLong(1, threadId);
            rs = m_selectByThreadStmt.executeQuery();
            int idx = 0;
            while(rs.next())
                buffer[idx++] = rs.getLong(1);
            long[] result = new long[idx];
            System.arraycopy(buffer, 0, result, 0, idx);
            return result;
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new SelectionOverflowException(buffer);
        }
        finally
        {
            if(rs != null)
                rs.close();
        }
    }
}
