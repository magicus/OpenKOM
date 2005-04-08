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
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.frontend.text.parser.StringParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CreateAlias extends AbstractCommand
{
	public CreateAlias(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { 
		        new StringParameter("create.alias.param.ask.0", true), 
		        new RawParameter("create.alias.param.ask.1", true) }, permissions);	
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        context.getParser().addAlias((String) parameters[0], (String) parameters[1]);
    }
}
