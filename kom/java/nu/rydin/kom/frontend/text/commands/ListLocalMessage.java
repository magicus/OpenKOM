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
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik Schröder
 */
public class ListLocalMessage extends SearchLocalCommand
{
    public ListLocalMessage(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[]
        { new UserParameter(false) }, permissions);
    }

    MessageSearchResult[] innerSearch(Context context,
            Object[] parameterArray, int offset) throws UnexpectedException
    {
        LocalMessageSearchResult[] lmsr;
        NameAssociation user;
        if (parameterArray[0] == null)
        {
            // No parameter given, default to listing ALL messages in current conference.
            lmsr = context.getSession().listAllMessagesLocally(
                    context.getSession().getCurrentConferenceId(), offset,
                    CHUNK_SIZE);
        } else
        {
            user = (NameAssociation) parameterArray[0];
            // Author given, list all messages by that author in current conference
            lmsr = context.getSession().listMessagesLocallyByAuthor(
                    context.getSession().getCurrentConferenceId(),
                    user.getId(), offset, CHUNK_SIZE);
        }

        return lmsr;
    }
}