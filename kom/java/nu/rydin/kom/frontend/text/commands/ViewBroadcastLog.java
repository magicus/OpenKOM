/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ViewBroadcastLog extends ViewMessageLog
{
    public ViewBroadcastLog(Context context, String fullName)
    {
        super(context, fullName);
    }

    public void execute(Context context, Object[] parameterArray)
    throws KOMException
    {	
    	DisplayController dc = context.getDisplayController();
    	dc.broadcastMessageBody();
        this.innerExecute(context, parameterArray, MessageLogKinds.BROADCAST);
    }
}
