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
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author Henrik Schröder
 */
public abstract class SearchCommand extends AbstractCommand
{
    protected static final int CHUNK_SIZE = 50;
    
    public SearchCommand(String fullName,
            CommandLineParameter[] signature)
    {
        super(fullName, signature);
    }

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
		KOMWriter out = context.getOut();

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
    
    protected static void printNoSearchResultsMessage(Context context)
    {
        MessageFormatter formatter = context.getMessageFormatter();
        context.getOut().println(formatter.format("search.noresults"));
    }
    
    abstract MessageSearchResult[] innerSearch(Context context, Object[] parameterArray, int offset)
    throws UnexpectedException;
    
    abstract void innerPrintSearchResultRow(Context context, KOMWriter out, MessageSearchResult msr);
    
    abstract void printSearchResultHeader(Context context);
}
