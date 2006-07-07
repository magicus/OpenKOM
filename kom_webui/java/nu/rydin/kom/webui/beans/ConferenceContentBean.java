package nu.rydin.kom.webui.beans;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.structs.LocalMessageSearchResult;

public class ConferenceContentBean
{
    private long conferenceId;
    
    private int start;
    
    private int length;
    
    private MessageHeaderBean[] messages;

    public long getConferenceId()
    {
        return conferenceId;
    }

    public void setConferenceId(long conferenceId)
    throws UnexpectedException
    {
        this.conferenceId = conferenceId;
        
        // Load message list
        //
        ServerSession session = KOMContext.getSession();
        LocalMessageSearchResult[] result = session.listAllMessagesLocally(conferenceId, start, length);
        int top = result.length;
        messages = new MessageHeaderBean[top];
        for(int idx = 0; idx < top; ++idx)
        {
            LocalMessageSearchResult each = result[idx];
            MessageHeaderBean bean = new MessageHeaderBean();
            bean.setAuthorId(each.getAuthor().getId());
            bean.setAuthorName(each.getAuthor().getName().getName());
            bean.setConference(conferenceId);
            bean.setGlobalId(each.getGlobalId());
            bean.setLocalId(each.getLocalId());
            bean.setSubject(each.getSubject());
            bean.setTimestamp(each.getTimestamp());
            messages[idx] = bean;
        }
    }

    public MessageHeaderBean[] getMessages()
    {
        return messages;
    }

    public void setMessages(MessageHeaderBean[] messages)
    {
        this.messages = messages;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }
}
