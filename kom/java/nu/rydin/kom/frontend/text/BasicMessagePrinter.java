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
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
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
		int top = receivers.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MessageOccurrence occ = occs[idx];
		    if (occ.getKind() == MessageManager.ACTION_NOCOMMENT)
		    {
		        //We probably shouldn't print the "no comments" in the header.
		    }
		    else
		    {
				out.println(formatter.format("BasicMessagePrinter.receiver", receivers[idx]));
				switch(occ.getKind())
				{
					case MessageManager.ACTION_COPIED:
						PrintUtils.printRepeated(out, ' ', space);
						out.println(formatter.format("BasicMessagePrinter.copied", 
							new Object[] { occ.getUserName(), occ.getTimestamp().toString() }));
						break;
					case MessageManager.ACTION_MOVED:
						PrintUtils.printRepeated(out, ' ', space);
						out.println(formatter.format("BasicMessagePrinter.moved", 
							new Object[] { occ.getUserName(), occ.getTimestamp().toString() }));
						break;					
				}
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
		
		// Print list of replies
		//
		Envelope.RelatedMessage[] replies = envelope.getReplies();
		top = replies.length;
		if(top > 0)
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
		for(int idx = 0; idx < occs.length; ++idx)
		{
			MessageOccurrence occ = occs[idx];
		    if (occ.getKind() == MessageManager.ACTION_NOCOMMENT)
		    {
		        out.println(formatter.format("BasicMessagePrinter.nocomment", occ.getUserName()));
		    }
		}
	}
}
