/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.SQLException;

import nu.rydin.kom.events.Event;
import nu.rydin.kom.exceptions.AllRecipientsNotReachedException;
import nu.rydin.kom.exceptions.AlreadyMemberException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.NoMoreMessagesException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
import nu.rydin.kom.exceptions.NoRulesException;
import nu.rydin.kom.exceptions.NotAReplyException;
import nu.rydin.kom.exceptions.NotLoggedInException;
import nu.rydin.kom.exceptions.NotMemberException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferencePermission;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.LocalMessageHeader;
import nu.rydin.kom.structs.MembershipInfo;
import nu.rydin.kom.structs.MembershipListItem;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.NamedObject;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.structs.UserListItem;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public interface ServerSession 
{
	/**
	 * Suggested action: No suggestion
	 */
	public static final short NO_ACTION = 0;

	/**
	 * Suggested action: Read next reply
	 */
	public static final short NEXT_REPLY = 1;

	/**
	 * Suggested action: Read next message
	 */
	public static final short NEXT_MESSAGE = 3;

	/**
	 * Suggested action: Go to next conference
	 */
	public static final short NEXT_CONFERENCE = 4;

	/**
	 * Changes the current conference.
	 * 
	 * @param id The id of the new conference
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 */
	public abstract void setCurrentConferenceId(long id)
		throws SQLException, ObjectNotFoundException;

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
	 * Given the current context, suggest a next action
	 *
	 * @return NO_ACTION, NEXT_REPLY, NEXT_MESSAGE or NEXT_CONFERENCE. 
	 * @throws UnexpectedException
	 */
	public abstract short suggestNextAction() 
	throws UnexpectedException;

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
	 * @param id The kind (conference or user)
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
	 * @param messageId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	
	public Envelope readLastMessage()
	throws ObjectNotFoundException, UnexpectedException;

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
	 * Retrieves the next unread message in the current conference and marks it as read
	 * 
	 * @throws NoMoreMessagesException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readNextMessage()
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
	 * @param visibility Visibility level.
	 * @param replyConf Conference to send replies to. -1 if same conference.
	 * @return New conference ID.
	 * @throws UnexpectedException
	 * @throws AmbiguousNameException
	 * @throws DuplicateNameException
	 */	
	public long createConference(String fullname, int permissions, short visibility, long replyConf)
	throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException;

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
	 * Stores a message in the current conference
	 * 
	 * @param msg The message
	 * @return Newly create message occurrence
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeMessage(UnstoredMessage msg)
	throws AuthorizationException, UnexpectedException;
	
	/**
	 * Store a magic message. Conference determined by kind of magic.
	 * 
	 * @param msg Unstored message.
	 * @param kind Magic type (see ConferenceManager.MAGIC_XXXXXXXX).
	 * @return The local occurrence.
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	
	public MessageOccurrence storeMagicMessage(UnstoredMessage msg, short kind, long object)
	throws UnexpectedException, AuthorizationException;

	/**
	 * Reads a magic message.
	 * 
	 * @param kind Kind of magic (user presentation, conference presentation or note).
	 * @param object Object identifier (unser or conference ID).
	 * @return The envelope around the latest matching message.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public Envelope readMagicMessage(short kind, long object)
	throws UnexpectedException, ObjectNotFoundException;

	/**
	 * Stores a reply to a message
	 * 
	 * @param conference Target conference
	 * @param msg The message
	 * @param replyTo Global essage id of the message replied to
	 * @return Newly create message occurrence
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	public MessageOccurrence storeReply(long conference, UnstoredMessage msg, long replyTo)
	throws UnexpectedException, AuthorizationException;

	/**
	 * Stores a reply to a message. The target conference is the current one.
	 * 
	 * @param msg The message
	 * @param replyTo Global essage id of the message replied to
	 * @return Newly create message occurrence
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	public MessageOccurrence storeReplyInCurrentConference(UnstoredMessage msg, long replyTo)
	throws AuthorizationException, UnexpectedException;
	
	/**
	 * Stores a reply to a message refered to by its conference and local number
	 * 
	 * @param msg The message
	 * @param replyToConfId The conference of the message replied to
	 * @param replyToLocalnum The local message number of the message replied to
	 * @return Newly create message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */	
	public MessageOccurrence storeReplyToLocal(UnstoredMessage msg, long replyToConfId, int replyToLocalnum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Stores a reply to a message in the current conference
	 * 
	 * @param msg The message
	 * @param replyToLocalnum The local message number in the current conference of
	 * the message replied to.
	 * @return Newly create message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeReplyToLocalInCurrentConference(UnstoredMessage msg, int replyToLocalnum)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;	

	/**
	 * Stores a reply to the last message read
	 * @param msg The message
	 * @return Newly create message occurrence
	 * @throws NoCurrentMessageException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence storeReplyToCurrentMessage(UnstoredMessage msg)
	throws AuthorizationException, NoCurrentMessageException, UnexpectedException;

    /**
     * Stores a "no comment" to the given message
     * @param message Global message id of the message not commented
     * @return
     */
    public void storeNoComment(long message)
    throws AuthorizationException, NoCurrentMessageException, UnexpectedException;

    /**
     * Stores a "no comment" to the last message read
     * @return
     */
    public void storeNoCommentToCurrentMessage()
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
	 * Stores a personal mail to a user. Also stores copy in the senders mailbox.
	 * 
	 * @param msg The message
	 * @param user The receiving user
	 * @param replyTo Global id of message to which this is a reply, or -1 if not a reply.
	 * @return  Newly create message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence storeMail(UnstoredMessage msg, long user, long replyTo)
	throws ObjectNotFoundException, UnexpectedException;

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
	 * Signs up for a conference, i.e. makes the current user a member of it.
	 * 
	 * @param conferenceId
	 * @return The name of the conference
	 * @throws ObjectNotFoundException
	 * @throws AlreadyMemberException
	 * @throws UnexpectedException
	 */	
	public String signup(long conferenceId)
	throws ObjectNotFoundException, AlreadyMemberException, UnexpectedException, AuthorizationException;

	/**
	 * Signs off from a conference.
	 * @param conferenceId Object identifier.
	 * @return Name of conference.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws NotMemberException
	 */

	public String signoff(long conferenceId)
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
	 * List the conferences the specified user is a member of
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
	 * @return An array of Strings containing the member names.
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public String[] listMemberNamesByConference(long confId)
	throws ObjectNotFoundException, UnexpectedException;
	
	
	/**
	 * Gets the name of a <tt>NamedObject</tt> by its id.
	 * 
	 * @param id The id
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public String getName(long id)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Gets the names of a set of <tt>NamedObject</tt> by their ids.
	 * 
	 * @param id The id
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public String[] getNames(long[] id)
	throws ObjectNotFoundException, UnexpectedException;

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
	 * List conferences containing unread messages
	 * 
	 * @throws UnexpectedException
	 */	
	public MembershipListItem[] listNews()
	throws UnexpectedException;
	
	/**
	 * Pools for an event. 
	 * 
	 * @param timeoutMs Timeout, in milliseconds. If no event could be
	 * delivered within this timeframe, <tt>null</tt> is returned.
	 * @return
	 */
	public Event pollEvent(int timeoutMs)
	throws InterruptedException;
	
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
	 * Sends a chat message to multiple recipients (users and conferences).
	 * @param destinations The intended message destinations
	 * @param message The message
	 * @return An array of NameAssociations of users that refused the message
	 */
	public NameAssociation[] sendMulticastMessage (long destinations[], String message)	
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
	 * @return An array of NameAssociations of users that refused the message
	 */
	public NameAssociation[] broadcastChatMessage(String message)
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
	 */
	public int getPermissionsInConference(long conferenceId)
	throws UnexpectedException;
	
	/**
	 * Returns the permissions mask in effect for the specified user in the
	 * specified conference
	 *
	 * @param userId The user id 
	 * @param conferenceId The conference id
	 * @throws UnexpectedException
	 */
	public int getUserPermissionsInConference(long userId, long conferenceId)
	throws UnexpectedException;

	/**
	 * Returns the permissions mask in effect for the logged in user in the
	 * current conference
	 * 
	 * @throws UnexpectedException
	 */	
	public int getPermissionsInCurrentConference()
	throws UnexpectedException;

	/**
	 * Checks if the currently logged on user has the permissions in the
	 * specified conference.
	 * 
	 * @param conferenceId The id of the conference
	 * @param mask The permission mask
	 * @throws SQLException
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
	 * @throws SQLException
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
	 * Moves a message to another conference. As with copyMessage(), the message isn't moved
	 * as such, only the occurrence is changed. The source occurrence is dropped and a destination
	 * occurrence is added. Furthermore, a matching MessageAttribute is created, in which we store
	 * the source conference ID.
	 * 
	 * @param localNum Local text number
	 * @param sourceConfId Conference to move from
	 * @param destConfId Conference to move to.
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void moveMessage(int localNum, long sourceConfId, long destConfId)
	throws AuthorizationException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Moves a message from the current conference to another conference. As with copyMessage(), 
	 * the message isn't moved as such, only the occurrence is changed. The source occurrence is 
	 * dropped and a destination occurrence is added. Furthermore, a matching MessageAttribute is 
	 * created, in which we store the source conference ID.
	 * 
	 * @param localNum Local text number
	 * @param destConfId Conference to move to.
	 * @throws AuthorizationException
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public void moveMessage(int localNum, long destConfId)
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
	 * Changes the password of a user.
	 * @param userId The id of the user to change the password for
	 * @param oldPassword The old password. Not checked if the caller holds the USER_ADMIN
	 * privilege.
	 * @param newPassword The new password
	 */
	public void changePassword(long userId, String oldPassword, String newPassword)
	throws ObjectNotFoundException, AuthorizationException, AuthenticationException, UnexpectedException;	
	
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
	 * Lists the messages in the given conference. Note that not all fields in the MessageHeader
	 * array returned contain legal values.
	 * 
	 * @param conference Conference Id
	 * @param start First row (given as offset from top) to return
	 * @param length Number of rows to return
	 * @return An array of MessageHeaders
	 * @throws UnexpectedException
	 */
	public MessageHeader[] listMessagesInConference(long conference, int start, int length)
	throws UnexpectedException;
	
	/**
	 * This method lists the messages in the users current conference.
	 * 
	 * @param start First row (given as offset from top) to return
	 * @param length Number of rows to return
	 * @return An array of MessageHeaders
	 * @throws UnexpectedException
	 */
	public MessageHeader[] listMessagesInCurrentConference(int start, int length)
	throws UnexpectedException;
	
	/**
	 * Returns the last message head.
	 * 
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	
	public MessageHeader getLastMessageHeader()
	throws ObjectNotFoundException, UnexpectedException;

	/**
	 * Skips all messages with the given subject in all conferences the user is a member of.
	 * 
	 * @param subject Subject to skip
	 * @return Number of texts skipped
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */
	public int skipMessagesBySubject (String subject)
	throws UnexpectedException, ObjectNotFoundException;
	
	/**
	 * Skips all messages in the tree rooted at the node given.
	 * 
	 * @param node Root of tree to skip.
	 * @return Number of texts skipped.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */	
	public int skipTree (long node)
	throws UnexpectedException, ObjectNotFoundException;
	
	/**
	 * This method drops a conference, including all message occurrences (and, sometimes, messages)
	 * stored in it. Refer to the dropMessageOccurrence source for a discussion on when the message,
	 * as opposed to the occurrence, is dropped.
	 * 
	 * @param conference Conference Id.
	 * @throws UnexpectedException
	 */
	public void deleteConference (long conference)
	throws UnexpectedException;
	
	public void createMagicConference (String fullname, int permissions, short visibility, long replyConf, short kind)
	throws DuplicateNameException, UnexpectedException, AuthorizationException, AmbiguousNameException;

	public long getMagicConference (short kind)
	throws ObjectNotFoundException, UnexpectedException;

	public boolean isMagicConference (long conference)
	throws ObjectNotFoundException, UnexpectedException;
	
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
	 * List all messages by specified user
	 * @return
	 * @throws UnexpectedException
	 */
	public LocalMessageHeader[] listGlobalMessagesByUser(long userId, int offset, int length)
	throws UnexpectedException;

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
	 * Returns an array of messages (chat or broadcast) from the message log
	 * 
	 * @param kind The message kind (char or broadcast)
	 * @param limit Maximum number of messages to return
	 * @throws UnexpectedException
	 */
	public MessageLogItem[] getMessagesFromLog(short kind, int limit)
	throws UnexpectedException;
	
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
    public MessageSearchResult[] searchMessagesInConference(long conference, String searchterm, int offset, int length)
    throws UnexpectedException;
    
    /**
     * Returns the HeartbeatListener associated with this session.
     */
    public HeartbeatListener getHeartbeatListener();

}
