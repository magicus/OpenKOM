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
import nu.rydin.kom.frontend.text.DisplayController;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ViewChatLog extends ViewMessageLog
{
    public ViewChatLog(String fullName)
    {
        super(fullName);
    }

    public void execute2(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
    	DisplayController dc = context.getDisplayController();
    	dc.chatMessageBody();
        this.innerExecute(context, parameterArray, MessageLogKinds.CHAT);
    }
}
