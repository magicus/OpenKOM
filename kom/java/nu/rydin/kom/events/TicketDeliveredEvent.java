/*
 * Created on Sep 18, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TicketDeliveredEvent extends ClientEvent
{
    private final String m_ticket;
 
    public TicketDeliveredEvent(long user, String ticket)
    {
        super(user);
        m_ticket = ticket;
    }

    public void dispatch(ClientEventTarget target)
    {
        target.onEvent(this);
    }
    
    public String getTicket()
    {
        return m_ticket;
    }
}
