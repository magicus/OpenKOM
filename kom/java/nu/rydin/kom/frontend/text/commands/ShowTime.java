/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.util.Date;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowTime extends AbstractCommand
{
	public ShowTime(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] args)
	{
		context.getOut().println(new Date().toString());
	}
}
