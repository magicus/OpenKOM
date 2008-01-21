/*
 * Created on Oct 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Henrik Schröder
 *
 */
public class ConferenceNotFoundException extends ObjectNotFoundException
{
    static final long serialVersionUID = 2005;
    
    public ConferenceNotFoundException()
    {
        super();
    }

    public ConferenceNotFoundException(String msg)
    {
        super(msg);
    }
    
    public ConferenceNotFoundException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
