/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NotMemberException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GotoConference extends AbstractCommand
{
	public GotoConference(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true)});
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, InterruptedException, IOException
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	    ServerSession session = context.getSession();
	    
		long id = nameAssociation.getId();
		try
		{
		    session.gotoConference(id);
		}
		catch(NotMemberException e)
		{
		    // Not members. Ask user if he wants to sign up.
		    //
		    PrintWriter out = context.getOut();
		    LineEditor in = context.getIn();
		    MessageFormatter formatter = context.getMessageFormatter();
		    out.print(formatter.format("goto.conference.signup", nameAssociation.getName().toString()));
		    out.flush();
		    String yes = formatter.format("misc.yes");
		    String answer = in.readLine(yes);
		    if(answer.length() == 0 || !yes.toUpperCase().startsWith(answer.toUpperCase()))
		        throw new OperationInterruptedException();
		    
		    // Try to sign up
		    //
		    session.signup(id);
		    session.gotoConference(id);
		    out.println();
		}
		context.printCurrentConference();
	}
}
