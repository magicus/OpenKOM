/*
 * Created on Sep 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.IntegerParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Henrik Schröder
 *
 */
public class ChangeTerminalWidth extends AbstractCommand
{

	public ChangeTerminalWidth(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter("change.width.param.0.ask", true) }, permissions );	
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException
	{
	    MessageFormatter mf = context.getMessageFormatter();
	    KOMWriter out = context.getOut();
	    
	    int width = ((Integer)parameterArray[0]).intValue();
	    if (width == 0)
	    {
	        context.setListenToTerminalSize(true);
	        out.println(mf.format("change.terminal.listenon"));
	    }
		else if (width > 10)
		{
		    context.setListenToTerminalSize(false);
		    context.setTerminalWidth(width);
		    out.println(mf.format("change.terminal.info") + context.getTerminalSettings().getWidth() + "x" + context.getTerminalSettings().getHeight());
		}
		else
		{
		    //TODO: Skrolle: Maybe print error message?
		    out.println(mf.format("change.terminal.info") + context.getTerminalSettings().getWidth() + "x" + context.getTerminalSettings().getHeight());
		}
	}
}
