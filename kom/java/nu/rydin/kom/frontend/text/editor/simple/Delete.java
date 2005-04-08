/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.LineNumberParameter;

public class Delete extends AbstractCommand
{
	public Delete(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new LineNumberParameter(true) }, permissions );
	}
	
	public void execute(Context context, Object[] parameterArray)
	{
	    Integer lineInteger = (Integer) parameterArray[0];
	    
		int line = lineInteger.intValue();

		// Delete the line
		//
		Buffer buffer = ((EditorContext) context).getBuffer();
		
		buffer.remove(line - 1);
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