/*
 * Created on May 31, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Copy extends AbstractCommand
{
	public Copy(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) });	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, NoCurrentMessageException
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
		long conference = nameAssociation.getId();
		
		ServerSession session = context.getSession();
		long message = session.getCurrentMessage();
		if(message == -1)
			throw new NoCurrentMessageException();

		// Call backend
		//
		session.copyMessage(message, conference);		
		
		// Print confirmation
		//
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("copy.confirmation", 
			new Object [] { new Long(message), session.getName(conference) } ));
	}
}
