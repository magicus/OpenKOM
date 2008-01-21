/*
 * Created on Sep 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CacheInformation implements Serializable
{
    static final long serialVersionUID = 2005;
    
    private final long numAccesses;
    
    private final long numHits;
    
    public CacheInformation(long numAccesses, long numHits)
    {
        this.numAccesses 	= numAccesses;
        this.numHits 		= numHits;
    }
    public long getNumAccesses()
    {
        return numAccesses;
    }
    
    public long getNumHits()
    {
        return numHits;
    }
    
    public double getHitRatio()
    {
        return numAccesses != 0 ? (double) numHits / (double) numAccesses : 0;
    }
}
