/*
 * Created on Oct 16, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;
import java.sql.Timestamp;

import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
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
public class BasicMessagePrinter extends AbstractMessagePrinter 
{
    protected void printHeaderReceivers(Context context, Envelope envelope)
    throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        MessageAttribute[] attributes = envelope.getAttributes();
        
		int space = formatter.format(getResourceKey("receiver"), "").length();
		MessageOccurrence[] occs = envelope.getOccurrences();
		NameAssociation[] receivers = envelope.getReceivers();
		for(int idx = 0; idx < receivers.length; ++idx)
		{
			MessageOccurrence occ = occs[idx];
			
			// Make sure we only print occurrences in conferences we have read-permission in!
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
				        formatter.format(getResourceKey("receiver"), conferenceName), 
				        width, 0);
				switch(occ.getKind())
				{
					case MessageManager.ACTION_COPIED:
						PrintUtils.printIndented(out, 
						        formatter.format(getResourceKey("copied"), 
						                new Object[] { context.formatObjectName(occ.getUser()), 
						                context.smartFormatDate(occ.getTimestamp()) }),
						                width, space);
						break;
					case MessageManager.ACTION_MOVED:
						PrintUtils.printIndented(out,
						        formatter.format(getResourceKey("moved.long"), 
						                new Object[] { getMovedFrom(attributes), context.formatObjectName(occ.getUser()), context.smartFormatDate(occ.getTimestamp()) }),
						                width, space);
						break;					
				}   
			}
		}
    }

    
    protected String getFormattedOriginInfo(Context context, Envelope.RelatedMessage replyTo, MessageAttribute[] attrs)
    {
        MessageFormatter formatter = context.getMessageFormatter();

        if (replyTo.isLocal())
        {
            // Simple case: Original text was in same conference
            //
            return attrs == null ? 
                formatter.format(getResourceKey("reply.to.same.conference"), 
                    new Object[] { new Long(replyTo.getOccurrence().getLocalnum()), 
                        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }) :
                formatter.format(getResourceKey("reply.to.same.conference.type"), 
                        new Object[] { new String(attrs[0].getValue()), new Long(replyTo.getOccurrence().getLocalnum()), 
                            context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) });
        }
        else
        {
            // Complex case: Original text was in a different conference
            //
            return attrs == null ? 
                formatter.format(getResourceKey("reply.to.different.conference"), 
                    new Object[] { new Long(replyTo.getOccurrence().getLocalnum()),
                        new Long(replyTo.getOccurrence().getGlobalId()),
                        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
                        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }) :
                formatter.format(getResourceKey("reply.to.different.conference.type"), 
                    new Object[] { new String (attrs[0].getValue()), 
                        new Long(replyTo.getOccurrence().getLocalnum()),
                        new Long(replyTo.getOccurrence().getGlobalId()),
                        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
                        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) });
        }
    }

    
    protected String getFormattedOriginDeletedInfo(Context context, MessageAttribute originalDeletedAttribute)
    {
        return context.getMessageFormatter().format(getResourceKey("reply.to.deleted.text"), 
                context.formatObjectName(originalDeletedAttribute.getUsername(), 
                        originalDeletedAttribute.getUserId()));
    }


    protected void printMailInformation(Context context, Envelope envelope) 
    throws UnexpectedException, NumberFormatException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        MessageAttribute[] attributes = envelope.getAttributes();

        // Print the original mail recipient if this is a personal message.
		// Also, print email sender and timestamps, if any.
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    switch(each.getKind())
		    {
		    case MessageAttributes.MAIL_RECIPIENT:
			    if(each.getUserId() != context.getLoggedInUserId())
			        PrintUtils.printIndented(out,
			                formatter.format(getResourceKey("original.mail.recipient"), context.formatObjectName(each.getUsername(), each.getUserId())),
			                width, 0);		        
			    break;
		    case MessageAttributes.EMAIL_SENDER:
		    	PrintUtils.printIndented(out, formatter.format(getResourceKey("email.sender"), each.getValue()), width, 0);
		    	break;
		    case MessageAttributes.EMAIL_SENT:
		    	PrintUtils.printIndented(out, formatter.format(getResourceKey("email.sent"), context.smartFormatDate(new Timestamp(Long.valueOf(each.getValue())))), width, 0);
		    	break;
		    case MessageAttributes.EMAIL_RECEIVED:
		    	PrintUtils.printIndented(out, formatter.format(getResourceKey("email.received"), context.smartFormatDate(new Timestamp(Long.valueOf(each.getValue())))), width, 0);
		    	break;		    	
		    }
		}
    }


    protected StringBuffer getFormattedMessageId(Context context, Envelope envelope) throws ObjectNotFoundException, UnexpectedException 
    {

        MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();
        Message message = envelope.getMessage();
        
        StringBuffer sb = new StringBuffer();
        
        // If we have a primary occurrence, AND if we are in the conference of this 
        // Occurrence, then print the local messagenumber.
        //
        if((primaryOcc != null) && (primaryOcc.getConference() == context.getSession().getCurrentConferenceId()))
        {
            sb.append(primaryOcc.getLocalnum());
            sb.append(" ");
        }
        
        // Print global number
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
        
        return sb;
    }
        
    protected String getFormattedReply(Context context, Envelope.RelatedMessage reply, MessageOccurrence occ, MessageAttribute[] attrs)
    {
        MessageFormatter formatter = context.getMessageFormatter();

        // Reply in same conference? 
        //
        if(reply.isLocal())
        {
            return null == attrs ?
                formatter.format(getResourceKey("reply.same.conference"),
                    new Object[] { new Long(occ.getLocalnum()), reply.getAuthorName() }) :
                formatter.format(getResourceKey("reply.same.conference.type"),
                        new Object[] { new String(attrs[0].getValue()), new Long(occ.getLocalnum()), reply.getAuthorName() });
        }
        else
        {                   
            return null == attrs ? 
                formatter.format(getResourceKey("reply.different.conference"),
                    new Object[] { new Long(occ.getLocalnum()), new Long(occ.getGlobalId()), 
                        reply.getAuthorName(), reply.getConferenceName() }) :
                formatter.format(getResourceKey("reply.different.conference.type"),
                    new Object[] { new String(attrs[0].getValue()), new Long(occ.getLocalnum()), new Long(occ.getGlobalId()), 
                        reply.getAuthorName(), reply.getConferenceName() });                            
        }
    }

    protected void printFooterReceivers(Context context, Envelope envelope)
            throws AuthorizationException, ObjectNotFoundException,
            UnexpectedException
    {
        // Do nothing
    }

}
