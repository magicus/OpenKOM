/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Signup extends AbstractCommand
{
	public Signup(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) });	
	}
	
	public void execute2(Context context, Object[] parameterArray) 
	throws KOMException
	{
		long conference = ((NameAssociation)parameterArray[0]).getId();

		// Call backend
		//
		String name = context.getSession().signup(conference);		
		
		// Print confirmation
		//
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("signup.confirmation", context.formatObjectName(name, conference)));
	}	
}
