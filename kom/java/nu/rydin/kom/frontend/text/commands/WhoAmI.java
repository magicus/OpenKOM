/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class WhoAmI extends AbstractCommand
{
    public WhoAmI(String fullName)
    {
        super(fullName);
    }

    public void execute2(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException {
        context.getOut().println(context.getMessageFormatter().
                format("who.am.i.message", context.getCachedUserInfo().getName()));
    }
    
    public void execute(Context context, String[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        context.getOut().println(context.getMessageFormatter().
                format("who.am.i.message", context.getCachedUserInfo().getName()));
    }
}
