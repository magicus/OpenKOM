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
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.TextNumber;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class WriteMailReply extends AbstractCommand
{
    public WriteMailReply(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(false)});
    }

	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
		// Parse parameters. No parameters means we're replying to the
		// last text read.
		//
		TextNumber textNumber = (TextNumber) parameterArray[0];
		MessageHeader mh;
		ServerSession session = context.getSession();
		if (textNumber == null)
		{
		    mh = session.getMessageHeader(session.getCurrentMessage());
		}
		else
		{
		    mh = session.getMessageHeader(session.getGlobalMessageId(textNumber));
		}

		// Get editor and execute it
		//
        MessageEditor editor = context.getMessageEditor();
        editor.setRecipient(new NameAssociation(mh.getAuthor(), mh.getAuthorName()));
        editor.setReplyTo(mh.getId());
        UnstoredMessage msg = editor.edit(mh.getId());
		
		// Store the message
		//
		MessageOccurrence occ = session.storeReplyAsMail(editor.getRecipient().getId(), msg, mh.getId());
		
        context.getOut().println(
                context.getMessageFormatter().format("write.mail.saved", session.getUser(editor.getRecipient().getId()).getName()));

	}
}
