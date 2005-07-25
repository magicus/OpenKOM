/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * Constants for user permissions.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserPermissions
{
	/**
	 * Permission to create new conferences
	 */
	public static final long CREATE_CONFERENCE 		= 0x0001L;
	
	/**
	 * Permission to create, delete and change users 
	 */
	public static final long USER_ADMIN				= 0x0002L;
	
	/**
	 * Permission to delete and modify all conferences
	 */
	public static final long CONFERENCE_ADMIN		= 0x0004L;
	
	/**
	 * Normal user, i.e. not a twit
	 */
	public static final long NOT_TWIT				= 0x0008L;
	
	/**
	 * Disregard conference permissions, i.e. enter any conference
	 */
	public static final long DISREGARD_CONF_PERM	= 0x0010L;
	
	/**
	 * Permission to delete any message
	 */
	public static final long DELETE_ANY_MESSAGE		= 0x0020L;
		
	/**
	 * Permission to change the name of any object
	 */
	public static final long CHANGE_ANY_NAME		= 0x0040L;
		
	/**
	 * Permission to log in
	 */
	public static final long LOGIN					= 0x0080L;
	
	/**
	 * Permission to run admin commands
	 */
	public static final long ADMIN					= 0x0100L;
	
	/**
	 * Permission to send chat messages
	 */
	public static final long CHAT					= 0x0200L;
	
	/**
	 * Permission to send broadcast messages
	 */
	public static final long BROADCAST				= 0x0400L;
	
	/**
	 * Permission to post messages
	 */
	public static final long POST					= 0x0800L;
	
	/**
	 * Permission to copy messages
	 */
	public static final long COPY					= 0x1000L;
	
	/**
	 * Permission to move message
	 */
	public static final long MOVE					= 0x2000L;	
	
	/**
	 * Permission to change suffix	
	 */
	public static final long CHANGE_SUFFIX			= 0x4000L;
		
	/**
	 * All permissions
	 */
	public static final long EVERYTHING				= 0xffffffffffffffffL;
	
	/**
	 * Normal permissions
	 */
	public static final long NORMAL					= LOGIN | CREATE_CONFERENCE | NOT_TWIT 
														| CHAT | BROADCAST | POST | COPY | MOVE
														| CHANGE_SUFFIX;
	/**
	 * Self registered user permissions permissions
	 */
	public static final long SELF_REGISTERED_USER	= LOGIN | CHAT | BROADCAST | NOT_TWIT;

}
