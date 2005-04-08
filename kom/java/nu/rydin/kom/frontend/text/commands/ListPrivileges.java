/*
 * Created on Aug 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.FlagUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListPrivileges extends AbstractCommand
{
	public ListPrivileges(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) }, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
	    UserInfo user = parameterArray[0] != null
	    	? context.getSession().getUser(((NameAssociation) parameterArray[0]).getId())  
	        : context.getCachedUserInfo();
	    PrintWriter out = context.getOut();
	    MessageFormatter formatter = context.getMessageFormatter();
	    out.println(formatter.format("list.privileges.header", user.getName()));
		FlagUtils.printFlags(context.getOut(), context.getMessageFormatter(), context.getFlagLabels("userprivs"), 
		        new long[] { user.getRights() });	
	}
}
