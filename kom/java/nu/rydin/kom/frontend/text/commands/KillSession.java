/*
 * Created on Sep 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KillSession extends AbstractCommand
{
	public KillSession(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(true) });	
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        context.getSession().killSession(((NameAssociation) parameters[0]).getId());
    }
}
