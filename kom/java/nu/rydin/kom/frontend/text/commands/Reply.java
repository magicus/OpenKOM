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
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Reply extends AbstractCommand
{
	public Reply(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(false) });	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
		// Check permissions
		//
		if(!context.getSession().hasPermissionInCurrentConference(ConferencePermissions.REPLY_PERMISSION))
			throw new AuthorizationException();

		// Parse parameters. No parameters means we're replying to the
		// last text read.
		//
		ServerSession session = context.getSession();

		long replyToId = -1;
		TextNumber replyTo = (TextNumber) parameterArray[0];
		if (replyTo == null)
		{
		    replyToId = session.getCurrentMessage();
		}
		else
		{
		    replyToId = session.getGlobalMessageId(replyTo);
		}
			
		// Get editor and execute it
		//
		MessageEditor editor = context.getMessageEditor();
		ConferenceInfo confInfo = session.getCurrentConference();
		if(confInfo.getReplyConf() != -1)
		    confInfo = session.getConference(confInfo.getReplyConf());
		editor.setRecipient(new NameAssociation(confInfo.getId(), confInfo.getName()));
		editor.setReplyTo(replyToId);
		UnstoredMessage msg = editor.edit(replyToId);
		
		// Store the message
		//
		MessageOccurrence occ = session.storeReply(editor.getRecipient().getId(), msg, replyToId);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
