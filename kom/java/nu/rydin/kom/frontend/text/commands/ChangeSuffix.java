/*
 * Created on Jun 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
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

	public ChangeSuffix(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) });
	}
	
	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException, InterruptedException
	{
		ServerSession session = context.getSession();
	    long id = -1;
	    if (parameterArray[0] != null) 
	    {
			// Are we trying to change the suffix of another user? We need the
			// CHANGE_ANY_NAME to do that!
	        //
			session.checkRights(UserPermissions.CHANGE_ANY_NAME);
	        NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	        id = nameAssociation.getId();
	    }

		// Set up
		//
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter formatter = context.getMessageFormatter();
		DisplayController dc = context.getDisplayController();

		// Get the old suffix
		//
		String oldName = session.getName(id != -1 ? id : context.getLoggedInUserId()).getName();
		String oldSuffix = "";
		if (oldName.indexOf("/") > 0)
		{
		    oldSuffix = oldName.substring(oldName.indexOf("/") + 1);
		}
		
		// Get the new suffix
		//
		dc.normal();
		out.print(formatter.format("change.suffix.ask"));
		dc.input();
		String suffix = in.readLine(oldSuffix);
		
		// Strip suffix before presentation.
		//
		oldName = NameUtils.stripSuffix(oldName).trim();
		
		// Execute
		//
		try
        {
            if (id == -1)
            {
                session.changeSuffixOfLoggedInUser(suffix);
            }
            else
            {
                session.changeSuffixOfUser(id, suffix);
            }

    		// Print confirmation
    		//
    		out.println();
    		if ("".equals(suffix))
    		{
    		    out.println(formatter.format("change.suffix.deletion", oldName));
    		}
    		else
    		{
    		    out.println(formatter.format("change.suffix.confirmation", oldName));
    		}

        } 
		catch (DuplicateNameException e)
        {
		    //Mmmkay, if we ended up here we didn't submit any changes
		    out.println();
		    out.println(formatter.format("change.suffix.nochange", oldName));
        }
		
	}
}
