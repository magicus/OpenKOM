/*
 * Created on Jun 7, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */

package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.CantSignoffMailboxException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SignOff extends AbstractCommand 
{
	public SignOff (String fullname)
	{
		super(fullname);
	}
	
	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if(parameters.length == 0)
			throw new MissingArgumentException();
		
		long conference = NamePicker.resolveNameToId(NameUtils.assembleName(parameters), (short) -1, context);
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

	public boolean acceptsParameters()
	{
		return true;
	}
}
