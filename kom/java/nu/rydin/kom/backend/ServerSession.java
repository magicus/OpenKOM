/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.Timestamp;

import nu.rydin.kom.events.Event;
import nu.rydin.kom.exceptions.*;
import nu.rydin.kom.structs.*;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public interface ServerSession 
{
    /**
     * Returns the unique session id
     */
    public int getSessionId();
    
    /**
     * Returns the type of client that created us
     */
    public short getClientType();
    
	/**
	 * Changes the current conference.
	 * 
	 * @param id The id of the new conference
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public abstract void setCurrentConferenceId(long id)
		throws UnexpectedException, ObjectNotFoundException;

	/**
	 * Returns information about the current conference
	 */		
	public abstract ConferenceInfo getCurrentConference();
	
	/**
	 * Returns the id of the current conference
	 */
	public abstract long getCurrentConferenceId();
	
	/**
	 * Returns information about the user currently logged on
	 */
	public abstract UserInfo getLoggedInUser();
	
	/**
	 * Returns the id of the user currently logged on
	 */	
	public abstract long getLoggedInUserId();
	
	/**
	 * Returns the system time when the current user logged in
	 */
	public abstract long getLoginTime();
	
	/**
	 * Returns the session state, i.e. suggested action, a "focused" conference
	 * and number of unread messages in tha conference.
	 * 
	 * @throws UnexpectedException
	 */
	public abstract SessionState getSessionState() 
	throws UnexpectedException;
	
	/** 
	 * Checks for permissions in a conference and throws an
	 * <tt>AuthorizationException</tt> if the calling user does not
	 * have the requested permissions in that conference.
	 * 
	 * @param conferenceId The conference
	 * @param mask The permission mask to check for
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void assertConferencePermission(long conferenceId, int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Checks if the current user has the permissions to modify the given
	 * conference and throws an AuthorizationException if not.
	 * This function takes user privileges into consideration, and not
	 * only conference permissions.
	 * 
	 * @param conferenceId
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void assertModifyConference(long conferenceId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns name associations (of users or conferences) based on a pattern.
	 * 
	 * @param pattern The pattern
	 * @throws UnexpectedException
	 */	
	public NameAssociation[] getAssociationsForPattern(String pattern)
	throws UnexpectedException;

	/**
	 * Returns name associations of a certain kind based on a pattern.
	 * 
	 * @param pattern The pattern
	 * @param kind The kind (conference or user)
	 * @throws UnexpectedException
	 */		
	public NameAssociation[] getAssociationsForPatternAndKind(String pattern, short kind)
	throws UnexpectedException;
	
	/**
	 * Counts the number of unread messages the current user has in a conference.
	 * 
	 * @param conference The id of the conference
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public int countUnread(long conference)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Retrievs a message and marks it as unread
	 * 
	 * @param messageId The gloval message id
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope innerReadMessage(long messageId)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Re-read the last read message.
	 * 
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws NoCurrentMessageException
	 */
	
	public Envelope readLastMessage()
	throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException;

	/**
	 * Retrievs a message and marks it as unread
	 * 
	 * @param localnum Local number in the current conference
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public Envelope readLocalMessage(int localnum)
	throws ObjectNotFoundException, UnexpectedException;
	

	/**
	 * Retrievs a message and marks it as unread
	 * 
	 * @param conf Id of the conference
	 * @param localnum Local number in conference
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readLocalMessage(long conf, int localnum)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Retrieves the next unread message in the specified conference and marks it as read
	 * @param conf The conference if
	 * 
	 * @throws NoMoreMessagesException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readNextMessage(long conf)
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException;


	/**
	 * Retrieves the next unread message in the current conference and marks it as read
	 * 
	 * @throws NoMoreMessagesException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readNextMessageInCurrentConference()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Retrieves the next reply in a depth-first fashion and marks it as read.
	 * 
	 * @throws NoMoreMessagesException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readNextReply()
	throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Creates a conference and makes the current user a member and the administrator of it.
	 *  
	 * @param fullname The full name of the conference
	 * @param permissions Default permissions
	 * @param nonmemberPermissions Default permissions for non-members
	 * @param visibility Visibility level.
	 * @param replyConf Conference to send replies to. -1 if same conference.
	 * @return New conference ID.
	 * @throws UnexpectedException
	 * @throws AmbiguousNameException
	 * @throws DuplicateNameException
	 */	
	public long createConference(String fullname, int permissions, int nonmemberPermissions, short visibility, long replyConf)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException;
	
	/**
	 * Returns a list of conferences, sorted by the date of the last text.
	 * @throws UnexpectedException
	 */
	public ConferenceListItem[] listConferencesByDate()
	throws UnexpectedException;

	/**
	 * Returns a list of conferences, sorted by their normalized name.
	 * @throws UnexpectedException
	 */
	public ConferenceListItem[] listConferencesByName()
	throws UnexpectedException;

	/**
	 * Creates a new user.
	 * 
	 * @param userid The login id
	 * @param password The password
	 * @param fullname Full name of user
	 * @param address1 First address line
	 * @param address2 Second address line
	 * @param address3 Third address line
	 * @param address4 Fourth address line
	 * @param phoneno1 Phone number
	 * @param phoneno2 Alternate phone number
	 * @param email1 Email
	 * @param email2 Alternate email
	 * @param url URL to homepage
	 * @param flags1 Flagword 1
	 * @param flags2 Flagword 2
	 * @param flags3 Flagword 3
	 * @param flags4 Flagword 4
	 * @param rights User privilege bits
	 * 
	 * @throws UnexpectedException
	 * @throws AmbiguousNameException
	 * @throws DuplicateNameException
	 * @throws AuthorizationException
	 */	
	public void createUser(String userid, String password, String fullname, String address1,
		String address2, String address3, String address4, String phoneno1, 
		String phoneno2, String email1, String email2, String url, String charset, 
		long flags1, long flags2, long flags3, long flags4, long rights)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException;
	
	/**
	 * Changes the contact info of a user.
	 * 
	 * @param ui
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void changeContactInfo(UserInfo ui)
	throws ObjectNotFoundException, UnexpectedException, AuthorizationException;

	/**
	 * Changes the default conference scope
	 * 
	 * @param id The id of the conference to move to
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 * @throws NotMemberException
	 */	
	public void gotoConference(long id)
	throws UnexpectedException, ObjectNotFoundException, NotMemberException;

	/**
	 * Changes the default conference scope to the next conference that
	 * has unread messages.
	 *
	 * @throws NoMoreNewsException
	 * @throws UnexpectedException
	 */	
	public long gotoNextConference()
	throws NoMoreNewsException, UnexpectedException;
	
	/**
	 * Stores a message in a conference
	 * 
	 * @param conf The conference to store the message in
	 * @param msg The message
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeMessage(long conf, UnstoredMessage msg)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;

	/**
	 * Stores a message as a personal mail to a user. May store a copy in the
	 * senders mailbox if that flag is set. 
	 * 
	 * @param recipient The id of the receiving user
	 * @param msg The message
	 * @return  Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence storeMail(long recipient, UnstoredMessage msg)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Stores a reply to a message in a conference
	 * 
	 * @param conference The conference to store the message in
	 * @param msg The message
	 * @param replyTo Global message id of the message replied to
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	public MessageOccurrence storeReplyAsMessage(long conference, UnstoredMessage msg, long replyTo)
	throws ObjectNotFoundException, UnexpectedException, AuthorizationException;
	
	/**
	 * Stores a message as a personal mail to a user. May store a copy in the
	 * senders mailbox if that flag is set.
	 * 
	 * @param recipient The id of the receiving user
	 * @param msg The message
	 * @param replyTo Global message id of the message replied to
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeReplyAsMail(long recipient, UnstoredMessage msg, long replyTo)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Stores a presentation of an object
	 * 
	 * @param msg Unstored message.
	 * @param object The object id
	 * @return The local occurrence.
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */	
	public MessageOccurrence storePresentation(UnstoredMessage msg, long object)
	throws UnexpectedException, AuthorizationException, ObjectNotFoundException;

	/**
	 * Reads a tagged message.
	 * 
	 * @param tag Message tag (user presentation, conference presentation or note).
	 * @param object Object identifier (unser or conference ID).
	 * @return The envelope around the latest matching message.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public Envelope readTaggedMessage(short tag, long object)
	throws UnexpectedException, ObjectNotFoundException;

    /**
     * Stores a "no comment" to the given message
     * @param message Global message id of the message not commented
     * @return
     */
    public void storeNoComment(long message)
    throws AuthorizationException, NoCurrentMessageException, UnexpectedException;

    /**
     * Reads a message identified by a global message id.
     * 
     * @param globalId The global message id
     * 
     * @throws ObjectNotFoundException
     * @throws AuthorizationException
     * @throws UnexpectedException
     */
	public Envelope readGlobalMessage(long globalId)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;
    
	/**
	 * Reads the "original message" of the current message, i.e. the message
	 * to which it is a reply
	 *
	 * @throws NoCurrentMessageException If there was no current message
	 * @throws NotAReplyException If the current message is not a reply
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readOriginalMessage()
	throws NoCurrentMessageException, NotAReplyException, ObjectNotFoundException, AuthorizationException, UnexpectedException;

	/**
	 * Returns an occurrence of the specified message in the specified conference, or, 
	 * if the message does not exist in that conference, the first occurrence.
	 * 
	 * @param conferenceId The preferred conference
	 * @param globalNum The global message number
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence globalToLocalInConference(long conferenceId, long globalNum)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns an occurrence of the specified message in the current conference, or,
	 * if the message does not exist in that conference, the first occurrence.
	 * 
	 * @param globalNum The global message number 
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence globalToLocal(long globalNum)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns the global message id given a conference and a local number.
	 * 
	 * @param conference The conference
	 * @param localNum The local number
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public long localToGlobal(long conference, int localNum)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns the global message id given a local number in the current conference.
	 * 
	 * @param localNum
	 * @return
	 */
	
	public long localToGlobalInCurrentConference(int localNum)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Returns the global message id given a TextNumber object.
	 * If the TextNumber object contains a local number, it will 
	 * try to look it up in the current conference.
	 * 
	 * @param textnumber
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public long getGlobalMessageId(TextNumber textnumber)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns the global id of the last read message.
	 * 
	 * @throws NoCurrentMessageException
	 */		
	public long getCurrentMessage()
	throws NoCurrentMessageException;

	/**
	 * Returns an occurrence of the last read message, either in the current conference, or, 
	 * if no such occurrence could be found, the first occurrence.
	 * 
	 * @throws NoCurrentMessageException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence getCurrentMessageOccurrence()
	throws NoCurrentMessageException, UnexpectedException;		

	/**
	 * Returns the occurrence of the given message that is most relevant given
	 * the conference.
	 * @param conferenceId
	 * @param messageId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence getMostRelevantOccurrence(long conferenceId, long messageId) 
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns the original occurrence of a message, which is defined as:
	 * 1) If there is one with kind=ACTION_CREATED, pick it, otherwise
	 * 2) Pick the one with kind=ACTION_MOVED, or if that also does not exist,
	 * 3) Throw ObjectNotFoundException. 
	 * 
	 * @param messageId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence getOriginalMessageOccurrence(long messageId)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Signs up for a conference, i.e. makes the current user a member of it.
	 * 
	 * @param conferenceId
	 * @return The name of the conference
	 * @throws ObjectNotFoundException
	 * @throws AlreadyMemberException
	 * @throws UnexpectedException
	 */	
	public Name signup(long conferenceId)
	throws ObjectNotFoundException, AlreadyMemberException, UnexpectedException, AuthorizationException;

	/**
	 * Signs off from a conference.
	 * @param conferenceId Object identifier.
	 * @return Name of conference.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws NotMemberException
	 */

	public Name signoff(long conferenceId)
	throws ObjectNotFoundException, UnexpectedException, NotMemberException;
	
	/**
	 * Re-prioritizes conferences for the current user, placing the given conference 
	 * at the position of the given targetconference, and shifting every conference 
	 * in between either up or down.
	 * @param conference
	 * @param targetconference
	 * @return The number of steps the given conference was shuffled, negative means it was shuffled down.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws NotMemberException
	 */
	public long prioritizeConference(long conference, long targetconference)
	throws ObjectNotFoundException, UnexpectedException, NotMemberException;
	
	/**
	 * Returns a user record based on a global id
	 * @param userId The global id
	 * 
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public UserInfo getUser(long userId)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Returns a conference record based on a global id
	 * @param conferenceId The global id
	 * 
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public ConferenceInfo getConference(long conferenceId)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns a named object, i.e. a conference or user, matching
	 * a global id.
	 * @param id The global id
	 * 
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public NamedObject getNamedObject(long id)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * List the conferences the specified user is a member of in 
	 * order of prioritization
	 * 
	 * @param userId The global id of the user
	 * @throws ObjectNotFoundException If the user could not be found
	 * @throws UnexpectedException
	 */	
	public NameAssociation[] listMemberships(long userId)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * List the members in the specified conference.
	 * @param confId Conference identifier.
	 * @return An array of MembershipInfo items.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MembershipInfo[] listConferenceMembers(long confId)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * List the members in the specified conference.
	 * @param confId Conference identifier.
	 * @return An array of NameAssociations containing the member names.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public NameAssociation[] listMembersByConference(long confId)
	throws ObjectNotFoundException, UnexpectedException;
	
	
	/**
	 * Gets the name of a <tt>NamedObject</tt> by its id.
	 * 
	 * @param id The id
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Name getName(long id)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Gets the names of a set of <tt>NamedObject</tt> by their ids.
	 * 
	 * @param id The id
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public Name[] getNames(long[] id)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Marks all messages in a thread as unread
	 * @param root The root of the thread
	 * @return The number of messages marked as unread
	 * @throws UnexpectedException
	 */
	public int markThreadAsUnread(long root)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Marks all messages with a matching subject as unread
	 * 
	 * @param subject The subject
	 * @param localOnly Look in current conference only
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public int markSubjectAsUnread(String subject, boolean localOnly)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Queues up a message to be marked as unread at logout.
	 * 
	 * @param messageId The message id
	 * @throws UnexpectedException
	 */
	public void markAsUnreadAtLogout(long messageId)
	throws UnexpectedException;
	
	/**
	 * Queues up a message to be marked as unread at logout.
	 * 
	 * @param localNum The local id
	 * @throws UnexpectedException
	 */
	public void markAsUnreadAtLogoutInCurrentConference(int localnum)
	throws UnexpectedException;
	
	/**
	 * Changes the number of unread messages in the current conference. Destroys
	 * any previous read-message-markers.
	 * 
	 * @param nUnread Wanted number of unread messages
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public void changeUnread(int nUnread)
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Rolls back the <i>n</i> latest reads, marking the read messages
	 * as unread. If n > the total number of messages read, all messages
	 * read during the session are marked as unread.
	 * 
	 * @param n Number of messages to process
	 * @return Number of messages actually processed
	 * @throws UnexpectedException
	 */
	public int rollbackReads(int n)
	throws UnexpectedException;

	/**
	 * List conferences containing unread messages
	 * 
	 * @throws UnexpectedException
	 */	
	public MembershipListItem[] listNews()
	throws UnexpectedException;
		
	/**
	 * List unread texts for the given user.
	 * 
	 * @param userId User ID.
	 * @throws UnexpectedException
	 */
	
	public MembershipListItem[] listNewsFor(long userId)
	throws UnexpectedException;
	/**
	 * Returns an array of <tt>UserListItems</tt> with the user currently logged in.
	 * @return
	 */
	public UserListItem[] listLoggedInUsers()
	throws UnexpectedException;

	/**
	 * Returns true if the given user currently has an active session. 
	 * @param userId User ID.
	 */
	public boolean hasSession (long userId);
	
	/**
	 * Returns the <tt>EventSource</tt> i.e. an object returning event objects
	 * when they are ready to be picked up.
	 */
	public EventSource getEventSource();
		
	/**
	 * Sends a chat message to multiple recipients (users and conferences).
	 * @param destinations The intended message destinations
	 * @param message The message
	 * @param logAsMulticas 'true' if message should be logged as multicas 
	 * (i.e. message to everyone in a conference)
	 * @return An array of NameAssociations of users that refused the message
	 */
	public NameAssociation[] sendMulticastMessage (long destinations[], String message,
	        boolean logAsMulticast)	
	throws NotLoggedInException, ObjectNotFoundException, AllRecipientsNotReachedException, UnexpectedException;

	/**
	 * Verifies a list of chat recipients and returns an array of status codes.
	 * See nu.rydin.kom.constants.ChatStatusCodes for explanation.
	 * 
	 * @param recepipents List of recipients
	 * @return List of status codes
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public int[] verifyChatRecipients(long recepipents[])
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Broadcasts a chat message to all logged in users
	 * @param message The message
	 * @param kind The message kind
	 * @return An array of NameAssociations of users that refused the message
	 */
	public NameAssociation[] broadcastChatMessage(String message, short kind)
	throws UnexpectedException;
		
	/**
	 * Posts an event to the session-private event queue. Not intended to 
	 * be called by client code.
	 * 
	 * @param e The  event
	 */
	public void postEvent(Event e);
	
	/**
	 * Shuts down this session
	 */
	public abstract void close() 
	throws UnexpectedException;
    
    /**
     * Detaches a server session from any frontends it may be attached to,
     * but keeps the session alive.
     * 
     * @throws UnexpectedException
     */
    public abstract void detach()
    throws UnexpectedException;

	/**
	 * Persistently updates the character set setting for the current user
	 * 
	 * @param charsetname of the new character set
	 * @throws UnexpectedException
	 */	
	public void updateCharacterset(String charset)
	throws UnexpectedException;
	
	/**
	 * Persistently updates the time zone setting of the current user.
	 * 
	 * @param timeZone The new time zone
	 * @throws UnexpectedException
	 */
	public void updateTimeZone(String timeZone)
	throws UnexpectedException;
	
	/**
	 * Changes the permissions of a user in a conference
	 * 
	 * @param conf The id of the conference
	 * @param user The is of the user
	 * @param permissions The permission bitmap
	 * @throws UnexpectedException
	 */	
	public void setConferencePermissions(long conf, long user, int permissions)
	throws UnexpectedException;

	/**
	 * Sets the permissions of a user in the current conference
	 * 
	 * @param user
	 * @param permissions
	 * @throws UnexpectedException
	 */	
	public void setConferencePermissionsInCurrentConference(long user, int permissions)
	throws UnexpectedException;

	/**
	 * Returns a conference permissions of a user.
	 * 
	 * @param conf The id of the conference
	 * @param user The id of the user
	 * @throws UnexpectedException
	 */
	public void revokeConferencePermissions(long conf, long user)
	throws UnexpectedException;	

	/**
	 * Revokes all permissions from a user in the current conference
	 * 
	 * @param user The id of the user
	 * @throws UnexpectedException
	 */
	public void revokeConferencePermissionsInCurrentConference(long user)
	throws UnexpectedException;
	
	/**
	 * Lists all users with permissions in the specified conference, along with their
	 * permission bits.
	 * 
	 * @param conf The id of the conference
	 * @throws UnexpectedException
	 */
	public ConferencePermission[] listConferencePermissions(long conf)
	throws UnexpectedException;

	/**
	 * Lists all users with permissions in the current conference, along with their
	 * permission bits.
	 * 
	 * @throws UnexpectedException
	 */	
	public ConferencePermission[] listConferencePermissionsInCurrentConference()
	throws UnexpectedException;
	
	/**
	 * Returns the permissions mask in effect for the logged in user in the
	 * specified conference
	 * 
	 * @param conferenceId The conference id
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public int getPermissionsInConference(long conferenceId)
	throws UnexpectedException, ObjectNotFoundException;
	
	/**
	 * Returns the permissions mask in effect for the specified user in the
	 * specified conference
	 *
	 * @param userId The user id 
	 * @param conferenceId The conference id
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public int getUserPermissionsInConference(long userId, long conferenceId)
	throws UnexpectedException, ObjectNotFoundException;

	/**
	 * Returns the permissions mask in effect for the logged in user in the
	 * current conference
	 * 
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */	
	public int getPermissionsInCurrentConference()
	throws UnexpectedException, ObjectNotFoundException;

	/**
	 * Checks if the currently logged on user has the permissions in the
	 * specified conference.
	 * 
	 * @param conferenceId The id of the conference
	 * @param mask The permission mask
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 */	
	public boolean hasPermissionInConference(long conferenceId, int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Checks if the currently logged on user has the permissions in the
	 * current conference.
	 * 
	 * @param mask The permission mask
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 */
	public boolean hasPermissionInCurrentConference(int mask)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Checks user rights and throws an exeption if the logged in user
	 * does not have the permissions specified in the mask.
	 * 
	 * @param mask The required permiessions
	 * @throws AuthorizationException
	 */	
	public void checkRights(long mask)
	throws AuthorizationException;
	
	/**
	 * Checks if a user with a certain userid exists.
	 * @param userid The userid to check for
	 * @return <tt>true</tt> if the user exists
	 */
	public boolean checkForUserid(String userid)
	throws UnexpectedException;

	/**
	 * Change reply-to conference.
	 * @param originalConferenceId Conference to change reply-to conference for
	 * @param newReplyToConferenceId Use -1 to clear reply-to conference
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
    public void changeReplyToConference(long originalConferenceId, long newReplyToConferenceId)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Copies a message to another conference. The message itself isn't really
	 * copied, but a new MessageOccurrence record is created to form a "symbolic link"
	 * to the message.
	 * 
	 * @param globalNum Global message number to copy
	 * @param conferenceId Id of conference to copy to
	 */
	public void copyMessage(long globalNum, long conferenceId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Moves a message to another conference. It is only possible to move the original
	 * occurrence, and not any copies of a message, therefore this method tries to
	 * locate the original occurrence, drop it, and create a new moved occurrence in
	 * the destination conference. Furthermore, a matching MessageAttribute is 
	 * created, in which we store the source conference ID.
	 * 
	 * @param messageId Message to move.
	 * @param destConfId Conference to move to.
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void moveMessage(long messageId, long destConfId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Delete the given local message occurrence (and the message itself, if this was the
	 * last occurrence).
	 * 
	 * @param localNum Local text number
	 * @param conference Conference ID
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void deleteMessage(int localNum, long conference)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Delete the given local message occurrence (and the message itself, if this was the
	 * last occurrence).
	 * 
	 * @param localNum Local text number
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void deleteMessageInCurrentConference(int localNum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Adds a message attribute to a message
	 * 
	 * @param message The global message id
	 * @param attribute The attribute type
	 * @param payload The attribute payload
	 * @param deleteOld Delete previous occurrences of this attribute
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	public void addMessageAttribute(long message, short attribute, String payload, boolean deleteOld)
	throws UnexpectedException, AuthorizationException;
	
	/**
	 * Returns various debug information
	 * @return
	 */
	public String getDebugString();

	/**
	 * Renames an object
	 * @param id The id of the object 
	 * @param newName The new name
	 * @throws DuplicateNameException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void renameObject(long id, String newName)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException;
	
	/**
	 * Changes the suffix of the logged in user
	 * @param suffix The new suffix
	 * @throws DuplicateNameException
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public void changeSuffixOfLoggedInUser(String suffix)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException;

	/**
	 * Changes the suffix of a specified user.
	 * @param id The ide of the user  
	 * @param suffix The new suffix
	 * @throws DuplicateNameException
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */	
	public void changeSuffixOfUser(long id, String suffix)
	throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException;

	/**
	 * Returns true if the current user has the right to change the name of
	 * the specified object.
	 * @param id The object id.
	 * @throws DuplicateNameException
	 * @throws UnexpectedException
	 */
	public boolean userCanChangeNameOf(long id)
	throws DuplicateNameException, UnexpectedException;

	/**
	 * Returns the if the current user can manipulate an object, e.g. change
	 * the presentation of it or delete it
	 * 
	 * @param object The id of the object to check
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public boolean canManipulateObject(long object)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Updates conference permissions and visibility
	 * @param id Id of conference to change
	 * @param permissions New permission mask
	 * @param nonmemberpermissions New permission mask for non-members
	 * @param visibility New visibility
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public void updateConferencePermissions(long id, int permissions, int nonmemberpermissions,
	        short visibility)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;

	/**
	 * Changes the password of a user.
	 * @param userId The id of the user to change the password for
	 * @param oldPassword The old password. Not checked if the caller holds the USER_ADMIN
	 * privilege.
	 * @param newPassword The new password
	 */
	public void changePassword(long userId, String oldPassword, String newPassword)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException, BadPasswordException;	
	
	/**
	 * Sets or resets user flags of the logged in user
	 * @param set The flags to set
	 * @param reset The flags to reset
	 */
	public void changeUserFlags(long[] set, long[] reset)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Sets or resets user permissions of the logged in user
	 * @param user The user to change
	 * @param set The permissions to set
	 * @param reset The permissions to reset
	 */
	public void changeUserPermissions(long user, long set, long reset)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;	

	/**
	 * Lists the messages in the given conference.
	 * 
	 * @param conference Conference Id
	 * @param start First row (given as offset from top) to return
	 * @param length Number of rows to return
	 * @return An array of LocalMessageSearchResults
	 * @throws UnexpectedException
	 */
	public LocalMessageSearchResult[] listAllMessagesLocally(long conference, int start, int length)
	throws UnexpectedException;
	
	/**
	 * Counts the number of messages in a conference
	 * 
	 * @param conference The conference
	 * @return The number of messages
	 * @throws UnexpectedException
	 */
	public long countAllMessagesLocally(long conference)
	throws UnexpectedException, AuthorizationException, ObjectNotFoundException;

	/**
	 * Lists the messages in the given conference written by the given user.
	 * 
	 * @param conference Conference Id
	 * @param start First row (given as offset from top) to return
	 * @param length Number of rows to return
	 * @return An array of LocalMessageSearchResults
	 * @throws UnexpectedException
	 */
	public LocalMessageSearchResult[] listMessagesLocallyByAuthor(long conference, long user, int start, int length)
	throws UnexpectedException;
	
	/**
	 * Returns the approximate number of messages in the given conference written by the given user.
	 * @param conference The conference
	 * @param user The user 
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public long countMessagesLocallyByAuthor(long conference, long user)
	throws UnexpectedException, AuthorizationException, ObjectNotFoundException;
	
	/**
	 * List all messages written by a given user.
	 * 
	 * @param user
	 * @param offset
	 * @param length
	 * @return An array of GlobalMessageSearchResults
	 * @throws UnexpectedException
	 */
    public GlobalMessageSearchResult[] listMessagesGloballyByAuthor(long user, int offset, int length)
	throws UnexpectedException;
    
    /**
     * Returns the approximate number of messages written by a cerain user. Counts only messages in 
     * conferences the caller is a member of.
     * 
     * @param user The user
     * @throws UnexpectedException
     */
    public long countMessagesGloballyByAuthor(long user)
	throws UnexpectedException;
    
    /**
     * Returns an array of results from doing a global search on the given searchterm.
     * 
     * @param searchterm
     * @param offset
     * @param length
     * @throws UnexpectedException
     */
    public GlobalMessageSearchResult[] searchMessagesGlobally(String searchterm, int offset, int length)
	throws UnexpectedException;
    
    /**
     * Returns the approximate number of messages matching a given search term
     * @param searchterm The search term
     * @throws UnexpectedException
     */
    public long countSearchMessagesGlobally(String searchterm)
	throws UnexpectedException;

	/**
	 * Returns the last message head.
	 * 
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	
	public MessageHeader getLastMessageHeader()
	throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException;

	/**
	 * Skips all messages with the given subject in all conferences the user is a member of.
	 * 
	 * @param subject Subject to skip
	 * @return Number of texts skipped
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public int skipMessagesBySubject (String subject, boolean skipGlobal)
	throws UnexpectedException, NoCurrentMessageException, ObjectNotFoundException;
	
	/**
	 * Skips an entire thread.
	 * 
	 * @param msg A message in the thread to skip
	 * @return Number of texts skipped.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */	
	public int skipThread (long msg)
	throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException, 
	SelectionOverflowException;
	
	/**
	 * Skips all messages in the tree rooted at the node given.
	 * 
	 * @param node Root of tree to skip.
	 * @return Number of texts skipped.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */	
	public int skipBranch (long node)
	throws UnexpectedException, NoCurrentMessageException, ObjectNotFoundException;

	
	/**
	 * This method drops a conference, including all message occurrences (and, sometimes, messages)
	 * stored in it. Refer to the dropMessageOccurrence source for a discussion on when the message,
	 * as opposed to the occurrence, is dropped.
	 * 
	 * @param conference Conference Id.
	 * @throws UnexpectedException
	 */
	public void deleteConference (long conference)
	throws AuthorizationException, UnexpectedException;
		
	public short getObjectKind (long conference)
	throws ObjectNotFoundException;
	
	/**
	 * Returns the Envelope for the last rule posting (which is the last message which has a 
	 * MessageAttribute.Kind of ATTR_RULEPOST in the specified conference.
	 * 
	 * @param conference Conference ID.
	 * @return Message envelope.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public Envelope getLastRulePostingInConference (long conference)
	throws ObjectNotFoundException, NoRulesException, UnexpectedException;

	/**
	 * Returns the Envelope for the last rule posting (which is the last message which has a
	 * MessageAttribute.Kind of ATTR_RULEPOST) in the current conference.
	 * 
	 * @return Message envelope.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public Envelope getLastRulePosting()
	throws ObjectNotFoundException, NoRulesException, UnexpectedException;

	/**
	 * Store a rule posting (an ordinary message containg a message attribute of the kind
	 * ATTR_RULEPOST) in the current conference.
	 * 
	 * @param msg The message to store.
	 * @return The occurrence info (local message identifer and so on).
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public MessageOccurrence storeRulePosting(UnstoredMessage msg)
	throws AuthorizationException, UnexpectedException, ObjectNotFoundException;

	/**
	 * Set last login date/time
	 * 
	 * @throws UnexpectedException
	 */	
	public void updateLastlogin()
	throws ObjectNotFoundException, UnexpectedException;

	/** 
	 * Returns the message header for a message identified by a global id.
	 * 
	 * @param globalId The global id
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public MessageHeader getMessageHeader(long globalId)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;
	
	/**
	 * Returns a message header for a message identified by a conference and a local number.
	 * @param conference The conference id
	 * @param localNum Local message number in conference
	 *
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public MessageHeader getMessageHeaderInConference(long conference, int localNum)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;
	
	/**
	 * Returns a message header of a message with the specified local number in the
	 * current conference.
	 * 
	 * @param localNum The local message number
	 * @return
	 */
	public MessageHeader getMessageHeaderInCurrentConference(int localNum)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException;
	
	/**
	 * Returns an array of chat messages from the message log
	 *
	 * @param limit Maximum number of messages to return
	 * @throws UnexpectedException
	 */
	public MessageLogItem[] getChatMessagesFromLog(int limit)
	throws UnexpectedException;
	
	/**
	 * Returns an array of multicast messages from the message log
	 *
	 * @param limit Maximum number of messages to return
	 * @throws UnexpectedException
	 */
	public MessageLogItem[] getMulticastMessagesFromLog(int limit)
	throws UnexpectedException;	

	/**
	 * Returns an array of broadcast messages from the message log
	 *
	 * @param limit Maximum number of messages to return
	 * @throws UnexpectedException
	 */
	public MessageLogItem[] getBroadcastMessagesFromLog(int limit)
	throws UnexpectedException;
    
    /**
     * Does a simple grep-like search in the given conference.
     * 
     * @param conference
     * @param searchterm
     * @param offset
     * @param length
     * @return
     */
    public LocalMessageSearchResult[] grepMessagesLocally(long conference, String searchterm, int offset, int length)
    throws UnexpectedException;
    
    /**
     * Count approximate number of hits with a grep-like search
     * @param conference
     * @param searchterm
     * @return
     * @throws UnexpectedException
     * @throws ObjectNotFoundException
     */
    public long countGrepMessagesLocally(long conference, String searchterm)
    throws UnexpectedException, AuthorizationException, ObjectNotFoundException;

    
	/**
	 * Returns an array of results from doing a search in the given conference
	 * with the given searchterm.
	 * 
	 * @param conference The id of the conference to search in
	 * @param searchterm The searchterm in MySQL IN BOOLEAN MODE format.
	 * @param offset
	 * @param length
	 * @throws UnexpectedException
	 */
    public abstract LocalMessageSearchResult[] searchMessagesLocally(long conference, String searchterm, int offset, int length)
    throws UnexpectedException;
    
    /**
     * Counts the approximate number of hits for a search term in the current conference.
     * 
     * @param conference The conference to searh
     * @param searchterm The search term
     * @return
     * @throws UnexpectedException
     * @throws ObjectNotFoundException
     */
    public abstract long countSearchMessagesLocally(long conference, String searchterm)
    throws UnexpectedException, AuthorizationException, ObjectNotFoundException;
    
    /**
     * Lists the user log for all users, sorted by date (descending)
     * 
     * @param start The start date
     * @param end The end date
     * @param offset List offset
     * @param length List limit
     * @throws UnexpectedException
     */
    public UserLogItem[] listUserLog(Timestamp start, Timestamp end, int offset, int length)
    throws UnexpectedException;

    /**
     * Lists the user log for a specific user, sorted by date (descending)
     * 
     * @param user The id of the user
     * @param start The start date
     * @param end The end date
     * @param offset List offset
     * @param length List limit
     * @throws UnexpectedException
     */
    public UserLogItem[] listUserLog(long user, Timestamp start, Timestamp end, int offset, int length)
    throws UnexpectedException;
    
    /**
     * Checks the status of a file
     * 
     * @param parent The id of the parent object
     * @param name The name of the file
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public FileStatus statFile(long parent, String name)
    throws ObjectNotFoundException, UnexpectedException;
    
    /**
     * Lists files under a specified parent matching a pattern.
     * 
     * @param parent The id of the parent object
     * @param pattern The pattern to search for
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public FileStatus[] listFiles(long parent, String pattern)
    throws ObjectNotFoundException, UnexpectedException;
    
    /**
     * Returns the contents of a file
     * 
     * @param parent The id of the parent object
     * @param name The name of the file
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public String readFile(long parent, String name)
    throws ObjectNotFoundException, AuthorizationException, UnexpectedException;
    
    /**
     * Stores content in a file. If a file with the specified name
     * did not exist, a new file is created.
     * 
     * @param parent The id of the parent object
     * @param name The file name
     * @param content The content
     * @param permissions The permissions
     * @throws UnexpectedException
     */
    public void storeFile(long parent, String name, String content, int permissions)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
    
    /**
     * Deletes a file.
     * 
     * @param parent The id of the parent object 
     * @param name The file name
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public void deleteFile(long parent, String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
    
    /**
     * Reads the contents of a named system file.
     * 
     * @param name The file name
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public String readSystemFile(String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
    
    /**
     * Stores a system file.
     * 
     * @param name The file name
     * @param content The content
     * @throws AuthorizationException
     * @throws UnexpectedException
     */
    public void storeSystemFile(String name, String content)
    throws AuthorizationException, UnexpectedException;
    
    /**
     * Deletes a system file.
     * 
     * @param name The file name
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public void deleteSystemFile(String name)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

    /**
     * Returns the HeartbeatListener associated with this session.
     */
    public HeartbeatListener getHeartbeatListener();
    
    /**
     * Returns the date and time of the last heartbeat received
     */
    public long getLastHeartbeat();

    /**
     * Kills a user session. Tries to request the frontend to shut down
     * the connection to the user.
     * 
     * @param sessionId The session we want to shutdown
     * @throws AuthenticationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public void killSession(int sessionId)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
    
    /**
     * Kills all user sessions.
     * 
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public void killAllSessions()
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
    
    /**
     * Prohibits logins for users other than sysop.
     * 
     * @throws AuthenticationException
     */
    public void prohibitLogin()
    throws AuthorizationException;

    /**
     * Allows login for everyone.
     * 
     * @throws AuthenticationException
     */
    public void allowLogin()
    throws AuthorizationException;
    
    /**
     * Returns information and statistics about the system.
     */
    public SystemInformation getSystemInformation()
    throws UnexpectedException;
    
    /**
     * Changes a setting having a string value.
     * 
     * @param name The name of the setting
     * @param value The value
     * @throws UnexpectedException
     */
    public void changeSetting(String name, String value)
    throws AuthorizationException, UnexpectedException;

    /**
     * Changes a setting having a numeric value.
     * 
     * @param name The name of the setting
     * @param value The value
     * @throws UnexpectedException
     */
    public void changeSetting(String name, long value)
    throws AuthorizationException, UnexpectedException;
    
    /**
     * Creates a user filter, causing us to ignore certain objects
     * and events from a user.
     * 
     * @param jinge The user to ignore
     * @param flags What to ignore
     */
    public void createUserFilter(long jinge, long flags)
    throws ObjectNotFoundException, UnexpectedException;
    
    /**
     * Drops a user filter, i.e. stop ignoring objects and events 
     * from a user.
     * @param user The user
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    public void dropUserFilter(long user)
    throws ObjectNotFoundException, UnexpectedException;
    
    /**
     * Lists the user filters in effect for the logged in user.
     * 
     * @throws UnexpectedException
     */
    public Relationship[] listFilters()
    throws UnexpectedException;
    
    /**
     * Clears all caches
     */
    public void clearCache()
    throws AuthorizationException;
    
    /**
     * Allows self registration.
     * 
     * @throws AuthorizationException
     * @throws UnexpectedException
     */
    public void enableSelfRegistration()
    throws AuthorizationException, UnexpectedException;
    
    /**
     * Disallows self registration.
     * 
     * @throws AuthorizationException
     * @throws UnexpectedException
     */
    public void disableSelfRegistration()
    throws AuthorizationException, UnexpectedException;

    /**
     * Searches and returns the messages that are comments to messages written by the given user.
     * 
     * @param user
     * @param offset
     * @param length
     * @return
     * @throws UnexpectedException
     */
	public abstract MessageSearchResult[] listCommentsGloballyToAuthor(long user, Timestamp startDate, int offset, int length)
	throws UnexpectedException;

	/**
	 * Counts the number of messages that are comments to messages written by the given user.
	 * 
	 * @param user
	 * @return
	 * @throws UnexpectedException
	 */
	public abstract long countCommentsGloballyToAuthor(long user, Timestamp startDate)
	throws UnexpectedException;
}
