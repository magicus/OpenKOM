/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL licens.
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
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.TextNumber;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Reply extends AbstractCommand
{
	public Reply(String fullName)
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
		MessageFormatter formatter = context.getMessageFormatter();
		ServerSession session = context.getSession();

		long replyToId = -1;
		TextNumber replyTo = (TextNumber) parameterArray[0];
		if (replyTo == null)
		{
		    replyToId = session.getCurrentMessage();
		}
		else
		{
		    replyToId = replyTo.getNumber();
		}
			
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(context, replyToId);
		
		// Store the message
		//
		MessageOccurrence occ = context.getSession().storeReplyInCurrentConference(msg, replyToId);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
