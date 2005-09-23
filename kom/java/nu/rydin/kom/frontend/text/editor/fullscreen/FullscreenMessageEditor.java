/*
 * Created on Aug 19, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.fullscreen;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.AmbiguousPatternException;
import nu.rydin.kom.exceptions.EscapeException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;
import nu.rydin.kom.frontend.text.KeystrokeTokenizerDefinition;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.Shell;
import nu.rydin.kom.frontend.text.constants.Keystrokes;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.frontend.text.terminal.TerminalController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Pontus Rydin
 */
public class FullscreenMessageEditor extends FullscreenEditor implements
        MessageEditor
{
    private final static int NUM_HEADER_ROWS = 3;

    private String replyHeader;

    private String recipientHeader;

    private int headerRows;

    public FullscreenMessageEditor(Context context) throws IOException,
            UnexpectedException
    {
        super(context);
    }

    public UnstoredMessage edit() throws KOMException, InterruptedException
    {
        return this.edit(-1, -1, -1, "", -1, "", "");
    }

    protected void printHeader()
    {
        TerminalController tc = this.getTerminalController();
        DisplayController dc = this.getDisplayController();
        PrintWriter out = this.getOut();
        LineEditor in = this.getIn();
        MessageFormatter formatter = this.getMessageFormatter();
        tc.eraseScreen();
        tc.setCursor(0, 0);
        dc.messageHeader();

        if (replyHeader != null)
            out.println(replyHeader);
        out.println(recipientHeader);
        String subjLine = formatter.format("simple.editor.subject");
        out.print(subjLine);
        String subject = this.getSubject();
        if (subject == null)
            subject = "";
        dc.messageBody();
        out.println(subject);
        dc.messageHeader();
        PrintUtils
                .printRepeated(out, '-', subjLine.length() + subject.length());
        out.flush();
    }

    protected void refresh()
    {
        this.printHeader();
        this.getOut().println();
        this.getDisplayController().messageBody();
        super.refresh();
    }

    public UnstoredMessage edit(long replyTo, long replyToLocal,
            long recipientId, String recipientName, long replyToAuthor,
            String replyToAuthorName, String oldSubject) throws KOMException,
            InterruptedException
    {
        TerminalController tc = this.getTerminalController();
        DisplayController dc = this.getDisplayController();
        PrintWriter out = this.getOut();
        LineEditor in = this.getIn();
        MessageFormatter formatter = this.getMessageFormatter();
        try
        {
            tc.eraseScreen();
            tc.setCursor(0, 0);
            dc.messageHeader();

            // Handle reply
            //
            if (replyTo != -1)
            {
                if (this.getRecipient().getId() == recipientId)
                {
                    // Simple case: Original text is in same conference
                    //
                    replyHeader = formatter.format(
                            "CompactMessagePrinter.reply.to.same.conference",
                            new Object[]
                            {
                                    new Long(replyToLocal),
                                    this.formatObjectName(replyToAuthorName,
                                            replyToAuthor) });
                } else
                {
                    // Complex case: Original text was in a different conference
                    //
                    replyHeader = formatter
                            .format(
                                    "CompactMessagePrinter.reply.to.different.conference",
                                    new Object[]
                                    {
                                            new Long(replyToLocal),
                                            this.formatObjectName(
                                                    recipientName, recipientId),
                                            this.formatObjectName(
                                                    replyToAuthorName,
                                                    replyToAuthor) });
                }
            }

            // Construct receiver
            //
            recipientHeader = formatter.format("simple.editor.receiver", this
                    .formatObjectName(this.getRecipient()));
            this.printHeader();

            // Read subject
            //
            tc.up(1);
            dc.input();
            out.flush();
            this.setSubject(in.readLine(oldSubject));

            // Establish viewport
            //
            headerRows = NUM_HEADER_ROWS;
            if (replyTo != -1)
                ++headerRows;
            this.pushViewport(headerRows, this.getTerminalSettings()
                    .getHeight() - 1);
            this.refresh();

            // Enter the main editor loop
            //
            boolean pageBreak = in.getPageBreak();
            in.setPageBreak(false);
            try
            {
                this.mainloop();
            }
            finally
            {
                in.setPageBreak(pageBreak);
                this.popViewport();
            }
            return new UnstoredMessage(this.getSubject(), this.getBuffer()
                    .toString());
        } catch (IOException e)
        {
            throw new KOMRuntimeException(formatter
                    .format("error.reading.user.input"), e);
        }
    }

    protected KeystrokeTokenizer getKeystrokeTokenizer()
    {
        // Get a copy of the keystroke tokenizer definition
        //
        KeystrokeTokenizerDefinition kstd = this.getTerminalController()
                .getKeystrokeTokenizer().getDefinition().deepCopy();

        // Add keystrokes specific to us
        //
        try
        {
            kstd.addPattern("\u000b\u000b", Keystrokes.TOKEN_COMMAND
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K Ctrl-K
            kstd.addPattern("\u000bK", Keystrokes.TOKEN_COMMAND
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K K
            kstd.addPattern("\u000bk", Keystrokes.TOKEN_COMMAND
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K k
            kstd.addPattern("\u000b\u0011", Keystrokes.TOKEN_QUOTE
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K Ctrl-Q
            kstd.addPattern("\u000bQ", Keystrokes.TOKEN_QUOTE
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K Q
            kstd.addPattern("\u000bq", Keystrokes.TOKEN_QUOTE
                    | Keystrokes.TOKEN_MOFIDIER_BREAK); // Ctrl-K q
            return kstd.createKeystrokeTokenizer();
        } catch (AmbiguousPatternException e)
        {
            Logger.error(this, "Ambigous keystroke pattern", e);
            throw new RuntimeException(e);
        }
    }

    protected void unknownToken(KeystrokeTokenizer.Token token)
            throws EscapeException
    {
        LineEditor in = this.getIn();
        try
        {

            switch (token.getKind() & ~Keystrokes.TOKEN_MOFIDIER_BREAK)
            {
            case Keystrokes.TOKEN_QUOTE:
                this.quote();
                break;
            case Keystrokes.TOKEN_COMMAND:
                in.popTokenizer();
                try
                {
                    TerminalController tc = this.getTerminalController();
                    tc.eraseScreen();
                    tc.setCursor(0, 0);
                    Shell i = new Shell(Parser.load(
                            "fullscreeneditorcommands.xml", this));
                    i.run(this, "editor");
                    this.refresh();
                } finally
                {
                    in.pushTokenizer(this.getKeystrokeTokenizer());
                }
                break;
            default:
                super.unknownToken(token);
            }
        } 
        catch (IOException e)
        {
            throw new RuntimeException(e);
        } 
        catch (KOMException e)
        {
            throw new RuntimeException(e);
        }

    }
    
    protected void addQuote(String line)
    {
        Buffer buffer = this.getBuffer(); 
        buffer.set(m_cy + m_viewportStart, line + '\n');
        // buffer.setNewline(m_cy + m_viewportStart, true);
        this.insertLine("");
        this.refreshCurrentLine();
    	this.moveDown();
    	m_tc.eraseToEndOfLine();
    }

    public void quote() throws KOMException, IOException
    {
        KOMWriter out = this.getOut();
        long replyTo = this.getReplyTo();
        if(replyTo == -1)
            return;
        try
        {
            int middle = (this.getTerminalSettings().getHeight() - headerRows) / 2;
            this.pushViewport(0, middle);
            this.revealCursor(false);
            this.refreshViewport();
            m_tc.setCursor(middle, 0);
            m_tc.reverseVideo();
            m_tc.messageHeader();
            String divider = "--" + this.getMessageFormatter().format("fullscreen.editor.quote.help");
            out.print(divider);
            PrintUtils.printRepeated(out, '-', this.getTerminalSettings().getWidth() - divider.length() - 1);
            m_tc.reset();
            m_tc.messageBody();
            QuoteEditor quoter = new QuoteEditor(this, replyTo, this);
            quoter.pushViewport(middle + 5, this.getTerminalSettings().getHeight() - 1);
            quoter.edit();
            this.refresh();
        }
        catch(InterruptedException e)
        {
            // Just return
        }
        finally
        {
            this.popViewport();
            this.refresh();
        }
    }
}