/*
 * Created on Nov 7, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowLicense extends AbstractCommand
{
	public ShowLicense(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}

	public void execute(Context context, Object[] parameterArray)
	{
	    PrintWriter out = context.getOut();
	    out.println(context.getMessageFormatter().format("license.version"));
	    out.println(context.getMessageFormatter().format("license.text1"));
	    out.println(context.getMessageFormatter().format("license.text2"));
	}
}
