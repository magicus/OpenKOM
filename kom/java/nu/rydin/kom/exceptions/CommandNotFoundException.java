/*
 * Created on 2004-aug-20
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse
 */
public class CommandNotFoundException extends UserException {

    public CommandNotFoundException() {
        super();
    }

    public CommandNotFoundException(String msg) {
        super(msg);
    }
}
