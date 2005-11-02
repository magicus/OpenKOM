/*
 * Created on Sep 29, 2004
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
 * @author Henrik Schröder
 */
public class CompactMessagePrinter implements MessagePrinter
{
	public void printMessage(Context context, Envelope envelope)
		throws KOMException
	{
	    DisplayController dc = context.getDisplayController();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		Message message = envelope.getMessage();
		MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();
		MessageAttribute[] attributes = envelope.getAttributes();
		int width = context.getTerminalSettings().getWidth();
		
		// Clear screen if requested by user
		//
		if(context.isFlagSet(0, UserFlags.CLEAR_SCREEN_BEFORE_MESSAGE))
			out.print(ANSISequences.CLEAR_DISPLAY);

		// Start printing header
		//
		dc.messageHeader();
		out.print(formatter.format("CompactMessagePrinter.textnumber"));

		// If we have a primary occurence, AND if we are in the conference of this 
		// occurence, then print the local messagenumber, otherwise print the global 
		// messagenumber. 
		if((primaryOcc != null) && (primaryOcc.getConference() == context.getSession().getCurrentConferenceId()))
		{
			out.print(primaryOcc.getLocalnum());
		}
		else
		{
			out.print("(");
			out.print(message.getId());
			out.print(')');
		}
		
		// Print name of author
		//
		out.print("; ");
		out.print(context.formatObjectName(message.getAuthorName(), message.getAuthor()));
				
		// Print creation date
		//
		out.print("; ");
		out.println(context.smartFormatDate(message.getCreated()));
		
		// Print the original mail recipient if this is a mail.
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.MAIL_RECIPIENT && each.getUserId() != context.getLoggedInUserId())
		    {
		        out.println(formatter.format("CompactMessagePrinter.original.mail.recipient", context.formatObjectName(each.getUsername(), each.getUserId())));		        
		    }
		}
		
		// Print reply info (if any)
		//
		Envelope.RelatedMessage replyTo = envelope.getReplyTo();
		if(replyTo != null)
		{
			if(replyTo.isLocal())
			{
				// Simple case: Original text was in same conference
				//
				out.println(formatter.format("CompactMessagePrinter.reply.to.same.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), 
						context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) } ));		
			}
			else
			{
				// Complex case: Original text was in a different conference
				//
				out.println(formatter.format("CompactMessagePrinter.reply.to.different.conference", 
					new Object[] { new Long(replyTo.getOccurrence().getLocalnum()),
				        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
				        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }));
			}
		}
		else
		{
		    // Even though this text looks like it's not a comment, it might be
		    // a comment to a deleted text.
			for(int idx = 0; idx < attributes.length; ++idx)
			{
			    MessageAttribute each = attributes[idx];
			    if(each.getKind() == MessageAttributes.ORIGINAL_DELETED)
			    {
			        out.println(formatter.format("BasicMessagePrinter.reply.to.deleted.text", context.formatObjectName(each.getUsername(), each.getUserId())));		        
			    }
			}
		}
		
	    // Print original occurence and any moves. This should typically 
	    // result in only one line, because it is very uncommon to move a copy
	    // and if that is done, it will be pretty hard to interpret the header anyway. :-)
		//
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
				switch(occ.getKind())
				{
					case MessageManager.ACTION_CREATED:
					    out.println(formatter.format("CompactMessagePrinter.original", 
						        context.formatObjectName(receivers[idx].getName(), receivers[idx].getId())));
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
						out.println(formatter.format("CompactMessagePrinter.moved", 
							new Object[] { context.formatObjectName(receivers[idx].getName(), receivers[idx].getId()), 
						        movedFrom, 
						        context.formatObjectName(occ.getUser()) }));
						break;					
				}
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
				out.println(formatter.format("CompactMessagePrinter.local.footer", 
				        new Object[] { new Integer(primaryOcc.getLocalnum()), 
				        	context.formatObjectName(message.getAuthorName(), message.getAuthor()) }));
			}
			else
			{
				out.println(formatter.format("CompactMessagePrinter.global.footer", 
				        new Object[] { new Long(message.getId()), 
				        	context.formatObjectName(message.getAuthorName(), message.getAuthor()) }));
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

		// Print copies
		//
		top = receivers.length;
		for(int idx = 0; idx < top; ++idx)
		{
		    MessageOccurrence occ = occs[idx];
			// Make sure we only print occurences in conferences we have read-permission in!
			//
			if (context.getSession().hasPermissionInConference(occ.getConference(), ConferencePermissions.READ_PERMISSION))
			{
				switch(occ.getKind())
				{
					case MessageManager.ACTION_COPIED:
						out.println(formatter.format("CompactMessagePrinter.copied", 
							new Object[] { context.formatObjectName(receivers[idx].getName(), receivers[idx].getId()), 
						        context.formatObjectName(occ.getUser()) }));
						break;					
				}
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
				out.println(formatter.format("CompactMessagePrinter.reply.same.conference",
					new Object[] { new Long(occ.getLocalnum()), each.getAuthorName() }));
			}
			else
			{
				out.println(formatter.format("CompactMessagePrinter.reply.different.conference",
					new Object[] { new Long(occ.getLocalnum()), each.getConferenceName(), 
						each.getAuthorName() }));
			}
		}

		// Print list of "no comments"
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.NOCOMMENT)
		    {
				out.println(formatter.format("BasicMessagePrinter.nocomment", context.formatObjectName(each.getUsername(), each.getUserId())));
		    }
		}
	}
}
