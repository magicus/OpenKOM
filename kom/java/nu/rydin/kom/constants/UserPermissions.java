/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL licens.
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
	 * Permission to create new users
	 */
	public static final long CREATE_USER			= 0x0002L;
	
	/**
	 * Permission to delete conferences
	 */
	public static final long DELETE_CONFERENCE		= 0x0004L;
	
	/**
	 * Permission to delete users
	 */
	public static final long DELETE_USER			= 0x0008L;
	
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
	 * All permissions
	 */
	public static final long EVERYTHING				= 0xffffffffffffffffL;
	
	/**
	 * Normal permissions
	 */
	public static final long NORMAL					= LOGIN | CREATE_CONFERENCE;
}
