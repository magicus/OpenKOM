/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangePassword extends AbstractCommand
{
	public ChangePassword(String fullName)
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException
	{
		ServerSession session = context.getSession();
		long user;
		if(parameters.length != 0)
		{
			session.checkRights(UserPermissions.USER_ADMIN);
			user = NamePicker.resolveName(parameters[0], UserManager.USER_KIND, context);
		}
		else
			user = context.getLoggedInUserId();
		
		// Ask for password and confirmation
		//
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter formatter = context.getMessageFormatter();
		String currentPassword = null;
		if(user == context.getLoggedInUserId())
		{
			out.print(formatter.format("change.password.current"));
			out.flush();
			currentPassword = in.readPassword();
			
			// TODO: Pre-authenticate user (for user convenience)
		}
		out.print(formatter.format("change.password.new"));
		out.flush();
		String newPassword = in.readPassword();
		out.print(formatter.format("change.password.confirm"));
		out.flush();
		String confirmation = in.readPassword();
		
		// Check that passwords match
		//
		if(!newPassword.equals(confirmation))
		{
			out.println(formatter.format("change.password.mismatch"));
			return;
		}
			
		// Everything seems ok. Execute!
		//
		session.changePassword(user, currentPassword, newPassword);
		out.println(formatter.format("change.password.confirmation"));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
