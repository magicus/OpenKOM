/*
 * Created on Sep 11, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 */
public abstract class SearchLocalCommand extends SearchCommand
{
    protected static final int LOCALID_COL_WIDTH = 7;

    protected static final int AUTHOR_COL_WIDTH = 30;

    protected static final int SUBJECT_COL_WIDTH = 28;

    public SearchLocalCommand(String fullName, CommandLineParameter[] signature, long permissions)
    {
        super(fullName, signature, permissions);
    }

    protected void innerPrintSearchResultRow(Context context, KOMWriter out,
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
		dc.highlight();
		out.print(msr.getReplyTo() > 0 ? "  " : "* ");
		dc.output();        
        PrintUtils
                .printLeftJustified(out, lmsr.getSubject(), SUBJECT_COL_WIDTH);
        out.println();
        dc.normal();
        out.flush();
    }

    protected void printSearchResultHeader(Context context)
    {
        KOMWriter out = context.getOut();
        DisplayController dc = context.getDisplayController();
        dc.normal();
        MessageFormatter formatter = context.getMessageFormatter();
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