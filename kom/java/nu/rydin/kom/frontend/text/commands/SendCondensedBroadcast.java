/*
 * Created on Sep 14, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SendCondensedBroadcast extends AbstractCommand
{

    public SendCondensedBroadcast(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[]
        { new RawParameter("send.condensed.param.0.ask", true) });
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        context.getSession().broadcastChatMessage((String) parameters[0], MessageLogKinds.CONDENSED_BROADCAST);
    }
}
