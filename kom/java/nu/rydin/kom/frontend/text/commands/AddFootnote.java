/*
 * Created on Jan 9, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.StringParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;

/**
 * @author Pontus Rydin
 */
public class AddFootnote extends AbstractCommand
{
    public AddFootnote(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new TextNumberParameter(
                false), new StringParameter("add.footnote.text.ask", true) }, permissions);
    }
    
    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        ServerSession ss = context.getSession();
        Integer msgNrObj = (Integer) parameters[0];
        long msgId = msgNrObj == null
        	? ss.getCurrentMessage()
        	: ss.localToGlobal(ss.getCurrentConferenceId(), msgNrObj.intValue());
        ss.addMessageAttribute(msgId, MessageAttributes.FOOTNOTE, (String) parameters[1], false);
    }
}
