/*
 * Created on Oct 12, 2003
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
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.backend.CacheManager;
import nu.rydin.kom.backend.SQLUtils;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageOccurrence;

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
	private final PreparedStatement m_addMessageOccurrenceStmt;
	private final PreparedStatement m_listOccurrencesStmt;
	private final PreparedStatement m_getFirstOccurrenceStmt;
	private final PreparedStatement m_getOccurrenceInConferenceStmt;
	private final PreparedStatement m_getGlobalIdStmt;
	private final PreparedStatement m_getRepliesStmt;
	private final PreparedStatement m_getReplyIdsStmt;
	private final PreparedStatement m_loadOccurrenceStmt;
	private final PreparedStatement m_getVisibleOccurrencesStmt;
	private final PreparedStatement m_getMessageAttributesStmt;
	private final PreparedStatement m_addMessageAttributeStmt;
	private final PreparedStatement m_dropMessageOccurrenceStmt;
	private final PreparedStatement m_countMessageOccurrencesStmt;
	private final PreparedStatement m_dropMessageStmt;
	private final PreparedStatement m_listOccurrencesInConferenceStmt;
	private final PreparedStatement m_getLocalIdsInConfStmt;
	private final PreparedStatement m_dropConferenceStmt;
	private final PreparedStatement m_getGlobalBySubjectStmt;
	private final PreparedStatement m_findLastOccurrenceInConferenceWithAttrStmt;
	private final PreparedStatement m_getLatestMagicMessageStmt;
	private final PreparedStatement m_updateConferenceLasttext;
	
	private final Connection m_conn; 
	
	public static final short ACTION_CREATED 	= 0;
	public static final short ACTION_COPIED	= 1;
	public static final short ACTION_MOVED		= 2;

	public static final short ATTR_NOCOMMENT = 0;
	public static final short ATTR_MOVEDFROM = 1;
	public static final short ATTR_RULEPOST = 2;
	public static final short ATTR_PRESENTATION = 3;
	public static final short ATTR_NOTE = 4;
	
	public MessageManager(Connection conn)
	throws SQLException
	{
		m_conn = conn;
		m_loadMessageStmt = conn.prepareStatement(
			"SELECT created, author, author_name, reply_to, subject, body FROM messages WHERE id = ?");
		m_loadMessageInConfStmt = conn.prepareStatement(
			"SELECT m.id, m.created, m.author, m.author_name, m.reply_to, m.subject, m.body " +			"FROM messages m, messageoccurrences mo " +			"WHERE m.id = mo.message AND mo.conference = ? AND mo.localnum = ?");
		m_loadMessageHeaderStmt = conn.prepareStatement(
			"SELECT created, author, author_name, reply_to, subject FROM messages WHERE id = ?");			
		m_loadMessageOccurrenceStmt = conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name FROM messages " +
			"WHERE conference = ? AND localNum = ?");
		m_getNextNumStmt = conn.prepareStatement(			"SELECT MAX(localnum) FROM messageoccurrences WHERE conference = ? FOR UPDATE");
		m_addMessageStmt = conn.prepareStatement(
			"INSERT INTO messages(created, author, author_name, reply_to, subject, body) " +			"VALUES(?, ?, ?, ?, ?, ?)");
		m_addMessageOccurrenceStmt = conn.prepareStatement(
			"INSERT INTO messageoccurrences(message, action_ts, kind, user, user_name, conference, localnum) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?)");
		m_updateConferenceLasttext = conn.prepareStatement(
			"UPDATE conferences SET lasttext=? WHERE id=?");
		m_listOccurrencesStmt = conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +			"WHERE message = ? ORDER BY action_ts");
		m_getOccurrenceInConferenceStmt = conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +
			"WHERE conference = ? AND message = ?");
			
		// TODO: Probably slow...
		//
		m_getFirstOccurrenceStmt = conn.prepareStatement(
			"SELECT message, action_ts, kind, user, user_name, conference, localnum FROM messageoccurrences " +
			"WHERE message = ? ORDER BY action_ts LIMIT 1");
		m_getGlobalIdStmt = conn.prepareStatement(
			"SELECT message FROM messageoccurrences WHERE conference = ? AND localnum = ?");
		m_getRepliesStmt = conn.prepareStatement(
			"SELECT id, created, author, author_name, reply_to, subject FROM messages " +			"WHERE reply_to = ?");
		m_getReplyIdsStmt = conn.prepareStatement(
			"SELECT id FROM messages WHERE reply_to = ?");
		m_loadOccurrenceStmt = conn.prepareStatement(
			"SELECT message, action_ts, kind, user, conference, localnum FROM messageoccurrences " +
			"WHERE conference = ? AND localnum = ?");
		m_getVisibleOccurrencesStmt = conn.prepareStatement(
			"SELECT o.message, o.action_ts, o.kind, o.user, o.user_name, o.conference, o.localnum " +			"FROM messageoccurrences o, memberships m " +			"WHERE o.conference = m.conference AND m.user = ? AND o.message = ?");
		m_getMessageAttributesStmt = conn.prepareStatement(
		    "SELECT message, kind, created, value " +
		    "FROM messageattributes " +
		    "WHERE message = ? " +
			"order by created asc");
		m_addMessageAttributeStmt = conn.prepareStatement(
		    "INSERT INTO messageattributes (message, kind, created, value) " +
		    "VALUES (?, ?, ?, ?)");
		m_dropMessageOccurrenceStmt = conn.prepareStatement(
			"delete from messageoccurrences " +
			"where localnum = ? and conference = ?");
		m_countMessageOccurrencesStmt = conn.prepareStatement(
			 "select count(*) from messageoccurrences " +
			 "where message = ?");
		m_dropMessageStmt = conn.prepareStatement(
			 "delete from messages " + 
			 "where id = ?");
		m_listOccurrencesInConferenceStmt = conn.prepareStatement(
			 "select mo.localnum, mo.action_ts, m.author_name, m.subject " +
			 "from messages m join messageoccurrences mo " +
			 "on m.id=mo.message " +
			 "where mo.conference = ? " +
			 "order by localnum desc limit ? offset ?;");

		// Ooooh! There's room for optimization here :-) I suggest we optimize by moving to an
		// RDBMS that supports indexed views (not to mention stored procedures..)
		//		
		m_getGlobalBySubjectStmt = conn.prepareStatement(
			 "select Message " + 
			 "from MessageOccurrences as mo " +
			 "join Memberships as ms on mo.Conference = ms.Conference " +
			 "join Messages on mo.Message = Messages.ID " +
			 "where ms.User = ? and Subject = ?");
		
		// To speed things up, a special version to just retrieve the local ID.
		//
		m_getLocalIdsInConfStmt = conn.prepareStatement(
			 "select localnum from messageoccurrences where conference = ?");
		m_dropConferenceStmt = conn.prepareStatement(
			 "delete from conferences where id = ?");
		m_findLastOccurrenceInConferenceWithAttrStmt = conn.prepareStatement(
			 "select localnum " +
			 "from MessageOccurrences as MO " +
			 "join MessageAttributes as MA on MO.Message = MA.Message " +
			 "where MA.kind = ? and MO.conference = ? " +
			 "order by MO.Message desc " +
			 "limit 1 offset 0");
		m_getLatestMagicMessageStmt = conn.prepareStatement(
			 "select MO.Message " +
			 "from MessageOccurrences as MO " +
			 "join MagicConferences as MC on MO.Conference = MC.Conference " +
			 "join MessageAttributes as MA on MO.Message = MA.Message " +
			 "where MO.Conference = ? and MA.Kind = ? and MA.Value = ? " +
			 "order by MO.Message desc " +
			 "limit 1 offset 0");
		
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
		if(m_listOccurrencesInConferenceStmt != null)
			m_listOccurrencesInConferenceStmt.close();
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
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public Message loadMessage(long id)
	throws ObjectNotFoundException, SQLException
	{
		m_loadMessageStmt.clearParameters();
		m_loadMessageStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_loadMessageStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message id=" + id);
			return new Message(
				id,
				rs.getTimestamp(1),		// created
				rs.getLong(2), 			// author
				rs.getString(3),		// Author name
				rs.getLong(4),			// reply to
				rs.getString(5),		// subject
				rs.getString(6),		// body
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
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public Message loadMessage(long conf, int localNum)
	throws ObjectNotFoundException, SQLException
	{
		m_loadMessageInConfStmt.clearParameters();
		m_loadMessageInConfStmt.setLong(1, conf);
		m_loadMessageInConfStmt.setInt(2, localNum);
		ResultSet rs = null;
		try
		{
			rs = m_loadMessageInConfStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message conf=" + conf + " localnum=" + localNum);
			long id = rs.getLong(1);
			return new Message(
				id,
				rs.getTimestamp(2),		// created
				rs.getLong(3), 			// author
				rs.getString(4), 		// author name				
				rs.getLong(5),			// reply to
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
	
	
   /** Loads a message header (i.e. message, less the body) using its id
	* 
	* @param id The id
	* @return The message
	* @throws ObjectNotFoundException
	* @throws SQLException
	*/
   	public MessageHeader loadMessageHeader(long id)
   	throws ObjectNotFoundException, SQLException
   	{
	   m_loadMessageHeaderStmt.clearParameters();
	   m_loadMessageHeaderStmt.setLong(1, id);
	   ResultSet rs = null;
	   try
	   {
		   rs = m_loadMessageHeaderStmt.executeQuery();
		   if(!rs.next())
			   throw new ObjectNotFoundException("Message id=" + id);
		   return new MessageHeader(
		   		id,
			   	rs.getTimestamp(1),		// created
			   	rs.getLong(2), 			// author
			   	rs.getString(3),		// author name	
			   	rs.getLong(4),			// reply to
			   	rs.getString(5)		
			   	);
			
	   }
	   finally
	   {
		   if(rs != null)
			   rs.close();
	   }
   	}
   	
   	public MessageOccurrence createMessageOccurrence(long globalId, short kind, long user, String userName, long conference)
   	throws ObjectNotFoundException, SQLException
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
				throw new ObjectNotFoundException("conference id=" + conference);
			int num = rs.getInt(1) + 1;
			Timestamp now = new Timestamp(System.currentTimeMillis());
	
			// Create message occurrence record
			//
			m_addMessageOccurrenceStmt.clearParameters();
			m_addMessageOccurrenceStmt.setLong(1, globalId);
			m_addMessageOccurrenceStmt.setTimestamp(2, now);
			m_addMessageOccurrenceStmt.setShort(3, kind);
			m_addMessageOccurrenceStmt.setLong(4, user);
			m_addMessageOccurrenceStmt.setString(5, userName);			
			m_addMessageOccurrenceStmt.setLong(6, conference);
			m_addMessageOccurrenceStmt.setInt(7, num);
			m_addMessageOccurrenceStmt.executeUpdate();
			
			// This changes the number of messages in a convference. 
			// 
			CacheManager.instance().getConferenceCache().registerInvalidation(new Long(conference));
			
			// Update conference records "last text" field
			m_updateConferenceLasttext.clearParameters();
			m_updateConferenceLasttext.setTimestamp(1, now);
			m_updateConferenceLasttext.setLong(2,conference);
			m_updateConferenceLasttext.executeUpdate();
			
			// This changes the number of messages in a convference. 
			// 
			CacheManager.instance().getConferenceCache().registerInvalidation(new Long(conference));
			
			return new MessageOccurrence(globalId, now, kind, user, userName, conference, num);
   		}
   		finally
   		{
   			if(rs != null)
   				rs.close();
   		}

   	}
   
   	public MessageOccurrence addMessage(long author, String authorName, long conference, long replyTo, String subject, String body)
   	throws ObjectNotFoundException, SQLException
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
			return this.createMessageOccurrence(id, ACTION_CREATED, author, authorName, conference);
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
   					rs.getLong(4),		// User
					rs.getString(5),	// User name
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
   	 * @throws ObjectNotFoundException
   	 * @throws SQLException
   	 */
   	public MessageOccurrence getFirstOccurrence(long messageId)
   	throws ObjectNotFoundException, SQLException
   	{
		m_getFirstOccurrenceStmt.clearParameters();
		m_getFirstOccurrenceStmt.setLong(1, messageId);
		ResultSet rs = null;
		try
		{
			List list = new ArrayList();
			rs = m_getFirstOccurrenceStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message id=" + messageId);

			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				rs.getLong(4),		// User
				rs.getString(5),	// User name
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
   	 * @throws ObjectNotFoundException
   	 * @throws SQLException
   	 */
   	public long getGlobalMessageId(long conference, int localnum)
   	throws ObjectNotFoundException, SQLException
   	{
		m_getGlobalIdStmt.clearParameters();
		m_getGlobalIdStmt.setLong(1, conference);
		m_getGlobalIdStmt.setInt(2, localnum);
		ResultSet rs = null;
		try
		{
			List list = new ArrayList();
			rs = m_getGlobalIdStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message conference=" + conference + " localnum=" + localnum);
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
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence loadMessageOccurrence(long conference, int localnum)
	throws ObjectNotFoundException, SQLException
	{
		m_loadOccurrenceStmt.clearParameters();
		m_loadOccurrenceStmt.setLong(1, conference);
		m_loadOccurrenceStmt.setInt(2, localnum);
		ResultSet rs = null;
		try
		{
			rs = m_loadOccurrenceStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message conference=" + conference + " localnum=" + localnum);
			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				rs.getLong(4),		// User
				rs.getString(5),	// User name
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
   	 * @throws ObjectNotFoundException If the message didn't exist in this conference
   	 * @throws SQLException
   	 */
	public MessageOccurrence getOccurrenceInConference(long conferenceId, long messageId)
	throws ObjectNotFoundException, SQLException
	{
		m_getOccurrenceInConferenceStmt.clearParameters();
		m_getOccurrenceInConferenceStmt.setLong(1, conferenceId);
		m_getOccurrenceInConferenceStmt.setLong(2, messageId);
		ResultSet rs = null;
		try
		{
			List list = new ArrayList();
			rs = m_getOccurrenceInConferenceStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Message id=" + messageId);

			return new MessageOccurrence(
				rs.getLong(1),		// Global id
				rs.getTimestamp(2),	// Timestamp
				rs.getShort(3),		// Kind,
				rs.getLong(4),		// User
				rs.getString(5),	// User name
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
	 * <br>1: If the message exitst in the current conference, pick that one
	 * <br>2: Otherwise, pick the earliest occurrence 
	 * @param conference The conference id
	 * @param id The message id
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence getMostRelevantOccurrence(long conference, long id)
	throws ObjectNotFoundException, SQLException
	{
		try
		{
			return this.getOccurrenceInConference(conference, id);   	
		}
		catch(ObjectNotFoundException e)
		{
			// Does not exist in this conference. Pick the first occurrence!
			//
			return this.getFirstOccurrence(id);
		}
	}
	
	/**
	 * Returns the visible occurrences of a message, i.e. the occurrences
	 * that appear in conferences the specified user is a member of.
	 * 
	 * @param userId The user
	 * @param globalId Global message id
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public MessageOccurrence[] getVisibleOccurrences(long userId, long globalId)
	throws ObjectNotFoundException, SQLException
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
								rs.getLong(4),		// User
								rs.getString(5),	// User name
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
					 rs.getString(4),		// author name	
					 rs.getLong(5),			// reply to
					 rs.getString(6)		// subject	
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
		List list = new ArrayList();
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
	                    rs.getLong(1),		//message
	                    rs.getShort(2),		//kind
	                    rs.getTimestamp(3),	//created
	                    rs.getString(4)));	//value
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
	
	public MessageAttribute addMessageAttribute(long message, short kind, String value)
	throws ObjectNotFoundException, SQLException
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

		return new MessageAttribute(message, kind, now, value);
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
	throws SQLException, ObjectNotFoundException
	{
		// TODO: When we migrate to stored procedures, this is the first transaction we convert.
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
				
		// Delete
		//
		this.m_dropMessageStmt.clearParameters();
		this.m_dropMessageStmt.setLong(1, globalNum);
		this.m_dropMessageStmt.execute();
	}
	
	public MessageHeader[] getMessageOccurrencesInConference (long conference, int start, int limit)
	throws SQLException
	{
		this.m_listOccurrencesInConferenceStmt.clearParameters();
		this.m_listOccurrencesInConferenceStmt.setLong(1, conference);
		this.m_listOccurrencesInConferenceStmt.setInt(2, limit);
		this.m_listOccurrencesInConferenceStmt.setInt(3, start);
		ResultSet rs = this.m_listOccurrencesInConferenceStmt.executeQuery();
		List l = new ArrayList();
		while (rs.next())
		{
			l.add (new MessageHeader(
					rs.getInt(1),
					rs.getTimestamp(2),
					-1,
					rs.getString(3),
					-1,
					rs.getString(4)));
		}
		MessageHeader[] mh = new MessageHeader[l.size()]; 
		l.toArray(mh);
		return mh;
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
			catch (ObjectNotFoundException f)
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
	
	public long getLatestMagicMessageFor (long conference, long objectId, short kind)
	throws SQLException
	{
		ResultSet rs = null;
		try
		{
			this.m_getLatestMagicMessageStmt.clearParameters();
			this.m_getLatestMagicMessageStmt.setLong(1, conference);
			this.m_getLatestMagicMessageStmt.setShort(2, kind);
			this.m_getLatestMagicMessageStmt.setString(3, new Long(objectId).toString());
			rs = this.m_getLatestMagicMessageStmt.executeQuery();
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
}
