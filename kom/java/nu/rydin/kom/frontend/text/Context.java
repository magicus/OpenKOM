/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface Context
{
	/**
	 * Returns the <tt>Reader</tt> handling user input
	 */
	public LineEditor getIn();
	
	/**
	 * Returns the <tt>PrintWriter</tt> used to write messages
	 * to the user.
	 */
	public PrintWriter getOut();

	/**
	 * Returns a KOMPrinter, a Writer proxy that supports change of
	 * character set.
	 */
	public KOMPrinter getKOMPrinter();	
		
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
}
