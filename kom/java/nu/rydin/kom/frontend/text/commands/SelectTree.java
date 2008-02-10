/*
 * Created on Feb 8, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SelectTree extends AbstractCommand
{
    public SelectTree(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(false) }, permissions);
    }
   
    public void execute (Context context, Object[] parameterArray)
    throws NoCurrentMessageException, UnexpectedException, ObjectNotFoundException
    {
        MessageLocator ml;
        ServerSession ss = context.getSession();
        if (null != parameterArray[0])
        {
            ml = (MessageLocator)parameterArray[0];
        }
        else
        {
            ml = new MessageLocator (ss.getCurrentMessage());
        }
        ml = ss.resolveLocator(ml);
        ss.markThreadAsUnread(ml.getGlobalId(), true);
    }
}
