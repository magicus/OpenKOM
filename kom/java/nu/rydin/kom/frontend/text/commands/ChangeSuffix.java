/*
 * Created on Jun 5, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import com.sun.corba.se.ActivationIDL.InitialNameServicePackage.NameAlreadyBound;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeSuffix extends AbstractCommand
{

	public ChangeSuffix(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false), new RawParameter("change.suffix.param.1.ask", true)});
	}
	
	public void execute2(Context context, Object[] parameterArray)
		throws KOMException, IOException, InterruptedException
	{
		ServerSession session = context.getSession();
	    long id = -1;
	    if (parameterArray[0] != null) {
			// Are we trying to change the suffix of another user? We need the
			// CHANGE_ANY_NAME to do that!
			session.checkRights(UserPermissions.CHANGE_ANY_NAME);
			
	        NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	        id = nameAssociation.getId();
	    }

		// Set up
		//
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		
		String oldName = NameUtils.stripSuffix(session.getName(id != -1 ? id : context.getLoggedInUserId())).trim();

		// Get the new suffix
		//
	    String suffix = (String) parameterArray[1];
		
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
}
