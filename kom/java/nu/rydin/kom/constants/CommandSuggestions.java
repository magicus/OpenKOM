/*
 * Created on Oct 14, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * @author Pontus Rydin
 */
public class CommandSuggestions
{
	/**
	 * No suggestion
	 */
	public static final short NO_ACTION 		= 0;

	/**
	 * Read next reply
	 */
	public static final short NEXT_REPLY 		= 1;

	/**
	 * Read next message
	 */
	public static final short NEXT_MESSAGE 		= 3;

	/**
	 * Go to next conference
	 */
	public static final short NEXT_CONFERENCE 	= 4;
	
	/**
	 * Read next mail
	 */
	public static final short NEXT_MAIL			= 5;
	
	/**
	 * Error determining next command
	 */
	public static final short ERROR				= -1;
}
