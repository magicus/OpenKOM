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
import nu.rydin.kom.i18n.MessageFormatter;

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
	public MessageEditor getMessageEditor();

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
}