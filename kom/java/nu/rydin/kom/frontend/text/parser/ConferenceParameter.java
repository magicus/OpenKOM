/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.InvalidChoiceException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class ConferenceParameter extends NamedObjectParameter
{

	public ConferenceParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		super(missingObjectQuestionKey, isRequired);
	}

	public ConferenceParameter(boolean isRequired)
	{
		super("parser.parameter.conference.ask", isRequired);
	}

	protected Object innerResolve(Context context, Match match) throws UnexpectedException, IOException, InterruptedException, OperationInterruptedException, InvalidChoiceException
	{
		try
		{
			NameAssociation assoc = NamePicker.resolveName(match.getParsedObject().toString(), ConferenceManager.CONFERENCE_KIND, context);
			return assoc;
		}
		catch (ObjectNotFoundException e)
		{
			PrintWriter out = context.getOut();
			MessageFormatter fmt = context.getMessageFormatter();

			out.println(fmt.format("parser.parameter.conference.notfound", match.getMatchedString()));
			out.flush();
			return null;
		}
	}
	
}
