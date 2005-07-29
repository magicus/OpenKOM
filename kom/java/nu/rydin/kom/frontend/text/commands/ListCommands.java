/*
 * Created on 2004-aug-19
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.parser.CommandCategory;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;


public class ListCommands extends AbstractCommand
{
	public ListCommands(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}
	
	protected Collection getCategories(Context context) throws IOException, KOMException 
	{
		return context.getParser().getCategories();
	}
	
	public void execute(Context context, Object[] parameterArray)
    throws KOMException, IOException 
    {
		PrintWriter out = context.getOut();
		DisplayController dc = context.getDisplayController();
		MessageFormatter formatter = context.getMessageFormatter();
		Collection cats = this.getCategories(context);
		for(Iterator itor = cats.iterator(); itor.hasNext();)
		{
		    // Print category header
		    //
		    CommandCategory cat = (CommandCategory) itor.next();
		    dc.header();
		    out.println();
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