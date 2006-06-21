/*
 * Created on Dec 4, 2005
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import nu.rydin.kom.backend.data.MembershipManager;
import nu.rydin.kom.backend.data.RelationshipManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.FilterFlags;
import nu.rydin.kom.constants.RelationshipKinds;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.structs.Relationship;
import nu.rydin.kom.structs.UserInfo;

/**
 * User context shared across sessions.
 * 
 * @author Pontus Rydin
 */
public class UserContext
{
    private long user;
    private MembershipList memberships;
    private Map<Long, Long> filterCache;
    
    public UserContext(long user, MembershipManager mm, RelationshipManager rm)
    throws UnexpectedException, ObjectNotFoundException
    {
        this.user           = user;
        this.loadMemberships(mm);
        this.loadFilters(rm);
    }
    
    public long getUserId()
    {
        return user;
    }

    public Map<Long, Long> getFilterCache()
    {
        return filterCache;
    }

    public MembershipList getMemberships()
    {
        return memberships;
    }
    
    public synchronized void loadMemberships(MembershipManager mm)
    throws UnexpectedException, ObjectNotFoundException
    {
        try
        {
            memberships = new MembershipList(mm.listMembershipsByUser(user));
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(user, e);
        }
    }
    
    public synchronized void loadFilters(RelationshipManager rm)
    throws UnexpectedException
    {
        try
        {
            filterCache = new HashMap<Long, Long>();
            Relationship[] rels = rm.listByRefererAndKind(user, RelationshipKinds.FILTER);
            for (int idx = 0; idx < rels.length; idx++)
            {
                Relationship jinge = rels[idx];
                filterCache.put(jinge.getReferee(), jinge.getFlags());
            }
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(user, e);
        }
    }
    
    public synchronized void saveMemberships(MembershipManager mm)
    throws UnexpectedException
    {
        try
        {
            memberships.save(user, mm);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(user, e);
        }            
    }
    
    public boolean allowsChat(UserManager um, long originator)
    throws ObjectNotFoundException, UnexpectedException
    {
        try
        {
            UserInfo ui = um.loadUser(originator);
            return ui.testFlags(0, UserFlags.ALLOW_CHAT_MESSAGES)
                && !this.userMatchesFilter(originator, FilterFlags.CHAT);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(user, e);
        }            
    }

    public boolean allowsBroadcast(UserManager um, long originator)
    throws ObjectNotFoundException, UnexpectedException
    {
        try
        {
            UserInfo ui = um.loadUser(originator);
            return ui.testFlags(0, UserFlags.ALLOW_BROADCAST_MESSAGES)
                && !this.userMatchesFilter(originator, FilterFlags.BROADCASTS);
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(user, e);
        }                    
    }
    
    protected boolean userMatchesFilter(long user, long neededFlags)
    {
        Long flagObj = (Long) filterCache.get(new Long(user));
        if(flagObj == null)
            return false;
        long flags = flagObj.longValue();
        return (flags & neededFlags) == neededFlags; 
    }    
    
    protected void reloadMemberships(MembershipManager mm)
    throws ObjectNotFoundException, SQLException
    {
        // Load membership infos into cache
        //
        if(memberships != null)
            memberships.save(user, mm);
        memberships = new MembershipList(mm.listMembershipsByUser(user));
    }

}
