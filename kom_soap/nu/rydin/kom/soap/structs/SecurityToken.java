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
public class SecurityToken implements Serializable
{
    private String payload;
    
    public SecurityToken()
    {
    }
    
    public SecurityToken(String payload)
    {
        super();
        this.payload = payload;
    }
        
    public String getPayload()
    {
        return payload;
    }
    
    public void setPayload(String payload)
    {
        this.payload = payload;
    }
    
    public boolean equals(Object o)
    {
        if(o == null)
            return payload == null;
        try
        {
            return ((SecurityToken) o).payload.equals(payload);
        }
        catch(ClassCastException e)
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        return payload != null ? payload.hashCode() : 0;
    }
}
