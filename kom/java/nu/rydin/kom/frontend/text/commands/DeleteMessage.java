/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.TextNumber;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteMessage extends AbstractCommand 
{
	public DeleteMessage(String fullName) 
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(true)});
	}

	public void execute2(Context context, Object[] parameterArray)
	throws KOMException 
	{
	    TextNumber textNumber = (TextNumber) parameterArray[0];
		int n = textNumber.getNumber();
		if (textNumber.isGlobal()) {
		    // FIXME: Ihse: Do the right thing here
		    return;
		} else {
		    context.getSession().deleteMessageInCurrentConference(n);
		}

		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("delete.confirmation", 
			new Object [] { new Long(n) } ));	
	}
}
