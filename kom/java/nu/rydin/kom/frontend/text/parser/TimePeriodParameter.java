/*
 * Created on Jan 13, 2008
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.sql.Timestamp;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TimePeriodParameter extends FlagParameter
{
    private static final int WEEK = 0;
    private static final int MONTH = 1;
    private static final int YEAR = 2;
    private static final int ALL = 3;
    
    private static final long ONE_DAY = 86400000;
    
    public TimePeriodParameter(boolean isRequired, String[] flagLabels,
            DefaultStrategy def)
    {
        super(isRequired, flagLabels, def);
    }

    public TimePeriodParameter(boolean isRequired, String[] flagLabels)
    {
        super(isRequired, flagLabels);
    }

    public TimePeriodParameter(String missingObjectQuestionKey,
            String[] flagLabels, boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, flagLabels, isRequired, def);
    }

    public TimePeriodParameter(String missingObjectQuestionKey,
            String[] flagLabels, boolean isRequired)
    {
        super(missingObjectQuestionKey, flagLabels, isRequired);
    }
    
    public Object resolveFoundObject(Context context, Match match)
    throws IOException, InterruptedException, KOMException
    {
        Integer choice = (Integer) super.resolveFoundObject(context, match);
        if(choice == null)
            choice = WEEK;
        
        // We approximate a month as 30 days and a year as 365 days. 
        //
        switch(choice)
        {
        case MONTH:
            return new Timestamp(System.currentTimeMillis() - ONE_DAY * 30);
        case YEAR:
            return new Timestamp(System.currentTimeMillis() - ONE_DAY * 365);
        case ALL:
            return new Timestamp(0);
        case WEEK:
        default:
            return new Timestamp(System.currentTimeMillis() - ONE_DAY * 7);
        }
    }
}
