/*
 * Created on Oct 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

public class FilterFlags
{
    /**
     * Ignore all messages posted by a user
     */
    public static final long MESSAGES			= 0x0000000000000001;
    
    /**
     * Ignore all chat messages from a user
     */
    public static final long CHAT				= 0x0000000000000002;
    
    /**
     * Ignore all broadcast messages from a user
     */
    public static final long BROADCASTS			= 0x0000000000000004;
    
    /**
     * Ignore all replies to messages written by a user, 
     * regardless of the author.
     */
    public static final long REPLIES			= 0x0000000000000008;
    
    /**
     * Ignore all threads started by a user. (Not yet supported)
     */
    public static final long THREADS			= 0x0000000000000010;
    
    /**
     * Ignore all mails sent by a user
     */
    public static final long MAILS				= 0x0000000000000020;
    
    /**
     * Ignore everything
     */
    public static final long ALL				= 
        MESSAGES | CHAT | BROADCASTS | REPLIES | THREADS | MAILS;
}
