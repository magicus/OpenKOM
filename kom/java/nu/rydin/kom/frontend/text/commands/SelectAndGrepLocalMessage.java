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
import nu.rydin.kom.frontend.text.parser.RawParameter;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public class SelectAndGrepLocalMessage extends AbstractSelect
{

    public SelectAndGrepLocalMessage (Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter(
                "search.param.0.ask", true) }, permissions);
    }

    
    @Override
    protected boolean select(Context context, Object[] parameters)
    throws KOMException
    {
        return context.getSession().selectGrepMessagesLocally(context.getSession().getCurrentConferenceId(), (String) parameters[0]);
    }
    
}
