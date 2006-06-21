/*
 * Created on Dec 4, 2005
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.rydin.kom.backend.data.MembershipManager;
import nu.rydin.kom.backend.data.RelationshipManager;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.utils.Logger;

/**
 * Hands out user context. Only one context is kept per user, even though
 * it may be used from several different sessions.
 * 
 * @author Pontus Rydin
 *
 */
public class UserContextFactory
{
    private static class UserContextWrapper
    {
        private final UserContext context;
        private int refCount = 0;
        
        public UserContextWrapper(UserContext context)
        {
            this.context = context;
        }
        
        public int grab()
        {
            return ++refCount;
        }
        
        public int release()
        {
            return --refCount;
        }
    }
    
    private static final UserContextFactory s_instance = new UserContextFactory();
    
    private final Map<Long, UserContextWrapper> contexts = new HashMap<Long, UserContextWrapper>();
    
    public static UserContextFactory getInstance()
    {
        return s_instance;
    }
    
    public synchronized UserContext getOrCreateContext(long user, MembershipManager mm, 
            RelationshipManager rm)
    throws ObjectNotFoundException, UnexpectedException
    {
        UserContextWrapper ucw = contexts.get(user);
        if(ucw == null)
        {
            Logger.info(this, "Context not found for user " + user + ". Creating new.");
            ucw = new UserContextWrapper(new UserContext(user, mm, rm)); 
            contexts.put(user, ucw);
        }
        int refCount = ucw.grab();
        Logger.info(this, "Grabbing context. Refcount for user " + user + " is now " + refCount);
        return ucw.context;
    }
    
    public synchronized UserContext getContextOrNull(long user)
    {
        return contexts.get(user).context;
    }
    
    public synchronized void release(long user)
    {
        UserContextWrapper ucw = contexts.get(user);
        int refCount = ucw.release();
        if(refCount == 0)
        {
            Logger.info(this, "Context refcount reached zero for user " + user + ". Destroying context.");
            contexts.remove(user);
        }
        else
            Logger.info(this, "Releasing context. Refcount for user " + user + " is now " + refCount);
    }
    
    public synchronized List<UserContext> listContexts()
    {
        List<UserContext> answer = new ArrayList<UserContext>(contexts.size());
        for (UserContextWrapper context : contexts.values())
            answer.add(context.context);
        return answer;
    }
}
