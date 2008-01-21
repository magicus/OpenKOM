/*
 * Created on Dec 28, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Henrik Schröder
 */
public class ModuleException extends Exception
{
    static final long serialVersionUID = 2005;
    
    public ModuleException(String msg)
    {
        super(msg);
    }
    
    public ModuleException(Throwable t)
    {
        super(t);
    }

    public ModuleException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
