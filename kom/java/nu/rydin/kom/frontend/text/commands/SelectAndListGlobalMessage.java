/*
 * Created on Jan 12, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class SelectAndListGlobalMessage extends AbstractSelect
{

    public SelectAndListGlobalMessage(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new UserParameter(true) }, permissions);
    }

    
    @Override
    protected boolean select(Context context, Object[] parameters)
    throws KOMException
    {
        NameAssociation user = (NameAssociation) parameters[0];
        return context.getSession().selectMessagesGloballyByAuthor(user.getId());
    }
}
