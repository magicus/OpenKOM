/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
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
	public static long SHOW_ATTENDANCE_MESSAGES 			= 0x00000001; 
	public static long ALLOW_BROADCAST_MESSAGES				= 0x00000002; 
	public static long ALLOW_CHAT_MESSAGES		 			= 0x00000004; 
	public static long KEEP_COPIES_OF_MAIL		 			= 0x00000008; // TODO: Not supported
	public static long SHOW_TEXT_FOOTER						= 0x00000010;
	public static long CLEAR_SCREEN_BEFORE_MESSAGE			= 0x00000020;
	public static long ANSI_ATTRIBUTES						= 0x00000040;
	public static long SHOW_OBJECT_IDS						= 0x00000080;
	public static long EMPTY_LINE_FINISHES_CHAT				= 0x00000100;
	public static long ALWAYS_PRINT_FULL_DATE				= 0x00000200;
	public static long BEEP_ON_CHAT							= 0x00000400;
	public static long BEEP_ON_BROADCAST					= 0x00000800;
	public static long BEEP_ON_ATTENDANCE					= 0x00001000;

	/**
	 * Default value for flagword 1
	 */
	public static long DEFAULT_FLAGS1 = SHOW_ATTENDANCE_MESSAGES | ALLOW_BROADCAST_MESSAGES |
		ALLOW_CHAT_MESSAGES | KEEP_COPIES_OF_MAIL | EMPTY_LINE_FINISHES_CHAT | BEEP_ON_CHAT;
	
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
