/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Reply extends AbstractCommand
{
	public Reply(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] parameters) 
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
		long newMessageId = -1;
		long replyTo = parameters.length == 0 
			? session.getCurrentMessage()
			: Long.parseLong(parameters[0]);
			
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(context, replyTo);
		
		// Store the message
		//
		MessageOccurrence occ = context.getSession().storeReplyInCurrentConference(msg, replyTo);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
