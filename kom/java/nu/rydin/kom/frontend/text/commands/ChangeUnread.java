/*
 * Created on Nov 9, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.IntegerParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeUnread extends AbstractCommand
{
	public ChangeUnread(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter("change.unread.param.0.ask", true) } );	
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException
	{
	    Integer number = (Integer) parameterArray[0];
		
		context.getSession().changeUnread(number.intValue());
		context.printCurrentConference();
	}

}
