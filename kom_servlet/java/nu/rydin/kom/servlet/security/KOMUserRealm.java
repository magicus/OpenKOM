package nu.rydin.kom.servlet.security;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.RealmBase;

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

public class KOMUserRealm implements Realm
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
	    if (o == null)
		return false;
	    try
	    {
		KOMPrincipal p = (KOMPrincipal) o;
		if (p.name == name)
		    return true;
		if (p.name == null || name == null)
		    return false;
		return p.name.equals(name);
	    } catch (ClassCastException e)
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
	    if (this.hasRole(role))
		return true;
	    return underlying != null ? underlying.hasRole(role) : false;
	}
    }

    private String name;
    
    private Container container;

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

    public Principal authenticate(String username, String credentials)
    {
	try
	{
	    // Get session
	    //
	    ManagedServerSession session = KOMSessionFactory.getInstance()
		    .getSession(username, (String) credentials);

	    // Assiciate session with thread and http session
	    //
	    Principal principal = new KOMPrincipal(username);
	    session.grab();

	    // Set up roles based on KOM privileges.
	    // Note that we do this per user rather than per session. We may
                // want to
	    // revisit this later!
	    //
	    long privs = session.getLoggedInUser().getRights();
	    Set<String> roles = new HashSet<String>();
	    roles.add(KOMRoles.KOM_USER);
	    if ((privs & UserPermissions.ADMIN) != 0)
		roles.add(KOMRoles.KOM_ADMIN);
	    if ((privs & UserPermissions.LOGIN) != 0)
		roles.add(KOMRoles.KOM_WEB_USER);
	    synchronized (userRoles)
	    {
		userRoles.put(principal, roles);
	    }
	    return principal;
	} catch (AuthenticationException e)
	{
	    // Could not authenticate due to wrong user/password.
	    //
	    return null;
	} catch (UnexpectedException e)
	{
	    // Internal error!
	    //
	    Logger.error(this, "Internal error", e);
	    return null;
	} catch (LoginProhibitedException e)
	{
	    // Valid user, but not allowed to log in.
	    // TODO: For now, we treat it the same way as wrong
                // username/password, but we need to
	    // do something more intelligent here!
	    //
	    return null;
	}
    }


    public boolean hasRole(Principal user, String role)
    {
	return ((KOMPrincipal) user).hasRole(role);
    }

    protected Set<String> getUserRoles(Principal principal)
    {
	synchronized (userRoles)
	{
	    return userRoles.get(principal);
	}
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0)
    {
	// TODO Auto-generated method stub

    }

    public Principal authenticate(X509Certificate[] arg0)
    {
	// TODO Auto-generated method stub
	return null;
    }

    public Principal authenticate(String arg0, byte[] arg1)
    {
	// TODO Auto-generated method stub
	return null;
    }

    public Principal authenticate(String arg0, String arg1, String arg2,
	    String arg3, String arg4, String arg5, String arg6, String arg7)
    {
	// TODO Auto-generated method stub
	return null;
    }

    public void backgroundProcess()
    {
	// TODO Auto-generated method stub

    }

    public SecurityConstraint[] findSecurityConstraints(Request arg0,
	    Context arg1)
    {
	// TODO Auto-generated method stub
	return null;
    }

    public Container getContainer()
    {
	return this.container;
    }

    public String getInfo()
    {
	// TODO Auto-generated method stub
	return null;
    }

    public boolean hasResourcePermission(Request arg0, Response arg1,
	    SecurityConstraint[] arg2, Context arg3) throws IOException
    {
	// TODO Auto-generated method stub
	return false;
    }


    public boolean hasUserDataPermission(Request arg0, Response arg1,
	    SecurityConstraint[] arg2) throws IOException
    {
	// TODO Auto-generated method stub
	return false;
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0)
    {
	// TODO Auto-generated method stub

    }

    public void setContainer(Container container)
    {
	this.container = container;

    }
}
