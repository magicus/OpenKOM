/*
 * Created on Oct 16, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.ansi.ANSISequences;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class BasicMessagePrinter implements MessagePrinter
{
	public void printMessage(Context context, Envelope envelope)
		throws KOMException
	{
	    DisplayController dc = context.getDisplayController();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		Message message = envelope.getMessage();
		MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();
		
		// Clear screen if requested by user
		//
		if(context.isFlagSet(0, UserFlags.CLEAR_SCREEN_BEFORE_MESSAGE))
			out.print(ANSISequences.CLEAR_DISPLAY);
		
		// Could we figure out the local number?
		//
		dc.messageHeader();
		out.print(formatter.format("BasicMessagePrinter.textnumber"));
		if(primaryOcc != null)
			out.print(primaryOcc.getLocalnum());

		// Print global numbber
		//
		out.print(" (");
		out.print(message.getId());
		out.print(')');
		
		// Print name of author
		//
		out.print("; ");
		out.print(context.formatObjectName(message.getAuthorName(), message.getAuthor()));
				
		// Print creation date
		//
		out.print("; ");
		out.println(context.smartFormatDate(message.getCreated()));
		
		// Print reply info (if any)
		//
		Envelope.RelatedMessage replyTo = envelope.getReplyTo();
		if(replyTo != null)
		{
				 			
			if(replyTo.isLocal())
			{
				// Simple case: Original text was in same conference
				//
				out.println(formatter.format("BasicMessagePrinter.reply.to.same.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), 
						context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) } ));		
			}
			else
			{
				// Complex case: Original text was in a different conference
				//
				out.println(formatter.format("BasicMessagePrinter.reply.to.different.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), 
				        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()), 
				        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }));
			}
		}		
		
		// Print receiver list
		//
		int space = formatter.format("BasicMessagePrinter.receiver", "").length();
		MessageOccurrence[] occs = envelope.getOccurrences();
		NameAssociation[] receivers = envelope.getReceivers();
		String movedFrom = null;
		int top = receivers.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MessageOccurrence occ = occs[idx];
			out.println(formatter.format("BasicMessagePrinter.receiver", 
			        context.formatObjectName(receivers[idx].getName(), receivers[idx].getId())));
			switch(occ.getKind())
			{
				case MessageManager.ACTION_COPIED:
					PrintUtils.printRepeated(out, ' ', space);
					out.println(formatter.format("BasicMessagePrinter.copied", 
						new Object[] { context.formatObjectName(occ.getUserName(), occ.getUser()), 
					        context.smartFormatDate(occ.getTimestamp()) }));
					break;
				case MessageManager.ACTION_MOVED:
					MessageAttribute[] attributes = envelope.getAttributes();
					for(int attrIdx = attributes.length-1; 0 < attrIdx; --attrIdx)
					{
						MessageAttribute each = attributes[attrIdx];
						if(each.getKind() == MessageManager.ATTR_MOVEDFROM)
						{
							movedFrom = new String(each.getValue());
							break;
						}
					}
					PrintUtils.printRepeated(out, ' ', space);
					out.println(formatter.format("BasicMessagePrinter.moved.long", 
						new Object[] { movedFrom, context.formatObjectName(occ.getUserName(), 
						        occ.getUser()), context.smartFormatDate(occ.getTimestamp()) }));
					break;					
			}
		} 
				
		// Print subject
		//
		String subjLine = formatter.format("BasicMessagePrinter.subject"); 
		out.print(subjLine);
		dc.messageSubject();
		out.println(message.getSubject());
		dc.messageHeader();
		PrintUtils.printRepeated(out, '-', subjLine.length() + message.getSubject().length());
		out.println();
		
		// Print body
		//
		dc.messageBody();
		WordWrapper ww = context.getWordWrapper(message.getBody());
		String line;
		while((line = ww.nextLine()) != null)
			out.println(line);
		out.println();
		dc.messageFooter();
		
		// Print text footer if requested
		//
		if(context.isFlagSet(0, UserFlags.SHOW_TEXT_FOOTER))
			out.println(formatter.format("BasicMessagePrinter.footer", 
				new Object[] { new Long(primaryOcc.getLocalnum()), 
			        context.formatObjectName(message.getAuthorName(), message.getAuthor()) }));
		
		// Print list of replies
		//
		Envelope.RelatedMessage[] replies = envelope.getReplies();
		top = replies.length;
		for(int idx = 0; idx < top; ++idx)
		{
			Envelope.RelatedMessage each = replies[idx];	
			
			// Reply in same conference? 
			//
			if(each.isLocal())
			{
				out.println(formatter.format("BasicMessagePrinter.reply.same.conference",
					new Object[] { new Long(each.getOccurrence().getLocalnum()), each.getAuthorName() }));
			}
			else
			{
				out.println(formatter.format("BasicMessagePrinter.reply.different.conference",
					new Object[] { new Long(each.getOccurrence().getLocalnum()), 
						each.getAuthorName(), each.getConferenceName() }));
			}	
		}
		
		// Print list of "no comments"
		//
		MessageAttribute[] attributes = envelope.getAttributes();
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    
		    if(each.getKind() == MessageManager.ATTR_NOCOMMENT)
		    {
				out.println(formatter.format("BasicMessagePrinter.nocomment", each.getNoCommentUsername()));		        
		    }
		}
	}
}
