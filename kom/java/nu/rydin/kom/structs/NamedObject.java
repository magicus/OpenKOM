/*
 * Created on Nov 6, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NamedObject implements Serializable
{
	private final long id;
	private final Name name;
    private final String keywords;

	protected NamedObject(long id, Name name, String keywords)
	{
		this.id 	  = id;
		this.name     = name;
        this.keywords = keywords;
	}
	
	public long getId()
	{
		return id;
	}	
	
	public Name getName()
	{
		return name;
	}
    
    public String getKeywords()
    {
        return keywords;
    }
}
