/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.NameUtils;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteConference extends AbstractCommand 
{
	public DeleteConference (String fullname)
	{
		super(fullname);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if (0 == parameters.length)
		{
			throw new MissingArgumentException();
		}
		long conference=NamePicker.resolveName(NameUtils.assembleName(parameters), ConferenceManager.CONFERENCE_KIND, context);
		ServerSession ss = context.getSession();
		MessageFormatter mf = new MessageFormatter();
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
								  mf.format("parser.invalid.choice"));
		if (1 == choice)
		{
			ss.deleteConference(conference);
		}
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
