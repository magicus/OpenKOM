/*
 * Created on Sep 12, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik
 */
public class ListGlobalMessage extends SearchGlobalCommand
{
    public ListGlobalMessage(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[]
        { new UserParameter(true) });
    }

    MessageSearchResult[] innerSearch(Context context, Object[] parameterArray,
            int offset) throws UnexpectedException
    {
        NameAssociation user = (NameAssociation) parameterArray[0];
        return context.getSession().listMessagesGloballyByAuthor(
                user.getId(), offset, CHUNK_SIZE);
    }
}
