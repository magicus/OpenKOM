/*
 * Created on Oct 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Pontus Rydin
 */
public class NoSuchModuleException extends SystemException
{
    public NoSuchModuleException()
    {
        super();
    }
    
    public NoSuchModuleException(String msg)
    {
        super(msg);
    }    
}
