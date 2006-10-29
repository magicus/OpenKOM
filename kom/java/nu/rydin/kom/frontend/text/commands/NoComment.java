/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author Henrik Schröder
 */
public class NoComment extends AbstractCommand
{
    public NoComment(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(
                false) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
        // Store the "no comment"
        //
        context.getSession().storeNoComment((MessageLocator) parameterArray[0]);
        context.getOut().println(
                context.getMessageFormatter().format("no.comment.saved"));
    }
}