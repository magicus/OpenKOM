package nu.rydin.kom.frontend.text.commands;

import java.sql.Timestamp;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TimePeriodParameter;

public class SelectAndListComments extends AbstractSelect
{

    public SelectAndListComments(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] {
                new TimePeriodParameter(false, context.getFlagLabels("search.timespan")) }, permissions);
    }

    @Override
    protected boolean select(Context context, Object[] parameters)
    throws KOMException
    {
        Timestamp start = parameters[0] != null ? (Timestamp) parameters[0] : new Timestamp(System.currentTimeMillis() - 86400000 * 24 * 7); // Default to week
        return context.getSession().selectCommentsGloballyToAuthor(context.getLoggedInUserId(), start);
    }
}
