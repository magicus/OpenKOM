/*
 * Created on Nov 25, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferencePermissions
{
public static final int READ_PERMISSION 	= 0x0001;
public static final int WRITE_PERMISSION 	= 0x0002;
public static final int DELETE_PERMISSION 	= 0x0004;
public static final int REPLY_PERMISSION	= 0x0008;
public static final int ADMIN_PERMISSION	= 0x0010;
	
	public static final int ALL_PERMISSIONS 	= 0x7fff; 
	public static final int NORMAL_PERMISSIONS= READ_PERMISSION | WRITE_PERMISSION | REPLY_PERMISSION;
}
