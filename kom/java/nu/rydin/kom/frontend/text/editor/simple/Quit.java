/*
 * Created on Jun 21, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;


public class Quit extends AbstractCommand
{
	public Quit(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters)
	{
		// Not much to do here
	}
}