/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.ClientSession;
import nu.rydin.kom.frontend.text.Context;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Logout extends AbstractCommand
{
	public Logout(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);	
	}
	
    public void execute(Context context, Object[] parameterArray)
            throws KOMException 
    {
		// TODO: use argument string as a logout message 
		//       "log ska sova" broadcasts "Kalle Kula har loggat ut (ska sova)"

        ((ClientSession) context).logout();
    }
    
    public void printPostamble(PrintWriter out)
    {
        //
    }
    public void printPreamble(PrintWriter out)
    {
        //
    }
}
