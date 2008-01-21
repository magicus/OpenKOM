/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * Thrown when a user has made an invalid choice, for example when 
 * picking a name from a list of ambiguous ones.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class InvalidChoiceException extends UserException
{
    static final long serialVersionUID = 2005;
    
    public InvalidChoiceException()
    {
        super();
    }

    public InvalidChoiceException(String message)
    {
        super(message);
    }
}
