/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SimpleMessageEditor extends AbstractEditor
{		
	public SimpleMessageEditor(Context context)
	throws IOException, UnexpectedException
	{
		super("/editorcommands.list", context);
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
			// if this is a reply, retrieve subject from original message
			//
			if(replyTo > 0)
			{
				oldSubject = m_context.getSession().innerReadMessage(replyTo).getMessage().getSubject();
				
			}
				
			dc.messageHeader();
			// Print author
			//
			out.println(formatter.format("simple.editor.author", m_context.getCachedUserInfo().getName()));
				
			// Print receiver
			//
			out.println(formatter.format("simple.editor.receiver", "Tornado (I väntan på riktiga mötet)"));
			
			// Read subject
			//
			String subjLine = formatter.format("simple.editor.subject");
			out.print(subjLine);
			dc.input();
			out.flush();
			m_context.setSubject(in.readLine(oldSubject));
			dc.messageHeader();
			PrintUtils.printRepeated(out, '-', subjLine.length() + m_context.getSubject().length());
			dc.input();
			out.println();
						
			// Enter the main editor loop
			//
			if(!this.mainloop(false))
			    throw new OperationInterruptedException();
			return new UnstoredMessage(m_context.getSubject(), m_context.getBuffer().toString());
		}
		catch(IOException e)
		{
			throw new KOMRuntimeException(formatter.format("error.reading.user.input"), e);		
		}
	}	
}
