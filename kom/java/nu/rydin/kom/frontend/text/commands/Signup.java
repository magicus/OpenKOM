/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;
import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceWildcardParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class Signup extends AbstractCommand
{
	public Signup(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceWildcardParameter(true) }, permissions);	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException
	{
        // By public demand, a shortcut to join every joinable conference
        //
        if (-1 == ((NameAssociation)parameterArray[0]).getId())  //.equals(parameterArray[0].toString()))
        {
            MessageFormatter mf = context.getMessageFormatter();
            PrintWriter out = context.getOut();
            LineEditor in = context.getIn();
            int choice = in.getChoice(mf.format("signup.allconfs.verify") +
                                        " (" +
                                        mf.format("misc.y") +
                                        "/" + 
                                        mf.format("misc.n") +
                                        ")? ",
                                      new String[] { mf.format("misc.n"), 
                                                     mf.format("misc.y") },
                                      0,
                                      mf.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
            if (1 == choice)
            {
                int cnt = context.getSession().signupForAllConferences();
                out.println (mf.format ("signup.allconfs.result", cnt));
                return;
            }
            else
            {
                throw new OperationInterruptedException();
            }
        }
        
		long conference = ((NameAssociation)parameterArray[0]).getId();

		// Call backend
		//
		Name name = context.getSession().signup(conference);		
		
		// Print confirmation
		//
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("signup.confirmation", context.formatObjectName(name, conference)));
	}	
}
