/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */

package nu.rydin.kom.soap.interfaces;

import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.soap.exceptions.SessionExpiredException;
import nu.rydin.kom.soap.structs.MessageOccurrence;
import nu.rydin.kom.soap.structs.SecurityToken;
import nu.rydin.kom.soap.structs.UnstoredMessage;

/**
 * @author Pontus Rydin
 */
public interface MessagePoster
{
	/**
	 * Stores a message in a conference
	 * 
	 * @param token A security token obtained from an Authenticator
	 * @param conf The conference to store the message in
	 * @param msg The message
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeMessage(SecurityToken token, long conf, UnstoredMessage msg)
	throws ObjectNotFoundException, AuthorizationException, UnexpectedException, SessionExpiredException;

	/**
	 * Stores a message as a personal mail to a user. May store a copy in the
	 * senders mailbox if that flag is set. 
	 * 
	 * @param token A security token obtained from an Authenticator
	 * @param recipient The id of the receiving user
	 * @param msg The message
	 * @return  Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */	
	public MessageOccurrence storeMail(SecurityToken token, long recipient, UnstoredMessage msg)
	throws ObjectNotFoundException, UnexpectedException, SessionExpiredException;

	/**
	 * Stores a reply to a message in a conference
	 * 
	 * @param token A security token obtained from an Authenticator
	 * @param conference The conference to store the message in
	 * @param msg The message
	 * @param replyTo Global message id of the message replied to
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 * @throws AuthorizationException
	 */
	public MessageOccurrence storeReplyAsMessage(SecurityToken token, long conference, UnstoredMessage msg, long replyTo)
	throws ObjectNotFoundException, UnexpectedException, AuthorizationException, SessionExpiredException;
	
	/**
	 * Stores a message as a personal mail to a user. May store a copy in the
	 * senders mailbox if that flag is set.
	 * 
	 * @param token A security token obtained from an Authenticator
	 * @param recipient The id of the receiving user
	 * @param msg The message
	 * @param replyTo Global message id of the message replied to
	 * @return Newly created message occurrence
	 * @throws ObjectNotFoundException
	 * @throws UnexpectedException
	 */
	public MessageOccurrence storeReplyAsMail(SecurityToken token, long recipient, UnstoredMessage msg, long replyTo)
	throws ObjectNotFoundException, UnexpectedException, SessionExpiredException;
}
