/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class StringUtils 
{
    /**
     * Returns true if the supplie string can be parsed as a local or global message number.
     * 
     * @param s The string to test
     */
    public static boolean isMessageNumber(String s)
    {
        int top = s.length();
	    String maybeNumber = s.charAt(0) == '(' && s.charAt(top - 1) == ')'
	    	? s.substring(1, top - 1)
	    	: s;
	    top = maybeNumber.length();
	    for(int idx = 0; idx < top; ++idx)
	    {
	        if(!Character.isDigit(maybeNumber.charAt(idx)))
	            return false;
	    }		
	    return true;
    }
}
