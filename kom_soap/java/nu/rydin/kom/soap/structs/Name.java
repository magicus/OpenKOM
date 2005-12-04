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
public class Name
{
    private String name;
    private short kind;
    private short visibility;

    public Name()
    {
    }
    
    public Name(nu.rydin.kom.structs.Name nativeType)
    {
        this.name = nativeType.getName();
        this.kind = nativeType.getKind();
        this.visibility = nativeType.getVisibility();
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public short getVisibility()
    {
        return visibility;
    }
    public void setVisibility(short visibility)
    {
        this.visibility = visibility;
    }
    
    public nu.rydin.kom.structs.Name toNative()
    {
        return new nu.rydin.kom.structs.Name(name, kind, visibility);
    }
    public short getKind()
    {
        return kind;
    }
    public void setKind(short kind)
    {
        this.kind = kind;
    }
}
