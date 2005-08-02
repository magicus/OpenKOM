/*
 * Created on Jun 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
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
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class WriteRules extends AbstractCommand 
{
	public WriteRules (Context context, String fullname, long permissions)
	{
		super (fullname, AbstractCommand.NO_PARAMETERS, permissions);
	}
	
	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
		if(!context.getSession().hasPermissionInCurrentConference(ConferencePermissions.ADMIN_PERMISSION))
			throw new AuthorizationException();
			
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit();
				
		// Store text
		//
		MessageOccurrence occ = context.getSession().storeRulePosting(msg);
		
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
