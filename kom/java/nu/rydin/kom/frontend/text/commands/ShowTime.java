/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.util.Date;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowTime extends AbstractCommand
{
	public ShowTime(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}
	
	public void execute(Context context, Object[] parameterArray)
	{
		context.getOut().println(context.getMessageFormatter().format("show.time.format", new Date()));
	}
}
