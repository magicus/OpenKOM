/*
 * Created on Aug 19, 2005
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
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Pontus Rydin
 */
public class ListSystemThreads extends AbstractCommand
{
	public ListSystemThreads(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions); 
	}

    public void execute(Context context, Object[] parameters) throws KOMException, IOException, InterruptedException
    {
        MessageFormatter formatter = context.getMessageFormatter();
        KOMWriter out = context.getOut();
        
        // Print header
        //
		HeaderPrinter hp = new HeaderPrinter();
		hp.addHeader(formatter.format("listsystemthreads.flags"), 3, false);
		hp.addHeader(formatter.format("listsystemthreads.prio"), 6, true);
		hp.addSpace(1);
		int termWidth = context.getTerminalSettings().getWidth();
		int firstColsWidth = 3 + 6 + 1;
		int lastColWidth = termWidth - firstColsWidth - 1 ; 
		hp.addHeader(formatter.format("listsystemthreads.name"), lastColWidth, false);
		hp.printOn(out);
		
		// Print information for each thread
		//
        Thread[] threads = new Thread[10000];
        int top = Thread.enumerate(threads);
        for (int idx = 0; idx < top; idx++)
        {
            Thread t = threads[idx];
            
            // Print state
            //
            out.print(t.isAlive() ? 'A' : '-');
            out.print(t.isDaemon() ? 'D' : '-');
            out.print(t.isInterrupted() ? 'I' : '-');
            // out.print(' ');
            
            // Print priority
            //
            PrintUtils.printRightJustified(out, Integer.toString(t.getPriority()), 6);
            out.print(' ');
            
            // Print name
            //
            out.println(t.getName());
        }
    }
}
