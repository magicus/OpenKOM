/*
 * Created on Sep 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferenceListItem extends NameAssociation implements Serializable
{
    private final Timestamp created;
    
    private final Timestamp lastActive;
    
    public ConferenceListItem(long id, Name name, Timestamp created, Timestamp lastActive)
    {
        super(id, name);
        this.created 	= created;
        this.lastActive	= lastActive;
    }
    public Timestamp getCreated()
    {
        return created;
    }
    public Timestamp getLastActive()
    {
        return lastActive;
    }
}
