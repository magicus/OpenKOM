/*
 * Created on Apr 12, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

import java.sql.Timestamp;

/**
 * @author Pontus Rydin
 */
public class PasswdEntry
{
    private final String id;
    private final String password;
    private final String name;
    private final Timestamp lastLogin;
    
    public PasswdEntry(final String id, final String password, String name, Timestamp lastLogin)
    {
        super();
        this.id = id;
        this.password = password;
        this.name = name;
        this.lastLogin = lastLogin;
    }
    
    public String getId()
    {
        return id;
    }

    public String getPassword()
    {
        return password;
    }
    
    public String getName()
    {
        return name;
    }
    public Timestamp getLastLogin()
    {
        return lastLogin;
    }
}
