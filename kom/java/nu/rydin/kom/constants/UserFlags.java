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
	public static long KEEP_COPIES_OF_MAIL		 			= 0x00000008;
	public static long SHOW_TEXT_FOOTER						= 0x00000010;
	public static long CLEAR_SCREEN_BEFORE_MESSAGE			= 0x00000020;
	public static long ANSI_ATTRIBUTES						= 0x00000040;
	public static long SHOW_OBJECT_IDS						= 0x00000080;
	public static long EMPTY_LINE_FINISHES_CHAT				= 0x00000100;
	public static long ALWAYS_PRINT_FULL_DATE				= 0x00000200;
	public static long BEEP_ON_CHAT							= 0x00000400;
	public static long BEEP_ON_BROADCAST					= 0x00000800;
	public static long BEEP_ON_ATTENDANCE					= 0x00001000;
	public static long NARCISSIST							= 0x00002000;
	public static long PRIORITIZE_MAIL						= 0x00004000;
	public static long BEEP_ON_NEW_MESSAGES					= 0x00008000;
	public static long SHOW_NUM_UNREAD						= 0x00010000;
	public static long USE_COMPACT_MESSAGEPRINTER			= 0x00020000;
	public static long DISPLAY_THREAD_ID					= 0x00040000;
	public static long USE_FULL_SCREEN_EDITOR				= 0x00080000;
	public static long READ_CROSS_CONF_REPLIES				= 0x00100000;
	public static long READ_REPLY_TREE						= 0x00200000;
	public static long REPLY_IN_CURRENT_CONF				= 0x00400000;
	public static long SHOW_SUFFIX							= 0x00800000;
    public static long SHOW_END_OF_IDLE_MESSAGE             = 0x01000000;
    public static long COMPACT_WHO			             	= 0x02000000;
    public static long MORE_PROMPT_IN_BROADCAST             = 0x04000000;
    public static long SELECT_YOUNGEST_FIRST                = 0x08000000;
    public static long CONFIRM_BROADCAST_MESSAGES           = 0x10000000;

	/**
	 * Default value for flagword 1
	 */
	public static long DEFAULT_FLAGS1 = SHOW_ATTENDANCE_MESSAGES | ALLOW_BROADCAST_MESSAGES |
		ALLOW_CHAT_MESSAGES | KEEP_COPIES_OF_MAIL | EMPTY_LINE_FINISHES_CHAT | BEEP_ON_CHAT
		| PRIORITIZE_MAIL | BEEP_ON_NEW_MESSAGES | SHOW_NUM_UNREAD | READ_CROSS_CONF_REPLIES 
		| READ_REPLY_TREE | SHOW_SUFFIX | COMPACT_WHO | MORE_PROMPT_IN_BROADCAST;
	
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
