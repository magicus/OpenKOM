/*
 * Created on Nov 9, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceWildcardParameter;
import nu.rydin.kom.frontend.text.parser.IntegerParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ChangeUnread extends AbstractCommand
{
	public ChangeUnread(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter("change.unread.param.0.ask", true), new ConferenceWildcardParameter(false) }, permissions );	
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException
	{
	    Integer number = (Integer) parameterArray[0];
        ServerSession ss = context.getSession();
        if (null == parameterArray[1])
        {
            ss.changeUnread(number.intValue());
        }
        else
        {
            long conf = ((NameAssociation)parameterArray[1]).getId();
            if (-1 != conf)
                ss.changeUnread(number, conf);
            else
                ss.changeUnreadInAllConfs(number);
        }
		context.printCurrentConference();
	}

}
