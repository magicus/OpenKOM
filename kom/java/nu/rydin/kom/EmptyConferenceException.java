/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom;

/**
 * @author Jepson
 */
public class EmptyConferenceException extends KOMException 
{

	public EmptyConferenceException() 
	{
		super();
	}

	public EmptyConferenceException(String msg) 
	{
		super(msg);
	}
}
