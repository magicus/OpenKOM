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
public class InvalidParametersException extends UserException {

    public InvalidParametersException() {
        super();
    }

    public InvalidParametersException(String msg) {
        super(msg);
    }
}
