/*
 * Created on Oct 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Henrik Schr�der
 *
 */
public class UserNotFoundException extends ObjectNotFoundException
{
    public UserNotFoundException()
    {
        super();
    }

    public UserNotFoundException(String msg)
    {
        super(msg);
    }
    
    public UserNotFoundException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
