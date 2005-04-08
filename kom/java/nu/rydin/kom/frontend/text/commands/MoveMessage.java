/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class MoveMessage extends AbstractCommand 
{
	public MoveMessage(Context context, String fullName, long permissions) 
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true)}, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
	    ServerSession session = context.getSession();
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
	    DisplayController dc = context.getDisplayController();
	    
	    dc.normal();
		
		long messageid = session.getCurrentMessage();
		if(messageid == -1)
			throw new NoCurrentMessageException();
	    
	    long destination = ((NameAssociation)parameterArray[0]).getId();
		
        // Retrieve current original occurrence to try and get the local number
        //
        MessageOccurrence original = session
                .getOriginalMessageOccurrence(messageid);
        int localnum = -1;
        if (original.getConference() == session.getCurrentConferenceId())
        {
            //We're moving a message whose original is in the current conference...
            localnum = original.getLocalnum();
        }

        // MOVE ZIG!!
        //
        session.moveMessage(messageid, destination);
        
        if (localnum == -1)
        {
            //Print global confirmation
    		out.println(fmt.format("move.global.confirmation", 
    				new Object [] { new Long(messageid), session.getName(destination) } ));	
        }
        else
        {
            //Print local confirmation 
    		out.println(fmt.format("move.local.confirmation", 
    				new Object [] { new Long(localnum), session.getName(destination) } ));	
        }
	}
}
