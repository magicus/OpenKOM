/*
 * Created on Jul 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * Status codes returned when verifying chat recipients.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChatRecipientStatus
{
    /**
     * Recipient is a user that would receive the message ok.
     */
    public static final int OK_USER 			= 0;
    
    /**
     * Recipient is a conference that would receive the message ok.
     */
    public static final int OK_CONFERENCE 		= 1;
    
    /** 
     * Recipient does not exist
     */
    public static final int NONEXISTENT 		= 2;
    
    /**
     * Recipient is not logged in
     */
    public static final int NOT_LOGGED_IN 		= 3;
    
    /**
     * Recipient is logged in, but refuses messages
     */
    public static final int REFUSES_MESSAGES 	= 4;
}