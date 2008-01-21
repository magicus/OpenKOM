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
    static final long serialVersionUID = 2005;
    
    public NoSuchModuleException()
    {
        super();
    }
    
    public NoSuchModuleException(String msg)
    {
        super(msg);
    }    
}
