/*
 * Created on Jun 16, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.*;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class EditorContext implements Context
{
	private final Context m_underlying;
	
	private Buffer m_buffer;
	
	private String m_subject;
	
	private NameAssociation m_recipient;
	
	private long m_replyTo = -1;
	
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
		throws UnexpectedException
	{
		return m_underlying.getCachedUserInfo();
	}

	public String[] getFlagLabels()
	{
		return m_underlying.getFlagLabels();
	}
	
	public String[] getRightsLabels()
	{
		return m_underlying.getRightsLabels();
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
	
	public NameAssociation getRecipient()
	{
	    return m_recipient;
	}
	
	public void setRecipient(NameAssociation recipient)
	{
	    m_recipient = recipient;
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
	
    public void setTerminalHeight(int height)
    {
        m_underlying.setTerminalHeight(height);
    }
    public void setTerminalWidth(int width)
    {
        m_underlying.setTerminalWidth(width);
    }
    
	public String formatObjectName(String name, long id)
	{
	    return m_underlying.formatObjectName(name, id);
	}
	
	public String formatObjectName(Name name, long id)
	{
	    return m_underlying.formatObjectName(name, id);
	}
	
	public String formatObjectName(NameAssociation object)
	{
	    return m_underlying.formatObjectName(object);
	}
	
	public String smartFormatDate(Date date)
	throws UnexpectedException
	{
	    return m_underlying.smartFormatDate(date);
	}
	
	public DisplayController getDisplayController()
	{
	    return m_underlying.getDisplayController();
	}
	
	public void executeScript(String script)
	throws IOException, InterruptedException, KOMException
	{
	    m_underlying.executeScript(script);
	}
	
	public void executeScript(BufferedReader rdr)
	throws IOException, InterruptedException, KOMException
	{
	    m_underlying.executeScript(rdr);
	}

    public Parser getParser()
    {
        // FIXME: Ihse -- what is the correct thing to do???
        return m_underlying.getParser();
    }
    
    public long getReplyTo()
    {
        return m_replyTo;
    }
    public void setReplyTo(long replyTo)
    {
        m_replyTo = replyTo;
    }

    public String[] getExistingFlagLabels()
    {
        return m_underlying.getExistingFlagLabels();
    }

    public String[] getExistingRightsLabels()
    {
        return m_underlying.getExistingRightsLabels();
    }

}
