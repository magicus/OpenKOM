/*
 * Created on 2004-aug-19
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.ClientSession;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.parser.CommandCategory;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;


public class ListCommands extends AbstractCommand
{
	public ListCommands(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}
	
	protected Command[] getCommandList(Context context) throws IOException, KOMException 
	{
		Command[] cmds = ((ClientSession) context).getCommandList();
		return cmds;
	}

	
	public void execute(Context context, Object[] parameterArray)
    throws KOMException, IOException 
    {
		PrintWriter out = context.getOut();
		DisplayController dc = context.getDisplayController();
		MessageFormatter formatter = context.getMessageFormatter();
		TreeSet cats = context.getParser().getCategories();
		for(Iterator itor = cats.iterator(); itor.hasNext();)
		{
		    // Print category header
		    //
		    CommandCategory cat = (CommandCategory) itor.next();
		    dc.highlight();
		    out.println(formatter.format(cat.getI18nKey()));
		    
		    // Print commands
		    //
		    for(Iterator itor2 = cat.getCommands().iterator(); itor2.hasNext();)
		    {
		        Command command = (Command) itor2.next();
		        dc.normal();
		        out.print("  ");
		        out.print(command.getFullName());
				CommandLineParameter[] parameters = command.getSignature();
				for (int j = 0; j < parameters.length; j++) 
				{
	                CommandLineParameter parameter = parameters[j];
	                if (j > 0) 
	                    out.print(parameter.getSeparator());
	                out.print(" " + parameter.getUserDescription(context));
	            }
				out.println();		        
		    }
		}
    }
}