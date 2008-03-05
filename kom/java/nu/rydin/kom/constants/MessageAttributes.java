/*
 * Created on Jan 09, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.constants;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class MessageAttributes
{
    /**
     * "No comment" remark. Payload is user
     */
    public static final short NOCOMMENT 		= 0;
    
    /**
     * Moved from conference. Payload is id of original conference
     */
	public static final short MOVEDFROM 		= 1;
	
	/**
	 * Conference rule posting. Payload is object id of conference
	 */
	public static final short RULEPOST 			= 2;
	
	/**
	 * Presentation of object. Payload is object id.
	 */
	public static final short PRESENTATION 		= 3;
	
	/**
	 * User note. Payload is user id.
	 */
	public static final short NOTE 				= 4;
	
	/**
	 * Reply to deleted message. Payload is name of user
	 */
	public static final short ORIGINAL_DELETED 	= 5;
	
	/**
	 * TODO: What is this?
	 */
	public static final short MAIL_RECIPIENT 	= 6;
	
	/**
	 * Footnote. Payload is footnote text.
	 */
	public static final short FOOTNOTE			= 7;
	
	/**
	 * Email originating address
	 */
	public static final short EMAIL_SENDER		= 8;
	
	/**
	 * Email received timestamp
	 */
	public static final short EMAIL_RECEIVED	= 9;
	
	/**
	 * Email sent timestamp
	 */
	public static final short EMAIL_SENT		= 10;
    
    /**
     * Comment type, displayed in message header and comment lists.
     */
	public static final short COMMENT_TYPE      = 11;
	/**
	 * Flag-array determining whether a user can perform a command
	 * only on messages that she/he has created.
	 */
	public static final boolean[] onlyOwner = new boolean[] 
	    { false, false, false, false, false, true, true, true, true, true, false, false };
}
