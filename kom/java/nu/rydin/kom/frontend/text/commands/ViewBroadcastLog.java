/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ViewBroadcastLog extends ViewMessageLog
{
    public ViewBroadcastLog(String fullName)
    {
        super(fullName);
    }

    public void execute(Context context, String[] parameters)
    throws KOMException, IOException, InterruptedException	
    {	
        this.innerExecute(context, parameters, MessageLogKinds.BROADCAST);
    }
}
