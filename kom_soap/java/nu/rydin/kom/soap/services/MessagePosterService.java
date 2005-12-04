/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.services;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.soap.exceptions.SessionExpiredException;
import nu.rydin.kom.soap.interfaces.MessagePoster;
import nu.rydin.kom.soap.structs.MessageOccurrence;
import nu.rydin.kom.soap.structs.SecurityToken;
import nu.rydin.kom.soap.structs.UnstoredMessage;
import nu.rydin.kom.soap.support.SessionRegistry;

/**
 * @author Pontus Rydin
 */
public class MessagePosterService implements MessagePoster
{
    public MessageOccurrence storeMessage(SecurityToken token, long conf, UnstoredMessage msg) throws ObjectNotFoundException, AuthorizationException, UnexpectedException, SessionExpiredException
    {
        ServerSession ss = SessionRegistry.instance().get(token);
        return new MessageOccurrence(ss.storeMessage(conf, msg.toNative()));
    }

    public MessageOccurrence storeMail(SecurityToken token, long recipient, UnstoredMessage msg) throws ObjectNotFoundException, UnexpectedException, SessionExpiredException
    {
        ServerSession ss = SessionRegistry.instance().get(token);
        return new MessageOccurrence(ss.storeMail(recipient, msg.toNative()));
    }

    public MessageOccurrence storeReplyAsMessage(SecurityToken token, long conference, UnstoredMessage msg, long replyTo) throws ObjectNotFoundException, UnexpectedException, AuthorizationException, SessionExpiredException
    {
        ServerSession ss = SessionRegistry.instance().get(token);
        return new MessageOccurrence(ss.storeReplyAsMessage(conference, msg.toNative(), replyTo));
    }

    public MessageOccurrence storeReplyAsMail(SecurityToken token, long recipient, UnstoredMessage msg, long replyTo) throws ObjectNotFoundException, UnexpectedException, SessionExpiredException
    {
        ServerSession ss = SessionRegistry.instance().get(token);
        return new MessageOccurrence(ss.storeReplyAsMail(recipient, msg.toNative(), replyTo));
    }
}
