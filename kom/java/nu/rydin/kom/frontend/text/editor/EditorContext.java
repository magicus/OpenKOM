/*
 * Created on Jun 16, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor;

import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.MessageNotFoundException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.MessagePrinter;
import nu.rydin.kom.frontend.text.TerminalSettings;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class EditorContext implements Context
{
	private final Context m_underlying;
	
	private Buffer m_buffer;
	
	private String m_subject;
	
	public EditorContext(Context underlying)
	{
		m_underlying = underlying;
		m_buffer = new Buffer();
	}
	
	public EditorContext(Context underlying, Buffer buffer)
	{
		m_underlying 	= underlying;
		m_buffer 		= buffer;
	}
	
	public Buffer getBuffer()
	{
		return m_buffer;
	}

	public void clearUserInfoCache()
	{
		m_underlying.clearUserInfoCache();
	}

	public boolean equals(Object obj)
	{
		return m_underlying.equals(obj);
	}

	public UserInfo getCachedUserInfo()
		throws UnexpectedException, ObjectNotFoundException
	{
		return m_underlying.getCachedUserInfo();
	}

	public String[] getFlagLabels()
	{
		return m_underlying.getFlagLabels();
	}

	public LineEditor getIn()
	{
		return m_underlying.getIn();
	}

	public long getLoggedInUserId()
	{
		return m_underlying.getLoggedInUserId();
	}
	
	public String getSubject()
	{
		return m_subject;
	}
	
	public void setSubject(String subject)
	{
		m_subject = subject;
	}

	public MessageEditor getMessageEditor()
	throws UnexpectedException
	{
		return m_underlying.getMessageEditor();
	}

	public MessageFormatter getMessageFormatter()
	{
		return m_underlying.getMessageFormatter();
	}

	public MessagePrinter getMessagePrinter()
	{
		return m_underlying.getMessagePrinter();
	}

	public KOMWriter getOut()
	{
		return m_underlying.getOut();
	}

	public ServerSession getSession()
	{
		return m_underlying.getSession();
	}

	public int hashCode()
	{
		return m_underlying.hashCode();
	}

	public boolean isFlagSet(int flagWord, long mask)
		throws ObjectNotFoundException, UnexpectedException
	{
		return m_underlying.isFlagSet(flagWord, mask);
	}

	public void printCurrentConference()
		throws ObjectNotFoundException, UnexpectedException
	{
		m_underlying.printCurrentConference();
	}

	public void printDebugInfo()
	{
		m_underlying.printDebugInfo();
	}

	public String toString()
	{
		return m_underlying.toString();
	}

	public WordWrapper getWordWrapper(String content)
	{
		return m_underlying.getWordWrapper(content);
	}
	
	public WordWrapper getWordWrapper(String content, int width)
	{
		return m_underlying.getWordWrapper(content, width);
	}

	public TerminalSettings getTerminalSettings()
	{
		return m_underlying.getTerminalSettings();
	}
	
	public String formatObjectName(String name, long id)
	{
	    return m_underlying.formatObjectName(name, id);
	}
	
	public DisplayController getDisplayController()
	{
	    return m_underlying.getDisplayController();
	}
	
	public MessageHeader resolveMessageSpecifier(String specifier)
	throws MessageNotFoundException, AuthorizationException, UnexpectedException, BadParameterException
	{
	    return m_underlying.resolveMessageSpecifier(specifier);
	}
}
