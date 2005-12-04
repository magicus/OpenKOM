/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.structs;

import java.io.Serializable;

/**
 * @author Pontus Rydin
 */
public class UnstoredMessage implements Serializable
{
	private String subject;
	
	private String body;
	
	public UnstoredMessage()
	{
	}
	
	public UnstoredMessage(nu.rydin.kom.structs.UnstoredMessage nativeType)
	{
		subject 	= nativeType.getSubject();
		body 		= nativeType.getBody();
	}
	
	public String getSubject()
	{
		return subject;
	}
	
	public String getBody()
	{
		return body;
	}
	
    public void setBody(String body)
    {
        this.body = body;
    }
    
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
    public nu.rydin.kom.structs.UnstoredMessage toNative()
    {
        return new nu.rydin.kom.structs.UnstoredMessage(subject, body);
    }
}
