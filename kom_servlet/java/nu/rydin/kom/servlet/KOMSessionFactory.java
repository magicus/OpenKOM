/*
 * Created on Jun 30, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.servlet;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.constants.ClientTypes;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PasswordUtils;

/**
 * Creates and manages sessions with the KOM backend
 * 
 * @author Pontus Rydin
 */
public class KOMSessionFactory
{
    private static KOMSessionFactory instance;
    private final ServerSessionFactory sessionFactory; 
    private Map<String, ManagedServerSession> sessionCache = new HashMap<String, ManagedServerSession>();
    
    public static synchronized KOMSessionFactory getInstance()
    {
        // Lazy create, since we don't want this to be inadvertedly called
        // as part of some early class loading.
        //
        if(instance == null)
            instance = new KOMSessionFactory();
        return instance;
    }
    
    public KOMSessionFactory()
    {
        try
        {
            sessionFactory = (ServerSessionFactory) Modules.getModule("Backend");
        }
        catch(NoSuchModuleException e)
        {
            Logger.fatal(this, "OpenKOM backend not registered! Cannot continue.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a session for a previously unauthenticated user
     * 
     * @param username The username
     * @param password The password
     *
     * @throws AuthenticationException Could not authenticat
     * @throws LoginProhibitedException Authenticated, but not allowed to log in
     * @throws UnexpectedException Something went wrong internally
     */
    public synchronized ManagedServerSession getSession(String username, String password)
    throws AuthenticationException, LoginProhibitedException, UnexpectedException
    {
        // Do we already have an active session for this user? Grab it!
        //
        ManagedServerSession session = sessionCache.get(username);
        if(session != null)
        {
            // Got it! Check if the password digest matches
            //
            if(!session.authenticate(password))
                throw new AuthenticationException();
        } else
        {
            // No session in cache. Create a new one!
            //
            try
            {
                session = new ManagedServerSession(this, sessionFactory.login(username, (String) password, ClientTypes.WEB, true), 
                        PasswordUtils.gerenatePasswordDigest(password));
                sessionCache.put(username, session);
            }
            catch(NoSuchAlgorithmException e)
            {
                // Ooops! This measn we're out of luck authenticating. Not much we can do!
                //
                Logger.fatal(this, "No encryption algorithm", e);
                throw new AuthenticationException();
            }
            catch(AlreadyLoggedInException e)
            {
                Logger.fatal(this, "This can't happen!", e);
            }
        }
        return session;
    }
    
    /**
     * Returns a session for a previously authenticated user, or null if the user
     * has not been authenticated.
     * 
     * @param username The username
     */
    public synchronized ManagedServerSession getSession(String username)
    {
        ManagedServerSession session = sessionCache.get(username);
        if(session == null)
            return null;
        return session;
    }
    
    protected synchronized void releaseSession(ManagedServerSession session)
    {
        sessionCache.remove(session.getLoggedInUser().getUserid());
    }
}
