/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageLogKinds 
{
    /**
     * Chat message sent to one or many specific recipients.
     */
    public static short CHAT 				= 0;
    
    /**
     * Broadcast message sent to all users currently logged in.
     */
    public static short BROADCAST 			= 1;
    
    /**
     * Condensed broad cast on the form User Message, e.g. 
     * "Pontus Rydin goes home". 
     */
    public static short CONDENSED_BROADCAST	= 2;
}
