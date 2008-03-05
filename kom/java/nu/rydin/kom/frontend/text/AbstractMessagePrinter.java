package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.ansi.ANSISequences;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.Envelope.RelatedMessage;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public abstract class AbstractMessagePrinter implements MessagePrinter
{

    // The MessagePrinters are implemented using the Template Method pattern.
    // Implement the abstract methods below to decide the actual layout of each
    // part.
    // Most of them have a common implementation in this class that can be
    // overridden.

    public final void printMessage(Context context, Envelope envelope)
            throws KOMException
    {
        // Clear screen if requested by user
        //
        if (context.isFlagSet(0, UserFlags.CLEAR_SCREEN_BEFORE_MESSAGE))
        {
            context.getOut().print(ANSISequences.CLEAR_DISPLAY);
        }
        printHeader(context, envelope);
        printBody(context, envelope);
        printFooter(context, envelope);
    }

    private final void printHeader(Context context, Envelope envelope)
            throws UnexpectedException, AuthorizationException,
            ObjectNotFoundException
    {
        // All things to be printed before the body goes here.

        if (isMail(context, envelope)) {
            context.getDisplayController().mailMessageHeader();
        }
        else {
            context.getDisplayController().messageHeader();
        }
        printFirstLine(context, envelope);
        printMailInformation(context, envelope);
        printReplyOriginInfo(context, envelope);
        printHeaderReceivers(context, envelope);
        printSubject(context, envelope);
    }

    private final void printBody(Context context, Envelope envelope)
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();

        context.getDisplayController().messageBody();
        Message message = envelope.getMessage();
        WordWrapper ww = context.getWordWrapper(message.getBody());
        String line;
        while ((line = ww.nextLine()) != null)
        {
            dc.printWithAttributes(line);
            out.println();
        }
        out.println();
    }

    private final void printFooter(Context context, Envelope envelope)
            throws ObjectNotFoundException, UnexpectedException,
            AuthorizationException
    {
        // All things after the body goes here.

        printMessageFooter(context, envelope);
        printFootNotes(context, envelope);
        printFooterReceivers(context, envelope);
        printReplies(context, envelope);
        printNoComments(context, envelope);
    }

    /**
     * Prints the first line of the message, containing text number, author and
     * date.
     * 
     * @param context
     * @param envelope
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    protected void printFirstLine(Context context, Envelope envelope)
            throws ObjectNotFoundException, UnexpectedException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        Message message = envelope.getMessage();

        StringBuffer sb = new StringBuffer(200);
        sb.append(formatter.format(getResourceKey("textnumber")));
        sb.append(getFormattedMessageId(context, envelope));
        sb.append("; ");
        sb.append(context.formatObjectName(message.getAuthorName(), message
                .getAuthor()));
        sb.append("; ");
        sb.append(context.smartFormatDate(message.getCreated()));
        PrintUtils.printIndented(out, sb.toString(), width, 0);
    }

    /**
     * This method decides how the text number should look like and what parts
     * to print.
     * 
     * @param context
     * @param envelope
     * @return
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    protected abstract StringBuffer getFormattedMessageId(Context context,
            Envelope envelope) throws ObjectNotFoundException,
            UnexpectedException;

    /**
     * This method is supposed to print information about mail, typically stored
     * as Message Attributes.
     * 
     * @param context
     * @param envelope
     * @throws UnexpectedException
     * @throws NumberFormatException
     */
    protected abstract void printMailInformation(Context context,
            Envelope envelope) throws UnexpectedException,
            NumberFormatException;

    /**
     * Prints information about the origin of this text.
     * 
     * @param context
     * @param envelope
     */
    protected void printReplyOriginInfo(Context context, Envelope envelope)
    {
        PrintWriter out = context.getOut();
        int width = context.getTerminalSettings().getWidth();

        Envelope.RelatedMessage replyTo = envelope.getReplyTo();
        if (replyTo != null)
        {
            // Text is a comment
            //
            
            MessageAttribute[] attrs = null;
            try
            {
                attrs = context.getSession().getMatchingMessageAttributes(envelope.getMessage().getId(), MessageAttributes.COMMENT_TYPE);
            }
            catch (UnexpectedException e)
            {
                // Quietly ignore.
            }
            PrintUtils.printIndented(out, getFormattedOriginInfo(context,
                    replyTo, (attrs != null && attrs.length>0) ? attrs : null), width, 0);
        } else
        {
            // Even though this text looks like it's not a comment, it might be
            // a comment to a deleted text.
            //
            MessageAttribute[] attributes = envelope.getAttributes();

            for (int idx = 0; idx < attributes.length; ++idx)
            {
                MessageAttribute each = attributes[idx];
                if (each.getKind() == MessageAttributes.ORIGINAL_DELETED)
                {
                    PrintUtils.printIndented(out,
                            getFormattedOriginDeletedInfo(context, each),
                            width, 0);
                }
            }
        }
    }

    /**
     * Returns a string with information about the origin text.
     * 
     * @param context
     * @param replyTo
     * @return
     */
    protected abstract String getFormattedOriginInfo(Context context,
            RelatedMessage replyTo, MessageAttribute[] attrs);

    /**
     * Returns a string with information about the origin text (which is deleted
     * now).
     * 
     * @param context
     * @param originalDeletedAttribute
     * @return
     */
    protected abstract String getFormattedOriginDeletedInfo(Context context,
            MessageAttribute originalDeletedAttribute);

    /**
     * This method is supposed to print information about the receiving
     * conferences of this text. See also
     * AbstractMessagePrinter.getFooterReceivers which does the equivalent in
     * the footer.
     * 
     * @param context
     * @param envelope
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    protected abstract void printHeaderReceivers(Context context,
            Envelope envelope) throws AuthorizationException,
            ObjectNotFoundException, UnexpectedException;

    protected void printSubject(Context context, Envelope envelope)
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        Message message = envelope.getMessage();
        String subjLine = formatter.format(getResourceKey("subject"));
        out.print(subjLine);
        dc.messageSubject();

        int sl = subjLine.length();
        PrintUtils.printIndented(out, message.getSubject(), width - sl, 0, sl);
        dc.messageHeader();
        PrintUtils.printRepeated(out, '-', Math.min(width - 1, subjLine
                .length()
                + message.getSubject().length()));
        out.println();
    }

    /**
     * Prints information about the author of the text (if enabled by the
     * SHOW_TEXT_FOOTER flag).
     * 
     * @param context
     * @param envelope
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    protected void printMessageFooter(Context context, Envelope envelope)
            throws ObjectNotFoundException, UnexpectedException
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();
        Message message = envelope.getMessage();
        MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();

        dc.messageFooter();

        // Print text footer if requested
        //
        if (context.isFlagSet(0, UserFlags.SHOW_TEXT_FOOTER))
        {
            // If we have a primary occurrence, AND if we are in the conference
            // of this
            // Occurrence, then print the local messagenumber, otherwise print
            // the global
            // messagenumber.
            String authorText = null;
            if ((primaryOcc != null)
                    && (primaryOcc.getConference() == context.getSession()
                            .getCurrentConferenceId()))
            {
                authorText = formatter
                        .format(getResourceKey("local.footer"),
                                new Object[]
                                {
                                        new Integer(primaryOcc.getLocalnum()),
                                        context.formatObjectName(message
                                                .getAuthorName(), message
                                                .getAuthor()) });

            } else
            {
                authorText = formatter
                        .format(getResourceKey("global.footer"),
                                new Object[]
                                {
                                        new Long(message.getId()),
                                        context.formatObjectName(message
                                                .getAuthorName(), message
                                                .getAuthor()) });
            }
            PrintUtils.printIndented(out, authorText, width, 0);
        }
    }

    /**
     * Prints the footnotes to this text.
     * 
     * @param context
     * @param envelope
     */
    protected void printFootNotes(Context context, Envelope envelope)
    {
        MessageAttribute[] attributes = envelope.getAttributes();
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        for (int idx = 0; idx < attributes.length; ++idx)
        {
            MessageAttribute each = attributes[idx];
            if (each.getKind() == MessageAttributes.FOOTNOTE)
            {
                dc.header();
                String label = formatter.format(getResourceKey("footnote"));
                out.print(label);
                dc.messageBody();
                PrintUtils.printIndented(out, each.getValue(), width
                        - label.length(), 0, label.length());
            }
        }
    }

    /**
     * This method is supposed to print information about the receiving
     * conferences of this text. See also
     * AbstractMessagePrinter.getHeaderReceivers which does the equivalent in
     * the header.
     * 
     * @param context
     * @param envelope
     * @throws AuthorizationException
     * @throws ObjectNotFoundException
     * @throws UnexpectedException
     */
    protected abstract void printFooterReceivers(Context context,
            Envelope envelope) throws AuthorizationException,
            ObjectNotFoundException, UnexpectedException;

    /**
     * Prints information about the "No comments" added to this text.
     * 
     * @param context
     * @param envelope
     */
    protected void printNoComments(Context context, Envelope envelope)
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        dc.messageFooter();

        MessageAttribute[] attributes = envelope.getAttributes();

        for (int idx = 0; idx < attributes.length; ++idx)
        {
            MessageAttribute each = attributes[idx];
            if (each.getKind() == MessageAttributes.NOCOMMENT)
            {
                PrintUtils.printIndented(out, formatter.format(
                        getResourceKey("nocomment"), context.formatObjectName(
                                each.getUsername(), each.getUserId())), width,
                        0);
            }
        }
    }

    /**
     * Prints information about the replies to this text. The actual layout of
     * each reply is decided in the getFormattedReply method.
     * 
     * @param context
     * @param envelope
     */
    protected void printReplies(Context context, Envelope envelope)
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        int width = context.getTerminalSettings().getWidth();

        dc.messageFooter();

        Envelope.RelatedMessage[] replies = envelope.getReplies();
        for (int idx = 0; idx < replies.length; ++idx)
        {
            Envelope.RelatedMessage each = replies[idx];
            MessageOccurrence occ = each.getOccurrence();
            MessageAttribute[] ma = null;
            boolean hasAttr = false;
            try
            {
                ma = context.getSession().getMatchingMessageAttributes(occ.getGlobalId(), MessageAttributes.COMMENT_TYPE);
                hasAttr = (ma.length > 0);
            }
            catch (UnexpectedException e)
            {
                // Never mind.
            }

            PrintUtils.printIndented(out,
                    getFormattedReply(context, each, occ, hasAttr ? ma : null), width, 0);
        }
    }

    /**
     * This method specifies how a reply should be formatted
     * 
     * @param context
     * @param reply
     * @param occ
     * @return
     */
    protected abstract String getFormattedReply(Context context,
            RelatedMessage reply, MessageOccurrence occ, MessageAttribute[] attrs);

    /**
     * Finds the MOVEDFROM attribute and returns and returns it if found.
     * Otherwise null.
     * 
     * @param attributes
     * @return
     */
    protected String getMovedFrom(MessageAttribute[] attributes)
    {
        String movedFrom = null;
        for (int attrIdx = attributes.length - 1; 0 <= attrIdx; --attrIdx)
        {
            MessageAttribute each = attributes[attrIdx];
            if (each.getKind() == MessageAttributes.MOVEDFROM)
            {
                movedFrom = new String(each.getValue());
                break;
            }
        }
        return movedFrom;
    }

    /**
     * Get an actual resource key using the concrete class name as prefix.
     * 
     * @param keyName
     * @return a resource key that should match an entry in the
     *         messages.properties file.
     */
    protected String getResourceKey(String keyName)
    {
        return getClass().getSimpleName() + "." + keyName;
    }
    

    /**
     * Determines if the envelope contains a mail, in this case defines as
     * having an message occurrence in the current user's mailbox.
     * @param context
     * @param envelope
     * @return
     */
    protected boolean isMail(Context context, Envelope envelope)
    {
        
        long user = context.getLoggedInUserId();
        
        MessageOccurrence[] occs = envelope.getOccurrences();
        for(int idx = 0; idx < occs.length; ++idx)
        {
            MessageOccurrence occ = occs[idx];
            
            if (occ.getConference() == user)
            {
                return true;
            }
        }
        return false;

    }
    
}