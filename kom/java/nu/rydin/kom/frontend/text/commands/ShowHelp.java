/*
 * Created on 2004-aug-31
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.OtherCommandParameter;

/**
 * @author Magnus Ihse
 */
public class ShowHelp extends AbstractCommand
{

    public ShowHelp(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new OtherCommandParameter(true) } );
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        Command command = (Command) parameterArray[0];
        context.getOut().println("Så, du vill ha hjälp om '" + command.getFullName() + "'?");
        context.getOut().println("Din looser! Det finns ingen! Haha! :-)");
    }

}
