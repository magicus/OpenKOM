/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.TextNumber;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class WriteReply extends AbstractCommand
{
    public WriteReply(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(
                false) });
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();

        // First, we need to figure out which message we are replying to. If no 
        // parameter was given, that means we're replying to the last read message.
        //
        long replyToId = -1;
        TextNumber replyTo = (TextNumber) parameterArray[0];
        if (replyTo == null)
        {
            replyToId = session.getCurrentMessage();
        } 
        else
        {
            try
            {
                replyToId = session.getGlobalMessageId(replyTo);
            }
            catch (ObjectNotFoundException e) 
            {
                throw new MessageNotFoundException();
            }
        }

        // Second, we need to figure out which conference the original message was in.
        //
        MessageOccurrence originalMessage;
        try
        {
            originalMessage = session.getMostRelevantOccurrence(session.getCurrentConferenceId(), replyToId);
        }
        catch (ObjectNotFoundException e) 
        {
            throw new MessageNotFoundException();
        }
        
        ConferenceInfo originalConference = session.getConference(originalMessage.getConference());
        
        // Now, if the conference is the current user's mailbox, it means we should
        // make this a personal reply to the original author. Otherwise, we'll just
        // make this an ordinary reply and figure out which conference to put it in.
        //
        if (originalConference.getId() == context.getLoggedInUserId())
        {
            // Launch editor, get message, store message as mail.
            //
            MessageEditor editor = context.getMessageEditor();
            editor.setRecipient(originalMessage.getUser());
            editor.setReplyTo(replyToId);
            UnstoredMessage msg = editor.edit(replyToId);
            
            MessageOccurrence newMessage = session.storeReplyAsMail(editor.getRecipient().getId(), msg, replyToId);

            // Print confirmation
            //
            context.getOut().println(
                    context.getMessageFormatter().format("write.mail.saved", session.getUser(editor.getRecipient().getId()).getName()));
        }
        else
        {
            // It's an ordinary reply for which we need to figure out what conference
            // it should be stored in. This is very easy:
            // (1) orig. conf = A, A's reply-conf = none, user is in A --> store in A
            // (2) orig. conf = A, A's reply-conf = B, user is in A --> store in B
            // (3) orig. conf = A, A's reply-conf = none, user is in C --> store in C
            // (4) orig. conf = A, A's reply-conf = B, user is in C --> store in C
            ConferenceInfo recipient;
            if (session.getCurrentConferenceId() == originalConference.getId())
            {
                if (originalConference.getReplyConf() == -1)
                {
                    recipient = originalConference; //(1)
                }
                else
                {
                    recipient = session.getConference(originalConference.getReplyConf()); //(2)
                }
            }
            else
            {
                recipient = session.getCurrentConference(); //(3,4)
            }
            
            // Phew. NOW we can check if we actually have permission to reply here.
            //
            session.assertConferencePermission(recipient.getId(), ConferencePermissions.REPLY_PERMISSION);
            
            // Launch editor, get message, store message as reply.
            MessageEditor editor = context.getMessageEditor();
            editor.setRecipient(new NameAssociation(recipient.getId(), recipient.getName()));
            editor.setReplyTo(replyToId);
            UnstoredMessage msg = editor.edit(replyToId);

            MessageOccurrence newMessage = session.storeReplyAsMessage(editor.getRecipient().getId(), msg, replyToId);            

            // Print confirmation
            //
            context.getOut().println(
                    context.getMessageFormatter().format("write.message.saved",
                            new Integer(newMessage.getLocalnum())));
        }
    }
}