/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.LocalTextNumberParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson </a>
 */
public class DeleteMessage extends AbstractCommand
{
    public DeleteMessage(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new LocalTextNumberParameter(true) });
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
        int textNumber = ((Integer) parameterArray[0]).intValue();

        context.getSession().deleteMessageInCurrentConference(textNumber);

        PrintWriter out = context.getOut();
        MessageFormatter fmt = context.getMessageFormatter();
        out.println(fmt.format("delete.confirmation", 
                new Object[] { new Long(textNumber) }));
    }
}