/*
 * Created on Jan 11, 2008
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import nu.rydin.kom.exceptions.InternalException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.commands.MessageSearchResultPrinter;
import nu.rydin.kom.frontend.text.commands.MessageSearchResultPrinterFactory;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.MessageSearchResult;

/**
 * This class handles selection of texts in a MessageSearchResult array.
 * 
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class SelectedMessages
{
    
    private static int INITIAL_MESSSAGE_INDEX = -1;

    private MessageSearchResult[] m_messages;
    private int m_nextMessageIndex = INITIAL_MESSSAGE_INDEX;

    public boolean hasUnreadMessages() 
    {
        return (m_nextMessageIndex >= 0 && m_nextMessageIndex < m_messages.length);
    }
    
    public void clear()
    {
        m_messages = null;
        m_nextMessageIndex = INITIAL_MESSSAGE_INDEX;
    }
    
    public void setMessages(MessageSearchResult[] msr)
    {
        m_nextMessageIndex = 0;
        m_messages = msr;
    }
    
    public MessageSearchResult[] getMessages()
    {
        return m_messages;
    }
    
    public MessageLocator getNextMessage()
    {
        MessageLocator nextMessage = MessageLocator.NO_MESSAGE;
        if (hasUnreadMessages()) 
        {
            nextMessage = new MessageLocator(m_messages[m_nextMessageIndex].getGlobalId());
            m_nextMessageIndex++;
        }
        return nextMessage;
    }

    
    public MessageLocator getPreviousMessage()
    {
        MessageLocator previousMessage = MessageLocator.NO_MESSAGE;
        if (m_nextMessageIndex > 1)
        {
            m_nextMessageIndex--;
            m_nextMessageIndex--;
            previousMessage = new MessageLocator(m_messages[m_nextMessageIndex].getGlobalId());
            m_nextMessageIndex++;
        }
        return previousMessage;
    }
    
    public int getUnread() 
    {
        return m_messages.length - m_nextMessageIndex;
    }

    
    public MessageSearchResultPrinter getMessageSearchResultPrinter(Context context) throws KOMException
    {
        if (!hasUnreadMessages()) {
            throw new InternalException("No messages selected");
        }
        return MessageSearchResultPrinterFactory.createMessageSearchResultPrinter(context, m_messages[0].getClass());
    }

    
}
