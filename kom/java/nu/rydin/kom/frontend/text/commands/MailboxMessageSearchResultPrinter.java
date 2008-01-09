package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

public class MailboxMessageSearchResultPrinter implements
        MessageSearchResultPrinter
{

    protected static final int LOCALID_COL_WIDTH = 7;

    protected static final int USER_COL_WIDTH = 30;

    protected static final int SUBJECT_COL_WIDTH = 28;

    protected static final int DIRECTION_COL_WIDTH = 1;

    
    public void printSearchResultRow(Context context, KOMWriter out,
            MessageSearchResult msr)
    {
        MessageFormatter formatter = context.getMessageFormatter();
        LocalMessageSearchResult lmsr = (LocalMessageSearchResult) msr;
        DisplayController dc = context.getDisplayController();
        dc.normal();
        PrintUtils.printRightJustified(out, String.valueOf(lmsr.getLocalId()),
                LOCALID_COL_WIDTH);
        out.print(" ");
        dc.output();
        String user = null;
        String direction = null;
        NameAssociation mailRecipient = lmsr.getMailRecipient();
        if (lmsr.getAuthor().getId() != context.getLoggedInUserId() ||
                mailRecipient == null)
        {
            user = context.formatObjectName(lmsr.getAuthor());
            direction = formatter.format(
                    mailRecipient == null 
                        ? "search.mailbox.direction.copy"
                                : "search.mailbox.direction.from");
        }
        else 
        {
            user = mailRecipient.getName().getName();
            direction = formatter.format("search.mailbox.direction.to");
        } 
        dc.header();
        PrintUtils.printLeftJustified(out, direction, DIRECTION_COL_WIDTH);
        dc.output();        
        out.print(" ");
        PrintUtils.printLeftJustified(out, user, USER_COL_WIDTH);
        
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
        hp.addHeader(formatter.format("search.mailbox.heading.text"),
                LOCALID_COL_WIDTH, true);
        hp.addSpace(1);
        hp.addSpace(DIRECTION_COL_WIDTH);
        hp.addSpace(1);
        hp.addHeader(formatter.format("search.mailbox.heading.user"),
                USER_COL_WIDTH, false);
        hp.addSpace(3);
        hp.addHeader(formatter.format("search.mailbox.heading.subject"),
                SUBJECT_COL_WIDTH, false);
        hp.printOn(out);
    }

}
