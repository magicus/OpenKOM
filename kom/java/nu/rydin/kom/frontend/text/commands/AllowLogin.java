/*
 * Created on Sep 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AllowLogin extends AbstractCommand
{
	public AllowLogin(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        context.getSession().allowLogin();
        PrintWriter out = context.getOut();
        out.println(context.getMessageFormatter().format("allow.login.confirmation"));
    }
}