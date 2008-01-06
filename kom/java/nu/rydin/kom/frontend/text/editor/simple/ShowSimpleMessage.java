/*
 * Created on Jun 22, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author Henrik Schröder
 */
public class ShowSimpleMessage extends AbstractCommand
{
	public ShowSimpleMessage(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException
	{
	    //FIXME EDITREFACTOR: This code is copy&pasted from the SimpleMessageEditor init.
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		EditorContext edContext = (EditorContext) context;
		DisplayController dc = context.getDisplayController();
		
		//Preamble isn't always executed since ctrl-l executes this command with no ExecutableCommand wrapping. 
		out.println();

		
		// Print header
		//
		dc.messageHeader();
		out.println(formatter.format("simple.editor.author", context.getCachedUserInfo().getName()));
		
		// FIXME EDITREFACTOR: This whole thing does very unneccessary lookups since the editor doesn't hold enough information about the message being replied to.
		// Handle reply
		//
		if(edContext.getReplyTo().isValid())
		{
		    // Fetch reply-to
		    //
		    Message oldMessage = edContext.getSession().readMessage(edContext.getReplyTo()).getMessage();
		    MessageLocator oldMessageLocator = edContext.getSession().resolveLocator(edContext.getReplyTo()); 
		    
			if(edContext.getRecipient().getId() == oldMessageLocator.getConference())
			{
				// Simple case: Original text is in same conference
				//
				out.println(formatter.format("CompactMessagePrinter.reply.to.same.conference", 
					new Object[] { new Long(oldMessageLocator.getLocalnum()), 
				        edContext.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) } ));		
			}
			else
			{
				// Complex case: Original text was in a different conference
				//
				out.println(formatter.format("CompactMessagePrinter.reply.to.different.conference", 
					new Object[] { new Long(oldMessageLocator.getLocalnum()),
				        edContext.formatObjectName(edContext.getSession().getConference(oldMessageLocator.getConference()).getName(),
				        oldMessageLocator.getConference()), 
				        edContext.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) }));
			}
		}
		
		// Print recipient
		//
		out.println(formatter.format("simple.editor.receiver", edContext.formatObjectName(edContext.getRecipient())));
				
		// Print subject
		//
		String subjLine = formatter.format("simple.editor.subject");
		out.print(subjLine);
		dc.messageSubject();
		out.println(edContext.getSubject());
		dc.messageHeader();
		PrintUtils.printRepeated(out, '-', subjLine.length() + edContext.getSubject().length());
		out.println();		
		
		// Print body
		//
		Buffer buffer = edContext.getBuffer();
		int top = buffer.size();
		for(int idx = 0; idx < top; ++idx)
		{
		    dc.editorLineNumber();
			PrintUtils.printRightJustified(out, Integer.toString(idx + 1), 4);
			out.print(':');
			dc.messageBody();
			String line = buffer.get(idx).toString();
			int l = line.length();
			if(l > 0 && line.endsWith("\n"))
				line = line.substring(0, l - 1);
			out.println(line);
		}
	}

	public void printPreamble(PrintWriter out)
	{
		// Nothing 
	}
	
	public void printPostamble(PrintWriter out)
	{
		// Nothing 
	}	
}
