/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class LineNumberCommand extends AbstractCommand
{
	public LineNumberCommand(String fullName)
	{
		super(fullName);
	}
	
	public int parseLineNumber(Context context, String[] parameters)
	throws BadParameterException, MissingArgumentException
	{
		// Get parameter
		//
		int line = -1;
		if(parameters.length != 1)
			throw new MissingArgumentException();
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
		return line;
	}
	
	public boolean acceptsParameters()
	{
		return true;	
	}
}
