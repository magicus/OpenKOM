/*
 * Created on Aug 27, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GenericException extends UserException
{ 
    public GenericException()
    {
        super();
    }

    public GenericException(String message)
    {
        super(message);
    }
    
    public String formatMessage(Context context)
    {
        return this.getMessage();
    }
}
