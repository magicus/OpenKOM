/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Logout extends AbstractCommand
{
	public Logout(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context ctx, String[] args)
	throws ObjectNotFoundException, UnexpectedException
	{
		// TODO: use argument string as a logout message 
		//       "log ska sova" broadcasts "Kalle Kula har loggat ut (ska sova)"
		
		// Not much to do here. The command loop is supposed to 
		// recognize us and ends its own misery when it sees us.
		//
		ctx.getSession().updateLastlogin();
		ctx.getClientSession().logout();
	}
    public void execute2(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException {
        context.getSession().updateLastlogin();
        context.getClientSession().logout();
    }
}
