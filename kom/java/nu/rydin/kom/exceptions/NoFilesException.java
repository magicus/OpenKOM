/*
 * Created on Aug 27, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NoFilesException extends UserException
{
    static final long serialVersionUID = 2005;
    
    public NoFilesException()
    {
        super();
    }

    public NoFilesException(String message)
    {
        super(message);
    }

    public NoFilesException(Object[] msgArgs)
    {
        super(msgArgs);
    }

}
