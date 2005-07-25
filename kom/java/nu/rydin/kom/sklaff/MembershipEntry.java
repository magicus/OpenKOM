/*
 * Created on Apr 14, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

/**
 * @author Pontus Rydin
 */
public class MembershipEntry
{
    private final int id;
    private final String markers;
    
    
    /**
     * @param id
     * @param markers
     */
    public MembershipEntry(final int id, final String markers)
    {
        super();
        this.id = id;
        this.markers = markers;
    }
    public int getId()
    {
        return id;
    }
    public String getMarkers()
    {
        return markers;
    }
}
