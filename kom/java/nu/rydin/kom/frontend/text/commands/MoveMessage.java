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
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class MoveMessage extends AbstractCommand 
{
	public MoveMessage(String fullName) 
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true)});
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
		
		long conference = nameAssociation.getId();
		ServerSession ss = context.getSession();
		int localNum = ss.globalToLocalInConference(ss.getCurrentConferenceId(), 
													ss.getLastMessageHeader().getId()).getLocalnum();
		context.getSession().moveMessage(localNum, conference);

		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("move.confirmation", 
			new Object [] { new Long(localNum), context.getSession().getName(conference) } ));	
	}
}
