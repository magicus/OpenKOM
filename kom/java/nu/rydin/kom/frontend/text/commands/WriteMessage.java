/*
 * Created on Oct 15, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class WriteMessage extends AbstractCommand
{
	public WriteMessage(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
		// Check permissions
		//
		if(!context.getSession().hasPermissionInCurrentConference(ConferencePermissions.WRITE_PERMISSION))
			throw new AuthorizationException();
			
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(-1);
				
		// Store text
		//
		MessageOccurrence occ = context.getSession().storeMessage(msg);
		context.getOut().println();
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
