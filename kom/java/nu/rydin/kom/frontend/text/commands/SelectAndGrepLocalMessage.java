/*
 * Created on Jan 12, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class SelectAndGrepLocalMessage extends GrepLocalMessage
{

    public SelectAndGrepLocalMessage(Context context, String fullName,
            long permissions)
    {
        super(context, fullName, permissions);
    }

    
    @Override
    protected void processMessageResult(Context context, MessageSearchResult[] msr)
    {
        context.getSession().getSelectedMessages().setMessages(msr);
    }
    
}
