package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;

public class ListComments extends SearchGlobalCommand 
{
    public ListComments(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
    }

    MessageSearchResult[] innerSearch(Context context, Object[] parameterArray, int offset) throws UnexpectedException
    {
        return context.getSession().listCommentsGloballyToAuthor(context.getSession().getLoggedInUserId(), offset, CHUNK_SIZE);
    }

    long count(Context context, Object[] parameterArray) throws KOMException
    {
        return context.getSession().countCommentsGloballyToAuthor(context.getSession().getLoggedInUserId());
    }
}
