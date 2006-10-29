/*
 * Created on Jul 14, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class WriteMailReply extends AbstractCommand
{
    public WriteMailReply(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(false)}, permissions);
    }

	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
		// Parse parameters. No parameters means we're replying to the
		// last text read.
		//
        ServerSession session = context.getSession();
		MessageLocator textNumber = session.resolveLocator((MessageLocator) parameterArray[0]);
		
        MessageHeader mh = session.getMessageHeader(textNumber);
		
		// Get original message and conference
		//
        MessageOccurrence originalMessage;
        try
        {
            originalMessage = session.getMostRelevantOccurrence(session.getCurrentConferenceId(), mh.getId());
        }
        catch (ObjectNotFoundException e) 
        {
            throw new MessageNotFoundException();
        }
        ConferenceInfo originalConference = session.getConference(originalMessage.getConference());

		// Get editor and execute it
		//
        MessageEditor editor = context.getMessageEditor();
        editor.setRecipient(new NameAssociation(mh.getAuthor(), mh.getAuthorName()));
        editor.setReplyTo(textNumber);
        
        UnstoredMessage msg = editor.edit(
                textNumber,
                originalConference.getId(),
                originalConference.getName(),
                originalMessage.getUser().getId(), 
                originalMessage.getUser().getName(), 
                mh.getSubject()
                );

		// Store the message
		//
		MessageOccurrence occ = session.storeReplyAsMail(editor.getRecipient().getId(), msg, textNumber);
		
        context.getOut().println(
                context.getMessageFormatter().format("write.mail.saved", session.getUser(editor.getRecipient().getId()).getName()));

	}
}
