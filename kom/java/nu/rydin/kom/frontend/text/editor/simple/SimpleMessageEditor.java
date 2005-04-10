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
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
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
	
	public UnstoredMessage edit(long replyTo)
		throws KOMException, InterruptedException
	{
		DisplayController dc = m_context.getDisplayController();
		PrintWriter out = m_context.getOut();
		LineEditor in = m_context.getIn();
		MessageFormatter formatter = m_context.getMessageFormatter();	
		String oldSubject = null;
		try
		{
			dc.messageHeader();
			// Print author
			//
			out.println(formatter.format("simple.editor.author", m_context.getCachedUserInfo().getName()));

			// FIXME EDITREFACTOR: This whole thing does very unneccessary lookups since the editor doesn't hold enough information about the message being replied to.
			// Handle reply
			//
			if(replyTo != -1)
			{
			    // Fetch reply-to
			    //
			    Message oldMessage = m_context.getSession().innerReadMessage(replyTo).getMessage();
			    MessageOccurrence oldMessageOcc = m_context.getSession().getMostRelevantOccurrence(m_context.getSession().getCurrentConferenceId(), replyTo); 
			    
			    // Fetch old subject
			    //
			    oldSubject = oldMessage.getSubject();
			    
				if(m_context.getRecipient().getId() == oldMessageOcc.getConference())
				{
					// Simple case: Original text is in same conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.same.conference", 
						new Object[] { new Long(oldMessageOcc.getLocalnum()), 
							m_context.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) } ));		
				}
				else
				{
					// Complex case: Original text was in a different conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.different.conference", 
						new Object[] { new Long(oldMessageOcc.getLocalnum()),
					        m_context.formatObjectName(m_context.getSession().getConference(oldMessageOcc.getConference()).getName(),
					        oldMessageOcc.getConference()), 
					        m_context.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) }));
				}
			}
			
			// Print receiver
			//
			out.println(formatter.format("simple.editor.receiver", m_context.formatObjectName(m_context.getRecipient())));
			
			// Read subject
			//
			String subjLine = formatter.format("simple.editor.subject");
			out.print(subjLine);
			dc.input();
			out.flush();
			m_context.setSubject(in.readLine(oldSubject));
			dc.messageHeader();
			PrintUtils.printRepeated(out, '-', subjLine.length() + m_context.getSubject().length());
			out.println();
						
			// Enter the main editor loop
			//
			this.mainloop(false);
			return new UnstoredMessage(m_context.getSubject(), m_context.getBuffer().toString());
		}
		catch(IOException e) 
		{
			throw new KOMRuntimeException(formatter.format("error.reading.user.input"), e);		
		}
	}	
	
	protected void refresh() throws KOMException
	{

	    new ShowSimpleMessage(m_context, "", 0).execute(m_context, new Object[0]);
	}
	
	public void setRecipient(NameAssociation recipient)
	{
	    m_context.setRecipient(recipient);
	}
	
	public NameAssociation getRecipient()
	{
	    return m_context.getRecipient();
	}
	
	public void setReplyTo(long replyTo)
	{
	    m_context.setReplyTo(replyTo);
	}

	protected String getAbortQuestionFormat()
    {
        return "simple.editor.abortquestion";
    }    
}
