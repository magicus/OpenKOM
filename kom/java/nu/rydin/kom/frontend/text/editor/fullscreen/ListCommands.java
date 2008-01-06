/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.fullscreen;


import java.io.IOException;
import java.util.Collection;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandCategory;
import nu.rydin.kom.frontend.text.parser.Parser;

public class ListCommands extends nu.rydin.kom.frontend.text.commands.ListCommands
{
	public ListCommands(Context context, String fullName, long permissions)
	{
		super(context, fullName, permissions);
	}

    protected Collection<CommandCategory> getCategories(Context context) throws KOMException, IOException 
    {
        return Parser.load("fullscreeneditorcommands.xml", context).getCategories();
    }
}
