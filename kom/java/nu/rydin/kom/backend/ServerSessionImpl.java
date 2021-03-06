/*
 * Created on Oct 27, 2003
 *  
 * Distributed under the GPL license.
 */
package nu.rydin.kom.backend;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.FileManager;
import nu.rydin.kom.backend.data.MembershipManager;
import nu.rydin.kom.backend.data.MessageLogManager;
import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.data.RelationshipManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.constants.ChatRecipientStatus;
import nu.rydin.kom.constants.CommandSuggestions;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.FileProtection;
import nu.rydin.kom.constants.FilterFlags;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.constants.RelationshipKinds;
import nu.rydin.kom.constants.SettingKeys;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.events.BroadcastAnonymousMessageEvent;
import nu.rydin.kom.events.BroadcastMessageEvent;
import nu.rydin.kom.events.ChatAnonymousMessageEvent;
import nu.rydin.kom.events.ChatMessageEvent;
import nu.rydin.kom.events.DetachRequestEvent;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.MessageDeletedEvent;
import nu.rydin.kom.events.NewMessageEvent;
import nu.rydin.kom.events.ReloadUserProfileEvent;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.exceptions.AllRecipientsNotReachedException;
import nu.rydin.kom.exceptions.AlreadyMemberException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.BadPasswordException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.EmailRecipientNotRecognizedException;
import nu.rydin.kom.exceptions.EmailSenderNotRecognizedException;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.NoMoreMessagesException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
import nu.rydin.kom.exceptions.NoRulesException;
import nu.rydin.kom.exceptions.NotAReplyException;
import nu.rydin.kom.exceptions.NotLoggedInException;
import nu.rydin.kom.exceptions.NotMemberException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.OriginalsNotAllowedException;
import nu.rydin.kom.exceptions.RepliesNotAllowedException;
import nu.rydin.kom.exceptions.SelectionOverflowException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.structs.Bookmark;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferenceListItem;
import nu.rydin.kom.structs.ConferencePermission;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.FileStatus;
import nu.rydin.kom.structs.GlobalMessageSearchResult;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MembershipInfo;
import nu.rydin.kom.structs.MembershipListItem;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.NamedObject;
import nu.rydin.kom.structs.ReadLogItem;
import nu.rydin.kom.structs.Relationship;
import nu.rydin.kom.structs.SessionState;
import nu.rydin.kom.structs.SystemInformation;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.structs.UserLogItem;
import nu.rydin.kom.utils.FileUtils;
import nu.rydin.kom.utils.FilterUtils;
import nu.rydin.kom.utils.Logger;
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
            ServerSessionImpl that = ServerSessionImpl.this;
            long now = System.currentTimeMillis();
            if(now - that.m_lastHeartbeat > ServerSettings.getIdleNotificationThreashold())
            {
                UserInfo ui = that.getLoggedInUserInEventHandler();
                if(ui != null)
                    that.m_sessions.broadcastEvent(new UserAttendanceEvent(ui.getId(), ui.getName(), UserAttendanceEvent.AWOKE));
            }
            that.m_lastHeartbeat = System.currentTimeMillis();
        }
    }
    
    private static abstract class DeferredEvent
    {
        protected final Event m_event;
        
        public DeferredEvent(Event event)
        {
            m_event 	= event;
        }
        
        public abstract void dispatch(SessionManager manager);
    }
    
    private static class DeferredSend extends DeferredEvent
    {
        private final long m_recipient;
        
        public DeferredSend(long recipient, Event event)
        {
            super(event);
            m_recipient	= recipient;
        }
        
        public void dispatch(SessionManager manager)
        {
            manager.sendEvent(m_recipient, m_event);
        }
    }
    
    private static class DeferredBroadcast extends DeferredEvent
    {
        public DeferredBroadcast(Event event)
        {
            super(event);
        }
        
        public void dispatch(SessionManager manager)
        {
            manager.broadcastEvent(m_event);
        }
    }
 	
    private static interface MessageOperation
    {
        public void perform(long messageId)
        throws ObjectNotFoundException, UnexpectedException;
    }
    
	private class MarkAsUnreadOperation implements MessageOperation
	{
		public void perform(long messageId)
		throws ObjectNotFoundException, UnexpectedException
		{
		    ServerSessionImpl.this.innerMarkAsUnreadAtLogout(messageId);
		}
	}
    
    private class MarkAsReadOperation implements MessageOperation 
    {
    	public void perform(long messageId)
    	throws ObjectNotFoundException, UnexpectedException
    	{
    	    ServerSessionImpl that = ServerSessionImpl.this;
    	    try
    	    {
        	    MessageManager mm = that.m_da.getMessageManager();
				MessageOccurrence[] mos = mm.getVisibleOccurrences(that.getLoggedInUserId(),
						   messageId);
				for (int idx = 0; idx < mos.length; ++idx)
				{
				    MessageOccurrence mo = mos[idx];
				    that.markMessageAsReadEx(mo.getConference(), mo.getLocalnum());
				}
    	    }
    		catch (SQLException e)
    		{
    			throw new UnexpectedException (that.getLoggedInUserId(), e);
    		}
        }
    }
    
    private class SortableMembershipInfo implements Comparable<SortableMembershipInfo>
    {
        private final MembershipInfo m_mi;
        private final long m_parentCount;
        
        public SortableMembershipInfo (MembershipInfo mi, long parentCount)
        {
            m_mi = mi;
            m_parentCount = parentCount;
        }
        
        public MembershipInfo getMembershipInfo()
        {
            return m_mi;
        }
        
        protected long getId()
        {
            return m_mi.getConference();
        }
        
        protected long getPriority()
        {
            return m_mi.getPriority();
        }
        
        protected long getParentCount()
        {
            return m_parentCount;
        }
        
        public int compareTo (SortableMembershipInfo that)
        {
            return this.getParentCount() < that.getParentCount() ? -1 : 
                   (this.getParentCount() > that.getParentCount() ? 1 :
                   (this.getPriority() < that.getPriority() ? -1 : 1)); 
        }
    }
    
    private static final int MAX_SELECTION = 1000;

	/**
	 * Current conference id, or -1 if it could not be determined
	 */
	private long m_currentConferenceId;
	
	/**
	 * Time of login
	 */
	private final long m_loginTime;
    
    /**
     * The session id (assigned by factory)
     */
	private final int m_sessionId;
    
    private short m_clientType;
    
	/*
	 * Id of last message read, or -1 if no message has been read yet.
	 */
	private long m_lastReadMessageId = -1;
		
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
	private final LinkedList<Event> m_incomingEvents = new LinkedList<Event>();
	
	/**
	 * List of deferred events, i.e. events that will be sent once
	 * the current transaction is committed.
	 */
	private final LinkedList<DeferredEvent> m_deferredEvents = new LinkedList<DeferredEvent>();
	
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
	
	/**
	 * Backwards-linked list of read message
	 */
	private ReadLogItem m_readLog;
	
	/**
	 * Messages to mark as unread at logout
	 */
	private ReadLogItem m_pendingUnreads;
    
    /**
     * User variables shared across sessions for the same user
     */
    protected final UserContext m_userContext;
    
    /**
     * Last object acted on, used by user list
     */
    private long m_lastObject;

    /**
     * Free text used by user list, if set by user.
     */
    private String m_freeActivityText;
    
    /**
     * Activity types, used by user list. Last activity used to revert from a temp auto change.
     */
    private short m_currentActivity;
    private short m_lastActivity;
    
    private final int FILTER_ADJUST_THREASHOLD = 10;

    private SelectedMessages m_selectedMessages;
			
	public ServerSessionImpl(DataAccess da, long userId, int sessionId, short clientType, SessionManager sessions)
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
            m_clientType        = clientType;
			m_loginTime			= System.currentTimeMillis();
			m_sessions			= sessions;
            m_sessionId         = sessionId;
            m_currentActivity   = Activities.AUTO;
            m_lastActivity      = Activities.AUTO;
			m_selectedMessages  = new SelectedMessages();
            
            // Create user context
            //
            m_userContext = UserContextFactory.getInstance().getOrCreateContext(userId, m_da.getMembershipManager(), m_da.getRelationshipManager());
            
            // Let's lock the user context while we're manipulating it!
            //
            synchronized(m_userContext)
            {
    			// If we have saved unreads, load them and apply
    			//
    			try
    			{
    			    this.loadUnreadMarkers();
    			    this.applyUnreadMarkers();
                    m_userContext.saveMemberships(m_da.getMembershipManager());
    			}
    			catch(ObjectNotFoundException e)
    			{
    			    // No file, just ignore
    			}
    				
    			// Go to first conference with unread messages
    			//
    			long firstConf = m_userContext.getMemberships().getFirstConferenceWithUnreadMessages(
    				da.getConferenceManager());
    			this.setCurrentConferenceId(firstConf != -1 ? firstConf : userId);
    			
    			// Commit. We have to do this manually here, since we're not called through
    			// a TransactionalInvocationHandler.
    			//
    			m_da.commit();
    			m_da = null;
            }
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
		catch(ObjectNotFoundException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}	
		finally
		{
		    if(m_da != null)
		        m_da.rollback();
		}
	}

	public void finalize()
	{
		try
		{
			this.close();
		}
		catch(UnexpectedException e)
		{
			e.printStackTrace();
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
	throws UnexpectedException, ObjectNotFoundException
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
    
    public int getSessionId()
    {
        return m_sessionId;
    }
    
    public short getClientType()
    {
        return m_clientType;
    }
    
    public void setClientType(short clientType)
    {
        m_clientType = clientType;
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
	
	public SessionState getSessionState()
	{
		try
		{
            Map<Long, Long> filterCache = m_userContext.getFilterCache();
            MembershipList memberships = m_userContext.getMemberships();
		    long conf = this.getCurrentConferenceId();
		    long user = this.getLoggedInUserId();
		    ConferenceManager cm = m_da.getConferenceManager();
		    boolean hasFilters = filterCache.size() > 0;
		    
		    // While what we found matches a filter...
		    //
		    for(;;)
		    {			    
			    // Do we have any unread mail?
			    //
			    if((this.getLoggedInUser().getFlags1() & UserFlags.PRIORITIZE_MAIL) != 0)
			    {
			        try
			        {
			            int nextMail = memberships.getNextMessageInConference(user, cm);
			            try
			            {
					        if(nextMail != -1)
					        {
					            if(hasFilters && this.applyFiltersForMessage(
							            this.localToGlobal(user, nextMail), 
							            user, nextMail, FilterFlags.MAILS))
					            	continue;
					            return new SessionState(CommandSuggestions.NEXT_MAIL,
					                    user, this.countUnread(user));
					        }
			            }
				        catch(MessageNotFoundException e)
				        {
				            // Message is probably deleted. Skip it!
				            //
				            memberships.markAsRead(user, nextMail);
				            continue;
				        }
			        }
			        catch(ObjectNotFoundException e)
			        {
			            // Problem reading next mail. Try conferences instead
			            //
			            Logger.error(this, "Error finding next mail", e);
			        }
			    }
			    
			    
			    // Do we have any selected messages?
			    //
			    if (this.getSelectedMessages().hasUnreadMessages()) {
			        return new SessionState(CommandSuggestions.NEXT_SELECTED, conf, this.getSelectedMessages().getUnread());
			    }
		    
			    // First, try a reply. Then, try any unread message in current conference
			    //
			    long nextReply = this.peekReply();
			    if(nextReply != -1)
			    {
			        try
			        {
				        SessionState answer = new SessionState(CommandSuggestions.NEXT_REPLY, conf, 
			                    this.countUnread(conf));
				        MessageOccurrence occ = null;
				        try
				        {
				            occ = this.getMostRelevantOccurrence(conf, nextReply);
				        }
				        catch(MessageNotFoundException e)
				        {
				            // Probably deleted. Skip it!
				            //
				            this.popReply();
				            continue;
				        }

				        if(hasFilters && this.applyFiltersForMessage(nextReply, occ.getConference(), occ.getLocalnum(), FilterFlags.MESSAGES))
				            continue;
				        return answer;
			        }
			        catch(ObjectNotFoundException e)
			        {
			            // Problem getting next reply. Try messages
			            //
			            Logger.error(this, "Error finding next reply", e);
			        }
			    }
			    
			    // No replies to read. Try a normal message
			    //
			    int nextMessage = -1;
		        try
		        {
			        nextMessage = memberships.getNextMessageInConference(conf, cm);
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
	                    Logger.error(this, "Strange state!", e);
	                    return new SessionState(CommandSuggestions.ERROR, -1, 0);
	
				    }
				}
			    
			    // So? Did we get anything? Check if that message is filtered.
			    //
				try
				{
				    try
				    {
					    if(nextMessage != -1)
					    {
			                if(hasFilters && this.applyFiltersForMessage(
					            this.localToGlobal(conf, nextMessage), 
					            conf, nextMessage, FilterFlags.MESSAGES))
			                    continue;
				            return new SessionState(CommandSuggestions.NEXT_MESSAGE,
				                    conf, this.countUnread(conf));
					    }
				    }
				    catch(MessageNotFoundException e)
				    {
				        // Probably deleted. Skip!
				        //
				        memberships.markAsRead(conf, nextMessage);
				        continue;
				    }
				}
				catch(ObjectNotFoundException e)
				{
				    // Problems finding next message in this conference. Try
				    // next conference
				    //
				    Logger.error(this, "Error when looking for next message", e);
				}
									
				// Get next conference with unread messages
				//	
				long confId = memberships.getNextConferenceWithUnreadMessages(conf,
						m_da.getConferenceManager());
		
				// Do we have any unread messages?
				//
				if(confId == -1)
					return new SessionState(m_lastSuggestedCommand = CommandSuggestions.NO_ACTION,
					        conf, 0);    						        
					return new SessionState(m_lastSuggestedCommand = CommandSuggestions.NEXT_CONFERENCE,
					        this.getCurrentConferenceId(), 0);
		    }
		}
		catch(SQLException e)
		{
            Logger.error(this, "Strange state!", e);
            return new SessionState(CommandSuggestions.ERROR, -1, 0);
		}
		catch(UnexpectedException e)
		{
            Logger.error(this, "Strange state!", e);
            return new SessionState(CommandSuggestions.ERROR, -1, 0);
		}
	}
	
	protected boolean applyFiltersForMessage(long message, long conference, int localnum, 
	        long filter)
	throws UnexpectedException
	{
	    try
	    {
		    MessageManager mm= m_da.getMessageManager();
		    boolean skip = false;
		    MessageHeader mh = null;
		    try
	        {
	            mh = mm.loadMessageHeader(message);
	            skip = m_userContext.userMatchesFilter(mh.getAuthor(), filter);
	        }
	        catch(ObjectNotFoundException e)
	        {
	            // Message was deleted. Skip!
	            //
	            skip = true;
	        }
	        
	        // So... Should we skip this message?
	        //
	        if(!skip)
	            return false;
	        
	        // Skip this message. Mark it as read.
	        //
	        try
	        {
	            m_userContext.getMemberships().markAsRead(conference, localnum);
	        }
	        catch(ObjectNotFoundException e)
	        {
	            // Not found when trying to mark it as read? I guess
	            // it has disappeared then!
	            //
	            Logger.error(this, "Object not found when marking as read", e);
	        }
	        return true;
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
	throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException, AuthorizationException
	{
		return this.innerReadMessage(null);
	}
		
	public MessageHeader getLastMessageHeader()
	throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
	{
	    if(m_lastReadMessageId == -1)
	        throw new NoCurrentMessageException();
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
		 
    public short getActivity()
    {
        return m_currentActivity;
    }
    
    public void setActivity(short a, boolean keepLastState)
    {
        if (keepLastState)
        {
            m_lastActivity = m_currentActivity;
        }
        m_currentActivity = a;
    }
    
    public String getActivityString()
    {
        return m_freeActivityText;
    }
    
    public void setActivityString(String a)
    {
        m_freeActivityText = a;
    }
    
    public long getLastObject()
    {
        return m_lastObject;
    }
    
    public void setLastObject(long o)
    {
        m_lastObject = o;
    }
    
    public void restoreState()
    {
        m_currentActivity = m_lastActivity;
        m_lastActivity = Activities.AUTO;
        m_lastObject = -1;  // Since we're no longer operating on an object, we're surely not fiddling with this one
    }
    
    public void clearStates()
    {
        m_lastActivity = Activities.AUTO;
        restoreState();
    }
    
    public Envelope readMessage(MessageLocator message)
    throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException, AuthorizationException
    {
        return this.innerReadMessage(this.resolveLocator(message));
    }
	
	public Envelope readNextMessageInCurrentConference()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException, AuthorizationException
	{
	    return this.readNextMessage(this.getCurrentConferenceId());
	}
	
	public Envelope readNextMessage(long confId)
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException, AuthorizationException
	{
        MembershipList memberships = m_userContext.getMemberships();
		try
		{
			// Keep on trying until we've skipped all deleted messages
			//
			for(;;)
			{ 
				int next = memberships.getNextMessageInConference(confId, m_da.getConferenceManager());
				if(next == -1)
					throw new NoMoreMessagesException();
				try
				{
					this.pushReplies(confId, next);
					return this.readMessage(new MessageLocator(confId, next));
				}
				catch(ObjectNotFoundException e)
				{
					// We hit a deleted message. Mark it as read
					// and continue.
					//
					memberships.markAsRead(confId, next);
				}
			}
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
        catch(NoCurrentMessageException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
	}
	
	public Envelope readNextReply()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException, AuthorizationException
	{
		try
		{ 
			long next = this.popReply();
			if(next == -1)
				throw new NoMoreMessagesException();
			this.pushReplies(next);				
			return this.innerReadMessage(next);
		}
        catch(NoCurrentMessageException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}			
		
	public long createConference(String fullname, String keywords, int permissions, int nonmemberPermissions, short visibility, long replyConf)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
	{
		this.checkRights(UserPermissions.CREATE_CONFERENCE);
		try
		{
			long userId = this.getLoggedInUserId();
			long confId = m_da.getConferenceManager().addConference(fullname, keywords, userId, permissions, nonmemberPermissions, visibility, replyConf);

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
	        return (ConferenceListItem[]) this.censorNames(m_da.getConferenceManager().
	                listByDate(this.getLoggedInUserId()));
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
	        return (ConferenceListItem[]) this.censorNames(m_da.getConferenceManager().
	                listByName(this.getLoggedInUserId()));
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
			long nextId = m_userContext.getMemberships().getNextConferenceWithUnreadMessages(
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

	public MessageOccurrence storeMessage(long conf, UnstoredMessage msg)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
		return this.storeReplyAsMessage(conf, msg, -1L);
	}
	
	public void changeContactInfo(UserInfo ui)
	throws ObjectNotFoundException, UnexpectedException, AuthorizationException
	{
	    try
	    {
	        // We're only allowed to change address of ourselves, unless
	        // we hold the USER_ADMIN priv.
	        //
	        long id = ui.getId(); 
	        if(id != this.getLoggedInUserId())
	            this.checkRights(UserPermissions.USER_ADMIN);
	        m_da.getUserManager().changeContactInfo(ui);
	        this.sendEvent(id, new ReloadUserProfileEvent(id));
	    }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}	    
	}
	
    public MessageOccurrence storeMail(long recipient, UnstoredMessage msg)
    throws ObjectNotFoundException, UnexpectedException, AuthorizationException
	{
        return storeReplyAsMail(recipient, msg, -1L);
	}
    
    public MessageOccurrence storeReplyAsMessage(long conference,
            UnstoredMessage msg, MessageLocator replyTo) throws ObjectNotFoundException,
            UnexpectedException, AuthorizationException, NoCurrentMessageException
    {
        return storeReplyAsMessage(conference, msg, this.resolveLocator(replyTo).getGlobalId());
    }


    protected MessageOccurrence storeReplyAsMessage(long author, long conference,
            UnstoredMessage msg, long replyTo) throws ObjectNotFoundException,
            UnexpectedException, AuthorizationException
	{
		try
		{
			// If we're posting on behalf on someone else, we must hold the
			// POST_BY_PROXY privilege
			//
			if(author != this.getLoggedInUserId())
				this.checkRights(UserPermissions.POST_BY_PROXY);
		    MessageManager mm = m_da.getMessageManager();
		    UserInfo myinf = this.getLoggedInUser();

			// Check that we have the permission to write here. If it's a reply, we should
			// try check if we have the right to reply. It's ok to be able to reply without
			// being able to write. Great for conferences where users are only allowed to
			// reply to something posted by a moderator.
			//
			if(replyTo == -1)
				this.assertConferencePermission(author, conference, ConferencePermissions.WRITE_PERMISSION);
			else
				this.assertConferencePermission(author, conference, ConferencePermissions.REPLY_PERMISSION);
			
			// Store message in conference
			//
			MessageOccurrence occ = mm.addMessage(author, this.getCensoredName(author).getName(),
				conference, replyTo, msg.getSubject(), msg.getBody());
			
			// Mark message as read unless the user is a narcissist.
			//
			if((myinf.getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(conference, occ.getLocalnum());
			
			// Make this the "current" message
			//
			m_lastReadMessageId = occ.getGlobalId();
			
			// Notify the rest of the world that there is a new message!
			// Notice that we post the event with OUR user id and not the author's.
			// I think that's the right thing to do...
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
	}
    
    
    protected MessageOccurrence storeReplyAsMessage(long conference,
            UnstoredMessage msg, long replyTo) throws ObjectNotFoundException,
            UnexpectedException, AuthorizationException
	{
		return this.storeReplyAsMessage(this.getLoggedInUserId(), conference, msg, replyTo);
	}

    
    public MessageOccurrence storeReplyAsMail(long recipient, UnstoredMessage msg,
            MessageLocator replyTo) throws ObjectNotFoundException, UnexpectedException, NoCurrentMessageException, AuthorizationException
    {
        return storeReplyAsMail(recipient, msg, this.resolveLocator(replyTo).getGlobalId());
    }
	
    protected MessageOccurrence storeReplyAsMail(long recipient, UnstoredMessage msg,
            long replyTo) throws ObjectNotFoundException, UnexpectedException, AuthorizationException
    {
    	return this.storeReplyAsMail(this.getLoggedInUserId(), recipient, msg, replyTo);
    }
    
    protected MessageOccurrence storeReplyAsMail(long author, long recipient, UnstoredMessage msg,
            long replyTo) throws ObjectNotFoundException, UnexpectedException, AuthorizationException
    {
		try
		{
			// If we're posting on behalf on someone else, we must hold the
			// POST_BY_PROXY privilege
			//
			if(author != this.getLoggedInUserId())
				this.checkRights(UserPermissions.POST_BY_PROXY);

		    MessageManager mm = m_da.getMessageManager();
		    UserInfo myinf = this.getLoggedInUser();
		    
			// Store message in recipient's mailbox
			//
			MessageOccurrence occ = mm.addMessage(author, 
			    this.getCensoredName(author).getName(), 
				recipient, replyTo, msg.getSubject(), msg.getBody()); 
			
			// Set the mail attribute with the original recipient. This is used
			// to mark a text as a mail, and to keep track of the original
			// recipient in case the occurrences are lost.
			//
			String payload = MessageAttribute.constructUsernamePayload(recipient, this.getCensoredName(recipient).getName());
			mm.addMessageAttribute(occ.getGlobalId(), MessageAttributes.MAIL_RECIPIENT, payload);
			
			// Store a copy in sender's mailbox
			//
			if((myinf.getFlags1() & UserFlags.KEEP_COPIES_OF_MAIL) != 0)
			{
			    MessageOccurrence copy = mm.createMessageOccurrence(
				occ.getGlobalId(), MessageManager.ACTION_COPIED, author, this.getName(author).getName(), author);

			    // Mark copy as read unless the user is a narcissist.
				//
			    if((myinf.getFlags1() & UserFlags.NARCISSIST) == 0)
				{
				    this.markMessageAsRead(this.getLoggedInUserId(), copy.getLocalnum());
				}
			    
				// Make this the "current" message
				//
				m_lastReadMessageId = occ.getGlobalId();			    
			}

			// Notify recipient of the new message.
			// Notice that we post the event with OUR user id and not the author's.
			// I think that's the right thing to do...
			//
			this.sendEvent(recipient, new NewMessageEvent(this.getLoggedInUserId(), recipient, occ.getLocalnum(), occ.getGlobalId()));
			
			m_stats.incNumPosted(); 
			return occ;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
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
			
			// Mark as read (unless we're a narcissist)
			//
			if((this.getLoggedInUser().getFlags1() & UserFlags.NARCISSIST) == 0)
			    this.markMessageAsRead(conference, occ.getLocalnum());
			this.broadcastEvent(
				new NewMessageEvent(this.getLoggedInUserId(), occ.getConference(), occ.getLocalnum(), 
					occ.getGlobalId()));
			this.m_da.getMessageManager().addMessageAttribute(occ.getGlobalId(), MessageAttributes.PRESENTATION, new Long(object).toString());
			
			// Make this the "current" message
			//
			m_lastReadMessageId = occ.getGlobalId();
			return occ;
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}

	public Envelope readTaggedMessage(short tag, long object)
	throws UnexpectedException, ObjectNotFoundException, AuthorizationException
	{
		try
		{
			if (-1 == object)
			{
				object = this.getLoggedInUserId();
			}
			return this.innerReadMessage(m_da.getMessageManager().getTaggedMessage(object, tag));
		}
        catch(NoCurrentMessageException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public void addMessageAttribute(long message, short attribute, String payload, boolean deleteOld)
	throws UnexpectedException, AuthorizationException
	{
		try
		{
		    MessageManager mm = m_da.getMessageManager();
		    
			// Determine conference of message
		    //
			long targetConf = mm.getFirstOccurrence(message).getConference();

			// Check that we have the permission to write there.
			//
			this.assertConferencePermission(targetConf, ConferencePermissions.WRITE_PERMISSION);
			
			// If this is the kind of attribute that only the owner can change, we need
			// to check that we own the text (or that we have enough privs).
			//
			if(MessageAttributes.onlyOwner[attribute])
			{
			    if(mm.loadMessageHeader(message).getAuthor() != this.getLoggedInUserId())
			        throw new AuthorizationException();
			}

			// Delete already existing "no comment"
			//
			long user = this.getLoggedInUserId();
			if(deleteOld)
			{
				MessageAttribute[] attributes = mm.getMessageAttributes(message);
				for (int i = 0; i < attributes.length; i++)
	            {
	                if (attributes[i].getKind() == attribute && attributes[i].getUserId() == user)
	                    mm.dropMessageAttribute(attributes[i].getId(), message);
	            }
			}
			
			mm.addMessageAttribute(message, attribute, payload);
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
	
	public void storeNoComment(MessageLocator message)
	throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException, AuthorizationException
	{
	    this.addMessageAttribute(this.resolveLocator(message).getGlobalId(), MessageAttributes.NOCOMMENT, 
	            MessageAttribute.constructUsernamePayload(this.getLoggedInUser().getId(), this.getLoggedInUser().getName().toString()), true);
	}
	
	public void createUser(String userid, String password, String fullname, String keywords, String address1,
		String address2, String address3, String address4, String phoneno1, 
		String phoneno2, String email1, String email2, String url, String charset, long flags1, 
		long flags2, long flags3, long flags4, long rights)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
	{
		this.checkRights(UserPermissions.USER_ADMIN);
		try
		{
		    // Create the user
		    //
			long id = m_da.getUserManager().addUser(userid, password, fullname, keywords, address1, address2, address3, address4, 
				phoneno1, phoneno2, email1, email2, url, charset, "sv_SE", flags1, flags2, flags3, flags4, rights);
			
			// Add default login script if requested
			//
			try
			{
			    String content = FileUtils.loadTextFromResource("firsttime.login");
			    m_da.getFileManager().store(id, ".login.cmd", content);
			}
			catch(FileNotFoundException e)
			{
			    // No file specified? That's totally cool...
			}
			catch(IOException e)
			{
				throw new UnexpectedException(this.getLoggedInUserId(), e);
			}
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
    
    public void detach()
    {
        // Tell frontends to detach (hopefully they will)
        // TODO: Do we need to check that they actually do?
        //
        this.postEvent(new DetachRequestEvent(m_userId, m_userId));
    }
	
	public synchronized void close()
	throws UnexpectedException
	{
	    if(m_closed)
		return; // Closing twice is a noop!
		try
		{
			// Send notification!
			//
			m_sessions.broadcastEvent(new UserAttendanceEvent(m_userId, 
				this.getUser(m_userId).getName(), UserAttendanceEvent.LOGOUT)); 
			
			// Handle pending unreads
			//
			this.applyUnreadMarkers();

			// Make sure all message markers are saved
			//
			this.leaveConference();
			
			// Save statistics
			//
			m_stats.setLoggedOut(new Timestamp(System.currentTimeMillis()));
			m_da.getUserLogManager().store(m_stats);
			
			// Save last login timestamp
			//
			this.updateLastlogin();
			
			// Unregister and kiss the world goodbye
			//
            UserContextFactory.getInstance().release(m_userId);
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
	
	public int countUnread(long conference)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
            MembershipList ml = m_userContext.getMemberships();
			int n = ml.countUnread(conference, m_da.getConferenceManager());
            
            // If we're filtering users, and the conference has a reasonable
            // number of unreads, we adjust for any filtered users. We only 
            // adjust as long as we find filtered messages. 
            // This will not always yield an accurate result, but it will at
            // least prevent us from going into conferences where all messages
            // are posted by M�ns^h^h^h^hfiltered users.
            //
            if(n > 0 && n <= FILTER_ADJUST_THREASHOLD && m_userContext.getFilterCache().size() > 0)
            {
                int localnum;
                for(;;)
                {
                    localnum = ml.getNextMessageInConference(conference, m_da.getConferenceManager());
                    if(localnum == -1)
                        break;
                    boolean mailbox = conference == this.getLoggedInUserId();
                    if(this.applyFiltersForMessage(this.localToGlobal(conference, localnum), conference, localnum, 
                            mailbox ? FilterFlags.MAILS : FilterFlags.MESSAGES))
                    {
                        --n;
                        ml.markAsRead(conference, localnum);
                    }
                    else
                        break;
                } 
            }
            return n;
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
		
	public MessageOccurrence globalToLocalInConference(long conferenceId, long globalNum)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().getMostRelevantOccurrence(
			        this.getLoggedInUserId(), conferenceId, globalNum);
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

    public long getGlobalMessageId(MessageLocator textNumber)
            throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return this.resolveLocator(textNumber).getGlobalId();
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
	
    public long getThreadIdForMessage (MessageLocator ml) 
    throws MessageNotFoundException, UnexpectedException
    {
        if (!ml.isValid())
        {
            throw new MessageNotFoundException();
        }
        try
        {
            if (-1 == ml.getGlobalId())
            {
                ml.setGlobalId(this.localToGlobal(this.getCurrentConferenceId(), ml.getLocalnum()));
            }
            return m_da.getMessageManager().getThreadIdForMessage (ml.getGlobalId());
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
	
	private void moveMessage(int localNum, long sourceConfId, long destConfId)
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
			mm.addMessageAttribute(globId, MessageAttributes.MOVEDFROM, 
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

	public void moveMessage(long messageId, long destConfId) 
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
	    //I'm lazy, I'll just re-use the old move-methods...
	    MessageOccurrence originalOcc;
        try
        {
            originalOcc = m_da.getMessageManager().getOriginalMessageOccurrence(messageId);
            moveMessage(originalOcc.getLocalnum(), originalOcc.getConference(), destConfId);
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
			return m_da.getMessageManager().getMostRelevantOccurrence(this.getLoggedInUserId(),
			        m_currentConferenceId, this.getCurrentMessage());
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
	
	public MessageOccurrence getMostRelevantOccurrence(long conferenceId, long messageId) 
	throws ObjectNotFoundException, UnexpectedException
	{
	    try
        {
            return m_da.getMessageManager().getMostRelevantOccurrence(
                    this.getLoggedInUserId(), conferenceId, messageId);
        } 
	    catch (SQLException e)
        {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
        }
	}
	
	public MessageOccurrence getOriginalMessageOccurrence(long messageId)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			return m_da.getMessageManager().getOriginalMessageOccurrence(messageId);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}	
	
	public Name signup(long conferenceId)
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
			return this.getCensoredName(conferenceId);
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
    
    public Name[] signupForAllConferences()
    throws UnexpectedException, ObjectNotFoundException
    {
        long uid = this.getLoggedInUserId();
        MembershipManager mm = m_da.getMembershipManager();
        
        // Start by retrieving a list of all conferences
        //        
        long[] allconfs;
        MembershipInfo[] memberOf;
        try
        {
            allconfs = m_da.getConferenceManager().getConferenceIdsByPattern("%");
            memberOf = mm.listMembershipsByUser(uid);
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (uid, e);
        }
        catch (ObjectNotFoundException e)
        {
            // Throw a VERY unexpected exception..
            //
            throw new UnexpectedException (uid, e);
        }
        
        // Put them in a better structure ..
        //
        HashSet<Long> s = new HashSet<Long>();
        for (int i = 0; i < allconfs.length; ++i)
        {
            s.add (allconfs[i]);
        }
        
        // .. and clean out anything the user is already a member of.
        //
        for (int i = 0; i < memberOf.length; ++i)
        {
            // if (s.contains(joined[i].getConference()))
            // {
                s.remove(memberOf[i].getConference());
            // }
        }
        
        // Finally, call signup() for each conference left in the set, silently swallowing
        // every exception. This should be more efficient than verifying visibility and rights
        // for every conference. Prove me wrong :-)
        //
        ArrayList<Name> joins = new ArrayList<Name>(s.size());
        Iterator<Long> it = s.iterator();
        while (it.hasNext())
        {
            try
            {
                long l = it.next();
                mm.signup(uid, l, 0, 0, 0);
                joins.add(getCensoredName(l));
            }
            catch (Exception e)
            {
                // Don't bother differentiating between types, all we care about is that the
                // signup failed.
            }
        }
        
        // Finally, refresh membership list and return the number of joined conferences.
        //
        try
        {
            this.reloadMemberships();
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (uid, e);
        }
        Name[] joined = new Name[joins.size()];
        joins.toArray(joined);
        return joined;
    }
	
	public Name signoff (long conferenceId)
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
			return this.getCensoredName(conferenceId);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(userId, e);
		}
		// Return full name of conference
		//
	}
	
    public int signoffAllConferences()
    throws ObjectNotFoundException, UnexpectedException
    {
        // First, determine which conferences the user is a member of.
        //
        MembershipManager mm = m_da.getMembershipManager();
        long uid = this.getLoggedInUserId();
        MembershipInfo mo[];
        try
        {
            mo = mm.listMembershipsByUser(uid);
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (uid, e);
        }
        
        // Walk through the array, swallowing any exception silently
        //
        int cnt = 0;
        for (int i = 0; i < mo.length; ++i)
        {
            try
            {
                // Can't sign off mailbox.
                //
                if (uid == mo[i].getConference())
                    continue;

                mm.signoff(uid, mo[i].getConference());
                ++cnt;
            }
            catch (Exception e) { }
        }
        
        // Reload the memberships (should be quick by now) and return count.
        //
        try
        {
            this.reloadMemberships();
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (uid, e);
        }
        return cnt;
    }
    
	public long prioritizeConference(long conference, long targetconference)
	throws ObjectNotFoundException, UnexpectedException, NotMemberException
	{
	    long user = this.getLoggedInUserId();
	    try
	    {
	        // Check if we're actually member of the two given conferences.
	        //
	        if (!m_da.getMembershipManager().isMember(user, conference))
	        {
	            throw new NotMemberException(new Object[] { this.getCensoredName(conference).getName() });
	        }
	        if (!m_da.getMembershipManager().isMember(user, targetconference))
	        {
	            throw new NotMemberException(new Object[] { this.getCensoredName(targetconference).getName() });
	        }
	        
	        // Shufflepuck caf�
	        //
	        long result = m_da.getMembershipManager().prioritizeConference(user, conference, targetconference);
	        
			// Flush membership cache
			//
	        this.reloadMemberships();
	        return result;
	     }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(user, e);
	    }
	}
	
    public void autoPrioritizeConferences()
    throws UnexpectedException
    {
        try
        {
            ConferenceManager cm = this.m_da.getConferenceManager();
            MembershipManager mm = this.m_da.getMembershipManager();
            MembershipInfo[] mi = mm.listMembershipsByUser(this.getLoggedInUserId());

            // Put the data in a sortable wrapper and place in a better container.
            //
            List<SortableMembershipInfo> newOrder = new ArrayList<SortableMembershipInfo>(mi.length);
            for (int i = 0; i < mi.length; ++i)
            {
                // FIXME: If we experience a performance bottleneck (which I doubt), change
                // this to start by populating a hash table with conference ID + parent
                // count (one call, bulk transfer of n rows) instead of n separate calls.
                // Then again, the autoprioritize command won't get run all that often and
                // we'll be fine with an extra few seconds as long as the so called
                // database doesn't buckle under the load. 
                //
                newOrder.add(new SortableMembershipInfo(mi[i], cm.countParentsForConference(mi[i].getConference())));
            }
            java.util.Collections.sort(newOrder);
            
            // OK, we're now sorted according to the Algorithm of Darkness. Walk the list,
            // writing new priorities back to the database.
            //
            Iterator<SortableMembershipInfo> it = newOrder.listIterator();
            for (int i = 1; it.hasNext(); ++i)
            {
                mm.updatePriority(i, this.getLoggedInUserId(), it.next().getId());
            }
        }
        catch (Exception e)
        {
            throw new UnexpectedException (this.getLoggedInUserId(), e);
        }
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
					// Probably deleted while we were listing
					//
					answer[idx] = new NameAssociation(conf, 
					        new Name("???", Visibilities.PUBLIC, NameManager.CONFERENCE_KIND));
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

	public NameAssociation[] listMembersByConference(long confId)
	throws ObjectNotFoundException, UnexpectedException
	{
		MembershipInfo[] mi = this.listConferenceMembers(confId);
		NameAssociation[] s = new NameAssociation[mi.length];
		for (int i = 0; i < mi.length; ++i)
			s[i] = new NameAssociation(mi[i].getUser(), this.getName(mi[i].getUser()));
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
		m_userContext.getMemberships().printDebugInfo(new PrintStream(s));
		return s.toString();	
	}
	
	public void markAsUnreadAtLogoutInCurrentConference(int localnum)
	throws UnexpectedException
	{
	    this.innerMarkAsUnreadAtLogoutInCurrentConference(localnum);
	    this.saveUnreadMarkers();
	}
	
	public void markAsUnreadAtLogout(MessageLocator message)
	throws UnexpectedException, NoCurrentMessageException, ObjectNotFoundException
	{
	    this.innerMarkAsUnreadAtLogout(this.resolveLocator(message).getGlobalId());
	    this.saveUnreadMarkers();
	}

    public int markThreadAsUnread(long root, boolean immediate)
    throws ObjectNotFoundException, UnexpectedException, NoCurrentMessageException
    {
        ReadLogItem old = null;
        if (immediate)
        {
            old = this.m_pendingUnreads;
            this.m_pendingUnreads = null;
        }
        int n = this.markThreadAsUnread(root);
        if (immediate)
        {
            this.applyUnreadMarkers();
            this.m_pendingUnreads = old;
        }
        return n;
    }
    
	public int markThreadAsUnread(long root)
	throws ObjectNotFoundException, UnexpectedException
	{
	    int n = this.performTreeOperation(root, new MarkAsUnreadOperation());
	    this.saveUnreadMarkers();
	    return n;
	}
	
    public int markSubjectAsUnread(String subject, boolean localOnly, boolean immediate)
    throws ObjectNotFoundException, UnexpectedException
    {
        ReadLogItem old = null;
        if (immediate)
        {
            old = this.m_pendingUnreads;
            this.m_pendingUnreads = null;
        }
        int n = this.markSubjectAsUnread(subject, localOnly);
        if (immediate)
        {
            this.applyUnreadMarkers();
            this.m_pendingUnreads = old;
        }
        return n;
    }
    
	public int markSubjectAsUnread(String subject, boolean localOnly)
	throws ObjectNotFoundException, UnexpectedException
	{
	    int n = localOnly
	    	? this.performOperationBySubjectInConference(subject, new MarkAsUnreadOperation())
	    	: this.performOperationBySubject(subject, new MarkAsUnreadOperation());
	   this.saveUnreadMarkers();	  
	   return n;
	}

	
	public void changeUnread(int nUnread, long conference)
	throws ObjectNotFoundException, UnexpectedException
	{
		try
		{
			// Update message markers
			//
			ConferenceManager cm = m_da.getConferenceManager();
			ConferenceInfo ci = cm.loadConference(conference);
			int high = ci.getLastMessage();
			high = Math.max(0, high - nUnread);
			m_userContext.getMemberships().changeRead(ci.getId(), ci.getFirstMessage(), high);
			
			// Discard reply stack
			//
			m_replyStack = null;
			
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
    
    public void changeUnread(int nUnread)
    throws ObjectNotFoundException, UnexpectedException
    {
        changeUnread (nUnread, this.getCurrentConferenceId());
    }
    
    public void changeUnreadInAllConfs(int nUnread)
    throws ObjectNotFoundException, UnexpectedException
    {
        try
        {
            ConferenceManager cm = m_da.getConferenceManager();
            MembershipList ml = m_userContext.getMemberships();
            MembershipManager mm = this.m_da.getMembershipManager();
            MembershipInfo[] mi = mm.listMembershipsByUser(this.getLoggedInUserId());
            
            for (MembershipInfo m : mi)
            {
                try
                {
                    ConferenceInfo ci = cm.loadConference(m.getConference());
                    int high = ci.getLastMessage();
                    high = Math.max(0, high - nUnread);
                    ml.changeRead(ci.getId(), ci.getFirstMessage(), high);
                }
                catch (Exception e)
                {
                    // When does this happen? If the DB goes away? If a conference is 
                    // deleted while we run? If the user signs off a conference in another
                    // session while running this?  
                }
            }
            m_replyStack = null;
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (this.getLoggedInUserId(), e);
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
	        List<MembershipListItem> list = new ArrayList<MembershipListItem>(top);
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
        MembershipList memberships = m_userContext.getMemberships();
		MembershipInfo[] m = memberships.getMemberships();
		int top = m.length;
		List<MembershipListItem> list = new ArrayList<MembershipListItem>(top);
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
					int n = this.countUnread(conf);
 
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
	
	public UserListItem[] listLoggedInUsers()
	throws UnexpectedException
	{
		List<ServerSession> sessions = m_sessions.listSessions();
		UserListItem[] answer = new UserListItem[sessions.size()];
        int idx = 0;
		for(Iterator<ServerSession> itor = sessions.iterator(); itor.hasNext(); ++idx)
		{
			ServerSession session = itor.next();
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
			Name conferenceName = new Name("???", Visibilities.PUBLIC, NameManager.CONFERENCE_KIND);
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
          
			answer[idx] = new UserListItem(session.getSessionId(), new NameAssociation(user, new Name(userName, 
			        Visibilities.PUBLIC, NameManager.USER_KIND)), session.getClientType(), (short) 0, 
                    new NameAssociation(confId, conferenceName), inMailbox, session.getLoginTime(),
			        ((ServerSessionImpl) session).getLastHeartbeat(), session.getActivity(), session.getActivityString(), session.getLastObject()); 
		}
		return answer;
	}
		
	public boolean hasSession (long userId)
	{
		return m_sessions.userHasSession(userId);
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
	
    public void enableSelfRegistration()
    throws AuthorizationException, UnexpectedException
    {
        this.checkRights(UserPermissions.ADMIN);
        try
        {
            m_da.getSettingManager().changeSetting(SettingKeys.ALLOW_SELF_REGISTER, "", 1);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(-1, e);
        }
    }
    
    public void disableSelfRegistration()
    throws AuthorizationException, UnexpectedException
    {
        this.checkRights(UserPermissions.ADMIN);
        try
        {
            m_da.getSettingManager().changeSetting(SettingKeys.ALLOW_SELF_REGISTER, "", 0);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(-1, e);
        }

    }
		
	private void sendChatMessageHelper(long userId, String message)
	{
		if (m_sessions.userHasSession(userId))
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
	
	public NameAssociation[] sendMulticastMessage (long destinations[], String message,
	        boolean logAsMulticast)	
	throws NotLoggedInException, ObjectNotFoundException, AllRecipientsNotReachedException, UnexpectedException
	{
	    try
	    {
		    // Create a message log item. If we have multiple recipients, they all share
		    // the same item.
		    //
	        UserManager um = m_da.getUserManager();
		    MessageLogManager mlm = m_da.getMessageLogManager();
		    long logId = mlm.storeMessage(this.getLoggedInUserId(), this.getLoggedInUser().getName().getName(), message);
		    
		    // Create a link for the logged in user. Used for retrieving messages sent.
		    //
		    mlm.storeMessagePointer(logId, this.getLoggedInUserId(), true, 
		            logAsMulticast ? MessageLogKinds.MULTICAST : MessageLogKinds.CHAT);
		    
		    ArrayList<NameAssociation> refused = new ArrayList<NameAssociation>(destinations.length);
		    boolean explicitToSelf = false;
		    
		    
			// Set to make sure we don't send the message to the same user more than once.
			//
			HashSet<Long> s = new HashSet<Long>();
			HashSet<Name> rec_names = new HashSet<Name>();
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
                        UserContext uc = UserContextFactory.getInstance().getContextOrNull(each);
						if (uc != null && uc.allowsChat(um, this.getLoggedInUserId()))
						{
							s.add(each);
							rec_names.add(ui.getName());
						}
						else
						{
							refused.add(new NameAssociation(each, ui.getName()));
						}
					}
					else // conference
					{
						MembershipInfo[] mi = m_da.getMembershipManager().listMembershipsByConference(each);
						rec_names.add(this.getName(each));
						for (int j = 0; j < mi.length; ++j)
						{
						    long uid = mi[j].getUser();
                            UserContext uc = UserContextFactory.getInstance().getContextOrNull(uid);
							if (uc != null)
							{
							    UserInfo ui = um.loadUser(uid);
							    
							    // Does the receiver accept chat messages
							    //
							    if(uc.allowsChat(um, uid))
							        s.add(new Long(uid));
							    else
							        refused.add(new NameAssociation(uid, ui.getName()));
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
			
			// Temporary kludge. I need to extend the event class and rewrite
			// some of the formatting.
			//
			String msgToSend;
			if (s.size()>1)
			{
				msgToSend = "[";
				Iterator<Name> buildRecIter = rec_names.iterator();
				while (buildRecIter.hasNext())
				{
					msgToSend += buildRecIter.next().toString();
					msgToSend += ", ";
				}
				msgToSend = msgToSend.substring(0, msgToSend.length()-2);
				msgToSend += "] ";
				msgToSend += message;
			}
			else
			{
				msgToSend = message;
			}
			
			// Now just send it
			//
			Iterator<Long> iter = s.iterator();
			while (iter.hasNext())
			{
			    long user = iter.next().longValue();
				sendChatMessageHelper(user, msgToSend);
				
				// Create link from recipient to message
				//
				mlm.storeMessagePointer(logId, user, false, 
				        logAsMulticast ? MessageLogKinds.MULTICAST : MessageLogKinds.CHAT);
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
                    UserContext uc = UserContextFactory.getInstance().getContextOrNull(each);
                    if(uc == null)
                        answer[idx] = ChatRecipientStatus.NOT_LOGGED_IN;
                    else
                    {
                        // Logged in. Do they receive chat messages?
                        //
                        answer[idx] = uc.allowsChat(um, this.getLoggedInUserId())
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
		    long logId = mlm.storeMessage(this.getLoggedInUserId(), this.getLoggedInUser().getName().getName(), message);
		    	
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
			// that would give all kinds of concurrency and transactional problems,
			// since they are executing asynchronously. 
			// There is, of course, a slight chance that someone logs in out
			// while doing this and that the log won't be 100% correct, but that's
			// a tradeoff we're willing to make at this point.
			//
			ArrayList<NameAssociation> bounces = new ArrayList<NameAssociation>();
			for(UserContext uc : UserContextFactory.getInstance().listContexts())
            {
			    long userId = uc.getUserId();
			    try
			    {
			        // Find out if recipients allows broadcasts
			        //
				    UserInfo user = um.loadUser(userId);

                    if(uc.allowsBroadcast(um, this.getLoggedInUserId()))
				        mlm.storeMessagePointer(logId, userId, false, kind);
				    else 
				        bounces.add(new NameAssociation(userId, user.getName()));   
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
	
	public void clearCache()
	throws AuthorizationException
	{
	    // Only sysops can do this.
	    //
	    this.checkRights(UserPermissions.ADMIN);
	    CacheManager.instance().clear();
	    
	    // Tell all clients to reload their local user profile copy
	    //
	    this.broadcastEvent(new ReloadUserProfileEvent(-1));
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
		return this.hasPermissionInConference(this.getLoggedInUserId(), conferenceId, mask);
	}
	
	public boolean hasPermissionInConference(long userId, long conferenceId, int mask)
	throws ObjectNotFoundException, UnexpectedException
	{
		UserInfo ui = this.getUser(userId);
		try
		{
			// Do we have the permission to disregard conference permissions?
			//
			if(ui.hasRights(UserPermissions.DISREGARD_CONF_PERM))
			    // We can only disregard everything but ADMIN_PERMISSION.
			    //
			    if((mask & ConferencePermissions.ADMIN_PERMISSION) != mask)
			        return true;				
			return m_da.getMembershipManager().hasPermission(ui.getId(), conferenceId, mask);
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
	
    public void changeReplyToConference(long originalConferenceId,
            long newReplyToConferenceId) throws AuthorizationException,
            ObjectNotFoundException, UnexpectedException
    {
        // Check rights. This also checks if the original conference exists.
        this.assertModifyConference(originalConferenceId);
        
		try
		{
		    m_da.getConferenceManager().changeReplyToConference(originalConferenceId, newReplyToConferenceId);
		}
    	catch(SQLException e)
    	{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
    	}
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
			long id = ui.getId();
			long[] oldFlags = ui.getFlags();
			
			// Calculate new flags sets
			//
			long[] flags = new long[UserFlags.NUM_FLAG_WORD];
			for(int idx = 0; idx < UserFlags.NUM_FLAG_WORD; ++idx)
				flags[idx] = (oldFlags[idx] | set[idx]) & ~reset[idx];
			
			// Store in database
			//
			m_da.getUserManager().changeFlags(id, flags);
			
			// Force clients to reload local cache
			//
			this.sendEvent(id, new ReloadUserProfileEvent(id));
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
			
			// Force clients to invalidate local cache
			//
			this.sendEvent(user, new ReloadUserProfileEvent(user));
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}		
	}
	
	public void updateConferencePermissions(long id, int permissions, int nonmemberpermissions,
	        short visibility)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException
	{
	    // Load conference 
	    //
	    ConferenceInfo ci = this.getConference(id);
	    
	    // Check permissions. If we own the conference, we can change it. Otherwise, we
	    // can change it only if we hold the CONFERENCE_ADMIN priv.
	    //
	    if(ci.getAdministrator() != this.getLoggedInUserId() && 
	            !this.getLoggedInUser().hasRights(UserPermissions.CONFERENCE_ADMIN))
	        throw new AuthorizationException();

	    // Update!
	    //
	    try
	    {
	        m_da.getConferenceManager().changePermissions(id, permissions, nonmemberpermissions, visibility);
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
	}
	
	public int skipMessagesBySubject (String subject, boolean skipGlobal)
	throws UnexpectedException, ObjectNotFoundException
	{
	    return skipGlobal
	    	? this.performOperationBySubject(subject, new MarkAsReadOperation())
	    	: this.performOperationBySubjectInConference(subject, new MarkAsReadOperation());
	}
	
	public int skipThread(long message)
	throws UnexpectedException, ObjectNotFoundException, SelectionOverflowException
	{
	    try
	    {
		    Message m = m_da.getMessageManager().loadMessage(message);
		    long thread = m.getThread();
		    if(thread <= 0)
		        return 0;
		    return this.performThreadOperation(m.getThread(), 100000, new MarkAsReadOperation());
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	}	
		
	public int skipBranch(long root)
	throws UnexpectedException, ObjectNotFoundException
	{
	    return this.performTreeOperation(root, new MarkAsReadOperation());
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
	
	public MessageLogItem[] getMulticastMessagesFromLog(int limit)
	throws UnexpectedException
	{
	    try
	    {
	        return m_da.getMessageLogManager().listMulticastMessages(this.getLoggedInUserId(), limit);
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
	
    public void killSession(int sessionId)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        // We have to be sysops to do this!
        //
        this.checkRights(UserPermissions.ADMIN);
        
        // SET THEM UP THE BOMB! FOR GREAT JUSTICE!
        //
        try
        {
            m_sessions.killSessionById(sessionId);
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
        List<ServerSession> sessions = m_sessions.listSessions();
        for (ServerSession each : sessions)
        {
            if(each != this)
                this.killSession(each.getSessionId());
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
    
    public void createUserFilter(long jinge, long flags)
    throws ObjectNotFoundException, UnexpectedException
    {
        try
        {
            long me = this.getLoggedInUserId();
            RelationshipManager rm = m_da.getRelationshipManager();
            Relationship[] r = rm.find(me, jinge, RelationshipKinds.FILTER);
            if(r.length > 0)
                rm.changeFlags(r[0].getId(), flags);
            else
                rm.addRelationship(me, jinge, RelationshipKinds.FILTER, flags);
            
            // Update cache
            //
            m_userContext.getFilterCache().put(new Long(jinge), new Long(flags));
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }        
    }
    
    public void dropUserFilter(long user)
    throws ObjectNotFoundException, UnexpectedException
    {
        try
        {
            RelationshipManager rm = m_da.getRelationshipManager();
            Relationship[] r = rm.find(this.getLoggedInUserId(), user, RelationshipKinds.FILTER);
            for(int idx = 0; idx < r.length; ++idx)
                rm.delete(r[idx].getId());
            
            // Update cache
            //
            m_userContext.getFilterCache().remove(new Long(user));
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                
    }
    
    public Relationship[] listFilters()
    throws UnexpectedException
    {
        try
        {
            return m_da.getRelationshipManager().listByRefererAndKind(this.getLoggedInUserId(), 
                    RelationshipKinds.FILTER);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }                        
    }
    
    public NameAssociation[] listReaders(MessageLocator ml)
    throws UnexpectedException, NoCurrentMessageException
    {
        try
        {
            long globid = 0;
            
            if (null == ml)
            {
                if (-1 == this.m_lastReadMessageId)
                    throw new NoCurrentMessageException();
                else
                    globid = this.m_lastReadMessageId;
            }
            else
            {
                globid = this.localToGlobal(this.m_currentConferenceId, ml.getLocalnum());
            }

            return m_da.getMembershipManager().listReaders(globid);
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

	protected void markAsInvalid()
	{
		m_valid = false;
	}
	
	public boolean isValid()
	{
		return m_valid;
	}
	
	protected void leaveConference()
	throws UnexpectedException
	{
		// Save message markers
		//
		m_userContext.saveMemberships(m_da.getMembershipManager());
	}
		
	protected boolean markMessageAsReadEx(long conference, int localnum)
	throws ObjectNotFoundException
	{
		return m_userContext.getMemberships().markAsReadEx(conference, localnum);
	}
	
	protected int performThreadOperation(long thread, int max, MessageOperation op)
	throws UnexpectedException, ObjectNotFoundException, SelectionOverflowException
	{
	    try
	    {
	        long[] ids = m_da.getMessageManager().selectByThread(thread, max);
	        int top = ids.length;
	        for(int idx = 0; idx < top; ++idx)
	            op.perform(ids[idx]);
	        return top;
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException (this.getLoggedInUserId(), e);
	    }
	}
	
	protected int performTreeOperation(long root, MessageOperation op)
	throws UnexpectedException, ObjectNotFoundException
	{
	    try
	    {
		    MessageManager mm = m_da.getMessageManager();
		    Stack<Long> stack = new Stack<Long>();
		    stack.add(root);
		    int n = 0;
		    for(;!stack.isEmpty(); ++n)
		    {
		        // Perform operation
		        //
		        long id = stack.pop();
		        op.perform(id);
		        
		        // Push replies
		        //
		        MessageHeader[] mh = mm.getReplies(id);
		        for (int idx = 0; idx < mh.length; idx++)
		            stack.push(mh[idx].getId());
		    }
		    return n;
	    }
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
	protected int performOperationBySubjectInConference(String subject, MessageOperation op)
	throws UnexpectedException, ObjectNotFoundException
	{
		long currentConference = this.getCurrentConference().getId();
		try
		{
			MessageManager mm = m_da.getMessageManager();
			int[] ids = mm.getLocalMessagesBySubject(subject, currentConference);
			for (int idx = 0; idx < ids.length; ++idx)
			    op.perform(this.localToGlobalInCurrentConference(ids[idx]));
			return ids.length;
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}
	
	protected int performOperationBySubject(String subject, MessageOperation op)
	throws UnexpectedException, ObjectNotFoundException
	{
		try
		{
			MessageManager mm = m_da.getMessageManager();
			long[] globalIds = mm.getMessagesBySubject(subject, this.getLoggedInUserId());
			for (int idx = 0; idx < globalIds.length; ++idx)
			    op.perform(globalIds[idx]);
			return globalIds.length;
		}
		catch (SQLException e)
		{
			throw new UnexpectedException (this.getLoggedInUserId(), e);
		}
	}	
		
	protected void markMessageAsRead(long conference, int localnum)
	throws SQLException
	{
		try
		{
			// Mark it as read in the membership list
			//
		    m_userContext.getMemberships().markAsRead(conference, localnum);
			
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
	
	public int rollbackReads(int n)
	throws UnexpectedException
	{
	    long conf = -1;
	    int count = 0;
	    for(; count < n && m_readLog != null; m_readLog = m_readLog.getPrevious())
	    {
	        conf = m_readLog.getConference();
	        try
	        {
	            m_userContext.getMemberships().markAsUnread(conf, m_readLog.getLocalNum());
	            ++count;
	        }
	        catch(ObjectNotFoundException e)
	        {
	            // The conference disappeared. Not much we can do!
	        }
	    }
	    
	    // Go to the conference of the last unread message
	    //
	    try
	    {
		    if(conf != -1)
		        this.gotoConference(conf);
	    }
        catch(NotMemberException e)
        {
            // We have signed off from that conference. Not much to do
        }
        catch(ObjectNotFoundException e)
        {
            // The conference disappeared. Not much we can do!
        }        
	    return count;	    
	}
	
	public void assertConferencePermission(long conferenceId, int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		this.assertConferencePermission(this.getLoggedInUserId(), conferenceId, mask);
	}
	
	public void assertConferencePermission(long userId, long conferenceId, int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
		if(!this.hasPermissionInConference(userId, conferenceId, mask))
		{
		    if (mask == ConferencePermissions.REPLY_PERMISSION)
		    {
		        throw new RepliesNotAllowedException(new Object[] { this.getCensoredName(conferenceId).getName() });
		    }
		    else if (mask == ConferencePermissions.WRITE_PERMISSION)
		    {
		        throw new OriginalsNotAllowedException(new Object[] { this.getCensoredName(conferenceId).getName() });
		    }
		    else
		        throw new AuthorizationException();
		}
			
	}
	
		
	public void assertModifyConference(long conferenceId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException
	{
	    if(!(this.hasPermissionInConference(conferenceId, ConferencePermissions.ADMIN_PERMISSION)
			       || this.getLoggedInUser().hasRights(UserPermissions.CONFERENCE_ADMIN)))
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
	    // Not reading in reply-tree-order? We're outta here!
	    //
	    if((this.getLoggedInUser().getFlags1() & UserFlags.READ_REPLY_TREE) == 0)
	        return;
		try
		{
			long[] replies = m_da.getMessageManager().getReplyIds(messageId);
			
			// If we're not interested in replies across conferences, we have
			// to filter the list.
			//
			if((this.getLoggedInUser().getFlags1() & UserFlags.READ_CROSS_CONF_REPLIES) == 0)
			{
			    MessageManager mm = m_da.getMessageManager();
			    int p = 0;
				for(int idx = 0; idx < replies.length; idx++)
	            {
				    long reply = replies[idx];
				    try
				    {
				        mm.getOccurrenceInConference(this.getCurrentConferenceId(), reply);
				        replies[p++] = reply;
				    }
				    catch(ObjectNotFoundException e)
				    {
				        // Skip...
				    }
	            }
				if(p != replies.length)
				{
				    long[] copy = replies;
				    replies = new long[p];
				    System.arraycopy(copy, 0, replies, 0, p);
				}
			}
			if(replies.length > 0)
				m_replyStack = new ReplyStackFrame(replies, m_replyStack);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);	
		}
	}
    
    protected Envelope innerReadMessage(long globalId)
    throws ObjectNotFoundException, UnexpectedException, NoCurrentMessageException, AuthorizationException
    {
        return this.innerReadMessage(new MessageLocator(globalId));
    }
		
	protected Envelope innerReadMessage(MessageLocator locator)
	throws ObjectNotFoundException, UnexpectedException, NoCurrentMessageException, AuthorizationException
	{
		try
		{
			// Check that we are allowed to read this
			//
			locator = this.resolveLocator(locator); 
			this.assertMessageReadPermissions(locator.getGlobalId());
			
			// Resolve reply to (if any)
			//
			long conf = this.getCurrentConferenceId();
			MessageManager mm = m_da.getMessageManager();
			ConferenceManager cm = m_da.getConferenceManager();
			Message message = mm.loadMessage(locator.getConference(), locator.getLocalnum());
			long replyToId = message.getReplyTo();
			Envelope.RelatedMessage replyTo = null;
			if(replyToId > 0)
			{
				// This is a reply. Fill in info.
				//
				MessageOccurrence occ = mm.getMostRelevantOccurrence(
				        this.getLoggedInUserId(), conf, replyToId);
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
			ArrayList<Envelope.RelatedMessage> list = new ArrayList<Envelope.RelatedMessage>(top);
			
			for(int idx = 0; idx < top; ++idx)
			{
				MessageHeader each = replyHeaders[idx];
				MessageOccurrence replyOcc = mm.getMostRelevantOccurrence(
				        this.getLoggedInUserId(), conf, each.getId());
				
				// Don't show replies written by filtered users
				//
				if(m_userContext.userMatchesFilter(each.getAuthor(), FilterFlags.MESSAGES))
				    continue;
				
				// Don't show personal replies
				//
				if(cm.isMailbox(replyOcc.getConference()))
				    continue;
				
				// Don't show replies in conferences we're not allowed to see
				//
				if(this.getName(replyOcc.getConference()).getVisibility() == Visibilities.PROTECTED
				    && !this.hasPermissionInConference(replyOcc.getConference(), ConferencePermissions.READ_PERMISSION))
				    continue;
				    
				// Add to list
				//
				list.add(new Envelope.RelatedMessage(replyOcc, each.getAuthor(), each.getAuthorName(),
				        replyOcc.getConference(), this.getCensoredName(replyOcc.getConference()), replyOcc.getConference() == conf));  
			}
			Envelope.RelatedMessage[] replies = new Envelope.RelatedMessage[list.size()];
			list.toArray(replies);
			
			// Done assembling envelope. Now, mark the message as read in all
			// conferences where it appears and we are members.
			//
			MessageOccurrence[] occs = mm.getVisibleOccurrences(this.getLoggedInUserId(), 
				locator.getGlobalId());
			top = occs.length;
			for(int idx = 0; idx < top; ++idx)
			{
				MessageOccurrence each = occs[idx];
				this.markMessageAsRead(each.getConference(), each.getLocalnum());
			}
			
			// Put it in the read log. Notice that we log only one occurrence, as
			// it would confuse users if implicitly read occurences were included.
			//
			if(top > 0)
			    this.appendToReadLog(occs[0]);
			
			// Make this the "current" message
			//
			m_lastReadMessageId = locator.getGlobalId();
			
			// Create Envelope and return
			//
			m_stats.incNumRead();
			return new Envelope(message, this.toOccurrence(locator), replyTo, receivers, occ, attr, replies);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
    
    /**
     * Retrieves all message attributes, if any, of the given type for the given message.
     */
    public MessageAttribute[] getMatchingMessageAttributes (long message, short kind)
    throws UnexpectedException
    {
        try
        {
            return kind == -1 ? m_da.getMessageManager().getMessageAttributes(message) :
                                m_da.getMessageManager().getMatchingMessageAttributes(message, kind);
        }
        catch (SQLException e)
        {
            throw new UnexpectedException (this.getLoggedInUserId(), e);
        }
    }
    
    /**
     * Retrieves all message attributes for a given message
     */
    public MessageAttribute[] getMessageAttributes(long message)
    throws UnexpectedException
    {
        return getMatchingMessageAttributes(message, (short)-1);
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
	        // Treat no occurrences at all as an object not found, rather than 
	        // authorization problem
	        //
	        int top = occs.length;
	        if(top == 0)
	        	throw new ObjectNotFoundException("Messageid=" + globalId);
	        
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
				
			// Fetch next reply global id and translate into local occurrence
			//
			reply = m_replyStack.peek();
			try
			{
				MessageOccurrence occ = m_da.getMessageManager().getMostRelevantOccurrence(
				        this.getLoggedInUserId(), m_currentConferenceId, reply);
				
				// Check that we have permission to see this one
				//
				if(!this.hasMessageReadPermissions(reply))
				    continue;
				
				// If it's unread, we're done
				//
				if(m_userContext.getMemberships().isUnread(occ.getConference(), occ.getLocalnum()))
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
	
	protected void appendToReadLog(MessageOccurrence occ)
	{
	    m_readLog = new ReadLogItem(m_readLog, occ.getConference(), occ.getLocalnum());
	}
	
	public void checkRights(long mask)
	throws AuthorizationException
	{
		if(!this.getLoggedInUser().hasRights(mask))
			throw new AuthorizationException(); 
	}	
	
	public boolean checkForUserid(String userid)
	throws UnexpectedException
	{
	    UserManager um = m_da.getUserManager();
	    try
	    {
	        um.getUserIdByLogin(userid);
	        return true;
	    }
	    catch(ObjectNotFoundException e)
	    {
	        return false;
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
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
	    synchronized(m_deferredEvents)
	    {
	        m_deferredEvents.add(new DeferredSend(userId, e));
	    }
	}
    
    protected void reloadMemberships()
    throws SQLException, ObjectNotFoundException
    {
        m_userContext.reloadMemberships(m_da.getMembershipManager());
    }
	
	protected void flushEvents()
	{
	    synchronized(m_deferredEvents)
	    {
	        for(Iterator<DeferredEvent> itor = m_deferredEvents.iterator(); itor.hasNext();)
	            itor.next().dispatch(m_sessions);
	        m_deferredEvents.clear();
	    }
	}
	
	protected void discardEvents()
	{
	    m_deferredEvents.clear();
	}
	
	/**
	 * Broadcasts an event to all active users
	 * 
	 * @param e The event
	 */
	protected void broadcastEvent(Event e)
	{
	    // Push events onto a transaction-local queue that will
	    // be flushed once the current transaction is committed.
	    //
	    synchronized(m_deferredEvents)
	    {
	        m_deferredEvents.add(new DeferredBroadcast(e));
	    }
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
	    if(this.allowsChatInEventHandler(e.getOriginatingUser()))
	        this.postEvent(e);
	}

	public void onEvent(ChatAnonymousMessageEvent e)
	{
	    if(this.allowsChatInEventHandler(e.getOriginatingUser()))
	        this.postEvent(e);
	}
	
	public void onEvent(BroadcastMessageEvent e)
	{
	    if(this.allowsBroadcastInEventHandler(e.getOriginatingUser()))
	        this.postEvent(e);
	}
	
	public void onEvent(BroadcastAnonymousMessageEvent e)
	{
	    if(this.allowsBroadcastInEventHandler(e.getOriginatingUser()))
	        this.postEvent(e);
	}

	public void onEvent(UserAttendanceEvent e)
	{
        UserInfo ui = this.getLoggedInUserInEventHandler();
	    if(ui.testFlags(0, UserFlags.SHOW_ATTENDANCE_MESSAGES)
                && (e.getType() != UserAttendanceEvent.AWOKE || 
                    ui.testFlags(0, UserFlags.SHOW_END_OF_IDLE_MESSAGE)))
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
		    m_userContext.getMemberships().get(conf);
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
	    // Is this a new text in our mailbox? Then we're definately interested in it 
	    // and should always pass it on!
	    //
	    boolean debug = Logger.isDebugEnabled(this);
	    if(e.getConference() == this.getLoggedInUserId())
	    {
	        if(debug)
	            Logger.debug(this, "New mail. Passing event to client");
	        this.postEvent(e);
	        return;
	    }
	    
		// Already have unread messages? No need to send event! 
		// 
	    if(debug)
	        Logger.debug(this, "NewMessageEvent received. Conf=" + e.getConference());
		if(m_lastSuggestedCommand == CommandSuggestions.NEXT_MESSAGE 
		        || m_lastSuggestedCommand == CommandSuggestions.NEXT_REPLY)
		{
		    if(debug)
		        Logger.debug(this, "Event ignored since we already have an unread text in this conference");
		    return;
		}
			
			
		// Don't send notification unless we're members.
		//
		long conf = e.getConference();
		try
		{
		    // Last suggested command was NEXT_CONFERENCE and message 
		    // was not posted in current conference? No need to pass it on.
		    //
		    if(m_lastSuggestedCommand == CommandSuggestions.NEXT_CONFERENCE && conf != this.getCurrentConferenceId())
		    {
		        if(debug)
		            Logger.debug(this, "Event ignored because we're already suggesting going to the next conference");
		        return;
		    }
		        
		    // Try to load the membership to see if we should care.
		    //
			m_userContext.getMemberships().get(conf);
			if(debug)
			    Logger.debug(this, "Passing event to client");
			this.postEvent(e);
		}
		catch(ObjectNotFoundException ex)
		{
			// Not a member. No need to notify client
			//
		    if(debug)
		        Logger.debug(this, "Event ignored because we're not members where the new message was posted");
			return;
		}
	}
    
    protected UserInfo getUserInEventHandler(long user)
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
            return da.getUserManager().loadUser(user);
        }
        catch(Exception e)
        {
            // We're called from an event handler, so what can we do?
            //
            Logger.error(this, "Error in event handler", e);
            return null;
        }
        finally
        {
            if(da != null)
                pool.returnDataAccess(da);
        }        
    }
    
    protected UserInfo getLoggedInUserInEventHandler()
    {
        return this.getUserInEventHandler(this.getLoggedInUserId());
    }
	
	protected boolean testUserFlagInEventHandler(long user, int flagword, long mask)
	{
	    UserInfo ui = this.getUserInEventHandler(user);
        if(ui == null)
            return false;
	    return ui.testFlags(flagword, mask);
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
			return this.removeDuplicateMessages(this.censorMessages(
			        m_da.getMessageManager().listMessagesGloballyByAuthor(user, offset, length)));
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
	
	public MessageHeader getMessageHeader(MessageLocator locator)
	throws ObjectNotFoundException, AuthorizationException, NoCurrentMessageException, UnexpectedException
	{
        long globalId = this.resolveLocator(locator).getGlobalId();
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
	
	public void deleteConference (long conference)
	throws AuthorizationException, UnexpectedException
	{
		try
		{
		    // Do we have the right to do this?
		    //
		    this.assertModifyConference(conference);

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
	throws ObjectNotFoundException, NoRulesException, UnexpectedException, AuthorizationException
	{
		try
		{
			return this.readMessage(
                    new MessageLocator(conference, m_da.getMessageManager().findLastOccurrenceInConferenceWithAttrStmt(MessageAttributes.RULEPOST, conference)));
		}
        catch(NoCurrentMessageException e)
        {
            throw new UnexpectedException (this.getLoggedInUserId(), e);
        }
		catch (SQLException e)
		{
			throw new NoRulesException();
		}
	}
	
	public Envelope getLastRulePosting()
	throws ObjectNotFoundException, NoRulesException, UnexpectedException, AuthorizationException
	{
		return getLastRulePostingInConference (this.m_currentConferenceId);
	}

	public MessageOccurrence storeRulePosting(UnstoredMessage msg)
	throws AuthorizationException, UnexpectedException, ObjectNotFoundException
	{
		try
		{
		    MessageOccurrence mo = this.storeMessage(m_currentConferenceId, msg);
			m_da.getMessageManager().addMessageAttribute(mo.getGlobalId(), MessageAttributes.RULEPOST, null);
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
                    cm.getPermissionCache().getStatistics(),
		            m_da.getUserManager().countUsers(), m_da.getConferenceManager().countConferences(),
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
			return this.removeDuplicateMessages(this.censorMessages(m_da.getMessageManager().searchMessagesGlobally(searchterm, offset, length)));
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
    		return this.removeDuplicateMessages(m_da.getMessageManager().searchMessagesLocally(conference, searchterm, offset, length));
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
    		return this.removeDuplicateMessages(m_da.getMessageManager().grepMessagesLocally(conference, searchterm, offset, length));
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
    		return m_userContext.getMemberships().getOrNull(conferenceId) != null 
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
    	    	: new Name("", Visibilities.PROTECTED, NameManager.UNKNOWN_KIND);
    	    
    	}
    	catch (SQLException e)
		{
    		throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
    }
    
    private List<MessageSearchResult> innerRemoveDuplicateMessages(MessageSearchResult[] messages)
    {
        // The list is already sorted and we want to preserve the order, so we can
        // take the easy route to making thing unique.
        //
        long lastId = -1;
        List<MessageSearchResult> answer = new ArrayList<MessageSearchResult>(messages.length);
        for(MessageSearchResult each : messages)
        {
            if(each.getGlobalId() != lastId)
            {
                answer.add(each);
                lastId = each.getGlobalId();
            }
        }
        return answer;
    }
    
    protected GlobalMessageSearchResult[] removeDuplicateMessages(GlobalMessageSearchResult[] messages)
    {
        List<MessageSearchResult> result = this.innerRemoveDuplicateMessages(messages);
        return result.toArray(new GlobalMessageSearchResult[result.size()]);
    }
    
    
    protected LocalMessageSearchResult[] removeDuplicateMessages(LocalMessageSearchResult[] messages)
    {
        List<MessageSearchResult> result = this.innerRemoveDuplicateMessages(messages);
        return result.toArray(new LocalMessageSearchResult[result.size()]);

    }
    
    
    protected GlobalMessageSearchResult[] censorMessages(GlobalMessageSearchResult[] messages)
    throws ObjectNotFoundException, UnexpectedException
    {
        return (GlobalMessageSearchResult[]) FilterUtils.applyFilter(messages, 
            new FilterUtils.Filter()
            {
	    		public boolean include(Object obj)
	    		throws UnexpectedException
	    		{
	    		    try
	    		    {
	    		        GlobalMessageSearchResult gms = (GlobalMessageSearchResult) obj;
	    		        return ServerSessionImpl.this.hasMessageReadPermissions(gms.getGlobalId())
	    		        	&& !ServerSessionImpl.this.m_userContext.userMatchesFilter(
	    		        	        gms.getAuthor().getId(), FilterFlags.MESSAGES);
	    		    }
	    		    catch(ObjectNotFoundException e)
	    		    {
	    		        // Not found? Certainly not to be included!
	    		        //
	    		        Logger.error(this, e);
	    		        return false;
	    		    }
	            }
            });
    }
    
    protected NameAssociation[] censorNames(NameAssociation[] names)
    throws ObjectNotFoundException, UnexpectedException
    {
        return (NameAssociation[]) FilterUtils.applyFilter(names, 
                new FilterUtils.Filter() 
                {
            		public boolean include(Object obj)
            		throws UnexpectedException
            		{
            		    try
            		    {
            		        return ServerSessionImpl.this.isVisible(((NameAssociation) obj).getId());
            		    }
            		    catch(ObjectNotFoundException e)
            		    {
            		        // Not found? Certainly not visible!
            		        //
            		        Logger.error(this, e);
            		        return false;
            		    }            		    
            		}
                });
    }
     
    
    protected boolean allowsChatInEventHandler(long sender)
    {
        try
        {
            DataAccess da = DataAccessPool.instance().getDataAccess();
            try
            {
                return m_userContext.allowsChat(da.getUserManager(), sender);
            }
            finally
            {
                try
                {
                    da.rollback();   
                }
                finally
                {
                    DataAccessPool.instance().returnDataAccess(da);
                }
            }
        }
        catch(Exception e)
        {
            Logger.error(this, "Error in event handler", e);
            return false;
        }
    }

    protected boolean allowsBroadcastInEventHandler(long sender)
    {
        try
        {
            DataAccess da = DataAccessPool.instance().getDataAccess();
            try
            {
                return m_userContext.allowsBroadcast(da.getUserManager(), sender);
            }
            finally
            {
                try
                {
                    da.rollback();   
                }
                finally
                {
                    DataAccessPool.instance().returnDataAccess(da);
                }
            }
        }
        catch(Exception e)
        {
            Logger.error(this, "Error in event handler", e);
            return false;
        }
    }
    
	protected void innerMarkAsUnreadAtLogoutInCurrentConference(int localnum)
	throws UnexpectedException
	{
	    m_pendingUnreads = new ReadLogItem(m_pendingUnreads, this.getCurrentConferenceId(), localnum);
	}

	protected void innerMarkAsUnreadAtLogout(long messageId)
	throws UnexpectedException
	{
	    try
	    {
		    MessageOccurrence occ = m_da.getMessageManager().getMostRelevantOccurrence(
		            this.getLoggedInUserId(), this.getCurrentConferenceId(), messageId);
		    m_pendingUnreads = new ReadLogItem(m_pendingUnreads, occ.getConference(), occ.getLocalnum());
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
	
	protected void saveUnreadMarkers()
	throws UnexpectedException
	{
	    try
	    {
		    StringBuffer content = new StringBuffer();
		    for(ReadLogItem each = m_pendingUnreads; each != null; each = each.getPrevious())
		    {
		        content.append(each.externalizeToString());
		        if(each.getPrevious() != null)
		            content.append(',');
		    }
		    FileManager fm = m_da.getFileManager();
		    fm.store(this.getLoggedInUserId(), ".unread", content.toString());
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
	}
	
	protected void loadUnreadMarkers()
	throws ObjectNotFoundException, UnexpectedException
	{
	    try
	    {
		    FileManager fm = m_da.getFileManager();
	        String content = fm.read(this.getLoggedInUserId(), ".unread");
		    for(StringTokenizer st = new StringTokenizer(content, ","); st.hasMoreTokens();)
		    {
		        String each = st.nextToken();
		        int p = each.indexOf(':');
		        String confStr = each.substring(0, p);
		        String localNumStr = each.substring(p + 1);
		        m_pendingUnreads = new ReadLogItem(m_pendingUnreads, Long.parseLong(confStr),
		                Integer.parseInt(localNumStr));
		    }		    
	    }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
	}
	
	public void applyUnreadMarkers()
	throws UnexpectedException
	{
		for(; m_pendingUnreads != null; m_pendingUnreads = m_pendingUnreads.getPrevious())
		{
		    try
		    {
		        m_userContext.getMemberships().markAsUnread(m_pendingUnreads.getConference(), m_pendingUnreads.getLocalNum());
		    }
		    catch(ObjectNotFoundException e)
		    {
		        // The conference or message was deleted. Just ignore
		        //
		    }
		}
		
		// Delete backup file (if exists)
		//
		FileManager fm = m_da.getFileManager();
		try
		{
		    fm.delete(this.getLoggedInUserId(), ".unread");
		}
		catch(ObjectNotFoundException e)
		{
		    // No file to delete, no big deal!
		}
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
	}

    public long countMessagesLocallyByAuthor(long conference, long user) 
    throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        try
        {
            this.assertConferencePermission(conference, ConferencePermissions.READ_PERMISSION);
            MessageManager mm = m_da.getMessageManager();
            return mm.countMessagesLocallyByAuthor(conference, user);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e); 
	    }	    
    }

    public long countMessagesGloballyByAuthor(long user) throws UnexpectedException
    {
        try
        {
            MessageManager mm = m_da.getMessageManager();
            return mm.countMessagesGloballyByAuthor(user, this.getLoggedInUserId());
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
    }

    public long countSearchMessagesGlobally(String searchterm) 
    throws UnexpectedException
    {
        try
        {
            MessageManager mm = m_da.getMessageManager();
            return mm.countSearchMessagesGlobally(searchterm, this.getLoggedInUserId());
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
    }

    public long countGrepMessagesLocally(long conference, String searchterm) 
    throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        try
        {
            this.assertConferencePermission(conference, ConferencePermissions.READ_PERMISSION);
            MessageManager mm = m_da.getMessageManager();
            return mm.countGrepMessagesLocally(conference, searchterm);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
    }

    public long countSearchMessagesLocally(long conference, String searchterm) 
    throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        try
        {
            this.assertConferencePermission(conference, ConferencePermissions.READ_PERMISSION);
            MessageManager mm = m_da.getMessageManager();
            return mm.countSearchMessagesLocally(conference, searchterm);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
    }

    public long countAllMessagesLocally(long conference) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        try
        {
            this.assertConferencePermission(conference, ConferencePermissions.READ_PERMISSION);
            MessageManager mm = m_da.getMessageManager();
            return mm.countAllMessagesLocally(conference);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }	    
    }

	public MessageSearchResult[] listCommentsGloballyToAuthor(long user, Timestamp startDate, int offset, int length) throws UnexpectedException {
		try
		{
			return this.removeDuplicateMessages(this.censorMessages(m_da.getMessageManager().listCommentsGloballyToAuthor(user, startDate, offset, length)));
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

	public long countCommentsGloballyToAuthor(long user, Timestamp startDate) throws UnexpectedException 
    {
        try
        {
            MessageManager mm = m_da.getMessageManager();
            return mm.countCommentsGloballyToAuthor(user, this.getLoggedInUserId(), startDate);
        }
	    catch(SQLException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
	}

    public NameAssociation[] findObjects(String pattern) throws UnexpectedException
    {
        try
        {
            return this.censorNames(m_da.getNameManager().findObjects(pattern));
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

    public void changeKeywords(long id, String keywords) 
    throws UnexpectedException, ObjectNotFoundException, AuthorizationException
    {
        try
        {
            if(!this.canManipulateObject(id))
                throw new AuthorizationException();
            m_da.getNameManager().changeKeywords(id, keywords);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
    }

    public void changeEmailAlias(long id, String emailAlias) 
    throws UnexpectedException, ObjectNotFoundException, AuthorizationException
    {
        try
        {
            if(!this.canManipulateObject(id))
                throw new AuthorizationException();
            m_da.getNameManager().changeEmailAlias(id, emailAlias);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
    }
    
    
    public MessageLocator resolveLocator(MessageLocator message)
    throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        if(message == null)
            return this.getMostRelevantOccurrence(this.getCurrentConferenceId(), this.getCurrentMessage());
        if(!message.isValid())
            throw new ObjectNotFoundException("Invalid message locator"); // Should not happen...
        if(message.getGlobalId() == -1)
        {
            long conference = message.getConference() != -1 ?  message.getConference() : this.getCurrentConferenceId();
            return new MessageLocator(this.localToGlobal(conference, message.getLocalnum()), conference, message.getLocalnum());
        }
        else if(message.getLocalnum() == -1)
        {
            return this.getMostRelevantOccurrence(
                    message.getConference() == -1 ? this.getCurrentConferenceId() : message.getConference(),
                    message.getGlobalId());
        } 
        else
            return message;
    }
    
    private MessageOccurrence toOccurrence(MessageLocator message)
    throws ObjectNotFoundException, UnexpectedException
    {
        if(message instanceof MessageOccurrence)
            return (MessageOccurrence) message;
        try
        {
            return m_da.getMessageManager().loadMessageOccurrence(message.getConference(), message.getLocalnum());
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }
    }

    public void addBookmark(MessageLocator message, String annotation) 
    throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException
    {
        try
        {
            message = this.resolveLocator(message);
            m_da.getMessageManager().addBookmark(this.getLoggedInUserId(), message.getGlobalId(), annotation);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
    }

    public void deleteBookmark(MessageLocator message) 
    throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException
    {
        try
        {
            message = this.resolveLocator(message);
            m_da.getMessageManager().deleteBookmark(this.getLoggedInUserId(), message.getGlobalId());
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
    }

    public Bookmark[] listBookmarks() throws UnexpectedException
    {
        try
        {
            return m_da.getMessageManager().listBookmarks(this.getLoggedInUserId());
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(this.getLoggedInUserId(), e);
        }        
    }

	public MessageOccurrence postIncomingEmail(String sender, String receiver, Date sent, Date received, String subject, String content) throws EmailRecipientNotRecognizedException, EmailSenderNotRecognizedException, AuthorizationException, UnexpectedException 
	{
		// Make sure we hold the apropriate privileges
		//
		this.checkRights(UserPermissions.POST_ANYWHERE | UserPermissions.POST_BY_PROXY);
		
		UserManager um = m_da.getUserManager();
		NameManager nm = m_da.getNameManager();
		MessageManager mm = m_da.getMessageManager();
		try
		{
			// Try to map sender and receiver email
			//
			long senderId = um.matchEmailSender(sender);
			long receiverId = nm.findByEmailAlias(receiver);
			
			// Are we sending to a person or a conference?
			//
			MessageOccurrence occ;
			UnstoredMessage msg = new UnstoredMessage(subject, content);
			switch(this.getName(receiverId).getKind())
			{
			case NameManager.CONFERENCE_KIND:
				/// We need to check permissions if we're posting to a conference
				//
				if(!this.hasPermissionInConference(senderId, receiverId, ConferencePermissions.WRITE_PERMISSION))
					throw new AuthorizationException();
				occ = this.storeReplyAsMessage(senderId, receiverId, msg, -1);
				break;
			case NameManager.USER_KIND:
				// No need to check permissions here. Everyone can send mail
				//
				occ = this.storeReplyAsMail(senderId, receiverId, msg, -1);
				break;
			default:
				throw new IllegalStateException("Don't know how to handle message");
			}
			
			// Store sender email address, sent and received timestamps as message attributes
			//
			long id = occ.getGlobalId();
			mm.addMessageAttribute(id, MessageAttributes.EMAIL_SENDER, sender);
			if(sent != null)
			    mm.addMessageAttribute(id, MessageAttributes.EMAIL_SENT, Long.toString(sent.getTime()));
			if(received != null)
			    mm.addMessageAttribute(id, MessageAttributes.EMAIL_RECEIVED, Long.toString(received.getTime()));
			return occ;
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
	
    public SelectedMessages getSelectedMessages()
    {
        return m_selectedMessages;
    }

    public boolean selectGrepMessagesLocally(long conference, String searchterm)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.grepMessagesLocally(conference, searchterm, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }

    public boolean selectMessagesGlobally(String searchterm)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.searchMessagesGlobally(searchterm, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }

    public boolean selectMessagesGloballyByAuthor(long user)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.listMessagesGloballyByAuthor(user, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }

    public boolean selectMessagesLocally(long conference, String searchterm)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.searchMessagesLocally(conference, searchterm, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }

    public boolean selectMessagesLocallyByAuthor(long conference, long user)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.listMessagesLocallyByAuthor(conference, user, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }

    public boolean selectCommentsGloballyToAuthor(long user, Timestamp startDate)
            throws UnexpectedException
    {
        m_selectedMessages.setMessages(this.listCommentsGloballyToAuthor(user, startDate, 0, MAX_SELECTION), this.isSelectionReversed());
        return m_selectedMessages.getMessages().length < MAX_SELECTION;
    }
    
    private boolean isSelectionReversed()
    {
        return (this.getLoggedInUser().getFlags1() & UserFlags.SELECT_YOUNGEST_FIRST) == 0;
    }
}
