/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;


import java.io.IOException;
import java.util.Collection;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.Parser;

public class ListCommands extends nu.rydin.kom.frontend.text.commands.ListCommands
{
	public ListCommands(Context context, String fullName)
	{
		super(context, fullName);
	}

    protected Collection getCategories(Context context) throws KOMException, IOException 
    {
        return Parser.load("/editorcommands.xml", context).getCategories();
    }
}
