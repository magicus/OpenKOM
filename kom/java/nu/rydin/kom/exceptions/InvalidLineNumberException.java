/*
 * Created on 2004-aug-20
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse Bursie
 */
public class InvalidLineNumberException extends UserException 
{
    static final long serialVersionUID = 2005;
    
    public InvalidLineNumberException() 
    {
        super();
    }

    public InvalidLineNumberException(String msg) 
    {
        super(msg);
    }
}
