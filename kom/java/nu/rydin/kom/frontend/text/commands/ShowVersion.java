/*
 * Created on Sep 22, 2004
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

/**
 * @author Henrik Schröder
 *
 */
public class ShowVersion extends AbstractCommand
{

    public ShowVersion(Context context, String fullName)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS);	
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
	    PrintWriter out = context.getOut();
	    out.println(context.getMessageFormatter().format("version.text1"));
    }

}
