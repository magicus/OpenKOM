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
    static final long serialVersionUID = 2005;
    
	private final long id;
	private final Name name;
    private final String keywords;
    private final String emailAlias;

	protected NamedObject(long id, Name name, String keywords, String emailAlias)
	{
		this.id 	  = id;
		this.name     = name;
        this.keywords = keywords;
        this.emailAlias = emailAlias;
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
    
    public String getEmailAlias()
    {
        return emailAlias;
    }
}
