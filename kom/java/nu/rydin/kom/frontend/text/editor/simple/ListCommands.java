/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;


import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.Parser;

public class ListCommands extends nu.rydin.kom.frontend.text.commands.ListCommands
{
	public ListCommands(String fullName)
	{
		super(fullName);
	}

    protected Command[] getCommandList(Context context) throws KOMException, IOException {
		Command[] cmds = Parser.load("/editorcommands.list", 
				context.getMessageFormatter()).getCommandList();
		return cmds;
    }
}
