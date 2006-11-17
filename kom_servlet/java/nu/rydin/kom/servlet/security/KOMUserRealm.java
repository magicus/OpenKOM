package nu.rydin.kom.servlet.security;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.constants.ClientTypes;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.servlet.KOMContext;
import nu.rydin.kom.servlet.KOMSessionFactory;
import nu.rydin.kom.servlet.ManagedServerSession;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.Logger;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.ServletHttpRequest;

public class KOMUserRealm implements UserRealm
{
    private class KOMPrincipal implements Principal
    {
        private final String name;
        
        public KOMPrincipal(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        public int hashCode()
        {
            return name.hashCode();
        }
        
        public String toString()
        {
            return "KOMPrincipal(" + name + ')';
        }
        
        public boolean equals(Object o)
        {
            if(o == null)
                return false;
            try
            {
                KOMPrincipal p = (KOMPrincipal) o;
                if(p.name == name)
                    return true;
                if(p.name == null || name == null)
                    return false;
                return p.name.equals(name);
            }
            catch(ClassCastException e)
            {
                return false;
            }
        }
        
        protected boolean hasRole(String role)
        {
            Set<String> userRoles = KOMUserRealm.this.getUserRoles(this);
            return userRoles != null ? userRoles.contains(role) : false;
        }
    }
    
    private class WrappedPrincipal extends KOMPrincipal
    {
        private KOMPrincipal underlying;
        
        public WrappedPrincipal(String name, KOMPrincipal underlying)
        {
            super(name);
            this.underlying = underlying;
        }
        
        public boolean hasRole(String role)
        {
            if(this.hasRole(role))
                return true;
            return underlying != null ? underlying.hasRole(role) : false;
        }
    }
    
    private String name;
    private final Map<Principal, Set<String>> userRoles = new HashMap<Principal, Set<String>>();
    
    
    public KOMUserRealm(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public Principal getPrincipal(String username)
    {
        // TODO: What to do about session id?
        //
        return new KOMPrincipal(username);
    }

    public Principal authenticate(String username, Object password, HttpRequest rq)
    {
        try
        {
            // Get session
            //
            ManagedServerSession session = KOMSessionFactory.getInstance().getSession(username, (String) password);
             
            // Assiciate session with thread and http session
            //
            HttpSession httpSession = ((ServletHttpRequest) rq.getWrapper()).getSession();
            httpSession.setAttribute("komSession", session);
            Principal principal = new KOMPrincipal(username);
            session.grab();
            KOMContext.setSession(session);
            
            // Set up roles based on KOM privileges.
            // Note that we do this per user rather than per session. We may want to 
            // revisit this later!
            //
            long privs = session.getLoggedInUser().getRights();
            Set<String> roles = new HashSet<String>();
            roles.add(KOMRoles.KOM_USER);
            if((privs & UserPermissions.ADMIN) != 0)
                roles.add(KOMRoles.KOM_ADMIN);
            if((privs & UserPermissions.LOGIN) != 0)
                roles.add(KOMRoles.KOM_INTERACTIVE_USER);
            synchronized(userRoles)
            {
                userRoles.put(principal, roles);
            }
            return principal;
        } 
        catch (AuthenticationException e)
        {
            // Could not authenticate due to wrong user/password.
            //
            return null;
        } 
        catch (UnexpectedException e)
        {
            // Internal error!
            //
            Logger.error(this, "Internal error", e);
            return null;
        } 
        catch (LoginProhibitedException e)
        {
            // Valid user, but not allowed to log in. 
            // TODO: For now, we treat it the same way as wrong username/password, but we need to
            // do something more intelligent here!
            //
            return null;
        } 
    }

    public boolean reauthenticate(Principal principal)
    {
        ManagedServerSession session = KOMSessionFactory.getInstance().getSession(principal.getName());
        KOMContext.setSession(session);
        return session != null;
    }

    public boolean isUserInRole(Principal user, String role)
    {
        return ((KOMPrincipal) user).hasRole(role);
    }

    public void disassociate(Principal user)
    {
        Logger.info(this, "Disassociating from principal " + user.toString());
        KOMContext.disassociateSession();
    }

    public Principal pushRole(Principal user, String role)
    {
        WrappedPrincipal wp = new WrappedPrincipal(role, (KOMPrincipal) user);
        synchronized(userRoles)
        {
            Set<String> roles = new HashSet<String>();
            roles.add(role);
            userRoles.put(wp, roles);
        }
        return wp;
    }

    public Principal popRole(Principal user)
    {
        return user instanceof WrappedPrincipal ? ((WrappedPrincipal) user).underlying : user;
    }

    public void logout(Principal principal)
    {
        Logger.info(this, "Logging out " + principal.toString());
        synchronized(userRoles)
        {
            userRoles.put(principal, null);
        }
    }
    
    protected Set<String> getUserRoles(Principal principal)
    {
        synchronized(userRoles)
        {
            return userRoles.get(principal);
        }
    }
}
