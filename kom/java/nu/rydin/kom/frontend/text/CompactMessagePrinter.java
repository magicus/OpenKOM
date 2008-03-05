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
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class CompactMessagePrinter extends AbstractMessagePrinter
{

    protected void printHeaderReceivers(Context context, Envelope envelope) throws AuthorizationException, ObjectNotFoundException, UnexpectedException 
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        // Print original occurrence and any moves. This should typically 
	    // result in only one line, because it is very uncommon to move a copy
	    // and if that is done, it will be pretty hard to interpret the header anyway. :-)
		//
        MessageAttribute[] attributes = envelope.getAttributes();

		MessageOccurrence[] occs = envelope.getOccurrences();
		NameAssociation[] receivers = envelope.getReceivers();
		for(int idx = 0; idx < receivers.length; ++idx)
		{
		    MessageOccurrence occ = occs[idx];
			// Make sure we only print occurrences in conferences we have read-permission in!
			//
			if (context.getSession().hasPermissionInConference(occ.getConference(), ConferencePermissions.READ_PERMISSION))
			{
				switch(occ.getKind())
				{
					case MessageManager.ACTION_CREATED:
					    PrintUtils.printIndented(out, formatter.format(getResourceKey("original"), 
					            context.formatObjectName(receivers[idx].getName(), receivers[idx].getId())),
						        width, 0);
					    break;
					case MessageManager.ACTION_MOVED:
					    PrintUtils.printIndented(out, formatter.format(getResourceKey("moved"), 
					            new Object[] { context.formatObjectName(receivers[idx].getName(), receivers[idx].getId()), 
						            getMovedFrom(attributes), 
						            context.formatObjectName(occ.getUser()) }),
						        width, 0);
						break;					
				}
			}
		}
    }


    protected String getFormattedOriginInfo(Context context, Envelope.RelatedMessage replyTo, MessageAttribute[] attrs)
    {
        MessageFormatter formatter = context.getMessageFormatter();

        if(replyTo.isLocal())
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
                        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
                        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) }) :
                formatter.format(getResourceKey("reply.to.different.conference.type"), 
                    new Object[] { new String(attrs[0].getValue()), new Long(replyTo.getOccurrence().getLocalnum()),
                        context.formatObjectName(replyTo.getConferenceName(), replyTo.getConference()),
                        context.formatObjectName(replyTo.getAuthorName(), replyTo.getAuthor()) });
        }
    }

    protected String getFormattedOriginDeletedInfo(Context context, MessageAttribute originalDeletedAttribute)
    {
        MessageFormatter formatter = context.getMessageFormatter();

        return formatter.format(getResourceKey("reply.to.deleted.text"), 
                context.formatObjectName(originalDeletedAttribute.getUsername(), originalDeletedAttribute.getUserId()));
    }
    
    
    protected void printMailInformation(Context context, Envelope envelope)
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        MessageAttribute[] attributes = envelope.getAttributes();

        // Print the original mail recipient if this is a mail.
		//
		for(int idx = 0; idx < attributes.length; ++idx)
		{
		    MessageAttribute each = attributes[idx];
		    if(each.getKind() == MessageAttributes.MAIL_RECIPIENT && each.getUserId() != context.getLoggedInUserId())
		    {
		        PrintUtils.printIndented(out, formatter.format(getResourceKey("original.mail.recipient"), 
		                context.formatObjectName(each.getUsername(), each.getUserId())), width, 0);		        
		    }
		}
    }


    protected void printFooterReceivers(Context context, Envelope envelope) throws AuthorizationException,
            ObjectNotFoundException, UnexpectedException
    {
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int width = context.getTerminalSettings().getWidth();

        dc.messageFooter();

        MessageOccurrence[] occs = envelope.getOccurrences();
        NameAssociation[] receivers = envelope.getReceivers();
        
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
					case MessageManager.ACTION_COPIED:
					    PrintUtils.printIndented(out, formatter.format(getResourceKey("copied"), 
							new Object[] { context.formatObjectName(receivers[idx].getName(), receivers[idx].getId()), 
						        context.formatObjectName(occ.getUser()) }),
						    width, 0);
						break;					
				}
			}
		}
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
    

    protected StringBuffer getFormattedMessageId(Context context, Envelope envelope)
    {
        MessageOccurrence primaryOcc = envelope.getPrimaryOccurrence();
        
        StringBuffer sb = new StringBuffer();
        
        // If we have a primary occurence, AND if we are in the conference of this 
        // occurence, then print the local messagenumber, otherwise print the global 
        // messagenumber. 
        if((primaryOcc != null) && 
                (primaryOcc.getConference() == context.getSession().getCurrentConferenceId()))
        {
            sb.append(String.valueOf(primaryOcc.getLocalnum()));
        }
        else
        {
            sb.append('(');
            sb.append(envelope.getMessage().getId());
            sb.append(')');
        }
        return sb;
    }

}
