/*
 * Created on Sep 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeSubject extends AbstractCommand
{
    public ChangeSubject(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter("simple.editor.subject", true) });
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        ((EditorContext) context).setSubject((String) parameters[0]);
    }
    
	public void printPreamble(PrintWriter out)
	{
		// Nothing 
	}
	
	public void printPostamble(PrintWriter out)
	{
		// Nothing 
	}	
}
