/*
 * Created on May 31, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;
import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Copy extends AbstractCommand
{
	public Copy(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new NamedObjectParameter(true) }, permissions);	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException, NoCurrentMessageException
	{
	    ServerSession session = context.getSession();
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
	    DisplayController dc = context.getDisplayController();
	    
	    dc.normal();
		
		long messageid = session.getCurrentMessage();
		if(messageid == -1)
			throw new NoCurrentMessageException();

        ConferenceInfo ci = session.getCurrentConference();
        boolean mustConfirm = ((ci.getPermissions() & ConferencePermissions.READ_PERMISSION) == 0) || (ci.getVisibility() != Visibilities.PUBLIC);
        if (mustConfirm)
        {
            LineEditor in = context.getIn();
            int choice = in.getChoice(fmt.format("copy.confirm.leak",
                    ci.getName().getName()) +
                    " (" +
                    fmt.format("misc.yes") +
                    "/" + 
                    fmt.format("misc.no") +
                    ")? ",
                    new String[] { fmt.format("misc.no"), 
                                   fmt.format("misc.yes") },
                    0,
                    fmt.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
            
            if (1 != choice)
            {
                throw new OperationInterruptedException();
            }
        }
        
	    long destination = ((NameAssociation)parameterArray[0]).getId();

		// Call backend
		//
		session.copyMessage(messageid, destination);		
		
        // Retrieve current local occurrence to try and get the local number
        //
        MessageOccurrence local = session.getCurrentMessageOccurrence();
        int localnum = -1;
        if (local.getConference() == session.getCurrentConferenceId())
        {
            //We're moving a message whose original is in the current conference...
            localnum = local.getLocalnum();
        }
		
        if (localnum == -1)
        {
            //Print global confirmation
    		out.println(fmt.format("copy.global.confirmation", 
    				new Object [] { new Long(messageid), session.getName(destination) } ));	
        }
        else
        {
            //Print local confirmation 
    		out.println(fmt.format("copy.local.confirmation", 
    				new Object [] { new Long(localnum), session.getName(destination) } ));	
        }
	}
}
