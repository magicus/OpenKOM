/*
 * Created on Jun 7, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */

package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.CantSignoffMailboxException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
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
	public SignOff (Context context, String fullName)
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
		ServerSession ss = context.getSession();
		long currentConf = ss.getCurrentConferenceId();
		
		// Call backend
		//
		String name = ss.signoff(conference);
		
		// Print confirmation
		//
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("signoff.confirmation", context.formatObjectName(name, conference)));	
		
		// Go to next conference if we just signed of
		// from the current one.
		//
		if(currentConf == conference)
		{
		    out.println();
		    try
		    {
		        ss.gotoNextConference();
		    }
		    catch(NoMoreNewsException e)
		    {
		        // No more messages! Go to the mailbox!
		        // 
		        ss.gotoConference(context.getLoggedInUserId());
		    }
		    context.printCurrentConference();
		}
	}
}
