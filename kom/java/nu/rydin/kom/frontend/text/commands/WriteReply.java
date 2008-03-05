/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class WriteReply extends AbstractCommand
{
    public WriteReply(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(
                false), new RawParameter("", false) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();

        // First order of business, find out how many parameters we were given, and unless
        // it's two or zero, which one of them the user thinks (s)he supplied.
        //
        boolean hasId = false;
        boolean hasType = false;
        
        // The parser doesn't fill the parameterArray from the start, it puts the supplied
        // parameters in their respective location. Which is generally good, but occasionally
        // leads to interesting solutions, such as this one :-)
        //
        int parmcount = 0;
        if (null != parameterArray[0]) ++parmcount;
        if (null != parameterArray[1]) ++parmcount;

        // The usual case is no parameters.
        //
        if (0 != parmcount)
        {
            if (2 == parmcount)
            {
                // We got both. Good, that makes things much easier, since we know in
                // what order they were passed.
                //
                hasId = true;
                hasType = true;
            }
            else
            {
                if (null != parameterArray[0]) 
                    hasId = true;
                else
                    hasType = true;
            }
        }
        
        // After figuring out what parameters, if any, we were given, things can progress
        // the way they usually do.
        // First, we need to figure out which message we are replying to. If no 
        // parameter was given, that means we're replying to the last read message.
        //
        long replyToId = -1;
        MessageLocator replyTo = session.resolveLocator((MessageLocator) parameterArray[0]);
        try
        {
            replyToId = session.getGlobalMessageId(replyTo);
        }
        catch (ObjectNotFoundException e) 
        {
            if (hasId)
            {
                // If we were given a numeric parameter that couldn't be resolved, throw
                // an exception. Otherwise, just keep going.
                throw new MessageNotFoundException();
            }
        }

        // Second, we need to figure out which conference the original message was in.
        //
        MessageOccurrence originalMessage;
        MessageOccurrence newMessage;
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
            // Update state
            //
            session.setActivity(Activities.MAIL, true);
            session.setLastObject(originalMessage.getUser().getId());
            
            // Launch editor, get message, store message as mail.
            //
            MessageEditor editor = context.getMessageEditor();
            editor.setRecipient(originalMessage.getUser());
            editor.setReplyTo(replyTo);
            UnstoredMessage msg;
            try
            {
                msg = editor.edit(
                        replyTo, 
                        originalConference.getId(),
                        originalConference.getName(),
                        originalMessage.getUser().getId(), 
                        originalMessage.getUser().getName(), 
                        context.getSession().getMessageHeader(replyTo).getSubject()
                        );
            }
            finally
            {
                session.restoreState();
            }

            newMessage = session.storeReplyAsMail(editor.getRecipient().getId(), msg, replyTo);

            // Print confirmation
            //
            context.getOut().println(
                    context.getMessageFormatter().format("write.mail.saved", session.getUser(editor.getRecipient().getId()).getName()));
        }
        else
        {
            ConferenceInfo recipient;
            if((context.getCachedUserInfo().getFlags1() & UserFlags.REPLY_IN_CURRENT_CONF) == 0
                 || session.getCurrentConferenceId() == originalConference.getId())
            {
                // In same conference or user always want replies in same conference
                //
                if (originalConference.getReplyConf() == -1)
                    recipient = originalConference; 
                else
                    recipient = session.getConference(originalConference.getReplyConf()); 
           }
            else
            {
                recipient = session.getCurrentConference(); 
                if(recipient.getReplyConf() != -1)
                    recipient = session.getConference(recipient.getReplyConf());
            } 
            
            // Phew. NOW we can check if we actually have permission to reply here.
            //
            session.assertConferencePermission(recipient.getId(), ConferencePermissions.REPLY_PERMISSION);

            // Before we do anything useful, why don't we just update the state..
            //
            session.setActivity(Activities.POST, true);
            
            // Launch editor, get message, store message as reply.
            
            MessageEditor editor = context.getMessageEditor();
            editor.setRecipient(new NameAssociation(recipient.getId(), recipient.getName()));
            editor.setReplyTo(replyTo);
            UnstoredMessage msg;
            try
            {
                msg = editor.edit(
                    replyTo, 
                    recipient.getId(),
                    recipient.getName(),
                    originalMessage.getUser().getId(), 
                    originalMessage.getUser().getName(), 
                    context.getSession().getMessageHeader(replyTo).getSubject()
                    );
            }
            finally
            {
                session.restoreState();
            }

            newMessage = session.storeReplyAsMessage(editor.getRecipient().getId(), msg, replyTo);            

            // Print confirmation
            //
            context.getOut().println(
                    context.getMessageFormatter().format("write.message.saved",
                            new Object[] { 
                            	new Integer(newMessage.getLocalnum()),
                            	new Long(newMessage.getGlobalId()) } ));
        }
        
        // Just one more thing to do. Did we get a comment type that we need to set?
        //
        if (hasType)
        {
            session.addMessageAttribute(newMessage.getGlobalId(), MessageAttributes.COMMENT_TYPE, (String) parameterArray[1], false);
        }
    }
}