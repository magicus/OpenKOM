/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.TextNumber;

/**
 * @author Henrik Schröder
 */
public class NoComment extends AbstractCommand
{
	public NoComment(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(false)});	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		// Check permissions
		//
		if(!context.getSession().hasPermissionInCurrentConference(ConferencePermissions.REPLY_PERMISSION))
			throw new AuthorizationException();

		// Parse parameters. No parameters means we're "not commenting" to the
		// last text read.
		//
		TextNumber textNumber = (TextNumber) parameterArray[0];
		long message;
		if (textNumber == null) {
		    message = context.getSession().getCurrentMessage();
		} else {
		    message = textNumber.getNumber();
		    // FIXME:Ihse: Handle global numbers...
		}
			
		// Store the "no comment"
		//
		context.getSession().storeNoComment(message);
		context.getOut().println(context.getMessageFormatter().format("no.comment.saved"));
	}
}
