/*
 * Created on Jan 11, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.SelectedMessages;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class ListSelectedMessages extends AbstractCommand
{

    public ListSelectedMessages(Context context, String fullName, long permissions)
    {
        super(fullName, NO_PARAMETERS, permissions);
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        
        SelectedMessages selectedMessages = context.getSession().getSelectedMessages();
        KOMWriter out = context.getOut();
        
        if (selectedMessages.hasUnreadMessages()) 
        {
            MessageSearchResult[] messages = selectedMessages.getMessages();
            MessageSearchResultPrinter printer = selectedMessages.getMessageSearchResultPrinter(context);

            printer.printSearchResultHeader(context);
            for(int idx = 0; idx < messages.length; ++idx)
            {
                printer.printSearchResultRow(context, out, messages[idx]);
            }
        }
        else
        {
            out.println(context.getMessageFormatter().format("search.noselected"));
        } 
        
    }

    public GlobalMessageSearchResultPrinter getMessageSearchResultPrinter()
    {
        return new GlobalMessageSearchResultPrinter();
    }

}
