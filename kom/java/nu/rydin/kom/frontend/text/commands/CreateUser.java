/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CreateUser extends AbstractCommand
{
	public CreateUser(String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
		// Do we have the permission to do this?
		//
		context.getSession().checkRights(UserPermissions.CREATE_CONFERENCE);

		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();
		out.print(fmt.format("create.user.login"));		
		out.flush();
		String login = in.readLine();
		
		// Empty login? User interrupted
		//
		if(login.length() == 0)
			return;
		
		out.print(fmt.format("create.user.password"));
		out.flush();
		String password = in.readLine();
		out.print(fmt.format("create.user.fullname"));
		out.flush();
		String fullname = in.readLine();
		
		// Empty name? User interrupted
		//
		if(fullname.length() == 0)
			return;
		try
		{
			// TODO: Ask for rights!
			//
			context.getSession().createUser(login, password, fullname, "", "", "", "", "", "", "", "", "", 
				"ISO-8859-1", UserFlags.DEFAULT_FLAGS1, UserFlags.DEFAULT_FLAGS2, UserFlags.DEFAULT_FLAGS3, 
				UserFlags.DEFAULT_FLAGS4, UserPermissions.NORMAL);
		}
		catch(DuplicateNameException e)
		{
			out.println(context.getMessageFormatter().format("create.user.ambiguous", e.getMessage()));
		}
	}
}
