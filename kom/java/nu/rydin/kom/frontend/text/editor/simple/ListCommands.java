/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;


import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.CommandParser;
import nu.rydin.kom.frontend.text.Context;

public class ListCommands extends AbstractCommand
{
	public ListCommands(String fullName)
	{
		super(fullName);
	}
		
	public void execute(Context context, String[] args)
	throws KOMException
	{
		try
		{
			// TODO: Avoid reloading parser every time
			//
			PrintWriter out = context.getOut();
			Command[] cmds = CommandParser.load("/editorcommands.list", 
				context.getMessageFormatter()).getCommandList();
			int top = cmds.length;
			for(int idx = 0; idx < top; ++idx)
				out.println(cmds[idx].getFullName());
		}
		catch(IOException e)
		{
			throw new UnexpectedException(context.getLoggedInUserId(), e);
		}
	}
}
