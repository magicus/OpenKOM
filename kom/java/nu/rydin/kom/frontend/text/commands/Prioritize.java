/*
 * Created on Oct 1, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik Schröder
 */
public class Prioritize extends AbstractCommand
{
    public Prioritize(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] {
                new ConferenceParameter(true), new ConferenceParameter(false) });
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
        
        long conference = ((NameAssociation) parameterArray[0]).getId();

        long targetConference = -1;
        if (parameterArray[1] == null)
        {
            //Default to the top conference.
            //TODO: Maybe add a more efficient method for finding this out?
            targetConference = context.getSession().listMemberships(
                    context.getLoggedInUserId())[0].getId();
        } else
        {
            targetConference = ((NameAssociation) parameterArray[1]).getId();
        }

        long shuffled = context.getSession().prioritizeConference(conference, targetConference);
        
		out.println(fmt.format("prioritize.confirmation", new Object[] { new Long(shuffled), context.formatObjectName((NameAssociation) parameterArray[0]), new Long(Math.abs(shuffled)) }));
    }
}