/*
 * Created on Aug 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

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
public class SubtractPermission extends AbstractCommand
{
    public SubtractPermission(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new UserParameter("subtract.permission.user.question", true), 
                new RightParameter(true, context.getFlagLabels("userprivs")) });
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException
    {
        NameAssociation nameAssoc = (NameAssociation) parameters[0]; 
        long user = nameAssoc.getId();
        int flagNumber = ((Integer) parameters[1]).intValue();	
		context.getSession().changeUserPermissions(user, 0, 1 << flagNumber);
		context.getOut().println(context.getMessageFormatter().format("subtract.permission.confirmation", 
			new Object[] { context.getFlagLabels("userprivs")[flagNumber], nameAssoc.getName() } ));

		// Clear cache
		//
		context.clearUserInfoCache();
    }
}
