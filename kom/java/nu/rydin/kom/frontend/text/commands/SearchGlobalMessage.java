/*
 * Created on Sep 13, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author Henrik Schröder
 */
public class SearchGlobalMessage extends SearchGlobalCommand
{
    public SearchGlobalMessage(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter(
                "search.param.0.ask", true) });
    }

    MessageSearchResult[] innerSearch(Context context, Object[] parameterArray,
            int offset) throws UnexpectedException
    {
        String searchterm = ((String) parameterArray[0]).trim();
        return context.getSession().searchMessagesGlobally(searchterm,
                offset, CHUNK_SIZE);
    }
}