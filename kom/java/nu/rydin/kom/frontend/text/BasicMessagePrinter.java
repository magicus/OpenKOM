/*
 * Created on Oct 16, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.MessageAttributes;
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
	    int width = context.getTerminalSettings().getWidth();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		Message message = envelope.getMessage();
		MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();
		MessageAttribute[] attributes = envelope.getAttributes();
		
		// Clear screen if requested by user
		//
		if(context.isFlagSet(0, UserFlags.CLEAR_SCREEN_BEFORE_MESSAGE))
			out.print(ANSISequences.CLEAR_DISPLAY);
		
		// Start printing header
		//
		dc.messageHeader();
		StringBuffer sb = new StringBuffer(200);
		sb.append(formatter.format("BasicMessagePrinter.textnumber"));

		// If we have a primary occurence, AND if we are in the conference of this 
		// occurence, then print the local messagenumber.
		//
		if((primaryOcc != null) && (primaryOcc.getConference() == context.getSession().getCurrentConferenceId()))
		{
		    sb.append(primaryOcc.getLocalnum());
		    sb.append(" ");
		}
		
		// Print global numbber
		//
		sb.append("(");
		sb.append(message.getId());
		sb.append(')');
		
		// Print thread id if requested
		//
		if(context.isFlagSet(0, UserFlags.DISPLAY_THREAD_ID))
		{
		    sb.append(" [");
		    sb.append(message.getThread());
		    sb.append(']');
		}
		
		// Print name of author
		//
		sb.append("; ");
		sb.append(context.formatObjectName(message.getAuthorName(), message.getAuthor()));
				
		// Print creation date
		//
		sb.append("; ");
		sb.append(context.smartFormatDate(message.getCreated()));
		PrintUtils.printIndented(out, sb.toString(), width, 0);
		
		// Print the original mail recipient if this is a mail.
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.MAIL_RECIPIENT && each.getUserId() != context.getLoggedInUserId())
		    {
		        PrintUtils.printIndented(out,
		                formatter.format("BasicMessagePrinter.original.mail.recipient", context.formatObjectName(each.getUsername(), each.getUserId())),
		                width, 0);		        
		    }
		}
		
		// Print reply info (if any)
		//
		Envelope.RelatedMessage replyTo = envelope.getReplyTo();
		if(replyTo != null)
		{
			// Text is a comment
		    //
			if(replyTo.isLocal())
			{
				// Simple case: Original text was in same conference
				//
			    PrintUtils.printIndented(out,
			        formatter.format("BasicMessagePrinter.reply.to.same.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), 
						context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) } ),
						width, 0);
			}
			else
			{
				// Complex case: Original text was in a different conference
				//
			    PrintUtils.printIndented(out, 
			        formatter.format("BasicMessagePrinter.reply.to.different.conference", 
			            new Object[] { new Long(replyTo.getOccurrence().getLocalnum()),
			            new Long(replyTo.getOccurrence().getGlobalId()),
			            context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
			            context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }),
			            width, 0); 
			                    
			    	
			}
		}
		else
		{
		    // Even though this text looks like it's not a comment, it might be
		    // a comment to a deleted text.
		    //
			for(int idx = 0; idx < attributes.length; ++idx)
			{
			    MessageAttribute each = attributes[idx];
			    if(each.getKind() == MessageAttributes.ORIGINAL_DELETED)
			    {
			        PrintUtils.printIndented(out, 
			                formatter.format("BasicMessagePrinter.reply.to.deleted.text", context.formatObjectName(each.getUsername(), each.getUserId())),
			                width, 0);		        
			    }
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
			
			// Make sure we only print occurences in conferences we have read-permission in!
			//
			if (context.getSession().hasPermissionInConference(occ.getConference(), ConferencePermissions.READ_PERMISSION))
			{
			    String conferenceName;
			    if (occ.getKind() == MessageManager.ACTION_CREATED && receivers[idx].getId() == context.getLoggedInUserId())
			    {
			        conferenceName = context.formatObjectName(receivers[idx]);
			    }
			    else
			    {
			        conferenceName = context.formatObjectName(receivers[idx].getName(), receivers[idx].getId()); 
			    }
				PrintUtils.printIndented(out, 
				        formatter.format("BasicMessagePrinter.receiver", conferenceName), 
				        width, 0);
				switch(occ.getKind())
				{
					case MessageManager.ACTION_COPIED:
						PrintUtils.printIndented(out, 
						        formatter.format("BasicMessagePrinter.copied", 
						                new Object[] { context.formatObjectName(occ.getUser()), 
						                context.smartFormatDate(occ.getTimestamp()) }),
						                width, space);
						break;
					case MessageManager.ACTION_MOVED:
						for(int attrIdx = attributes.length-1; 0 <= attrIdx; --attrIdx)
						{
							MessageAttribute each = attributes[attrIdx];
							if(each.getKind() == MessageAttributes.MOVEDFROM)
							{
								movedFrom = new String(each.getValue());
								break;
							}
						}
						PrintUtils.printIndented(out,
						        formatter.format("BasicMessagePrinter.moved.long", 
						                new Object[] { movedFrom, context.formatObjectName(occ.getUser()), context.smartFormatDate(occ.getTimestamp()) }),
						                width, space);
						break;					
				}   
			}
		} 
				
		// Print subject
		//
		String subjLine = formatter.format("BasicMessagePrinter.subject"); 
		out.print(subjLine);
		dc.messageSubject();
        int sl = subjLine.length();
        PrintUtils.printIndented(
                out,
                message.getSubject(),
                width - sl, 0, sl);		        
		// out.println(message.getSubject());
		dc.messageHeader();
		PrintUtils.printRepeated(out, '-', Math.min(width - 1, subjLine.length() + message.getSubject().length()));
		out.println();
		
		// Print body
		//
		dc.messageBody();
		WordWrapper ww = context.getWordWrapper(message.getBody());
		String line;
		while((line = ww.nextLine()) != null)
		{
		    dc.printWithAttributes(line);
		    out.println();
		}
		out.println();
		dc.messageFooter();
		
		// Print text footer if requested
		//
		if(context.isFlagSet(0, UserFlags.SHOW_TEXT_FOOTER))
		{
			// If we have a primary occurence, AND if we are in the conference of this 
			// occurence, then print the local messagenumber, otherwise print the global 
			// messagenumber. 
			if((primaryOcc != null) && (primaryOcc.getConference() == context.getSession().getCurrentConferenceId()))
			{
			    PrintUtils.printIndented(out, 
			            formatter.format("BasicMessagePrinter.local.footer", 
				        new Object[] { new Integer(primaryOcc.getLocalnum()), 
				        	context.formatObjectName(message.getAuthorName(), message.getAuthor()) }),
				        	width, 0);
			}
			else
			{
			    PrintUtils.printIndented(out,
			            formatter.format("BasicMessagePrinter.global.footer", 
				        new Object[] { new Long(message.getId()), 
				        	context.formatObjectName(message.getAuthorName(), message.getAuthor()) }),
				        	width, 0);
			}
		}
		
		// Print list of footnotes
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.FOOTNOTE)
		    {
		        dc.header();
		        String label = formatter.format("BasicMessagePrinter.footnote"); 
		        out.print(label);
		        dc.messageBody();
		        int ll = label.length();
		        PrintUtils.printIndented(
		                out,
		                each.getValue(),
		                width - ll, 0, ll);		        
		    }
		}

		
		// Print list of replies
		//
		Envelope.RelatedMessage[] replies = envelope.getReplies();
		top = replies.length;
		for(int idx = 0; idx < top; ++idx)
		{
			Envelope.RelatedMessage each = replies[idx];	
			MessageOccurrence occ = each.getOccurrence();
			
			// Reply in same conference? 
			//
			if(each.isLocal())
			{
			    PrintUtils.printIndented(out, 
			            formatter.format("BasicMessagePrinter.reply.same.conference",
			                    new Object[] { new Long(occ.getLocalnum()), each.getAuthorName() }),
			                    width, 0);
			}
			else
			{			        
			    PrintUtils.printIndented(out,
			            formatter.format("BasicMessagePrinter.reply.different.conference",
			                    new Object[] { new Long(occ.getLocalnum()), new Long(occ.getGlobalId()), 
			                    each.getAuthorName(), each.getConferenceName() }),
			                    width, 0);
			}	
		}
		
		// Print list of "no comments"
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.NOCOMMENT)
		    {
		        PrintUtils.printIndented(
		                out,
		                formatter.format("BasicMessagePrinter.nocomment", context.formatObjectName(each.getUsername(), each.getUserId())),
		                width, 0);		        
		    }
		}
	}
}
