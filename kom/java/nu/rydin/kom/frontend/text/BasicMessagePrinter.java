/*
 * Created on Oct 16, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.frontend.text.ansi.ANSISequences;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageOccurrence;
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
		out.print(formatter.format("BasicMessagePrinter.textnumber"));
		if(primaryOcc != null)
			out.print(primaryOcc.getLocalnum());
		else
		{
			// Couldn't find a local number. Use global!
			//
			out.print('(');
			out.print(message.getId());
			out.print(')');
		}
		
		// Print name of author
		//
		out.print("; ");
		out.print(message.getAuthorName());
				
		// Print creation date
		//
		out.print("; ");
		out.println(message.getCreated());
		
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
						replyTo.getAuthorName() } ));		
			}
			else
			{
				// Complex case: Original text was in a different conference
				//
				out.println(formatter.format("BasicMessagePrinter.reply.to.different.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), replyTo.getConferenceName(), 
						 replyTo.getAuthorName()}));
			}
		}		
		
		// Print receiver list
		//
		int space = formatter.format("BasicMessagePrinter.receiver", "").length();
		MessageOccurrence[] occs = envelope.getOccurrences();
		String[] receivers = envelope.getReceivers();
		String movedFrom = null;
		int top = receivers.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MessageOccurrence occ = occs[idx];
			out.println(formatter.format("BasicMessagePrinter.receiver", receivers[idx]));
			switch(occ.getKind())
			{
				case MessageManager.ACTION_COPIED:
					PrintUtils.printRepeated(out, ' ', space);
					out.println(formatter.format("BasicMessagePrinter.copied", 
						new Object[] { occ.getUserName(), occ.getTimestamp().toString() }));
					break;
				case MessageManager.ACTION_MOVED:
					MessageAttribute[] attributes = envelope.getAttributes();
					for(int attrIdx = 0; attrIdx < attributes.length; ++attrIdx)
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
						new Object[] { movedFrom, occ.getUserName(), occ.getTimestamp().toString() }));
					break;					
			}
		} 
				
		// Print subject
		//
		String subjLine = formatter.format("BasicMessagePrinter.subject", message.getSubject()); 
		out.println(subjLine);
		PrintUtils.printRepeated(out, '-', subjLine.length());
		out.println();
		
		// Print body
		//
		out.println(message.getBody());
		
		// Print text footer if requested
		//
		if(context.isFlagSet(0, UserFlags.SHOW_TEXT_FOOTER))
			out.println(formatter.format("BasicMessagePrinter.footer", 
				new Object[] { new Long(primaryOcc.getLocalnum()), message.getAuthorName() }));
		
		// Print list of replies
		//
		Envelope.RelatedMessage[] replies = envelope.getReplies();
		top = replies.length;
		if(!context.isFlagSet(0, UserFlags.SHOW_TEXT_FOOTER) && top > 0)
			out.println();
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
