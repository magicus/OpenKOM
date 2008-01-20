/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.StringParameter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SetActivity extends AbstractCommand
{
    public SetActivity(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new StringParameter("activity.enter", false)}, permissions);
    }
    
    public void execute(Context context, Object[] parameterArray) 
    throws KOMException
    {
        if (null == parameterArray[0])
        {
            context.getSession().restoreState();
        }
        else
        {
            context.getSession().setActivity(Activities.FREETEXT, true);
            context.getSession().setActivityString((String)parameterArray[0]);
        }
    }
}
