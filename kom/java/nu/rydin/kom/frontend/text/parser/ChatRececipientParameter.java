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
        ServerSession session = context.getSession();
        ArrayList list = new ArrayList();
        UserListItem[] users = session.listLoggedInUsers();
        int top = users.length;
        for(int idx = 0; idx < top; ++idx)
        {
            NameAssociation name = users[idx].getUser();
            if(NameUtils.match(pattern, name.getName().getName(), true))
                list.add(name);
        }
        
        // Now, get the list of all matching conferences
        //
        NameAssociation[] conferences = session.getAssociationsForPatternAndKind(
                match.getMatchedString(), NameManager.CONFERENCE_KIND);
        top = conferences.length;
        for(int idx = 0; idx < top; ++idx)
            list.add(conferences[idx]);
        
        // What did we get?
        //
        switch(list.size())
        {
        	case 0:
        	{
        	    // Either the object doesn't exist, or the it represents
        	    // a user the wasn't logged in. Find out what the problem
        	    // is!
        	    //
        	    NameAssociation[] names = session.getAssociationsForPattern(pattern);
        	    if(names.length == 0)
        	        throw new ObjectNotFoundException(pattern);
        	    else
        	        throw new NotLoggedInException(names[0].getName().toString());
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
}
