/*
 * Created on Jun 7, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */

package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.CantSignoffMailboxException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SignOff extends AbstractCommand 
{
	public SignOff (String fullName)
	{
	    super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) });
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		long conference = ((NameAssociation)parameterArray[0]).getId();
		if (context.getLoggedInUserId() == conference)
		{
			throw new CantSignoffMailboxException();
		}

		// Call backend
		//
		String name = context.getSession().signoff(conference);
		
		// Print confirmation
		//
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("signoff.confirmation", context.formatObjectName(name, conference)));		
	}
}
