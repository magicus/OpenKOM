/*
 * Created on Jan 11, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class ReadNextSelectedMessage extends AbstractReadMessage
{
	public ReadNextSelectedMessage(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions, "read.next.selected.no.message");	
	}
	
	@Override
    protected MessageLocator getMessageToRead(Context context, Object[] parameterArray)
    {
        return context.getSession().getSelectedMessages().getNextMessage();
    }

}
