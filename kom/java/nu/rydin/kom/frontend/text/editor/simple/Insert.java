/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.EditorContext;

public class Insert extends LineNumberCommand
{
	public Insert(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters)
	throws KOMException
	{
		
		// Everything seems ok. Delete the line
		//
		((EditorContext) context).getBuffer().insertBefore(this.parseLineNumber(context, parameters) - 1, "\n");
	}
	
	public boolean acceptsParameters()
	{
		return true;
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