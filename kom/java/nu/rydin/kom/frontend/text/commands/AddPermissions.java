/*
 * Created on Nov 26, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AddPermissions extends AbstractCommand
{
	public AddPermissions(String fullName)
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		// Check that we have enough parameters
		//
		if(parameters.length == 0)
			throw new MissingArgumentException();
			
		// Check that we have administrator rights in the current conference
		//
		ServerSession session = context.getSession();
		if(!session.hasPermissionInCurrentConference(ConferencePermissions.ADMIN_PERMISSION))
			throw new AuthorizationException();
			
		// Resolve user
		//
		long user = NamePicker.resolveNameToId(NameUtils.assembleName(parameters), NameManager.USER_KIND, context);
		
		// Load current permissions (if any) 
		//
		int old = session.getUserPermissionsInConference(user, session.getCurrentConferenceId());
		int permissions = 0;
		
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
	throws IOException, InterruptedException
	{
		MessageFormatter formatter = context.getMessageFormatter();
		String error = formatter.format("create.conference.invalid.choice");
		String prefix = formatter.format("add.permissions.prefix");
		int defaultChoice = (old & permissions) == permissions ? 1 : 0; 
		int choice = context.getIn().getChoice(prefix + " " + formatter.format(format) + ": ", 
			new String[] { formatter.format("misc.no"), formatter.format("misc.yes")}, defaultChoice, error);
		return choice != 0 ? permissions : 0;
	}
	
	public boolean acceptsParameters()
	{
		return true; 
	}
}
