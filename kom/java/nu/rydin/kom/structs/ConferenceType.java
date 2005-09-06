/*
 * Created on Sep 5, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Pontus Rydin
 */
public class ConferenceType
{
    private final int permissions;
    
    private final int nonmemberpermissions;
    
    private final short visibility;
        
    public ConferenceType(final int permissions, final int nonmemberpermissions,
            final short visibility)
    {
        super();
        this.permissions = permissions;
        this.nonmemberpermissions = nonmemberpermissions;
        this.visibility = visibility;
    }
    
    public int getPermissions()
    {
        return permissions;
    }
    public int getNonMemberPermissions()
    {
        return nonmemberpermissions;
    }
    public short getVisibility()
    {
        return visibility;
    }
}
