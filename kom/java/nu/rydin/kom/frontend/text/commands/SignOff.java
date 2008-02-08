/*
 * Created on Jun 7, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */

package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;
import java.io.IOException;


import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.CantSignoffMailboxException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceWildcardParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SignOff extends AbstractCommand 
{
	public SignOff (Context context, String fullName, long permissions)
	{
	    super(fullName, new CommandLineParameter[] { new ConferenceWildcardParameter(true) }, permissions);
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, InterruptedException, IOException
	{
        if (-1 == ((NameAssociation)parameterArray[0]).getId())  //.equals(parameterArray[0].toString()))
        {
            MessageFormatter mf = context.getMessageFormatter();
            PrintWriter out = context.getOut();
            LineEditor in = context.getIn();
            out.println(mf.format("signoff.allconfs.info"));
            int choice = in.getChoice(mf.format("signoff.allconfs.verify") +
                                        " (" +
                                        mf.format("misc.yes") +
                                        "/" + 
                                        mf.format("misc.no") +
                                        ")? ",
                                      new String[] { mf.format("misc.no"), 
                                                     mf.format("misc.yes") },
                                      0,
                                      mf.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
            if (1 == choice)
            {
                int cnt = context.getSession().signoffAllConferences();
                out.println (mf.format ("signoff.allconfs.result", cnt));
                
                // All conferences include the current one, dump the user in the mailbox.
                //
                context.getSession().gotoConference(context.getLoggedInUserId());
                return;
            }
            else
            {
                throw new OperationInterruptedException();
            }
        }

        long conference = ((NameAssociation)parameterArray[0]).getId();
		if (context.getLoggedInUserId() == conference)
		{
			throw new CantSignoffMailboxException();
		}
		ServerSession ss = context.getSession();
		long currentConf = ss.getCurrentConferenceId();
		
		// Call backend
		//
		Name name = ss.signoff(conference);
		
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
