/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SimpleMessageEditor extends AbstractEditor
{		
	public SimpleMessageEditor(MessageFormatter formatter)
	throws IOException, UnexpectedException
	{
		super("/editorcommands.list", formatter);
	}
	
	public UnstoredMessage edit(Context underlying, long replyTo)
		throws KOMException, InterruptedException, IOException
	{
		EditorContext context = new EditorContext(underlying);
		DisplayController dc = context.getDisplayController();
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter formatter = context.getMessageFormatter();	
		String oldSubject = null;
		try
		{
			// if this is a reply, retrieve subject from original message
			//
			if(replyTo > 0)
			{
				oldSubject = context.getSession().innerReadMessage(replyTo).getMessage().getSubject();
				
			}
				
			dc.messageHeader();
			// Print author
			//
			out.println(formatter.format("simple.editor.author", context.getCachedUserInfo().getName()));
				
			// Print receiver
			//
			out.println(formatter.format("simple.editor.receiver", "Tornado (I väntan på riktiga mötet)"));
			
			// Read subject
			//
			String subjLine = formatter.format("simple.editor.subject");
			out.print(subjLine);
			dc.normal();
			out.flush();
			context.setSubject(in.readLine(oldSubject));
			dc.messageHeader();
			PrintUtils.printRepeated(out, '-', subjLine.length() + context.getSubject().length());
			dc.normal();
			out.println();
						
			// Enter the main editor loop
			//
			if(!this.mainloop(context, false))
			    throw new OperationInterruptedException();
			return new UnstoredMessage(context.getSubject(), context.getBuffer().toString());
		}
		catch(IOException e)
		{
			throw new KOMException(formatter.format("error.reading.user.input"), e);		
		}
	}	
}
