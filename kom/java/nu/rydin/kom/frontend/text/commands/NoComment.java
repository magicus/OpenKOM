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

/**
 * @author Henrik Schröder
 */
public class NoComment extends AbstractCommand
{
	public NoComment(String fullName)
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

		// Parse parameters. No parameters means we're "not commenting" to the
		// last text read.
		//
		MessageFormatter formatter = context.getMessageFormatter();
		ServerSession session = context.getSession();
		long message = parameters.length == 0 
			? session.getCurrentMessage()
			: Long.parseLong(parameters[0]); //TODO (skrolle) Convert local to global messageid
			
		// Store the "no comment"
		//
		context.getSession().storeNoComment(message);
		context.getOut().println(context.getMessageFormatter().format(
			"no.comment.saved"));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
