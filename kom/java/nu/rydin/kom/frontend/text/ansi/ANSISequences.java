/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.ansi;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ANSISequences
{
	public static final String CLEAR_DISPLAY 			= "\u001b[2J";
	public static final String SET_GRAPHIC_RENDITION 	= "\u001b[#";

	public static final String SET_CURSOR_POSITION		= "\u001b[{0};{1}H";
	public static final String MOVE_CURSOR_UP			= "\u001b[{0}A";
	public static final String MOVE_CURSOR_DOWN			= "\u001b[{0}B";
	public static final String MOVE_CURSOR_RIGHT		= "\u001b[{0}C";
	public static final String MOVE_CURSOR_LEFT			= "\u001b[{0}D";
	public static final String SAVE_CURSOR_POSITION		= "\u001b[s";
	public static final String RECALL_CURSOR_POSITION	= "\u001b[u";
	public static final String ERASE_CURRENT_LINE		= "\u001b[K";
}
