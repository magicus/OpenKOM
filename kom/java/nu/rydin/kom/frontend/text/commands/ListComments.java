package nu.rydin.kom.frontend.text.commands;

import java.sql.Timestamp;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.FlagParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;

public class ListComments extends SearchGlobalCommand 
{
	private static final int WEEK = 0;
	private static final int MONTH = 1;
	private static final int YEAR = 2;
	private static final int ALL = 3;
	
	private static final long ONE_DAY = 86400000;
	
	private Timestamp startDate;
	
    public ListComments(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] {
        		new FlagParameter(false, context.getFlagLabels("search.timespan")) }, permissions);
    }
    
	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
		int flag = parameterArray[0] != null 
			? ((Integer) parameterArray[0]).intValue()
			: WEEK; // Week is the default
			
		// We approximate a month as 30 days and a year as 365 days. 
	    //
		switch(flag)
		{
		case MONTH:
			startDate = new Timestamp(System.currentTimeMillis() - ONE_DAY * 30);
			break;
		case YEAR:
			startDate = new Timestamp(System.currentTimeMillis() - ONE_DAY * 365);
			break;
		case ALL:
			startDate = new Timestamp(0);
			break;
		case WEEK:
		default:
			startDate = new Timestamp(System.currentTimeMillis() - ONE_DAY * 7);
			break;

		}
		super.execute(context, parameterArray);
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
