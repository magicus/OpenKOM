/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GenerateTestdata extends AbstractCommand
{
	private static final String s_username = "Flinge Jücht";
	private static final String s_subject = "Flinge Jücht informerar";
	private static final String s_body1 = "Katter är solitära";
	private static final String s_body2 = "Jag har ingen TV";
	
	public GenerateTestdata(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException
	{
		int generate = 20;
		if (parameters.length != 0)
		{
			try
			{
				generate = Integer.parseInt(parameters[0]);
			}
			catch (Exception e)
			{
				// Nope.
			}
		}
		ServerSession session = context.getSession();
		for(int idx = 0; idx < generate; ++idx)
		{
			session.storeMessage(new UnstoredMessage(s_subject, s_body1));
			session.storeMessage(new UnstoredMessage(s_subject, s_body2));
		}
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
