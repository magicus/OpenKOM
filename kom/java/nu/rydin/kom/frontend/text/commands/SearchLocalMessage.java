/*
 * Created on Jul 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author Henrik Schröder
 */
public class SearchLocalMessage extends SearchLocalCommand
{
    public SearchLocalMessage(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[]
        { new RawParameter("search.param.0.ask", true) });
    }

    protected MessageSearchResult[] innerSearch(Context context,
            Object[] parameterArray, int offset) throws UnexpectedException
    {
        String searchterm = ((String) parameterArray[0]).trim();
        LocalMessageSearchResult[] lmsr = context.getSession()
                .searchMessagesLocally(
                        context.getSession().getCurrentConferenceId(),
                        searchterm, offset, CHUNK_SIZE);
        return lmsr;
    }
}