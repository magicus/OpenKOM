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
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowTime extends AbstractCommand
{
	public ShowTime(MessageFormatter formatter)
	{
		super(formatter);	
	}
	
	public void execute(Context context, String[] args)
	{
		context.getOut().println(new Date().toString());
	}
}
