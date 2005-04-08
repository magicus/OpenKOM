/*
 * Created on Sep 11, 2004
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
public class GrepLocalMessage extends SearchLocalCommand 
{
	public GrepLocalMessage(Context context, String fullName, long permissions) 
	{
		super(fullName, new CommandLineParameter[] { new RawParameter("search.param.0.ask", true) }, permissions);
	}

    MessageSearchResult[] innerSearch(Context context, Object[] parameterArray, int offset) throws UnexpectedException
    {
        String searchterm = ((String) parameterArray[0]).trim();
        LocalMessageSearchResult[] lmsr = context.getSession().grepMessagesLocally(context.getSession().getCurrentConferenceId(), searchterm, offset, CHUNK_SIZE);
        return lmsr;
    }
}
