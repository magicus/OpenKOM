/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListUsers extends AbstractCommand
{
	public ListUsers(MessageFormatter formatter)
	{
		super(formatter);	
	}
	
	public void execute(Context context, String[] args) 
	throws KOMException, IOException
	{
		PrintWriter out = context.getOut();
		NameAssociation[] names = context.getSession().getAssociationsForPatternAndKind("%", UserManager.USER_KIND);
		int top = names.length;
		for(int idx = 0; idx < top; ++idx)
			out.println(names[idx].getName());
	}
}
