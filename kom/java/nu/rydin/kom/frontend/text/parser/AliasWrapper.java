/*
 * Created on Sep 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AliasWrapper extends AbstractCommand
{
    private final String m_actualCommand;
    
    public AliasWrapper(String fullName, String actualCommand)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter(null, false) }, 0L);
        m_actualCommand = actualCommand;
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        // Build command line from wrapped command and parameters.
        //
        // TODO: Next version: If alias is given with a partial set of
        // parameters, the parameter string should be prepended with a
        // comma. This requires some refactoring of the parser.
        //
        String commandLine = m_actualCommand;
        if(parameters[0] != null)
            commandLine += " " + (String) parameters[0];
        Parser.ExecutableCommand cmd = context.getParser().parseCommandLine(context, commandLine);
        long rp = cmd.getCommand().getRequiredPermissions(); 
        if((rp & context.getCachedUserInfo().getRights()) != rp)
            throw new AuthorizationException();
        cmd.executeBatch(context);
    }
    
    public long getRequiredPermissions()
    {
        // Since we don't know the parameters here, we can't infer the command
        // class. Permissions are enfored by the execute method instead.
        //
        return 0L;
    }
}
