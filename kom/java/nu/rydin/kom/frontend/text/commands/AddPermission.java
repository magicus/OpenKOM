/*
 * Created on Aug 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RightParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AddPermission extends AbstractCommand
{
    public AddPermission(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new UserParameter("add.permission.user.question", true), 
                new RightParameter(true, context.getRightsLabels()) });
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        NameAssociation nameAssoc = (NameAssociation) parameters[0]; 
        long user = nameAssoc.getId();
        int flagNumber = ((Integer) parameters[1]).intValue();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();		
		context.getSession().changeUserPermissions(user, 1 << flagNumber, 0);
		context.getOut().println(context.getMessageFormatter().format("add.permission.confirmation", 
			new Object[] { context.getRightsLabels()[flagNumber], nameAssoc.getName() } ));

		// Clear cache
		//
		context.clearUserInfoCache();
    }
}
