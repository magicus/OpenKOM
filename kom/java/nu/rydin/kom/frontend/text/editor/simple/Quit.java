/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;


public class Quit extends AbstractCommand
{
	public Quit(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}
	
	public void execute(Context context, Object[] paramArray)
	throws QuitEditorException
	{
		throw new QuitEditorException();
	}
}