/*
 * Created on Aug 24, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.simple.AbstractEditor;
import nu.rydin.kom.frontend.text.editor.simple.SimpleChatEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik Schröder
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SendBroadcastChatMessage extends AbstractCommand
{

    public SendBroadcastChatMessage(Context context, String fullName,
            long permissions)
    {
        super(fullName, new CommandLineParameter[]
        { new RawParameter("chat.saytoall", false) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {

        // Set up
        //
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        ServerSession session = context.getSession();
        MessageFormatter formatter = context.getMessageFormatter();

        String message;

        if (parameterArray[0] == null)
        {
            //No message given, use chat message editor.
            dc.normal();
            out.println(context.getMessageFormatter().format("chat.saytoall"));
            out.flush();

            // Update state
            //
            session.setActivity(Activities.BROADCAST, true);
                        
            // Read message
            //
            AbstractEditor editor = new SimpleChatEditor(context);
            try
            {
                message = editor.edit().getBody();
            }
            finally
            {
                session.restoreState();
            }
        } 
        else
        {
            message = (String) parameterArray[0];
        }

        // Empty message? User interrupted
        //
        if (message.length() == 0)
            return;

        if (0 != (session.getLoggedInUser().getFlags1() & UserFlags.CONFIRM_BROADCAST_MESSAGES))
        {
            LineEditor in = context.getIn();
            int choice = in.getChoice(formatter.format("chat.confirm") +
                    " (" +
                    formatter.format("misc.y") +
                    "/" + 
                    formatter.format("misc.n") +
                    ")? ",
                    new String[] { formatter.format("misc.n"), 
                                   formatter.format("misc.y") },
                    -1,
                    formatter.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
            
            if (1 != choice)
            {
                out.print (formatter.format("chat.fulhack.raw.ask") + " ");
                out.flush();
                String recv = in.readLine();
                NameAssociation na = NamePicker.resolveName(recv, (short)-1, context);
                session.sendMulticastMessage(new long[] { na.getId() }, message, false);
                return;
            }            
        }
        
        // Send it
        //
        NameAssociation[] refused = session.broadcastChatMessage(message,
                MessageLogKinds.BROADCAST);

        // Print refused destinations (if any)
        //
        int top = refused.length;
        if (top > 0)
        {
            // Build message
            //
            StringBuffer sb = new StringBuffer(200);
            sb.append(formatter.format("chat.refused"));
            for (int idx = 0; idx < top; ++idx)
            {
                sb.append(refused[idx].getName());
                if (idx < top - 1)
                    sb.append(", ");
            }

            // Wordwrap it!
            //
            out.println();
            WordWrapper ww = context.getWordWrapper(sb.toString());
            String line = null;
            while ((line = ww.nextLine()) != null)
                out.println(line);
        }
    }
}