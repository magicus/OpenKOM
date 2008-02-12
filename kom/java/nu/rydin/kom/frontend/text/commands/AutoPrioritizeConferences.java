/*
 * Created on Feb 11, 2008
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class AutoPrioritizeConferences extends AbstractCommand
{
    public AutoPrioritizeConferences(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        MessageFormatter mf = context.getMessageFormatter();
        PrintWriter out = context.getOut();
        out.println (mf.format("autoprioritize.warning"));
        LineEditor in = context.getIn();
        int choice = in.getChoice(mf.format("autoprioritize.verify") +
                                  " (" +
                                  mf.format("misc.y") +
                                  "/" + 
                                  mf.format("misc.n") +
                                  ")? ",
                                  new String[] { mf.format("misc.n"), 
                                                 mf.format("misc.y") },
                                  0,
                                  mf.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
        if (1 == choice)
        {
            context.getSession().autoPrioritizeConferences();
            out.println(mf.format("autoprioritize.confirm"));
            return;
        }
        else
        {
            throw new OperationInterruptedException();
        }
    }
}
