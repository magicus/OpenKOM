/*
 * Created on Oct 27, 2003
 *  
 * Distributed under the GPL license.
 */
package nu.rydin.kom.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import nu.rydin.kom.backend.data.*;
import nu.rydin.kom.constants.*;
import nu.rydin.kom.events.*;
import nu.rydin.kom.exceptions.*;
import nu.rydin.kom.structs.*;
import edu.oswego.cs.dl.util.concurrent.Mutex;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ServerSessionImpl implements ServerSession, EventTarget, EventSource
{	
    private class HeartbeatListenerImpl implements HeartbeatListener
    {
        public void heartbeat()
        {
            ServerSessionImpl.this.m_lastHeartbeat = System.currentTimeMillis();
        }
    }
    
    private static class DeferredEvent
    {
        private final long m_recipient;
        
        private final Event m_event;
        
        public DeferredEvent(long recipient, Event event)
        {
            m_recipient	= recipient;
            m_event 	= event;
        }
        
        public void dispatch(SessionManager manager)
        {
            manager.sendEvent(m_recipient, m_event);
        }
    }
    
	/**
	 * Id of the user of this session
	 */
	
	/**
	 * Current conference id, or -1 if it could not be determined
	 */
	private long m_currentConferenceId;
	
	/**
	 * Time of login
	 */
	private final long m_loginTime;
	
	/**
	 * Id of last message read, or -1 if no message has been read yet.
	 */
	private long m_lastReadMessageId = -1;
	
	/**
	 * A cached list of memberships. Don't forget to reload it
	 * when things change, e.g. the user sign on or off from a conference.
	 */
	private MembershipList m_memberships;
	
	/**
	 * Reply stack.
	 */
	private ReplyStackFrame m_replyStack = null;

	/**
	 * The DataAccess object to use. Reset between transactions
	 */
	private DataAccess m_da;

	/**
	 * The currently active sessions
	 */
	private final SessionManager m_sessions;	

	/**
	 * Has this session been closed?
	 */
	private boolean m_closed = false;	
	
	/**
	 * List of incoming events
	 */
	private final LinkedList m_incomingEvents = new LinkedList();
	
	/**
	 * List of outgoing events
	 */
	private final LinkedList m_outgoingEvents = new LinkedList();
	
	/**
	 * Last suggested command
	 */
	private short m_lastSuggestedCommand = -1;

	/**
	 * Are we valid? If an attempt to gracefully shut down a session fails, 
	 * we may mark a session as invalid, thus prventing any client calls
	 * from getting through.
	 */
	private boolean m_valid = true;
	
	/**
	 * Timestamp of last heartbeat
	 */
	protected long m_lastHeartbeat = System.currentTimeMillis();
	
	/**
	 * Usage statistics
	 */
	private final UserLogItem m_stats;

	/**
	 * Mutex controlling session access
	 */
	private final Mutex m_mutex = new Mutex();
	
	/**
	 * The user currently logged in
	 */
	private long m_userId;
		
	public ServerSessionImpl(DataAccess da, long userId, SessionManager sessions)
	throws UnexpectedException
	{
		try
		{
		    // Set up statistics collection
		    //
		    m_stats = new UserLogItem(userId);
		    m_stats.setLoggedIn(new Timestamp(System.currentTimeMillis()));
		    
			// We'll need a DataAccess while doing this
			//
			m_da = da;
			
			// Set up member variables
			//
			m_userId 			= userId;
			m_loginTime			= System.currentTimeMillis();
			m_sessions			= sessions;
						
			// Load membership infos into cache
			//
			this.reloadMemberships();
				
			// Go to first conference with unread messages
			//
			long firstConf = m_memberships.getNextConferenceWithUnreadMessages(
				userId, da.getConferenceManager());
			this.setCurrentConferenceId(firstConf != -1 ? firstConf : userId);
			
			// Invalidate DataAccess
			//
			m_da = null;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}	
	}
	
	public void acquireMutex()
	throws InterruptedException
	{
	    m_mutex.acquire();
	}
	
	public void releaseMutex()
	{
	    m_mutex.release();
	}
	
	public void setCurrentConferenceId(long id)
	throws SQLException, ObjectNotFoundException
	{
		// We might need to save some membership stuff. 
		//
		if(m_currentConferenceId != id)
			this.leaveConference();
			
		m_currentConferenceId = id;
	}
	
	public ConferenceInfo getCurrentConference()
	{
		try
		{
			return m_da.getConferenceManager().loadConference(m_currentConferenceId);
		}
		catch(ObjectNotFoundException e)
		{
			// TODO: What do we do here? The current conference may have been 
			// deleted!
			//
			throw new RuntimeException(e);
		}
		catch(SQLException e)
		{
			// SQLExceptions here mean that something has gone terribly wrong!
			//
			throw new RuntimeException(e);
		}				
	}
	
	public long getCurrentConferenceId()
	{
		return m_currentConferenceId;
	}
	
	public UserInfo getLoggedInUser()
	{
		try
		{
			return m_da.getUserManager().loadUser(m_userId);
		}
		catch(ObjectNotFoundException e)
		{
			// The logged in user should definately be found!!!
			//
			throw new RuntimeException(e);
		}
		catch(SQLException e)
		{
			// SQLExceptions here mean that something has gone terribly wrong!
			//
			throw new RuntimeException(e);
		}		
	}
	
	public long getLoggedInUserId()
	{
		return m_userId;
	}
	
	public long getLoginTime()
	{
		return m_loginTime;
	}	
	
	public short suggestNextAction()
	throws UnexpectedException
	{
		try
		{
			// Do we have any unread replies?
			//
			if(this.peekReply() != -1)
				return m_lastSuggestedCommand = NEXT_REPLY;
			
			// Do we have any unread messages in current conference?
			//
			try
			{
				if(m_memberships.countUnread(this.getCurrentConferenceId(), m_da.getConferenceManager()) > 0)
				    return m_lastSuggestedCommand = NEXT_MESSAGE;
			}
			catch(ObjectNotFoundException e)
			{
			    // Can't find current conference, it must have been deleted.
			    // Reload memberships and try to go to the next conference.
			    //
			    try
			    {
			        this.reloadMemberships();
			    }
			    catch(ObjectNotFoundException e2)
			    {
			        // TODO: Why would we get here?
			        //
			        throw new UnexpectedException(this.getLoggedInUserId(), e2);
			    }
			}
			
			// Get next conference with unread messages
			//	
			long confId = m_memberships.getNextConferenceWithUnreadMessages(m_currentConferenceId,
					m_da.getConferenceManager());
	
			// Do we have any unread messages?
			//
			if(confId == -1)
				return m_lastSuggestedCommand = NO_ACTION;
				
			// Do we have messages in our current conference?
			//
			if(confId == m_currentConferenceId)
				return m_lastSuggestedCommand = NEXT_MESSAGE;
			return m_lastSuggestedCommand = NEXT_CONFERENCE;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public EventSource getEventSource()
	{
	    return this;
	}
	
	public Envelope readLastMessage()
	throws ObjectNotFoundException, UnexpectedException
	{
		return this.innerReadMessage(this.m_lastReadMessageId);
	}
	
	public Envelope innerReadMessage(long messageId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			long conf = this.getCurrentConferenceId(); 
			Envelope env = this.innerReadMessage(m_da.getMessageManager().getMostRelevantOccurrence(
				conf, messageId));
			return env;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public MessageHeader getLastMessageHeader()
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().loadMessageHeader(this.m_lastReadMessageId);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public Envelope readOriginalMessage()
	throws NoCurrentMessageException, NotAReplyException, ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		try
		{
			if(m_lastReadMessageId == -1)
				throw new NoCurrentMessageException();
				
			// Retrieve last message read and try to locate the message it replies to
			//
			MessageManager mm = m_da.getMessageManager();
			MessageHeader mh = mm.loadMessageHeader(m_lastReadMessageId);
			long replyTo = mh.getReplyTo();
			if(replyTo == -1)
				throw new NotAReplyException();
			
			// Do we have the right to see it?
			//
			this.assertMessageReadPermissions(replyTo);
				
			// We now know the message number. Go ahead and load it
			//
			return this.innerReadMessage(replyTo);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		
	}
	
	public Envelope readGlobalMessage(long globalId)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
	    // Check that we have the right to read this message
	    //
	    this.assertMessageReadPermissions(globalId);

	    // Read it
	    // 
	    return this.innerReadMessage(globalId);
	}
	
		
	public Envelope readLocalMessage(int localnum)
	throws ObjectNotFoundException, UnexpectedException
	{
		return this.readLocalMessage(this.getCurrentConferenceId(), localnum);
	}
	
	
	public Envelope readLocalMessage(long conf, int localnum)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return this.innerReadMessage(m_da.getMessageManager().loadMessageOccurrence(conf, localnum));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public Envelope readNextMessage()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Keep on trying until we've skipped all deleted messages
			//
			for(;;)
			{
				long confId = this.getCurrentConferenceId(); 
				int next = m_memberships.getNextMessageInConference(confId, m_da.getConferenceManager());
				if(next == -1)
					throw new NoMoreMessagesException();
				try
				{
					this.pushReplies(confId, next);
					return this.readLocalMessage(confId, next);
				}
				catch(ObjectNotFoundException e)
				{
					// We hit a deleted message. Mark it as read
					// and continue.
					//
					m_memberships.markAsRead(confId, next);
				}
			}
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public Envelope readNextReply()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException
	{
		try
		{ 
			long next = this.popReply();
			if(next == -1)
				throw new NoMoreMessagesException();
			this.pushReplies(next);				
			return this.innerReadMessage(next);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}			
		
	public long createConference(String fullname, int permissions, int nonmemberPermissions, short visibility, long replyConf)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
	{
		this.checkRights(UserPermissions.CREATE_CONFERENCE);
		try
		{
			long userId = this.getLoggedInUserId();
			long confId = m_da.getConferenceManager().addConference(fullname, userId, permissions, nonmemberPermissions, visibility, replyConf);

			// Add membership for administrator
			//
			m_da.getMembershipManager().signup(userId, confId, 0, ConferencePermissions.ALL_PERMISSIONS, 0);
				
			// Flush membership cache
			//
			this.reloadMemberships();
			return confId;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(AlreadyMemberException e)
		{
			// Already member of a conference we just created? Huh?!
			//
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			// User or newly created conference not found? Huh?!
			//
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public ConferenceListItem[] listConferencesByDate()
	throws UnexpectedException
	{
	    try
	    {
	        return (ConferenceListItem[]) this.censorNames(m_da.getConferenceManager().listByDate());
	    }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}

	/**
	 * Returns a list of conferences, sorted by their normalized name.
	 * @throws UnexpectedException
	 */
	public ConferenceListItem[] listConferencesByName()
	throws UnexpectedException
	{
	    try
	    {
	        return (ConferenceListItem[]) this.censorNames(m_da.getConferenceManager().listByName());
	    }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
		
	public void gotoConference(long id)
	throws UnexpectedException, ObjectNotFoundException, NotMemberException
	{
	    // Going to the current conference?
	    //
	    if(id == this.getCurrentConferenceId())
	        return;
	    
	    // Trying to go to a protected conference?
	    //
	    if(!this.isVisible(id))
	        throw new ObjectNotFoundException("id=" + id);
		try
		{
			// Are we members?
			//
			if(!m_da.getMembershipManager().isMember(this.getLoggedInUserId(), id))
				throw new NotMemberException(new Object[] { this.getCensoredName(id).getName() });
				
			// All set! Go there!
			//
			this.setCurrentConferenceId(id);
			
			// Clear reply stack
			//
			m_replyStack = null;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public long gotoNextConference()
	throws NoMoreNewsException, UnexpectedException
	{
		try
		{
			long nextId = m_memberships.getNextConferenceWithUnreadMessages(
				this.getCurrentConferenceId(), m_da.getConferenceManager());
		
			// Going nowhere or going to the same conference? We're outta here!
			//
			if(nextId == -1 || nextId == this.getCurrentConferenceId())
				throw new NoMoreNewsException();
		
			// Move focus...
			//
			this.setCurrentConferenceId(nextId);
			
			// Clear reply stack
			//
			m_replyStack = null;
			return nextId;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	
	public void createUser(String userid, String password, String fullname, String address1,
		String address2, String address3, String address4, String phoneno1, 
		String phoneno2, String email1, String email2, String url, String charset, long flags1, 
		long flags2, long flags3, long flags4, long rights)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
	{
		this.checkRights(UserPermissions.USER_ADMIN);
		try
		{
			m_da.getUserManager().addUser(userid, password, fullname, address1, address2, address3, address4, 
				phoneno1, phoneno2, email1, email2, url, charset, "sv_SE", flags1, flags2, flags3, flags4, rights);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
		
	public void close()
	throws UnexpectedException
	{
		try
		{
			// Send notification!
			//
			m_sessions.broadcastEvent(new UserAttendanceEvent(m_userId, 
				this.getUser(m_userId).getName(), UserAttendanceEvent.LOGOUT)); 

			// Make sure all message markers are saved
			//
			this.leaveConference();
			
			// Save statistics
			//
			m_stats.setLoggedOut(new Timestamp(System.currentTimeMillis()));
			m_da.getUserLogManager().store(m_stats);
			
			// Unregister and kiss the world goodbye
			//
			m_sessions.unRegisterSession(this);
			m_closed = true;			
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}				
	}	
	
	public void finalize()
	{
		try
		{
			if(!m_closed)
				this.close();
		}
		catch(UnexpectedException e)
		{
			e.printStackTrace();
		}
	}
	
	public int countUnread(long conference)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_memberships.countUnread(conference, m_da.getConferenceManager());
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public NameAssociation[] getAssociationsForPattern(String pattern)
	throws UnexpectedException
	{
		try
		{
			return this.censorNames(m_da.getNameManager().getAssociationsByPattern(pattern));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public NameAssociation[] getAssociationsForPatternAndKind(String pattern, short kind)
	throws UnexpectedException
	{
		try
		{
			return this.censorNames(m_da.getNameManager().getAssociationsByPatternAndKind(pattern, kind));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public MessageOccurrence storeMessage(long conf, UnstoredMessage msg)
	throws AuthorizationException, UnexpectedException
	{
		return this.storeReply(conf, msg, -1L);
	}
	
	public boolean canManipulateObject(long object)
	throws ObjectNotFoundException, UnexpectedException
	{
	    short kind = this.getObjectKind(object);
	    UserInfo ui = this.getLoggedInUser();
	    return ui.getId() == object 
	            || this.hasPermissionInConference(object, ConferencePermissions.ADMIN_PERMISSION)
	            || (kind == NameManager.USER_KIND && ui.hasRights(UserPermissions.USER_ADMIN))
	            || (kind == NameManager.CONFERENCE_KIND && ui.hasRights(UserPermissions.CONFERENCE_ADMIN));
	}
	
	public MessageOccurrence storeReplyToCurrentMessage(UnstoredMessage msg)
	throws NoCurrentMessageException, AuthorizationException, UnexpectedException
	{
		if(m_lastReadMessageId == -1)
			throw new NoCurrentMessageException();
		return this.storeReplyInCurrentConference(msg, m_lastReadMessageId);	
	}

	public MessageOccurrence storePresentation(UnstoredMessage msg, long object)
	throws UnexpectedException, AuthorizationException, ObjectNotFoundException
	{
		try
		{
		    // Permission checks: We have to be presenting ourselves, a conference
		    // we're the administrator of or we have to hold the USER_ADMIN (in case
		    // we're presenting a user) or CONFERENCE_ADMIN (in case of a conference)
		    //
			if (-1L == object) 
			{
				object = this.getLoggedInUserId();
			}
		    short kind = this.getObjectKind(object);
		    if(!this.canManipulateObject(object))
		        throw new AuthorizationException();
			long conference = m_da.getSettingManager().getNumber(
			        kind == NameManager.CONFERENCE_KIND ? SettingKeys.CONFERENCE_PRESENTATIONS
			                : SettingKeys.USER_PRESENTATIONS);
			MessageManager mm = m_da.getMessageManager();  
			MessageOccurrence occ = mm.addMessage(this.getLoggedInUserId(),
				this.getCensoredName(this.getLoggedInUserId()).getName(),
				conference, -1, msg.getSubject(), msg.getBody());
			if((this.getLoggedInUser().getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(conference, occ.getLocalnum());
			this.broadcastEvent(
				new NewMessageEvent(this.getLoggedInUserId(), occ.getConference(), occ.getLocalnum(), 
					occ.getGlobalId()));
			this.m_da.getMessageManager().addMessageAttribute(occ.getGlobalId(), MessageManager.ATTR_PRESENTATION, new Long(object).toString());
			return occ;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	public Envelope readTaggedMessage(short tag, long object)
	throws UnexpectedException, ObjectNotFoundException
	{
		try
		{
			if (-1 == object)
			{
				object = this.getLoggedInUserId();
			}
			return this.innerReadMessage(m_da.getMessageManager().getTaggedMessage(object, tag));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public MessageOccurrence storeReplyInCurrentConference(UnstoredMessage msg, long replyTo)
	throws UnexpectedException, AuthorizationException
	{
		try
		{
			// Determine target conference
			//
			long targetConf;
			if(replyTo != -1)
			{
				MessageOccurrence occ = m_da.getMessageManager().getMostRelevantOccurrence(
					this.getCurrentConferenceId(), replyTo);
				ConferenceInfo ci = m_da.getConferenceManager().loadConference(occ.getConference());
				
				// Does the conference specify a separate reply conference?
				//
				targetConf = ci.getReplyConf();
				if(targetConf == -1)
					targetConf = this.getCurrentConferenceId();
			}
			else
				targetConf = this.getCurrentConferenceId();
			return this.storeReply(targetConf, msg, replyTo);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public MessageOccurrence storeReply(long conference, UnstoredMessage msg, long replyTo)
	throws AuthorizationException, UnexpectedException
	{
		try
		{
			long user = this.getLoggedInUserId();
			
			// Check that we have the permission to write here. If it's a reply, we should
			// try check if we have the right to reply. It's ok to be able to reply without
			// being able to write. Great for conferences where users are only allowed to
			// reply to something posted by a moderator.
			//
			if(replyTo == -1)
				this.assertConferencePermission(conference, ConferencePermissions.WRITE_PERMISSION);
			else
				this.assertConferencePermission(conference, ConferencePermissions.REPLY_PERMISSION);
			
			// If this is a reply, we 
			
			MessageManager mm = m_da.getMessageManager();  
			MessageOccurrence occ = mm.addMessage(user, this.getCensoredName(user).getName(),
				conference, replyTo, msg.getSubject(), msg.getBody());
			if((this.getLoggedInUser().getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(conference, occ.getLocalnum());
			
			// Are we replying to a mail? In that case, store the mail in the recipient's
			// mailbox too!
			//
			if(replyTo != -1 && conference == this.getLoggedInUserId())
			{
				MessageHeader mh = mm.loadMessageHeader(replyTo);
				mm.createMessageOccurrence(occ.getGlobalId(), MessageManager.ACTION_COPIED, 
					this.getLoggedInUserId(), this.getName(this.getLoggedInUserId()).getName(), mh.getAuthor());		
			}
			
			// Notify the rest of the world that there is a new message!
			//
			this.broadcastEvent(
				new NewMessageEvent(this.getLoggedInUserId(), occ.getConference(), occ.getLocalnum(), 
					occ.getGlobalId()));
			m_stats.incNumPosted();
			return occ;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}	
	}
	
	public void storeNoComment(long message)
	throws UnexpectedException, AuthorizationException
	{
		try
		{
			// Determine conference of message
			long targetConf = m_da.getMessageManager().getFirstOccurrence(message).getConference();

			// Check that we have the permission to write there.
			this.assertConferencePermission(targetConf, ConferencePermissions.WRITE_PERMISSION);

			MessageManager mm = m_da.getMessageManager(); 

			mm.addMessageAttribute(message, MessageManager.ATTR_NOCOMMENT, MessageAttribute.constructNoCommentPayload(this.getLoggedInUser()));
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	
	public MessageOccurrence storeReplyToLocal(UnstoredMessage msg, long replyToConfId, int replyToLocalnum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		try
		{
			long replyTo = m_da.getMessageManager().getGlobalMessageId(replyToConfId, replyToLocalnum);
			return this.storeReplyInCurrentConference(msg, replyTo);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}	
	}
	
	public MessageOccurrence storeReplyToLocalInCurrentConference(UnstoredMessage msg, int replyToLocalnum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		return this.storeReplyToLocal(msg, this.getCurrentConferenceId(), replyToLocalnum);
	}
	
	public MessageOccurrence globalToLocalInConference(long conferenceId, long globalNum)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().getMostRelevantOccurrence(conferenceId, globalNum);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public MessageOccurrence globalToLocal(long globalNum)
	throws ObjectNotFoundException, UnexpectedException
	{
		return this.globalToLocalInConference(m_currentConferenceId, globalNum);
	}
	
	public long localToGlobal(long conferenceId, int localnum)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().getGlobalMessageId(conferenceId, localnum);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public long localToGlobalInCurrentConference(int localId)
	throws ObjectNotFoundException, UnexpectedException
	{
	    return localToGlobal(this.getCurrentConferenceId(), localId);
	}

    public long getGlobalMessageId(TextNumber textNumber)
            throws ObjectNotFoundException, UnexpectedException
    {
        if (textNumber.isGlobal())
        {
            return textNumber.getNumber();
        }
        else
        {
            return localToGlobalInCurrentConference((int)textNumber.getNumber());
        }
    }
	public MessageOccurrence storeMail(UnstoredMessage msg, long user, long replyTo)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Store message in recipient's mailbox
			//
			MessageManager mm = m_da.getMessageManager();
			long me = this.getLoggedInUserId();
			MessageOccurrence occ = mm.addMessage(me, 
			    this.getCensoredName(me).getName(), 
				user, replyTo, msg.getSubject(), msg.getBody()); 
			
			// Store a copy in sender's mailbox
			//
			MessageOccurrence copy = mm.createMessageOccurrence(
				occ.getGlobalId(), MessageManager.ACTION_COPIED, me, this.getName(me).getName(), me);
			
			// Mark local copy as read
			//
			if((this.getLoggedInUser().getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(me, copy.getLocalnum());
			m_stats.incNumPosted(); // TODO: Count mail separately?
			return occ;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public void copyMessage(long globalNum, long conferenceId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Check permissions.
			// TODO: Maybe a special copy-permission would be cool?
			//
			long me = this.getLoggedInUserId();
			
			MembershipManager mbr = m_da.getMembershipManager();
			if(!mbr.hasPermission(me, conferenceId, ConferencePermissions.WRITE_PERMISSION))
				throw new AuthorizationException();
			MessageManager mm = m_da.getMessageManager();
			MessageOccurrence occ = mm.createMessageOccurrence(globalNum, MessageManager.ACTION_COPIED, 
				me, this.getName(me).getName(), conferenceId);
			
			// Mark copy as read (unless we're narcissists)
			//
			if((this.getLoggedInUser().getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(conferenceId, occ.getLocalnum());			
			
			// Notify the rest of the world that there is a new message!
			//
			this.broadcastEvent(new NewMessageEvent(me, conferenceId, occ.getLocalnum(), globalNum));
			m_stats.incNumCopies();
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public void deleteMessageInCurrentConference (int localNum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		deleteMessage (localNum, this.getCurrentConferenceId());
	}
	
	public void deleteMessage (int localNum, long conference)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		if (!canDeleteOccurrence(localNum, conference))
		{
			throw new AuthorizationException();
		}
		
		try
		{
			m_da.getMessageManager().dropMessageOccurrence(localNum, conference);
			this.broadcastEvent(new MessageDeletedEvent(this.getLoggedInUserId(), conference));
		}
		catch (SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	private boolean canDeleteOccurrence (int localNum, long conference)
	throws UnexpectedException, ObjectNotFoundException
	{
		try
		{
			return (m_da.getMessageManager().loadMessageOccurrence(conference, localNum).getUser().getId() == this.getLoggedInUserId()) ||
					this.hasPermissionInConference(conference, ConferencePermissions.DELETE_PERMISSION | ConferencePermissions.ADMIN_PERMISSION);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}

	public void moveMessage(int localNum, long destConfId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		this.moveMessage(localNum, this.getCurrentConferenceId(), destConfId);
	}
	
	public void moveMessage(int localNum, long sourceConfId, long destConfId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Now, to move a message you need to be able to remove it from it's original location
			// and place it in the new location.
			//
			long me = this.getLoggedInUserId();
			if (!(this.hasPermissionInConference(destConfId, ConferencePermissions.WRITE_PERMISSION) && 
			      this.canDeleteOccurrence(localNum, sourceConfId)))
			{
				throw new AuthorizationException();
			}
			MessageManager mm = m_da.getMessageManager();
			long globId = this.localToGlobal(sourceConfId, localNum);

			// Start by creating the new occurrence 
			// We must retain the message occurrence, as we'll be using it in the broadcast event.
			//
			MessageOccurrence occ = mm.createMessageOccurrence(globId, 
									MessageManager.ACTION_MOVED, me, this.getName(me).getName(), destConfId);

			// Drop the original occurrence
			//
			mm.dropMessageOccurrence(localNum, sourceConfId);
			
			// Tag the message with an ATTR_MOVEDFROM attribute containing the source conference id.
			//
			mm.addMessageAttribute(globId, MessageManager.ATTR_MOVEDFROM, 
			        this.getCensoredName(sourceConfId).getName());

			// Hello, world!
			//			
			this.broadcastEvent(new NewMessageEvent(me, destConfId, occ.getLocalnum(), occ.getGlobalId()));
			this.broadcastEvent(new MessageDeletedEvent(me, this.getCurrentConferenceId()));
		}
		catch (SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
		
	public long getCurrentMessage()
	throws NoCurrentMessageException
	{
		if(m_lastReadMessageId == -1)
			throw new NoCurrentMessageException();
		return m_lastReadMessageId;
	}
	
	public MessageOccurrence getCurrentMessageOccurrence()
	throws NoCurrentMessageException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().getMostRelevantOccurrence(m_currentConferenceId, 
				this.getCurrentMessage());
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public String signup(long conferenceId)
	throws ObjectNotFoundException, AlreadyMemberException, UnexpectedException, AuthorizationException
	{
		try
		{
			long user = this.getLoggedInUserId();
						
			// Add membership (and grant all permissions)
			//
			m_da.getMembershipManager().signup(user, conferenceId, 0, 0, 0);
			
			// Flush membership cache
			//
			this.reloadMemberships();
			
			// Return full name of conference
			//
			return this.getCensoredName(conferenceId).getName();
		}
		catch (AlreadyMemberException e)
		{
		    throw new AlreadyMemberException(new Object[] { this.getCensoredName(conferenceId).getName() });
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public String signoff (long conferenceId)
	throws ObjectNotFoundException, UnexpectedException, NotMemberException
	{
		long userId = this.getLoggedInUserId();
		try
		{
			if (!m_da.getMembershipManager().isMember(userId, conferenceId))
			{
				throw new NotMemberException(new Object[] { this.getCensoredName(conferenceId).getName() });
			}
			m_da.getMembershipManager().signoff(userId, conferenceId);
			this.reloadMemberships();
			return this.getCensoredName(conferenceId).getName();
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(userId, e);
		}
		// Return full name of conference
		//
	}
	
	public UserInfo getUser(long userId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getUserManager().loadUser(userId);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);		
		}
	}
	
	public ConferenceInfo getConference(long conferenceId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
		    if(!this.isVisible(conferenceId))
		        throw new ObjectNotFoundException("id=" + conferenceId);
			return m_da.getConferenceManager().loadConference(conferenceId);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);		
		}
	}
	
	public NamedObject getNamedObject(long id)
	throws ObjectNotFoundException, UnexpectedException
	{
	    if(!this.isVisible(id))
	        throw new ObjectNotFoundException("id=" + id);
		try
		{
			// First, try users
			//
			return this.getUser(id);
		}
		catch(ObjectNotFoundException e)
		{
			// Not a user. Try conference!
			//
			return this.getConference(id);
		}
	}
	
	public NameAssociation[] listMemberships(long userId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			MembershipInfo[] mi = m_da.getMembershipManager().listMembershipsByUser(userId);
			int top = mi.length;
			NameAssociation[] answer = new NameAssociation[top];
			for(int idx = 0; idx < top; ++idx)
			{	
				long conf = mi[idx].getConference();
				try
				{
					answer[idx] = new NameAssociation(conf, this.getCensoredName(conf));
				}
				catch(ObjectNotFoundException e)
				{
					// Probably delete while we were listing
					//
					answer[idx] = new NameAssociation(conf, 
					        new Name("???", Visibilities.PUBLIC));
				}
			}
			return this.censorNames(answer);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	public MembershipInfo[] listConferenceMembers(long confId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMembershipManager().listMembershipsByConference(confId);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	public Name[] listMemberNamesByConference(long confId)
	throws ObjectNotFoundException, UnexpectedException
	{
		MembershipInfo[] mi = this.listConferenceMembers(confId);
		Name[] s = new Name[mi.length];
		for (int i = 0; i < mi.length; ++i)
			s[i] = this.getName(mi[i].getUser());
		return s;
	}
	
	public Name getName(long id)
	throws ObjectNotFoundException, UnexpectedException
	{
		return this.getCensoredName(id);
	}
	
	public Name[] getNames(long[] ids)
	throws ObjectNotFoundException, UnexpectedException
	{
		int top = ids.length;
		Name[] names = new Name[top];
		for(int idx = 0; idx < top; ++idx)
			names[idx] = this.getName(ids[idx]);
		return names;
	}
	
	public String getDebugString()
	{
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		m_memberships.printDebugInfo(new PrintStream(s));
		return s.toString();	
	}
	
	public void changeUnread(int nUnread)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Update message markers
			//
			ConferenceManager cm = m_da.getConferenceManager();
			ConferenceInfo ci = cm.loadConference(this.getCurrentConferenceId());
			int high = ci.getLastMessage();
			high = Math.max(0, high - nUnread);
			m_memberships.changeRead(ci.getId(), ci.getFirstMessage(), high);
			
			// Discard reply stack
			//
			m_replyStack = null;
			
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	public MembershipListItem[] listNewsFor(long userId)
	throws UnexpectedException
	{
	    try
	    {
	        ConferenceManager cm = m_da.getConferenceManager();
	        MembershipManager mm = m_da.getMembershipManager();
	        MembershipInfo[] m = mm.listMembershipsByUser(userId);
	        MembershipList ml = new MembershipList(m);
	        int top = m.length;
	        List list = new ArrayList (top);
	        for (int i = 0; i < top; ++i)
	        {
	            try
	            {
	                MembershipInfo item = m[i];
	                long confId = item.getConference();
	                if(!this.isVisible(confId))
	                {
	                    continue;
	                }
	                int n = ml.countUnread(confId, cm);
	                if (0 < n)
	                {
	                    list.add(new MembershipListItem(new NameAssociation(confId, this.getCensoredName(confId)), n));
	                }
	            }
	            catch (ObjectNotFoundException e)
	            {
	                // Ignore
	            }
	        }
			MembershipListItem[] answer = new MembershipListItem[list.size()];
			list.toArray(answer);
			return answer;
	    }
	    catch (SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	    catch (ObjectNotFoundException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	}
	
	public MembershipListItem[] listNews()
	throws UnexpectedException
	{
		try
		{
			ConferenceManager cm = m_da.getConferenceManager();
			MembershipInfo[] m = m_memberships.getMemberships();
			int top = m.length;
			List list = new ArrayList(top);
			for(int idx = 0; idx < top; ++idx)
			{
				try
				{
					MembershipInfo each = m[idx];
					long conf = each.getConference();
					
					// Don't include invisible conferences
					//
					if(!this.isVisible(conf))
					    continue;
					int n = m_memberships.countUnread(conf, cm); 
					if(n > 0)
						list.add(new MembershipListItem(new NameAssociation(conf, this.getCensoredName(conf)), n));
				}
				catch(ObjectNotFoundException e)
				{
					// Probably deleted. Just skip!
				}
			}
			MembershipListItem[] answer = new MembershipListItem[list.size()];
			list.toArray(answer);
			return answer;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public UserListItem[] listLoggedInUsers()
	throws UnexpectedException
	{
		ServerSession[] sessions = m_sessions.listSessions();
		int top = sessions.length;
		UserListItem[] answer = new UserListItem[top];
		for(int idx = 0; idx < top; ++idx)
		{
			ServerSession session = sessions[idx];
			long confId = session.getCurrentConferenceId();
			long user = session.getLoggedInUserId();
			String userName = "???";
			boolean inMailbox = false;
			try
			{
				userName = this.getCensoredName(user).getName();
				inMailbox = confId == user;
			}
			catch(ObjectNotFoundException e)
			{
				// User deleted! Strange, but we allow it. User will be displayed as "???"
				//
			}
			Name conferenceName = new Name("???", Visibilities.PUBLIC);
			try
			{
				conferenceName = this.getCensoredName(confId);
				
				// Wipe out protected names
				//
				if(!this.isVisible(confId))
				    conferenceName.hideName();
			}
			catch(ObjectNotFoundException e)
			{
				// Conference deleted. Display as "???"
			}
			answer[idx] = new UserListItem(new NameAssociation(user, new Name(userName, 
			        Visibilities.PUBLIC)), (short) 0, new NameAssociation(confId, conferenceName), inMailbox, session.getLoginTime(),
			        ((ServerSessionImpl) session).getLastHeartbeat()); 
		}
		return answer;
	}
		
	public boolean hasSession (long userId)
	{
		return m_sessions.hasSession(userId);
	}
	
	public synchronized void postEvent(Event e)
	{
		m_incomingEvents.addLast(e);
		this.notify();
	}
	
	public synchronized Event pollEvent(int timeoutMs)
	throws InterruptedException
	{
		if(m_incomingEvents.isEmpty())
			this.wait(timeoutMs);
		if(m_incomingEvents.isEmpty())
			return null;
		return (Event) m_incomingEvents.removeFirst();
	}
		
	public long getLastHeartbeat()
	{
	    return m_lastHeartbeat;
	}
		
	private void sendChatMessageHelper(long userId, String message)
	{
		if (m_sessions.hasSession(userId))
		{
			if(message.substring(0,1).equals("!")) 
			{
				m_sessions.sendEvent(userId, new ChatAnonymousMessageEvent(userId, 
						message.substring(1,message.length())));
			} 
			else 
			{
				m_sessions.sendEvent(userId, new ChatMessageEvent(userId, this.getLoggedInUserId(), 
					this.getLoggedInUser().getName(), message));
			}
		}
		m_stats.incNumChats();
	}
	
	public NameAssociation[] sendMulticastMessage (long destinations[], String message)	
	throws NotLoggedInException, ObjectNotFoundException, AllRecipientsNotReachedException, UnexpectedException
	{
	    try
	    {
		    // Create a message log item. If we have multiple recipients, they all share
		    // the same item.
		    //
	        UserManager um = m_da.getUserManager();
		    MessageLogManager mlm = m_da.getMessageLogManager();
		    long logId = mlm.storeMessage(this.getLoggedInUserId(), this.getLoggedInUser().getName(), message);
		    
		    // Create a link for the logged in user. Used for retrieving messages sent.
		    //
		    mlm.storeMessagePointer(logId, this.getLoggedInUserId(), true, MessageLogKinds.CHAT);
		    
		    ArrayList refused = new ArrayList(destinations.length);
		    boolean explicitToSelf = false;
		    
			// Set to make sure we don't send the message to the same user more than once.
			//
			HashSet s = new HashSet();
			for (int i = 0; i < destinations.length; ++i)
			{
			    long each = destinations[i];
				if (-1 == each)
				{
					break;
				}
				else
				{
					if (NameManager.USER_KIND == m_da.getNameManager().getObjectKind(destinations[i]))
					{
					    // Are we explicitly sending to ourselves`?
					    //
					    UserInfo ui = um.loadUser(each);
					    if(each == this.getLoggedInUserId())
					        explicitToSelf = true;
						if (m_sessions.hasSession(each) && ui.testFlags(0, UserFlags.ALLOW_CHAT_MESSAGES))
						{
							s.add(new Long(each));
						}
						else
						{
							refused.add(new NameAssociation(each, 
							        new Name(ui.getName(), Visibilities.PUBLIC)));
						}
					}
					else // conference
					{
						MembershipInfo[] mi = m_da.getMembershipManager().listMembershipsByConference(each);
						for (int j = 0; j < mi.length; ++j)
						{
							long uid = mi[j].getUser();
							if (m_sessions.hasSession(uid))
							{
							    UserInfo ui = um.loadUser(uid);
							    
							    // Does the receiver accept chat messages
							    //
							    if(ui.testFlags(0, UserFlags.ALLOW_CHAT_MESSAGES))
							        s.add(new Long(uid));
							    else
							        refused.add(new NameAssociation(uid, 
							                new Name(ui.getName(), Visibilities.PUBLIC)));
							}
						}
					}
				}
			} // for
			
			// Remove sending user
			// TODO: This should be a flag condition!
			//
			if (!explicitToSelf)
			{
				s.remove(new Long(this.getLoggedInUserId()));
			}
			
			// Now just send it
			//
			Iterator iter = s.iterator();
			while (iter.hasNext())
			{
			    long user = ((Long)iter.next()).longValue();
				sendChatMessageHelper(user, message);
				
				// Create link from recipient to message
				//
				mlm.storeMessagePointer(logId, user, false, MessageLogKinds.CHAT);
			}
			
			NameAssociation[] answer = new NameAssociation[refused.size()];
			refused.toArray(answer);
			return answer;
	    }
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}		
	}
	
	public int[] verifyChatRecipients(long[] recipients)
	throws ObjectNotFoundException, UnexpectedException
	{
	    NameManager nm = m_da.getNameManager();
	    UserManager um = m_da.getUserManager();
	    try
	    {
	        int top = recipients.length;
	        int[] answer = new int[top];
	        for (int idx = 0; idx < top; idx++)
            {
	            long each = recipients[idx];
	            try
	            {
	                short kind = nm.getObjectKind(each);
	                
	                // Conferences are always considered ok recipients
	                //
	                if(kind == NameManager.CONFERENCE_KIND)
	                    answer[idx] = ChatRecipientStatus.OK_CONFERENCE;
	                else
	                {
	                    // User. Check if logged in.
	                    //
	                    if(!m_sessions.hasSession(each))
	                        answer[idx] = ChatRecipientStatus.NOT_LOGGED_IN;
	                    else
	                    {
	                        // Logged in. Do they receive chat messages?
	                        //
	                        answer[idx] = (um.loadUser(each).getFlags1() & UserFlags.ALLOW_CHAT_MESSAGES) != 0
	                        	? ChatRecipientStatus.OK_USER
	                        	: ChatRecipientStatus.REFUSES_MESSAGES;
	                    }
	                }
	            }
	            catch(ObjectNotFoundException e)
	            {
	                answer[idx] = ChatRecipientStatus.NONEXISTENT;
	            }
            }
	        return answer;
	    }
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}			    
	}
	
	public NameAssociation[] broadcastChatMessage(String message, short kind)
	throws UnexpectedException
	{
	    try
	    {
		    // Create a message log item. If we have multiple recipients, they all share
		    // the same item.
		    //
	        UserManager um = m_da.getUserManager();
		    MessageLogManager mlm = m_da.getMessageLogManager();
		    long logId = mlm.storeMessage(this.getLoggedInUserId(), this.getLoggedInUser().getName(), message);
		    	
			if(message.substring(0,1).equals("!")) 
			{
				m_sessions.broadcastEvent(new BroadcastAnonymousMessageEvent( 
						message.substring(1,message.length()), logId));
			} 
			else 
			{
				m_sessions.broadcastEvent(new BroadcastMessageEvent(this.getLoggedInUserId(), 
					this.getLoggedInUser().getName(), message, logId, kind));
			}
			
			// Log to chat log. This could be done in the event handlers, but
			// that would give all kinds of concurrence and transactional problems,
			// since they are executing asynchronously. 
			// There is, of course, a slight chance that someone logs in out out
			// while doing this and that the log won't be 100% correct, but that's
			// a tradeoff we're willing to make at this point.
			//
			ServerSession[] sessions = m_sessions.listSessions();
			int top = sessions.length;
			ArrayList bounces = new ArrayList(top);
			for(int idx = 0; idx < top; ++idx)
			{
			    long userId = sessions[idx].getLoggedInUserId();
			    try
			    {
			        // Find out if recipients allows broadcasts
			        //
				    UserInfo user = um.loadUser(userId);
				    if((user.getFlags1() & UserFlags.ALLOW_BROADCAST_MESSAGES) != 0)
				        mlm.storeMessagePointer(logId, userId, false, kind);
				    else
				        bounces.add(new NameAssociation(userId, 
				                new Name(user.getName(), Visibilities.PUBLIC)));   
			    }
			    catch(ObjectNotFoundException e)
			    {
			        // Sending to nonexisting user? Probably deleted while
			        // we were iterating. Just skip!
			        //
			    }
			}
			NameAssociation[] answer = new NameAssociation[bounces.size()];
			bounces.toArray(answer);
			m_stats.incNumBroadcasts();
			return answer;
	    }
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}		
	}
	
	public short getObjectKind(long object)
	throws ObjectNotFoundException
	{
		return m_da.getNameManager().getObjectKind(object);
	}
	
	public void updateCharacterset(String charset)
	throws UnexpectedException
	{
		try
		{
			m_da.getUserManager().updateCharacterset(this.getLoggedInUserId(), charset);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public void updateTimeZone(String timeZone)
	throws UnexpectedException
	{
		try
		{
			m_da.getUserManager().changeTimezone(this.getLoggedInUserId(), timeZone);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
		
	public void setConferencePermissions(long conf, long user, int permissions)
	throws UnexpectedException
	{
		try
		{
			MembershipManager mm = m_da.getMembershipManager();
			
			// Get hold of conference permission set and calculate negation mask.
			// Any permission granted by default for the conference, but is denied
			// in user-specific mask should be included in the negation mask.
			//
			int c = this.getCurrentConference().getPermissions();
			int negations = c & ~permissions;  
			mm.updateConferencePermissions(user, conf, permissions, negations);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public void setConferencePermissionsInCurrentConference(long user, int permissions)
	throws UnexpectedException
	{
		this.setConferencePermissions(this.getCurrentConferenceId(), user, permissions);
	}
	
	
	public void revokeConferencePermissions(long conf, long user)
	throws UnexpectedException
	{
		try
		{
			m_da.getMembershipManager().updateConferencePermissions(user, conf, 0, 0xffffffff);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public void revokeConferencePermissionsInCurrentConference(long user)
	throws UnexpectedException
	{
		this.revokeConferencePermissions(this.getCurrentConferenceId(), user);
	}
	
	public ConferencePermission[] listConferencePermissions(long conf)
	throws UnexpectedException
	{
		try
		{
			return m_da.getMembershipManager().listPermissions(conf);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public ConferencePermission[] listConferencePermissionsInCurrentConference()
	throws UnexpectedException
	{
		return this.listConferencePermissions(this.getCurrentConferenceId());
	}
	
	public int getPermissionsInConference(long conferenceId)
	throws ObjectNotFoundException, UnexpectedException
	{
		return this.getUserPermissionsInConference(this.getLoggedInUserId(), conferenceId);
	}
	
	public int getUserPermissionsInConference(long userId, long conferenceId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMembershipManager().getPermissions(userId, conferenceId);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}		
	
	public int getPermissionsInCurrentConference()
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMembershipManager().getPermissions(this.getLoggedInUserId(), 
				this.getCurrentConferenceId());
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public boolean hasPermissionInConference(long conferenceId, int mask)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Do we have the permission to disregard conference permissions?
			//
			if(this.getLoggedInUser().hasRights(UserPermissions.DISREGARD_CONF_PERM))
				return true;				
			return m_da.getMembershipManager().hasPermission(this.getLoggedInUserId(), conferenceId, mask);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public boolean hasPermissionInCurrentConference(int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		return hasPermissionInConference(this.getCurrentConferenceId(), mask);
	}
	
	public void renameObject(long id, String newName)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		try
		{
			if(!this.userCanChangeNameOf(id))
				throw new AuthorizationException();
			m_da.getNameManager().renameObject(id, newName);
			this.sendEvent(id, new ReloadUserProfileEvent(id));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public void changeSuffixOfLoggedInUser(String suffix)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		try
		{
			long me = this.getLoggedInUserId();
			String name = this.getName(me).getName();
			m_da.getNameManager().renameObject(me, NameUtils.addSuffix(name, suffix));
			this.sendEvent(me, new ReloadUserProfileEvent(me));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}			
	}
	
	public void changeSuffixOfUser(long id, String suffix)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		try
		{
			this.checkRights(UserPermissions.CHANGE_ANY_NAME);
			Name name = this.getName(id);
			m_da.getNameManager().renameObject(id, NameUtils.addSuffix(name.getName(), suffix));
			this.sendEvent(id, new ReloadUserProfileEvent(id));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}			
	}
	
	
	public boolean userCanChangeNameOf(long id)
	throws UnexpectedException
	{
		// Do we have sysop rights? Anything goes!
		//
		if(this.getLoggedInUser().hasRights(UserPermissions.CHANGE_ANY_NAME))
			return true;
			
		// Otherwise, we may only change names of conferences we're the admin
		// of.
		//
		try
		{
			return id != this.getLoggedInUserId() && this.hasPermissionInConference(id, ConferencePermissions.ADMIN_PERMISSION);
		}
		catch(ObjectNotFoundException e)
		{
			// Conference not found. It's probably a user then, and we 
			// don't have the right to admin rights for that.
			//
			return false;
		}
	}
	
	public void changePassword(long userId, String oldPassword, String password)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException, BadPasswordException	
	{
		try
		{
			// Check permissions unless we change our own password
			//
			if(userId != this.getLoggedInUserId())
				this.checkRights(UserPermissions.USER_ADMIN);
			else
				m_da.getUserManager().authenticate(this.getUser(userId).getUserid(), oldPassword);
			m_da.getUserManager().changePassword(userId, password);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(AuthenticationException e)
		{
		    throw new BadPasswordException();
		}
	}
	
	public void changeUserFlags(long[] set, long[] reset)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Load current flags.
			//
			UserInfo ui = this.getLoggedInUser();
			long[] oldFlags = ui.getFlags();
			
			// Calculate new flags sets
			//
			long[] flags = new long[UserFlags.NUM_FLAG_WORD];
			for(int idx = 0; idx < UserFlags.NUM_FLAG_WORD; ++idx)
				flags[idx] = (oldFlags[idx] | set[idx]) & ~reset[idx];
			
			// Store in database
			//
			m_da.getUserManager().changeFlags(this.getLoggedInUserId(), flags);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public void changeUserPermissions(long user, long set, long reset)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		try
		{
		    // Check permissions
		    //
		    this.checkRights(UserPermissions.USER_ADMIN);
		    
			// Load current permissions
			//
		    UserManager um = m_da.getUserManager();
			UserInfo ui = um.loadUser(user);
			long oldFlags = ui.getRights();
			
			// Store new permissions in database
			//
			um.changePermissions(user, (oldFlags | set) & ~reset);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public int skipMessagesBySubject (String subject, boolean skipGlobal)
	throws UnexpectedException, ObjectNotFoundException
	{
		long loggedInUser = this.getLoggedInUserId();
		long currentConference = this.getCurrentConference().getId();
		int sillyCounter = 0;
		try
		{
			MessageManager mm = m_da.getMessageManager();
			
			if (skipGlobal)
			{
				long[] globalIds = mm.getMessagesBySubject(subject, loggedInUser);
				for (int i = 0; i < globalIds.length; ++i)
				{
					MessageOccurrence[] mos = mm.getVisibleOccurrences(loggedInUser, globalIds[i]);
					for (int j = 0; j < mos.length; ++j)
					{
						MessageOccurrence mo = mos[j];
						sillyCounter += this.markMessageAsReadEx(mo.getConference(), mo.getLocalnum()) ? 1 : 0;
					}
				}
			}
			else
			{
			    int[] localIds = mm.getLocalMessagesBySubject(subject, currentConference);
			    for (int i = 0; i < localIds.length; ++i)
			    {
			        sillyCounter += this.markMessageAsReadEx(currentConference, localIds[i]) ? 1 : 0;
			    }
			}
			return sillyCounter;
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (loggedInUser, e);
		}
	}
	
	public int skipTree(long root)
	throws UnexpectedException, ObjectNotFoundException
	{
		try
		{
			MessageManager mm = m_da.getMessageManager();
			ArrayList al = new ArrayList();
			
			// Set up our start position (root node in al[0], beginning and end index at zero.
			//			
			int idxBeg = 0;
			int idxEnd = 0;
			int addCount = 0;
			al.add(new Long(root));
			while(true) // until we say otherwise :-)
			{
				// Iterate over the last set of new nodes we got from getReplies (or init).
				//
				for (int i = idxBeg; i <= idxEnd; ++i)
				{
					// Retrieve all children of that particular node.
					//
					MessageHeader[] mh = mm.getReplies(((Long)al.get(i)).longValue()); // LISP
					if (0 == mh.length)
					{
						continue;
					}
					// Add them to the array list, we'll be iterating over them next time around.
					//
					for (int j = 0; j < mh.length; ++j)
					{
						al.add(new Long(mh[j].getId()));
					}
					addCount += mh.length;
				}
				// So, now we've gone through all nodes retrieved in the previous pass. Time to
				// update the indexes or break out of the loop.
				//
				if (0 == addCount)
				{
					// Nothing was added this time around. This means we've penetrated the full
					// depth of the tree and can go on to greater achievements.
					//
					break;
				}
				// Update the indices. The new idxBeg is, of course, the node after the previous
				// idxEnd, just as the new idxEnd is addCount steps after its old value.
				//
				idxBeg = idxEnd + 1;
				idxEnd+= addCount;
				addCount = 0;
			}
			
			// So, we now have a list of all messages in the tree branching out from the current
			// node. Now all we have to do is find all occurrences and mark them as read.
			//
			int sillyCounter = 0;
			Iterator it = al.listIterator();
			while (it.hasNext())
			{
				MessageOccurrence[] mos = mm.getVisibleOccurrences(this.getLoggedInUserId(),
																   ((Long)it.next()).longValue());
				for (int j = 0; j < mos.length; ++j)
				{
					MessageOccurrence mo = mos[j];
					sillyCounter += this.markMessageAsReadEx(mo.getConference(), mo.getLocalnum()) ? 1 : 0;
				}
			}
			
			return sillyCounter;
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
	public MessageLogItem[] getChatMessagesFromLog(int limit)
	throws UnexpectedException
	{
	    try
	    {
	        return m_da.getMessageLogManager().listChatMessages(this.getLoggedInUserId(), limit);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	}
	
	public MessageLogItem[] getBroadcastMessagesFromLog(int limit)
	throws UnexpectedException
	{
	    try
	    {
	        return m_da.getMessageLogManager().listBroadcastMessages(this.getLoggedInUserId(), limit);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	}	
	
	public HeartbeatListener getHeartbeatListener()
	{
	    return new HeartbeatListenerImpl();
	}
	
    public void killSession(long userId)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        // We have to be sysops to do this!
        //
        this.checkRights(UserPermissions.ADMIN);
        
        // SET THEM UP THE BOMB! FOR GREAT FREEDOM!
        //
        try
        {
            m_sessions.killSession(userId);
        }
        catch(InterruptedException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }
    }
    
    public void killAllSessions()
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        // We have to be sysops to do this!
        //
        this.checkRights(UserPermissions.ADMIN);

        // Disallow new logins
        //
        this.prohibitLogin();
        
        // SET UP US THE BIG BOMB!! 
        // Kill all sessions (except this one)
        //
        ServerSession[] sessions = m_sessions.listSessions();
        int top = sessions.length;
        for(int idx = 0; idx < top; ++idx)
        {
            ServerSession each = sessions[idx];
            if(each != this)
                this.killSession(each.getLoggedInUserId());
        }
    }
    
    public void prohibitLogin()
    throws AuthorizationException
    {
        // We have to be sysops to do this!
        //
        this.checkRights(UserPermissions.ADMIN);
        m_sessions.prohibitLogin();
    }

    public void allowLogin()
    throws AuthorizationException
    {
        // We have to be sysops to do this!
        //
        this.checkRights(UserPermissions.ADMIN);
        m_sessions.allowLogin();
    }
    
    public UserLogItem[] listUserLog(Timestamp start, Timestamp end, int offset, int length)
    throws UnexpectedException
    {
	    try
	    {
	        return m_da.getUserLogManager().getByDate(start, end, offset, length);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }        
    }

    public UserLogItem[] listUserLog(long user, Timestamp start, Timestamp end, int offset, int length)
    throws UnexpectedException
    {
	    try
	    {
	        return m_da.getUserLogManager().getByUser(user, start, end, offset, length);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }        
    }
    
    public FileStatus statFile(long parent, String name)
    throws ObjectNotFoundException, UnexpectedException
    {
	    try
	    {
	        return m_da.getFileManager().stat(parent, name);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                
    }
    
    public FileStatus[] listFiles(long parent, String pattern)
    throws UnexpectedException, ObjectNotFoundException
    {
	    if(!this.isVisible(parent))
	        throw new ObjectNotFoundException("id=" + parent);
	    try
	    {
	        return m_da.getFileManager().list(parent, pattern);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                
        
    }
    
    public String readFile(long parent, String name)
    throws ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
	    try
	    {
	        FileManager fm = m_da.getFileManager(); 
	        if(!((this.getLoggedInUser().getRights() & UserPermissions.ADMIN) != 0)
	            	&& !hasPermissionInConference(parent, ConferencePermissions.READ_PERMISSION)
	                && (fm.stat(parent, name).getProtection() & FileProtection.ALLOW_READ) == 0)
	            throw new AuthorizationException();
	        return fm.read(parent, name);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                        
    }
    
    public void storeFile(long parent, String name, String content, int permissions)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
	    try
	    {
	        FileManager fm = m_da.getFileManager();
	        boolean isSysop = (this.getLoggedInUser().getRights() & UserPermissions.ADMIN) != 0;
	        boolean hasParentRights = isSysop || hasPermissionInConference(parent, ConferencePermissions.WRITE_PERMISSION); 
	        try
	        {
	            FileStatus fs = fm.stat(parent, name);
	            if(!hasParentRights && (fs.getProtection() & FileProtection.ALLOW_WRITE) == 0)
	            	throw new AuthorizationException();
	        }
	        catch(ObjectNotFoundException e)
	        {
	            // New file. Just check parent permission
	            //
	            if(!hasParentRights)
	                throw new AuthorizationException();
	        }
	        fm.store(parent, name, content);
	        fm.chmod(parent, name, permissions);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                
        
    }
    
    public void deleteFile(long parent, String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
	    try
	    {
	        this.assertConferencePermission(parent, ConferencePermissions.WRITE_PERMISSION);
	        m_da.getFileManager().delete(parent, name);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                   
    }    
    
    public String readSystemFile(String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
	    try
	    {
	        return this.readFile(m_da.getUserManager().getSysopId(), name);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                   
    }
    
    public void storeSystemFile(String name, String content)
    throws AuthorizationException, UnexpectedException
    {
        try
        {
            long parent = m_da.getUserManager().getSysopId();
            this.storeFile(parent, name, content, FileProtection.ALLOW_READ);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                        
	    catch(ObjectNotFoundException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
    }
    
    public void deleteSystemFile(String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
	    try
	    {
	        this.deleteFile(m_da.getUserManager().getSysopId(), name);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
    }

	protected void markAsInvalid()
	{
		m_valid = false;
	}
	
	protected boolean isValid()
	{
		return m_valid;
	}
	
	protected void leaveConference()
	throws SQLException
	{
		// Save message markers
		//
		m_memberships.save(m_userId, m_da.getMembershipManager());
	}
	
	protected void reloadMemberships()
	throws ObjectNotFoundException, SQLException
	{
		// Load membership infos into cache
		//
		if(m_memberships != null)
			m_memberships.save(m_userId, m_da.getMembershipManager());
		m_memberships = new MembershipList(m_da.getMembershipManager().listMembershipsByUser(m_userId));
	}
	
	protected boolean markMessageAsReadEx(long conference, int localnum)
	throws ObjectNotFoundException
	{
		return m_memberships.markAsReadEx(conference, localnum);
	}
	
	protected void markMessageAsRead(long conference, int localnum)
	throws SQLException
	{
		try
		{
			// Mark it as read in the membership list
			//
			m_memberships.markAsRead(conference, localnum);
			
			// Update last read message
			//
			m_lastReadMessageId = m_da.getMessageManager().getGlobalMessageId(conference, localnum);
			
		}
		catch(ObjectNotFoundException e)
		{
			// The text was probably deleted. Do nothing.
			//
		}
	}
	
	public void assertConferencePermission(long conferenceId, int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		if(!this.hasPermissionInConference(conferenceId, mask))
			throw new AuthorizationException();
	}
		
	protected void pushReplies(long conference, int localnum)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			this.pushReplies(m_da.getMessageManager().getGlobalMessageId(conference, localnum));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);	
		}
	}
	
	protected void pushReplies(long messageId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			long[] replies = m_da.getMessageManager().getReplyIds(messageId);
			if(replies.length > 0)
				m_replyStack = new ReplyStackFrame(replies, m_replyStack);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);	
		}
	}
		
	protected Envelope innerReadMessage(MessageOccurrence primaryOcc)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Resolve reply to (if any)
			//
			long conf = this.getCurrentConferenceId();
			MessageManager mm = m_da.getMessageManager();
			ConferenceManager cm = m_da.getConferenceManager();
			Message message = mm.loadMessage(primaryOcc.getConference(), primaryOcc.getLocalnum());
			long replyToId = message.getReplyTo();
			Envelope.RelatedMessage replyTo = null;
			if(replyToId > 0)
			{
				// This is a reply. Fill in info.
				//
				MessageOccurrence occ = mm.getMostRelevantOccurrence(conf, replyToId);
				MessageHeader replyToMh = mm.loadMessageHeader(replyToId);
				replyTo = new Envelope.RelatedMessage(occ, replyToMh.getAuthor(), replyToMh.getAuthorName(), 
					occ.getConference(), this.getCensoredName(occ.getConference()), occ.getConference() == conf);
			} 
				
			// Create receiver list
			//
			MessageOccurrence[] occ = message.getOccurrences();
			int top = occ.length;
			NameAssociation[] receivers = new NameAssociation[top]; 
			for(int idx = 0; idx < top; ++idx)
				receivers[idx] = new NameAssociation(occ[idx].getConference(), this.getCensoredName(occ[idx].getConference()));  
			
			// Create attributes list
			//
			MessageAttribute[] attr = mm.getMessageAttributes(message.getId());
			
			// Create list of replies
			//
			MessageHeader[] replyHeaders = mm.getReplies(message.getId());
			top = replyHeaders.length;
			ArrayList list = new ArrayList(top);
			
			for(int idx = 0; idx < top; ++idx)
			{
				MessageHeader each = replyHeaders[idx];
				MessageOccurrence replyOcc = mm.getMostRelevantOccurrence(conf, each.getId()); 
				
				// Don't show personal replies
				//
				if(cm.isMailbox(replyOcc.getConference()))
				    continue;
				list.add(new Envelope.RelatedMessage(replyOcc, each.getAuthor(), each.getAuthorName(),
				        replyOcc.getConference(), this.getCensoredName(replyOcc.getConference()), replyOcc.getConference() == conf));  
			}
			Envelope.RelatedMessage[] replies = new Envelope.RelatedMessage[list.size()];
			list.toArray(replies);
			
			// Done assembling envelope. Now, mark the message as read in all
			// conferences where it appears and we are members.
			//
			MessageOccurrence[] occs = mm.getVisibleOccurrences(this.getLoggedInUserId(), 
				primaryOcc.getGlobalId());
			top = occs.length;
			for(int idx = 0; idx < top; ++idx)
			{
				MessageOccurrence each = occs[idx];
				this.markMessageAsRead(each.getConference(), each.getLocalnum());
			}
			
			// Create Envelope and return
			//
			m_stats.incNumRead();
			return new Envelope(message, primaryOcc, replyTo, receivers, occ, attr, replies);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	/**
	 * Checks that at least one occurrence of the message is readable to
	 * the logged in user.
	 */
	protected void assertMessageReadPermissions(long globalId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
	    if(!hasMessageReadPermissions(globalId))
	        throw new AuthorizationException();
	}

	/**
	 * Checks that at least one occurrence of the message is readable to
	 * the logged in user.
	 */
	protected boolean hasMessageReadPermissions(long globalId)
	throws ObjectNotFoundException, UnexpectedException
	{
	    try
	    {
	        // Get all occurrences
	        //
	        MessageManager mm = m_da.getMessageManager();
	        MembershipManager mbrMgr = m_da.getMembershipManager();
	        MessageOccurrence[] occs = mm.getOccurrences(globalId);
	        long me = this.getLoggedInUserId();
	        
	        // Check whether we have read access in at least one of them
	        //
	        int top = occs.length;
	        for(int idx = 0; idx < top; ++idx)
	        {
	            // Get out of here as soon as we have read access in a conference
	            // where this text occurrs.
	            //
                if(mbrMgr.hasPermission(me, occs[idx].getConference(), ConferencePermissions.READ_PERMISSION))
                    return true;
	        }
	        
	        // We didn't find an occurrence in a conference we have access to?
	        // We're not allowed to see it, then!
	        //
	        return false;
	    }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	protected long popReply()
	throws ObjectNotFoundException, UnexpectedException, SQLException
	{
		return this.peekReply() != -1 ? m_replyStack.pop() : -1;
	}
	
	protected long peekReply()
	throws SQLException, UnexpectedException
	{
		long reply = -1;

		// Do we have anything at all in the stack?
		//
		if(m_replyStack == null)
			return -1;


		// Loop until we have an unread reply
		//
		for(;; m_replyStack.pop())
		{

			// Frame exhausted? Try next!
			//
			if(!m_replyStack.hasMore())
				m_replyStack = m_replyStack.next();
			if(m_replyStack == null)
				return -1;
				
			// Fetch next reply global id adn translate into local occurrence
			//
			reply = m_replyStack.peek();
			try
			{
				MessageOccurrence occ = m_da.getMessageManager().getMostRelevantOccurrence(m_currentConferenceId, reply);
				
				// Check that we have permission to see this one
				//
				if(!this.hasMessageReadPermissions(reply))
				    continue;
				
				// If it's unread, we're done
				//
				if(m_memberships.isUnread(occ.getConference(), occ.getLocalnum()))
					break;
			}
			catch(ObjectNotFoundException e)
			{
				// Not found. Probably deleted, so just skip it!
			}				
		}
		return reply;
	}
	
	
	protected void setDataAccess(DataAccess da)
	{
		m_da = da;	
	}
	
	public void checkRights(long mask)
	throws AuthorizationException
	{
		if(!this.getLoggedInUser().hasRights(mask))
			throw new AuthorizationException(); 
	}	
	
	/**
	 * Sends an event to a specified user
	 * 
	 * @param userId
	 * @param e The event
	 */
	protected void sendEvent(long userId, Event e)
	{
	    // Push events onto a transaction-local queue that will
	    // be flushed once the current transaction is committed.
	    //
	    synchronized(m_outgoingEvents)
	    {
	        m_outgoingEvents.add(new DeferredEvent(userId, e));
	    }
	}
	
	protected void flushEvents()
	{
	    synchronized(m_outgoingEvents)
	    {
	        for(Iterator itor = m_outgoingEvents.iterator(); itor.hasNext();)
	            ((DeferredEvent) itor.next()).dispatch(m_sessions);
	        m_outgoingEvents.clear();
	    }
	}
	
	protected void discardEvents()
	{
	    m_outgoingEvents.clear();
	}
	
	/**
	 * Broadcasts an event to all active users
	 * 
	 * @param e The event
	 */
	protected void broadcastEvent(Event e)
	{
		m_sessions.broadcastEvent(e);
	}
	
	// Implementation of EventTarget
	//
	public void onEvent(Event e)
	{
		// Catch-all method for events without a dedicated methods.
		// Just stuff it in the queue
		//
		this.postEvent(e);
	}
	
	public void onEvent(ChatMessageEvent e)
	{
	    if(this.testUserFlagInEventHandler(this.getLoggedInUserId(), 0, UserFlags.ALLOW_CHAT_MESSAGES))
	        this.postEvent(e);
	}

	public void onEvent(ChatAnonymousMessageEvent e)
	{
	    if(this.testUserFlagInEventHandler(this.getLoggedInUserId(), 0, UserFlags.ALLOW_CHAT_MESSAGES))
	        this.postEvent(e);
	}
	
	public void onEvent(BroadcastMessageEvent e)
	{
	    if(this.testUserFlagInEventHandler(this.getLoggedInUserId(), 0, UserFlags.ALLOW_BROADCAST_MESSAGES))
	        this.postEvent(e);
	}
	
	public void onEvent(BroadcastAnonymousMessageEvent e)
	{
	    if(this.testUserFlagInEventHandler(this.getLoggedInUserId(), 0, UserFlags.ALLOW_BROADCAST_MESSAGES))
	        this.postEvent(e);
	}

	public void onEvent(UserAttendanceEvent e)
	{
	    if(this.testUserFlagInEventHandler(this.getLoggedInUserId(), 0, UserFlags.SHOW_ATTENDANCE_MESSAGES))
	        this.postEvent(e);
	}
	
	public void onEvent(ReloadUserProfileEvent e)
	{
		// Just post it!
		// 
		this.postEvent(e);
	}
	
	public void onEvent(MessageDeletedEvent e)
	{
		long conf = e.getConference();
		try
		{
			m_memberships.get(conf);
			this.postEvent(e);
		}
		catch(ObjectNotFoundException ex)
		{
			// Not member. No need to notify client
			//
			return;
		}
	}
	
	public synchronized void onEvent(NewMessageEvent e)
	{
		// Already have unread messages? No need to send event! 
		// 
		if(m_lastSuggestedCommand == NEXT_MESSAGE || m_lastSuggestedCommand == NEXT_REPLY)
			return;
			
		// Don't send notification unless we're members.
		//
		long conf = e.getConference();
		try
		{
		    // Last suggestest command was NEXT_CONFERENCE and message 
		    // was not posted in current conference? No need to pass it on.
		    //
		    if(m_lastSuggestedCommand == NEXT_CONFERENCE && conf != this.getCurrentConferenceId())
		        return;
		    
		    // Try to load the membership to see if we should care.
		    //
			m_memberships.get(conf);
			this.postEvent(e);
		}
		catch(ObjectNotFoundException ex)
		{
			// Not a member. No need to notify client
			//
			return;
		}
	}
	
	protected boolean testUserFlagInEventHandler(long user, int flagword, long mask)
	{
	    // Borrow a UserManager from the pool
	    //
	    DataAccessPool pool = DataAccessPool.instance();
	    DataAccess da = null;
	    try
	    {
	        da = pool.getDataAccess();
	  
	        // Load user
	        //
	        UserInfo ui = da.getUserManager().loadUser(user);
	        return ui.testFlags(flagword, mask);
	    }
	    catch(Exception e)
	    {
	        // We're called from an event handler, so what can we do?
	        //
	        e.printStackTrace();
	        return false;
	    }
	    finally
	    {
	        if(da != null)
	            pool.returnDataAccess(da);
	    }
	}

	public LocalMessageSearchResult[] listAllMessagesLocally(long conference, int start, int length)
	throws UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().listAllMessagesLocally(conference, start, length);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
		
	}

	public LocalMessageSearchResult[] listMessagesLocallyByAuthor(long conference, long user, int offset, int length)
	throws UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().listMessagesLocallyByAuthor(conference, user, offset, length);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
    public GlobalMessageSearchResult[] listMessagesGloballyByAuthor(long user, int offset, int length) 
    throws UnexpectedException
    {
		try
		{
			return this.censorMessages(m_da.getMessageManager().listMessagesGloballyByAuthor(user, offset, length));
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
		catch (ObjectNotFoundException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}		
    }
	
	public MessageHeader getMessageHeader(long globalId)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
	    this.assertMessageReadPermissions(globalId);
	    try
	    {
	        return m_da.getMessageManager().loadMessageHeader(globalId);
	    }
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
	public MessageHeader getMessageHeaderInConference(long conference, int localNum)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
	    return this.getMessageHeader(this.localToGlobal(conference, localNum));
	}
	
	public MessageHeader getMessageHeaderInCurrentConference(int localNum)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
	    return this.getMessageHeaderInConference(this.getCurrentConferenceId(), localNum);
	}

	public void deleteConference (long conference)
	throws AuthorizationException, UnexpectedException
	{
		try
		{
		    // Do we have the right to do this?
		    //
		    if(!(this.hasPermissionInConference(conference, ConferencePermissions.ADMIN_PERMISSION)
		       || this.getLoggedInUser().hasRights(UserPermissions.CONFERENCE_ADMIN)))
		       throw new AuthorizationException();
		    
		    // So far so, so good. Go ahead and delete!
		    //
			m_da.getMessageManager().deleteConference(conference);
			m_da.getNameManager().dropNamedObject(conference);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
		    throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
	public Envelope getLastRulePostingInConference (long conference)
	throws ObjectNotFoundException, NoRulesException, UnexpectedException
	{
		try
		{
			return this.readLocalMessage(conference, m_da.getMessageManager().findLastOccurrenceInConferenceWithAttrStmt(MessageManager.ATTR_RULEPOST, conference));
		}
		catch (SQLException e)
		{
			throw new NoRulesException();
		}
	}
	
	public Envelope getLastRulePosting()
	throws ObjectNotFoundException, NoRulesException, UnexpectedException
	{
		return getLastRulePostingInConference (this.m_currentConferenceId);
	}

	public MessageOccurrence storeRulePosting(UnstoredMessage msg)
	throws AuthorizationException, UnexpectedException, ObjectNotFoundException
	{
		try
		{
			MessageOccurrence mo = this.storeReplyInCurrentConference(msg, -1L);
			m_da.getMessageManager().addMessageAttribute(mo.getGlobalId(), MessageManager.ATTR_RULEPOST, null);
			return mo;
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
		
    public void changeSetting(String name, String value)
    throws AuthorizationException, UnexpectedException
    {
		try
		{
		    this.checkRights(UserPermissions.ADMIN);
			m_da.getSettingManager().changeSetting(name, value, 0);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}        
    }

    public void changeSetting(String name, long value)
    throws AuthorizationException, UnexpectedException
    {
		try
		{
		    this.checkRights(UserPermissions.ADMIN);
			m_da.getSettingManager().changeSetting(name, null, value);
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}        
    }
	
	public void updateLastlogin()
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{	
			m_da.getUserManager().updateLastlogin(this.getLoggedInUserId());
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public SystemInformation getSystemInformation()
	throws UnexpectedException
	{
	    try
	    {
		    CacheManager cm = CacheManager.instance();
		    return new SystemInformation(m_sessions.canLogin(), cm.getNameCache().getStatistics(),
		            cm.getUserCache().getStatistics(), cm.getConferenceCache().getStatistics(),
		            m_da.getUserManager().countUsers(), m_da.getConferenceManager().countCounferences(),
		            m_da.getMessageManager().countMessages());
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
	}

    public GlobalMessageSearchResult[] searchMessagesGlobally(String searchterm, int offset, int length)
	throws UnexpectedException
    {
		try
		{
			return this.censorMessages(m_da.getMessageManager().searchMessagesGlobally(searchterm, offset, length));
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
		catch (ObjectNotFoundException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}		
    }
	
    public LocalMessageSearchResult[] searchMessagesLocally(long conference, String searchterm, int offset, int length)
    throws UnexpectedException
	{
		try
		{
    		return m_da.getMessageManager().searchMessagesLocally(conference, searchterm, offset, length);
		}
    	catch (SQLException e)
		{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

    public LocalMessageSearchResult[] grepMessagesLocally(long conference, String searchterm, int offset, int length)
    throws UnexpectedException
	{
    	try
		{
    		return m_da.getMessageManager().grepMessagesLocally(conference, searchterm, offset, length);
		}
    	catch (SQLException e)
		{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
    
    protected boolean isVisibleFor(long conferenceId, long userId)
    throws ObjectNotFoundException, UnexpectedException
    {
    	try
		{
    		return m_memberships.getOrNull(conferenceId) != null 
    			|| m_da.getNameManager().getNameById(conferenceId).getVisibility() == Visibilities.PUBLIC
    			|| m_da.getMembershipManager().hasPermission(userId, conferenceId, 
    			        ConferencePermissions.READ_PERMISSION);
		}
    	catch (SQLException e)
		{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
    }
    
    protected boolean isVisible(long conferenceId)
    throws ObjectNotFoundException, UnexpectedException
    {
        return isVisibleFor (conferenceId, this.getLoggedInUserId());
    }
    
    protected Name getCensoredName(long id)
    throws ObjectNotFoundException, UnexpectedException
    {
    	try
		{
    	    return this.isVisible(id)
    	    	? m_da.getNameManager().getNameById(id)
    	    	: new Name("", Visibilities.PROTECTED);
    	    
    	}
    	catch (SQLException e)
		{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
    }
    
    protected GlobalMessageSearchResult[] censorMessages(GlobalMessageSearchResult[] messages)
    throws ObjectNotFoundException, UnexpectedException
    {
        ArrayList list = new ArrayList(messages.length);
        int top = messages.length;
        for(int idx = 0; idx < top; ++idx)
        {
            GlobalMessageSearchResult each = messages[idx];
            if(this.hasMessageReadPermissions(each.getGlobalId()))
                list.add(each);
        }
        GlobalMessageSearchResult[] answer = new GlobalMessageSearchResult[list.size()];
        list.toArray(answer);
        return answer;
    }
    
    protected NameAssociation[] censorNames(NameAssociation[] names)
    throws ObjectNotFoundException, UnexpectedException
    {
        // We're lazy: Assuume nothing gets wiped out and
        // reallocate array only if we need to.
        //
        ArrayList list = null;
        int top = names.length;
        for (int idx = 0; idx < top; idx++)
        {
            NameAssociation association = names[idx];
            if(!this.isVisible(association.getId()))
            {
                // Is this the first invisible name we see? 
                // Allocate a list!
                //
                if(list == null)
                {
                    list = new ArrayList(top - 1);
                    for(int idx2 = 0; idx2 < idx; ++idx2)
                        list.add(names[idx2]);
                }
            }
            else
            {
                if(list != null)
                    list.add(association);
            }
        }
        
        // So? Was the list untouched? Good in that case!
        //
        if(list == null)
            return names;
        
        // Create array of the correct derived type.
        // (Non object-jocks are not supposed to understand this)
        //
        names = (NameAssociation[]) Array.newInstance(names.getClass().getComponentType(), list.size());
        list.toArray(names);
        return names;
    }
}
