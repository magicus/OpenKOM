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
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SetActivity extends AbstractCommand
{
    public SetActivity(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter("activity.enter", false)}, permissions);
    }
    
    public void execute(Context context, Object[] parameterArray) 
    throws KOMException
    {
        KOMWriter out = context.getOut();
        MessageFormatter fmt = context.getMessageFormatter();
        if (null == parameterArray[0])
        {
            context.getSession().restoreState();
            out.println(fmt.format("set.activity.none"));
        }
        else
        {
            context.getSession().setActivity(Activities.FREETEXT, true);
            String txt = (String)parameterArray[0];
            context.getSession().setActivityString(txt);
            out.println(fmt.format("set.activity.freetext", txt));
        }
    }
}
