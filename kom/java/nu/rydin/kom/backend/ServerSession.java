/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.SQLException;

import nu.rydin.kom.AllRecipientsNotReachedException;
import nu.rydin.kom.AlreadyMemberException;
import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.NoCurrentMessageException;
import nu.rydin.kom.NoMoreMessagesException;
import nu.rydin.kom.NoMoreNewsException;
import nu.rydin.kom.NotAReplyException;
import nu.rydin.kom.NotLoggedInException;
import nu.rydin.kom.NotMemberException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferencePermission;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.MembershipListItem;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.NamedObject;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.structs.UserListItem;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
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
	 * 
	 * @throws UnexpectedException
	 * @throws AmbiguousNameException
	 * @throws DuplicateNameException
	 */	
	public void createConference(String fullname, int permissions, short visibility, long replyConf)
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
	 * @param flags User flags
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
		long flags, long rights)
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
	 * Reads the "original message" of the current message, i.e. the message
	 * to which it is a reply
	 *
	 * @throws NoCurrentMessageException If there was no current message
	 * @throws NotAReplyException If the current message is not a reply
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public Envelope readOriginalMessage()
	throws NoCurrentMessageException, NotAReplyException, ObjectNotFoundException, UnexpectedException;

	/**
	 * Stores a personal mail to a user. Also stores copy in the senders mailbox.
	 * 
	 * @param msg The message
	 * @param user The receiving user
	 * @return  Newly create message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence storeMail(UnstoredMessage msg, long user)
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
	 * Liest the conferences the specified user is a member of
	 * 
	 * @param userId The global id of the user
	 * @throws ObjectNotFoundException If the user could not be found
	 * @throws UnexpectedException
	 */	
	public NameAssociation[] listMemberships(long userId)
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
	 * Sends a chat message to a specified user.
	 * 
	 * @param userId The id of the receiving user
	 * @param message The message
	 * @throws NotLoggedInException
	 */	
	public void sendChatMessage(long userId, String message)
	throws NotLoggedInException;
	
	/**
	 * Sends a chat message to multiple recipients (users and conferences).
	 * @param destinations The intended message destinations
	 * @param message The message
	 */
	public void sendMulticastMessage (long destinations[], String message)	
	throws NotLoggedInException, ObjectNotFoundException, AllRecipientsNotReachedException;
	
	/**
	 * Broadcasts a chat message to all logged in users
	 * @param message The message
	 */
	public void broadcastChatMessage(String message);
		
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
}