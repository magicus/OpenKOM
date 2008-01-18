package nu.rydin.kom.frontend.text.commands;

import java.sql.Timestamp;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TimePeriodParameter;
import nu.rydin.kom.structs.MessageSearchResult;

public class ListComments extends SearchGlobalCommand 
{	
	private Timestamp startDate;
	
    public ListComments(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] {
        		new TimePeriodParameter(false, context.getFlagLabels("search.timespan")) }, permissions);
    }
    
	public void execute(Context context, Object[] parameters)
	throws KOMException
	{
	    startDate = parameters[0] != null ? (Timestamp) parameters[0] : new Timestamp(System.currentTimeMillis() - 86400000 * 24 * 7); // Default to week
		super.execute(context, parameters);
	}

    MessageSearchResult[] innerSearch(Context context, Object[] parameterArray, int offset) throws UnexpectedException
    {
        return context.getSession().listCommentsGloballyToAuthor(context.getSession().getLoggedInUserId(), this.startDate, offset, CHUNK_SIZE);
    }

    long count(Context context, Object[] parameterArray) throws KOMException
    {
        return context.getSession().countCommentsGloballyToAuthor(context.getSession().getLoggedInUserId(), this.startDate);
    }
}
