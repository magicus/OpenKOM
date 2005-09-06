/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SimpleMessageEditor extends AbstractEditor implements MessageEditor
{		
	public SimpleMessageEditor(Context context)
	throws IOException, UnexpectedException
	{
		super("editorcommands.xml", context);
	}
	
	public UnstoredMessage edit()
	throws KOMException, InterruptedException
	{
	    return this.edit(-1, -1, -1, null, -1, null, null);
	}
	
	public UnstoredMessage edit(long replyTo, long replyToLocal, long recipientId, 
	        String recipientName, long replyToAuthor, String replyToAuthorName, String oldSubject)
		throws KOMException, InterruptedException
	{
		DisplayController dc = this.getDisplayController();
		PrintWriter out = this.getOut();
		LineEditor in = this.getIn();
		MessageFormatter formatter = this.getMessageFormatter();	
		int width = this.getTerminalSettings().getWidth();
		try
		{
			dc.messageHeader();
			// Print author
			//
			out.println(formatter.format("simple.editor.author", this.getCachedUserInfo().getName()));
			
			if(replyTo != -1)
			{
                long replyToConferenceId = this.getSession().getMostRelevantOccurrence(this.getSession().getCurrentConferenceId(), replyTo).getConference(); 
                
				if(this.getRecipient().getId() == replyToConferenceId)
				{
					// Simple case: Original text is in same conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.same.conference", 
						new Object[] { new Long(replyToLocal), 
							this.formatObjectName(recipientName, recipientId) } ));		
				}
				else
				{
					// Complex case: Original text was in a different conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.different.conference", 
							new Object[] { new Long(replyToLocal),
	                            this.formatConferenceName(replyToConferenceId, this.getSession().getConference(replyToConferenceId).getName()),
						        this.formatObjectName(replyToAuthorName, replyToAuthor) }));
				}
			}
			
			// Print receiver
			//
			out.println(formatter.format("simple.editor.receiver", this.formatObjectName(this.getRecipient())));
			
			// Read subject
			//
			String subjLine = formatter.format("simple.editor.subject");
			out.print(subjLine);
			dc.input();
			out.flush();
			this.setSubject(in.readLine(oldSubject));
			dc.messageHeader();
			PrintUtils.printRepeated(out, '-', Math.min(width - 1, subjLine.length() + this.getSubject().length()));
			out.println();
						
			// Enter the main editor loop
			//
			this.mainloop(false);
			return new UnstoredMessage(this.getSubject(), this.getBuffer().toString());
		}
		catch(IOException e) 
		{
			throw new KOMRuntimeException(formatter.format("error.reading.user.input"), e);		
		}
	}	
	
	protected void refresh() throws KOMException
	{

	    new ShowSimpleMessage(this, "", 0).execute(this, new Object[0]);
	}
	
	protected String getAbortQuestionFormat()
    {
        return "simple.editor.abortquestion";
    }    
}
