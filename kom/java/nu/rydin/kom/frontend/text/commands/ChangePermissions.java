/*
 * Created on Nov 26, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.CantChangeMailboxPermissionsException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangePermissions extends AbstractCommand
{
	public ChangePermissions(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter("change.permissions.user.question", true) });
	}

    public void execute(Context context, Object[] parameterArray) 
    throws KOMException, IOException, InterruptedException
	{
		ServerSession session = context.getSession();
		MessageFormatter formatter = context.getMessageFormatter();
		KOMWriter out = context.getOut();
		long conference = session.getCurrentConferenceId();

		// Check that user is not trying to change rights in own mailbox.
		//
		if (conference == context.getLoggedInUserId())
		{
		    throw new CantChangeMailboxPermissionsException();
		}
		
        // Check that we have administrator rights in the current conference
		//
		session.assertModifyConference(conference);
			
		// Resolve user
		//
		NameAssociation userAssoc = (NameAssociation) parameterArray[0];
		long user = userAssoc.getId();
		
		// Load current permissions (if any) 
		//
		int old = session.getUserPermissionsInConference(user, conference);
		int permissions = 0;
		
		// Display some information about what the hell we're actually doing.
		//
		out.println(formatter.format("change.permissions.header", new Object[] { userAssoc.getName().getName(), session.getCurrentConference().getName() }));
		out.println();
		
		// Ask for permissions
		//
		permissions |= this.askForPermission(context, "permission.read", ConferencePermissions.READ_PERMISSION, old);
		permissions |= this.askForPermission(context, "permission.write", ConferencePermissions.WRITE_PERMISSION, old);
		permissions |= this.askForPermission(context, "permission.reply", ConferencePermissions.REPLY_PERMISSION, old);
		permissions |= this.askForPermission(context, "permission.delete", ConferencePermissions.DELETE_PERMISSION, old);			
		permissions |= this.askForPermission(context, "permission.administrator", ConferencePermissions.ADMIN_PERMISSION, old);
		
		// Set the permissions
		//
		session.setConferencePermissionsInCurrentConference(user, permissions);
	}
	
	protected int askForPermission(Context context, String format, int permissions, int old)
	throws IOException, InterruptedException, OperationInterruptedException
	{
		MessageFormatter formatter = context.getMessageFormatter();
		String error = formatter.format("create.conference.invalid.choice");
		String prefix = formatter.format("change.permissions.prefix");
		int defaultChoice = (old & permissions) == permissions ? 1 : 0; 
		int choice = context.getIn().getChoice(prefix + " " + formatter.format(format) + ": ", 
			new String[] { formatter.format("misc.no"), formatter.format("misc.yes")}, defaultChoice, error);
		return choice != 0 ? permissions : 0;
	}
}
