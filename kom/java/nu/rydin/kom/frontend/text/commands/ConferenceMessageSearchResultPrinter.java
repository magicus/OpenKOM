/*
 * Created on Jan 9, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class ConferenceMessageSearchResultPrinter implements
        MessageSearchResultPrinter
{
    protected static final int LOCALID_COL_WIDTH = 7;

    protected static final int AUTHOR_COL_WIDTH = 30;

    protected static final int SUBJECT_COL_WIDTH = 28;


    public void printSearchResultRow(Context context, KOMWriter out,
            MessageSearchResult msr)
    {
        LocalMessageSearchResult lmsr = (LocalMessageSearchResult) msr;
        DisplayController dc = context.getDisplayController();
        dc.normal();
        PrintUtils.printRightJustified(out, String.valueOf(lmsr.getLocalId()),
                LOCALID_COL_WIDTH);
        out.print(" ");
        dc.output();
        PrintUtils.printLeftJustified(out, context.formatObjectName(lmsr
                .getAuthor()), AUTHOR_COL_WIDTH);
        out.print(" ");
        dc.header();
        out.print(msr.getReplyTo() > 0 ? "  " : "* ");
        dc.output();        
        PrintUtils
                .printLeftJustified(out, lmsr.getSubject(), SUBJECT_COL_WIDTH);
        out.println();
        dc.normal();
        out.flush();
    }
    
    public void printSearchResultHeader(Context context)
    {
        KOMWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        DisplayController dc = context.getDisplayController();
        dc.normal();
        HeaderPrinter hp = new HeaderPrinter();
        hp.addHeader(formatter.format("search.heading.text"),
                LOCALID_COL_WIDTH, true);
        hp.addSpace(1);
        hp.addHeader(formatter.format("search.heading.writtenby"),
                AUTHOR_COL_WIDTH, false);
        hp.addSpace(3);
        hp.addHeader(formatter.format("search.heading.subject"),
                SUBJECT_COL_WIDTH, false);
        hp.printOn(out);
    }

}
