/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.services;

import java.security.SecureRandom;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.soap.exceptions.SessionExpiredException;
import nu.rydin.kom.soap.interfaces.Authenticator;
import nu.rydin.kom.soap.structs.SecurityToken;
import nu.rydin.kom.soap.support.SessionRegistry;
import nu.rydin.kom.utils.Base64;

/** 
 * @author Pontus Rydin
 */
public class AuthenticatorService implements Authenticator
{
    private SessionRegistry registry = SessionRegistry.instance();
    
    public SecurityToken login(String username, String password) 
    throws AuthenticationException, UnexpectedException, AlreadyLoggedInException, LoginProhibitedException
    {
        try
        {
            // Create a session
            //
            ServerSessionFactory ssf = (ServerSessionFactory) Modules.getModule("Backend");
            ServerSession ss = ssf.login(username, password);
            
            // Create a token and register. 32 bytes of random junk is probably
            // enough as a token.
            //
            byte[] ticket = new byte[32];
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(ticket);
            SecurityToken token = new SecurityToken(Base64.encodeBytes(ticket));
            registry.put(token, ss);
            return token;
        }
        catch(NoSuchModuleException e)
        {
            throw new UnexpectedException(-1, e);
        }
    }

    public void discardToken(SecurityToken token) throws AuthenticationException, UnexpectedException, SessionExpiredException
    {
        ServerSession ss = registry.get(token);
        if(ss == null)
            return;
        ss.close();
        registry.remove(token);
    }
}
