/*
 * Created on Sep 20, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Henrik Schröder
 */
public class BadPasswordException extends UserException
{
    static final long serialVersionUID = 2005;
    
	public BadPasswordException()
	{
		super();
	}
	public BadPasswordException(String msg)
	{
		super(msg);
	}	

}