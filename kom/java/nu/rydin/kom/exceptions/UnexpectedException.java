/*
 * Created on Oct 28, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UnexpectedException extends SystemException
{
    static final long serialVersionUID = 2005;
    
	private long m_user;
		
	public UnexpectedException(long user, Throwable t)
	{
		super(t);
		m_user = user;
	}
	
	public UnexpectedException(long user, String msg, Throwable t)
	{
		super(msg, t);
		m_user = user;
	}	
	
	public long getUser()
	{
		return m_user;
	}
}
