/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GotoConference extends AbstractCommand
{
	public GotoConference(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true)});
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	    
		long id = nameAssociation.getId();
		context.getSession().gotoConference(id);
		context.printCurrentConference();
	}
}
