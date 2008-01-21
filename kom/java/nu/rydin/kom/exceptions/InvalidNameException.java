/*
 * Created on Sep 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class InvalidNameException extends UserException
{
    static final long serialVersionUID = 2005;
    
    public InvalidNameException()
    {
        super();
    }

    public InvalidNameException(String message)
    {
        super(message);
    }

    public InvalidNameException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
