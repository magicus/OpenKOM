/*
 * Created on Sep 10, 2004
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
import nu.rydin.kom.frontend.text.parser.IntegerParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KillSession extends AbstractCommand
{
	public KillSession(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter(true) }, permissions);	
	}

    public void checkAccess(Context context) throws AuthorizationException
    {
        context.getSession().checkRights(UserPermissions.ADMIN);
    }
    
    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        context.getSession().killSession((Integer) parameters[0]);
    }
}
