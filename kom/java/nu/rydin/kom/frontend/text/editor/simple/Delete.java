/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.PrintWriter;

import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;

public class Delete extends LineNumberCommand
{
	public Delete(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters)
	throws KOMException
	{
		// TODO: Support ranges
		//
		// Get parameter
		//
		if(parameters.length != 1)
			throw new MissingArgumentException();
		int line = -1;
		try
		{
			line = Integer.parseInt(parameters[0]);
		}
		catch(NumberFormatException e)
		{
			throw new BadParameterException();
		}
		
		// Is is a valid line?
		//
		Buffer buffer = ((EditorContext) context).getBuffer();
		if(line < 1 || line > buffer.size())
			throw new BadParameterException();
		
		// Everything seems ok. Delete the line
		//
		buffer.remove(line - 1);
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