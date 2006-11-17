/*
 * Created on Jun 30, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.servlet;

/**
 * Holds context specific variables.
 * 
 * @author Pontus Rydin
 */
public class KOMContext
{
    private static final ThreadLocal<ManagedServerSession> sessionHolder = new ThreadLocal<ManagedServerSession>();
    
    public static final ManagedServerSession getSession()
    {
        return sessionHolder.get();
    }
    
    public static final void setSession(ManagedServerSession session)
    {
        if(sessionHolder.get() != null)
            throw new IllegalStateException("Session is already set!");
        sessionHolder.set(session);
    }
    
    public static final void disassociateSession()
    {
        sessionHolder.set(null);
    }
}
