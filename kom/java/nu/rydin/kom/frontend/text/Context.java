/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.util.Date;

import nu.rydin.kom.AuthenticationException;
import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.MessageNotFoundException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface Context extends TerminalSettingsProvider
{
	/**
	 * Returns the <tt>Reader</tt> handling user input
	 */
	public LineEditor getIn();
	
	/**
	 * Returns the <tt>PrintWriter</tt> used to write messages
	 * to the user.
	 */
	public KOMWriter getOut();	
		
	/**
	 * Returns the defaul message formatter
	 */
	public MessageFormatter getMessageFormatter();
	
	/**
	 * Returns the <tt>MessagePrinter</tt> associated with this session
	 */
	public MessagePrinter getMessagePrinter();
	
	/** 
	 * Returns the <tt>MessageEditor</tt> configured for this session 
	 */
	public MessageEditor getMessageEditor()
	throws UnexpectedException;

	/**
	 * Prints debug info
	 */
	public void printDebugInfo();
	
	/**
	 * Returns the backend session
	 * @return
	 */
	public ServerSession getSession();
	
	/**
	 * Prints information about the current conference
	 *
	 */
	public void printCurrentConference()
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns the id of the logged in user
	 */
	public long getLoggedInUserId();

	/**
	 * Return cached user information. NOTE: Although an effort is made to
	 * keep this info up to date, it is not 100% reliable. Don't use for 
	 * operations where slightly stale data could cause problems.
	 * @throws UnexpectedException
	 * @throws ObjectNotFoundException
	 */	
	public UserInfo getCachedUserInfo()
	throws UnexpectedException, ObjectNotFoundException;
	
	/**
	 * Invalidates user info cache.
	 */
	public void clearUserInfoCache();
	
	/**
	 * Returns true if the specified flag in the specified flag word i set
	 * @param flagWord The flag word index
	 * @param mask The mask to check
	 * @return
	 */
	boolean isFlagSet(int flagWord, long mask)
	throws ObjectNotFoundException, UnexpectedException;
	
	/**
	 * Returns localized names of user flags.
	 */
	public String[] getFlagLabels();

	/**
	 * Returns the WordWrapper to use when formatting messages
	 * @param content The text to wrap
	 */
	public WordWrapper getWordWrapper(String content);
	
	/**
	 * Returns the WordWrapper to use when formatting messages
	 * @param content The text to wrap
	 * @param length Maximum line length
	 */
	public WordWrapper getWordWrapper(String content, int length);
	
	/**
	 * Returns terminal information
	 */
	public TerminalSettings getTerminalSettings();
	
	/**
	 * Formats an object name according to user settings
	 * @param name Object name
	 * @param id Object id
	 */
	public String formatObjectName(String name, long id);
	
	/**
	 * Formats a timestamp in a space efficient way.
	 * 
	 * @param date The date to format
	 * @return A formatted date
	 */
	public String smartFormatDate(Date date);
	
	/**
	 * Returns a <tt>DisplayController</tt> according to the user preferences
	 */
	public DisplayController getDisplayController();
	
	/**
	 * Resolves a message specifier on the form "123" for a local number
	 * relative to the current conference, or "(123)" for a global message id.
	 * 
	 * @param specifier The specifier string
	 * @return The <tt>MessageHeader</tt> of the specified message.
	 * @throws MessageNotFoundException
	 * @throws AuthenticationException
	 * @throws UnexpectedException
	 * @throws NumberFormatException
	 */
	public MessageHeader resolveMessageSpecifier(String specifier)
	throws MessageNotFoundException, AuthorizationException, UnexpectedException, BadParameterException;
}
