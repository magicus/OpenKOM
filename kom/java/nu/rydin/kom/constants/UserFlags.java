/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserFlags
{
	/**
	 * Maximum number of flags: 256 (four 64-bit flag words). Should be enough
	 */
	public static int NUM_FLAGS								= 256;
	
	/**
	 * Number of flag words
	 */
	public static int NUM_FLAG_WORD							= NUM_FLAGS / 64;
	
	// Flagword 1:
	//
	public static long SHOW_ATTENDANCE_MESSAGES 			= 0x00000001; // TODO: Not supported
	public static long ALLOW_BROADCAST_MESSAGES				= 0x00000002; // TODO: Not supported
	public static long ALLOW_CHAT_MESSAGES		 			= 0x00000004; // TODO: Not supported
	public static long KEEP_COPIES_OF_MAIL		 			= 0x00000008; // TODO: Not supported
	public static long SHOW_TEXT_FOOTER						= 0x00000010;
	public static long CLEAR_SCREEN_BEFORE_MESSAGE			= 0x00000020;
	public static long ANSI_ATTRIBUTES						= 0x00000040; // TODO: Not supported

	/**
	 * Default value for flagword 1
	 */
	public static long DEFAULT_FLAGS1 = SHOW_ATTENDANCE_MESSAGES | ALLOW_BROADCAST_MESSAGES |
		ALLOW_CHAT_MESSAGES | KEEP_COPIES_OF_MAIL;
	
	/**
	 * Default value for flagword 2
	 */
	public static long DEFAULT_FLAGS2 = 0;
		
	/**
	 * Default value for flagword 3
	 */
	public static long DEFAULT_FLAGS3 = 0;
	
	/**
	 * Default value for flagword 3
	 */
	public static long DEFAULT_FLAGS4 = 0;
}
