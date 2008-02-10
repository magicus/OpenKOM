/*
 * Created on Feb 8, 2008
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
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ReadFirstPost extends AbstractCommand
{
    public ReadFirstPost(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(false) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
    throws KOMException
    {
        MessageLocator ml;
        
        // If we were passed a text number, use it..
        //
        if (null != parameterArray[0])
        {
            ml = (MessageLocator)parameterArray[0];
        }
        
        // Otherwise, assume we're operating on the last read message.
        //
        else
        {
            ml = new MessageLocator (context.getSession().getCurrentMessage());
        }
                
        // Call the backend to get the first message in the thread, and read it.
        //
        Envelope env = context.getSession().readMessage(new MessageLocator (context.getSession().getThreadIdForMessage(ml)));
        context.getMessagePrinter().printMessage(context, env);         
    }
}
