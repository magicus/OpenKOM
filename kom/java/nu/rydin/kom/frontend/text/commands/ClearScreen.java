/*
 * Created on Sep 13, 2004
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
import nu.rydin.kom.frontend.text.ansi.ANSISequences;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClearScreen extends AbstractCommand
{
	public ClearScreen(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        PrintWriter out = context.getOut(); 
        out.print(ANSISequences.CLEAR_DISPLAY);
        out.flush();
    }

    public void printPostamble(PrintWriter out)
    {
    }
}
