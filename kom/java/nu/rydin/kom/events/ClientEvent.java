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
public abstract class ClientEvent extends Event
{
    public ClientEvent(long user)
    {
        super(user);
    }
    
    public abstract void dispatch(ClientEventTarget target);
}
