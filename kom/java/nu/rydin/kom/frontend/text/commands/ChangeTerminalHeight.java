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
public class ChangeTerminalHeight extends AbstractCommand
{

	public ChangeTerminalHeight(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter("change.height.param.0.ask", true) } );	
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException
	{
	    MessageFormatter mf = context.getMessageFormatter();
	    KOMWriter out = context.getOut();
	    
	    int height = ((Integer)parameterArray[0]).intValue();
	    if (height == 0)
	    {
	        context.setListenToTerminalSize(true);
	        out.println(mf.format("change.terminal.listenon"));
	    }
		else if (height > 10)
		{
		    context.setListenToTerminalSize(false);
		    context.setTerminalHeight(height);
		    out.println(mf.format("change.terminal.info") + context.getTerminalSettings().getWidth() + "x" + context.getTerminalSettings().getHeight());
		}
		else
		{
		    //TODO: Skrolle: Maybe print error message?
		    out.println(mf.format("change.terminal.info") + context.getTerminalSettings().getWidth() + "x" + context.getTerminalSettings().getHeight());
		}
	}
}
