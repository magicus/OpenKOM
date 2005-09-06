/*
 * Created on Aug 19, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import nu.rydin.kom.exceptions.EscapeException;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.ImmediateShutdownException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.OutputInterruptedException;
import nu.rydin.kom.exceptions.UserException;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.frontend.text.parser.Parser.ExecutableCommand;
import nu.rydin.kom.utils.Logger;

/**
 * @author Pontus Rydin
 */
public class Shell
{
    private final Parser parser;

    public Shell(Parser parser)
    {
        this.parser = parser;
    }

    public void run(Context context, String prompt)
    throws EscapeException
    {
        LineEditor in = context.getIn();
        KOMWriter out = context.getOut();
        for (;;)
        {
            try
            {
                out.print(prompt);
                out.print("> ");
                out.flush();
                String cmdString = null;
                try
                {
                    cmdString = in.readLine("", "", 0, LineEditor.FLAG_ECHO
                            | LineEditor.FLAG_RECORD_HISTORY
                            | LineEditor.FLAG_ALLOW_HISTORY);
                } 
                catch (EventDeliveredException e)
                {
                    // Shouldn't happen
                    //
                    continue;
                }

                if (cmdString.trim().length() > 0)
                {
                    ExecutableCommand executableCommand = parser
                            .parseCommandLine(context, cmdString);
                    executableCommand.execute(context);
                }
            }
            catch (OutputInterruptedException e)
            {
                out.println();
                out.println(e.formatMessage(context));
                out.println();
            } 
            catch (UserException e)
            {
                out.println();
                out.println(e.formatMessage(context));
                out.println();
            } 
            catch(EscapeException e)
            {
                // We're outta here...
                //
                throw e;
            }
            catch (InterruptedException e)
            {
                // SOMEONE SET UP US THE BOMB! Let's get out of here!
                // Can happen if connection is lost, or if an admin
                // requested shutdown.
                //
                return;
            } 
            catch (ImmediateShutdownException e)
            {
                // SOMEONE SET UP US THE *BIG* BOMB!
                //
                return;
            } 
            catch (KOMRuntimeException e)
            {
                out.println(e.formatMessage(context));
                out.println();
                Logger.error(this, e);
            } 
            catch (Exception e)
            {
                e.printStackTrace(out);
                out.println();
                Logger.error(this, e);
            }
        }
    }
}