/*
 * Created on Sep 11, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * @author Henrik Schröder
 */
public abstract class SearchLocalCommand extends SearchCommand
{
    protected static final int LOCALID_COL_WIDTH = 7;

    protected static final int AUTHOR_COL_WIDTH = 30;

    protected static final int SUBJECT_COL_WIDTH = 28;
    
    protected MessageSearchResultPrinter m_resultPrinter = null;

    public SearchLocalCommand(String fullName, CommandLineParameter[] signature, long permissions)
    {
        super(fullName, signature, permissions);
    }

    protected void preparePrinting(Context context) 
    {
        if (context.getSession().getCurrentConferenceId() == context.getLoggedInUserId()) {
            m_resultPrinter = new MailboxMessageSearchResultPrinter();
        }
        else
        {
            m_resultPrinter = new ConferenceMessageSearchResultPrinter();
        }
    }
    
    protected void innerPrintSearchResultRow(Context context, KOMWriter out,
            MessageSearchResult msr)
    {
        m_resultPrinter.printSearchResultRow(context, out, msr);        
    }
    
    protected void printSearchResultHeader(Context context)
    {
        m_resultPrinter.printSearchResultHeader(context);
    }
}