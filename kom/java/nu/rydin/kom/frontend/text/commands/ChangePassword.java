/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangePassword extends AbstractCommand
{
	public ChangePassword(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) });
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException
	{
		ServerSession session = context.getSession();
		long user;
		if(parameterArray[0] != null)
		{
			session.checkRights(UserPermissions.USER_ADMIN);
			NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
			user = nameAssociation.getId();
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
}
