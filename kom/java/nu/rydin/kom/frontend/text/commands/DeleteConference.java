/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteConference extends AbstractCommand 
{
	public DeleteConference (String fullname)
	{
		super(fullname, new CommandLineParameter[] { new ConferenceParameter(true)});
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
		long conference = nameAssociation.getId();
		ServerSession ss = context.getSession();
		MessageFormatter mf = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		if (ss.isMagicConference(conference))
		{
			out.println(mf.format("delete.magic.warning"));
		}
		out.println(mf.format("delete.conference.warning"));
		int choice = in.getChoice(mf.format("delete.conference.confirm",
											ss.getName(conference)) +
				     			  	" (" +
									mf.format("misc.yes") +
									"/" + 
									mf.format("misc.no") +
									")? ",
			                      new String[] { mf.format("misc.no"), 
												 mf.format("misc.yes") },
								  0,
								  mf.format("nu.rydin.kom.InvalidChoiceException.format"));
		if (1 == choice)
		{
			ss.deleteConference(conference);
		}
	}
}
