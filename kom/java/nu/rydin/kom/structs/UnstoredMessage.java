/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UnstoredMessage
{
	private String m_subject;
	
	private String m_body;
	
	public UnstoredMessage(String subject, String body)
	{
		m_subject 	= subject;
		m_body 		= body;
	}
	
	public String getSubject()
	{
		return m_subject;
	}
	
	public String getBody()
	{
		return m_body;
	}
}
