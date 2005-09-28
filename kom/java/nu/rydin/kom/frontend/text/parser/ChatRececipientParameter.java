/*
 * Created on Sep 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.util.ArrayList;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.exceptions.AmbigiousAndNotLoggedInException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NotLoggedInException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UserListItem;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChatRececipientParameter extends NamedObjectParameter
{
    public final static NameAssociation ALL_USERS = new NameAssociation(-1, "alla");

    public ChatRececipientParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }
    
    public ChatRececipientParameter(boolean isRequired)
    {
        super(isRequired);
    }

    public Object resolveFoundObject(Context context, Match match)
    throws KOMException, IOException, InterruptedException
    {
        // First, get the list of logged in users and extract the names
        //
        String pattern = match.getMatchedString();
        
        if (pattern.equals("*")) {
            return ALL_USERS;
        }
        
        ServerSession session = context.getSession();
        ArrayList list = new ArrayList();
        if (pattern.startsWith("*")) {
            // Argument was prefixed by "*". That means, we want to select
            // recipient from the list of conferences.
            pattern = pattern.substring(1); // Remove "*".
            
            NameAssociation[] conferences = session.getAssociationsForPatternAndKind(
                    pattern, NameManager.CONFERENCE_KIND);
            for (int idx = 0; idx < conferences.length; ++idx) {
                list.add(conferences[idx]);
            }
        } else {
            // Otherwise, we select it from the list of logged in users.
            UserListItem[] users = session.listLoggedInUsers();
            for (int idx = 0; idx < users.length; ++idx) {
                NameAssociation name = users[idx].getUser();
                if (NameUtils.match(pattern, name.getName().getName(), false)) {
                    list.add(name);
                }
            }
        }
        
        // What did we get?
        //
        switch(list.size())
        {
        	case 0:
        	{
        	    // Either the object doesn't exist, or the it represents
        	    // a user the wasn't logged in. Find out what the problem
        	    // is!
                if (pattern.startsWith("*")) {
                    pattern = pattern.substring(1); // Remove "*".
                    throw new ObjectNotFoundException(pattern);
                } else {
                    NameAssociation[] names = session.getAssociationsForPatternAndKind(pattern, NameManager.USER_KIND);
                    if (names.length == 0) {
                        throw new ObjectNotFoundException(pattern);
                    } else if (names.length == 1) {
                        throw new NotLoggedInException(names[0].getName().toString());
                    } else {
                        throw new AmbigiousAndNotLoggedInException(pattern);
                    }
                }
        	    //
        	}
        	case 1:
        	    return ((NameAssociation) list.get(0));
        	default:
        	{
        	    NameAssociation[] names = new NameAssociation[list.size()];
        	    list.toArray(names);
        	    return NamePicker.pickName(names, context);
        	}
        }
    }

    protected boolean isValidName(String name)
    {
        return (super.isValidName(name) || name.equals("*") || (name.startsWith("*") && super.isValidName(name.substring(1))));
    }
}
