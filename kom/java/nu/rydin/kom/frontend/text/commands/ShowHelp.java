/*
 * Created on 2004-aug-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
