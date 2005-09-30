/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.soap.exceptions.SessionExpiredException;
import nu.rydin.kom.soap.structs.SecurityToken;
import nu.rydin.kom.utils.Logger;

/**
 * @author Pontus Rydin
 */
public class SessionRegistry
{
    private final Map sessions = new HashMap();

    private final Timer sessionScavenger = new Timer(true);

    private final long sessionTtl;

    private class CleeeeaaaaningLady extends TimerTask
    {
        private final SecurityToken token;

        public CleeeeaaaaningLady(SecurityToken token)
        {
            this.token = token;
        }

        public void run()
        {
            synchronized (SessionRegistry.this)
            {
                Item item = (Item) SessionRegistry.this.sessions.get(token);
                if(item == null)
                    return;
                try
                {
                    item.session.close();
                }
                catch(Exception e)
                {
                    // We're just a lowly cleaning lady, so there's not much we
                    // can do!
                    //
                    Logger.error(this, "Error while closing session", e);
                }
                SessionRegistry.this.sessions.remove(token);
            }
            
        }
    }
    
    private static class Item
    {
        private final ServerSession session;
        private CleeeeaaaaningLady sanitaryTechnician;
        
        public Item(final ServerSession session,
                final CleeeeaaaaningLady sanitaryTechnician)
        {
            this.session = session;
            this.sanitaryTechnician = sanitaryTechnician;
        }
        
        public CleeeeaaaaningLady getSanitaryTechnician()
        {
            return sanitaryTechnician;
        }
        
        public ServerSession getSession()
        {
            return session;
        }
    }
    
    // TODO: Read ttl from config
    private static SessionRegistry instance = new SessionRegistry(300000);
    
    public static SessionRegistry instance()
    {
        return instance;
    }

    private SessionRegistry(long sessionTtl)
    {
        this.sessionTtl = sessionTtl;
    }

    public synchronized void put(SecurityToken token, ServerSession session)
    {
        CleeeeaaaaningLady piga = new CleeeeaaaaningLady(token);
        sessions.put(token, new Item(session, piga));
        sessionScavenger.schedule(piga, sessionTtl);
    }
    
    public synchronized void remove(SecurityToken token)
    {
        Item item = (Item) sessions.get(token);
        if(item == null)
            return;
        
        // Fire the sanitary technician
        //
        item.sanitaryTechnician.cancel();
        sessions.remove(token);
    }
    
    public synchronized ServerSession get(SecurityToken token)
    throws SessionExpiredException
    {
        ServerSession ss = this.getOrNull(token);
        if(ss == null)
            throw new SessionExpiredException(token.getPayload());
        return ss;
    }
    
    public synchronized ServerSession getOrNull(SecurityToken token)
    {
        Item item = (Item) sessions.get(token);
        if(item == null)
            return null;
        
        // Fire the old sanitary technician and leave her on 
        // the street to rot. Then hire an new one we can wear out!
        //
        item.sanitaryTechnician.cancel();
        CleeeeaaaaningLady piga = new CleeeeaaaaningLady(token);
        sessionScavenger.schedule(piga, sessionTtl);
        item.sanitaryTechnician = piga;
        return item.session;
    }
}