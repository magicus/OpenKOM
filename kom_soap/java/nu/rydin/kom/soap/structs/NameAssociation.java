/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.structs;

/**
 * @author Pontus Rydin
 */
public class NameAssociation
{
	private long id;
	private Name name;
	
	public NameAssociation()
	{
	}
	
	public NameAssociation(nu.rydin.kom.structs.NameAssociation nativeType)
	{
	    this.id = nativeType.getId();
	    this.name = new Name(nativeType.getName());
	}

    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public Name getName()
    {
        return name;
    }
    public void setName(Name name)
    {
        this.name = name;
    }
    
    public nu.rydin.kom.structs.NameAssociation toNative()
    {
        return new nu.rydin.kom.structs.NameAssociation(id, name.toNative());
    }
}
