/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class MoveMessage extends AbstractCommand 
{
	public MoveMessage(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if (0 == parameters.length)
		{
			throw new BadParameterException();
		}
		
		long conference=NamePicker.resolveNameToId(NameUtils.assembleName(parameters), NameManager.CONFERENCE_KIND, context);
		ServerSession ss = context.getSession();
		int localNum = ss.globalToLocalInConference(ss.getCurrentConferenceId(), 
													ss.getLastMessageHeader().getId()).getLocalnum();
		context.getSession().moveMessage(localNum, conference);

		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("move.confirmation", 
			new Object [] { new Long(localNum), context.getSession().getName(conference) } ));	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
