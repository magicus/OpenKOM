/*
 * Created on Sep 19 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 */
public class ShowSimpleFile extends AbstractCommand
{
	public ShowSimpleFile(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException
	{
	    //FIXME EDITREFACTOR: This code is copy&pasted from the SimpleMessageEditor init.
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		EditorContext edContext = (EditorContext) context;
		DisplayController dc = context.getDisplayController();

		//Preamble isn't always executed since ctrl-l executes this command with no ExecutableCommand wrapping. 
		out.println();
		
		// Print header
		//
		dc.normal();
		
		//FIXME EDITREFACTOR: We do not know the name of the file at this point.
		out.println(formatter.format("edit.file.header", ""));
		
		// Print body
		//
		Buffer buffer = edContext.getBuffer();
		int top = buffer.size();
		for(int idx = 0; idx < top; ++idx)
		{
		    dc.editorLineNumber();
			PrintUtils.printRightJustified(out, Integer.toString(idx + 1), 4);
			out.print(':');
			dc.messageBody();
			String line = buffer.get(idx).toString();
			int l = line.length();
			if(l > 0 && line.endsWith("\n"))
				line = line.substring(0, l - 1);
			out.println(line);
		}
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
