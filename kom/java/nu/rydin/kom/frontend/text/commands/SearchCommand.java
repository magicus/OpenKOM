/*
 * Created on Sep 11, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author Henrik Schröder
 */
public abstract class SearchCommand extends AbstractCommand
{
    protected static final int CHUNK_SIZE = 100;
    
    public SearchCommand(String fullName,
            CommandLineParameter[] signature, long permissions)
    {
        super(fullName, signature, permissions);
    }

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
		KOMWriter out = context.getOut();
		
		// Allow the concrete classes to do context specific initialisation
		// before printing the result
		preparePrinting(context);
		
		// Get approximate count
		//
		long n = this.count(context, parameterArray);
		if(n == 0)
		{
		    printNoSearchResultsMessage(context);
		    return;
		}
		else
		{
		    printCount(context, n);
		}

		boolean hasPrintedHeader = false;
		
		for(int offset = 0;; offset += CHUNK_SIZE)
		{
            MessageSearchResult[] msr = innerSearch(context, parameterArray, offset);
		    
		    int top = msr.length;
		    if(top == 0)
		    {
		        if (!hasPrintedHeader)
		        {
		            printNoSearchResultsMessage(context);
		        }
		        break;
		    }
		    
		    if((offset == 0) && !hasPrintedHeader)
		    {
		        printSearchResultHeader(context);
		        hasPrintedHeader = true;
		    }
		    
			for (int i = 0; i < top; ++i)
			{
				innerPrintSearchResultRow(context, out, msr[i]);
			}
		}
	}
    
    protected void preparePrinting(Context context) 
    {
        // Default behavior - do nothing
    }

    protected static void printNoSearchResultsMessage(Context context)
    {
        MessageFormatter formatter = context.getMessageFormatter();
        context.getOut().println(formatter.format("search.noresults"));
    }
    
    abstract MessageSearchResult[] innerSearch(Context context, Object[] parameterArray, int offset)
    throws UnexpectedException;
    
    abstract long count(Context context, Object[] parameterArray)
    throws KOMException;
    
    abstract void innerPrintSearchResultRow(Context context, KOMWriter out, MessageSearchResult msr);
    
    abstract void printSearchResultHeader(Context context);
    
    protected void printCount(Context context, long count)
    {
        KOMWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        DisplayController dc = context.getDisplayController();
        dc.normal();
        out.println(formatter.format("search.count", new Object[] { new Long(count) }));
        out.println();
    }

}
