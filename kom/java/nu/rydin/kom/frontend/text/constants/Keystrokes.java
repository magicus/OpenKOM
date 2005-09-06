/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.constants;

/**
 * @author Pontus Rydin
 */
public class Keystrokes
{
    // Keystroke codes below 16 are reserved
    // Core keystrokes
    //
    public static final int TOKEN_UP				= 16;
	public static final int TOKEN_DOWN				= 17;
	public static final int TOKEN_LEFT				= 18;
	public static final int TOKEN_RIGHT				= 19;
	public static final int TOKEN_CLEAR_LINE		= 20;
	public static final int TOKEN_DELETE_WORD		= 21;
	public static final int TOKEN_BOL				= 22;
	public static final int TOKEN_EOL				= 23;
	public static final int TOKEN_BS				= 24;
	public static final int TOKEN_CR				= 25;
	public static final int TOKEN_PREV				= 26;
	public static final int TOKEN_NEXT				= 27;
	public static final int TOKEN_ABORT				= 28;
	public static final int TOKEN_DONE				= 29;
	public static final int TOKEN_REFRESH			= 30;
	public static final int TOKEN_DELETE_LINE		= 31;
	public static final int TOKEN_SKIP				= 200;
	public static final int TOKEN_MOFIDIER_BREAK	= 0x8000000;
	
	// Message editor keystrokes
	//
	public static final int TOKEN_COMMAND			= 1001;
	public static final int TOKEN_QUOTE				= 1002;
}
