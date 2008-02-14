/*
 * Created on Feb 14, 2008
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
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.StringParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteAlias extends AbstractCommand
{
    public DeleteAlias(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { 
                new StringParameter("delete.alias.param.ask.0", true) }, permissions); 
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {            
        PrintWriter out = context.getOut();
        LineEditor in = context.getIn();
        MessageFormatter mf = context.getMessageFormatter();
        String alias=(String)parameters[0];
        int choice = in.getChoice(mf.format("delete.alias.verify", alias) +
                                  " (" +
                                  mf.format("misc.y") +
                                  "/" + 
                                  mf.format("misc.n") +
                                  ")? ",
                                  new String[] { mf.format("misc.n"), 
                                                 mf.format("misc.y") },
                                  -1,
                                  mf.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
        if (1 == choice)
        {
            context.getParser().removeAlias(alias);
            out.println (mf.format ("delete.alias.confirmed", alias));
            return;
        }
        else
        {
            throw new OperationInterruptedException();
        }
    }
}