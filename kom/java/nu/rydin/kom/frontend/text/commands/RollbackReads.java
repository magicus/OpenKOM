/*
 * Created on Sep 21, 2004
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
import nu.rydin.kom.frontend.text.parser.IntegerParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class RollbackReads extends AbstractCommand
{

	public RollbackReads(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter("rollback.reads.param.0.ask", true) } );	
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        context.getSession().rollbackReads(((Integer) parameters[0]).intValue());
        context.getOut().println();
        context.printCurrentConference();
    }
}
