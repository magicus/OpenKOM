/*
 * Created on Jun 5, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeSuffix extends AbstractCommand
{

	public ChangeSuffix(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		long id = parameters.length != 0 
			? NamePicker.resolveNameToId(NameUtils.assembleName(parameters), (short) -1, context)
			: -1;
			
		// Are we trying to change the suffix of another user? We need the
		// CHANGE_ANY_NAME to do that!
		//
		ServerSession session = context.getSession();
		if(id != -1)
			session.checkRights(UserPermissions.CHANGE_ANY_NAME);
		
		// Set up
		//
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		
		String oldName = NameUtils.stripSuffix(session.getName(id != -1 ? id : context.getLoggedInUserId())).trim();
		
		// Print prompt
		//
		out.print(formatter.format("change.suffix.prompt", oldName));
		out.flush();
		
		// Read new suffix
		//
		String suffix = in.readLine();
		
		// Execute
		//
		if(id == -1)
			session.changeSuffixOfLoggedInUser(suffix);
		else
			session.changeSuffixOfUser(id, suffix);
		
		// Print confirmation
		//
		out.println(formatter.format("change.suffix.confirmation", oldName));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
