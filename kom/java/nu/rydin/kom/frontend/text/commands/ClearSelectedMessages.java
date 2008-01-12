/*
 * Created on Jan 12, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class ClearSelectedMessages extends AbstractCommand
{
    
    public ClearSelectedMessages(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);    
    }


    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        context.getSession().getSelectedMessages().clear();
    }

}
