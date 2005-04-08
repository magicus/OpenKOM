/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.FlagUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListFlags extends AbstractCommand
{
	public ListFlags(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) }, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
	    long[] flags = parameterArray[0] != null
	    	? context.getSession().getUser(((NameAssociation) parameterArray[0]).getId()).getFlags()
	        : context.getCachedUserInfo().getFlags();
		FlagUtils.printFlags(context.getOut(), context.getMessageFormatter(), context.getFlagLabels("userflags"), flags);	
	}
}
