/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * Thrown when the current command has been interrupted, typically
 * on a user request. Note that java.lang.InterruptedException interrupts
 * the entire session, whereas this exception just interrupts the current
 * command.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class OperationInterruptedException extends KOMUserException
{
}
