/*
 * Created on Jan 12, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.InternalException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.GlobalMessageSearchResult;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class MessageSearchResultPrinterFactory
{

    public static MessageSearchResultPrinter createMessageSearchResultPrinter(Context context, Class<? extends MessageSearchResult> messageSearchResultClass) throws InternalException
    {
        MessageSearchResultPrinter printer = null;
        if (messageSearchResultClass == GlobalMessageSearchResult.class) {
            printer = new GlobalMessageSearchResultPrinter();
        }
        else if (messageSearchResultClass == LocalMessageSearchResult.class) {
            if (context.getSession().getCurrentConferenceId() == context.getLoggedInUserId()) {
                printer = new MailboxMessageSearchResultPrinter();
            }
            else
            {
                printer = new ConferenceMessageSearchResultPrinter();
            }
        }
        else
        {
            throw new InternalException("Unsupported MessageSearchResult");
        }
        return printer;
    }
}
