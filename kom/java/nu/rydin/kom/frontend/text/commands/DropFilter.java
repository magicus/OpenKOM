/*
 * Created on Oct 20, 2004
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
 * @author Pontus Rydin
 */
public class DropFilter extends AbstractCommand
{
	public DropFilter(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(true) }, permissions);	
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        NameAssociation user = (NameAssociation) parameters[0];
        context.getSession().dropUserFilter(user.getId());
        context.getOut().println(context.getMessageFormatter().
               format("drop.filter.confirmation", user.getName().toString()));
    }
}
