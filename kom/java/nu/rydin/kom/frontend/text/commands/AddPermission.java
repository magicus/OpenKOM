/*
 * Created on Aug 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RightParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AddPermission extends AbstractCommand
{
    public AddPermission(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new UserParameter("add.permission.user.question", true), 
                new RightParameter(true, context.getFlagLabels("userprivs")) });
    }

    public void checkAccess(Context context) throws AuthorizationException
    {
        context.getSession().checkRights(UserPermissions.ADMIN);
    }
    
    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        NameAssociation nameAssoc = (NameAssociation) parameters[0]; 
        long user = nameAssoc.getId();
        int flagNumber = ((Integer) parameters[1]).intValue();	
		context.getSession().changeUserPermissions(user, 1 << flagNumber, 0);
		context.getOut().println(context.getMessageFormatter().format("add.permission.confirmation", 
			new Object[] { context.getFlagLabels("userprivs")[flagNumber], nameAssoc.getName() } ));

		// Clear cache
		//
		context.clearUserInfoCache();
    }
}
