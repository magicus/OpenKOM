/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GotoConference extends AbstractCommand
{
	public GotoConference(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException, InterruptedException
	{
		if(parameters.length == 0)
			throw new MissingArgumentException();
		long id = NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);
		context.getSession().gotoConference(id);
		context.printCurrentConference();
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
