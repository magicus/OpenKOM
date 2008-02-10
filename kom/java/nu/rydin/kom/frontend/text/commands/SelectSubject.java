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

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SelectSubject extends AbstractCommand
{
    public SelectSubject(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
    }
   
    public void execute (Context context, Object[] parameterArray)
    throws NoCurrentMessageException, UnexpectedException, ObjectNotFoundException
    {
        ServerSession ss = context.getSession();
        ss.markSubjectAsUnread(ss.getLastMessageHeader().getSubject(), true, true);
    }
}
