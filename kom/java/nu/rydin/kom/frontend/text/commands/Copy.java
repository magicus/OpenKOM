/*
 * Created on May 31, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.NoCurrentMessageException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Copy extends AbstractCommand
{
	public Copy(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) });	
	}
	
	public void execute2(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException, NoCurrentMessageException
	{
		if(parameters.length == 0)
			throw new MissingArgumentException();
		long conference = NamePicker.resolveNameToId(NameUtils.assembleName(parameters), (short) -1, context);
		
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
