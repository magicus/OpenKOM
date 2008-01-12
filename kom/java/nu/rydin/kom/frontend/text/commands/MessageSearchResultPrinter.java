/*
 * Created on Jan 9, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public interface MessageSearchResultPrinter
{

    void printSearchResultRow(Context context, KOMWriter out,
            MessageSearchResult msr);

    void printSearchResultHeader(Context context);

}
