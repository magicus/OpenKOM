/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.IntegerParameter;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class GenerateTestdata extends AbstractCommand
{
	private static final String s_username = "Flinge J�cht";
	private static final String s_subject = "Flinge J�cht informerar";
	private static final String s_body1 = "Katter �r solit�ra";
	private static final String s_body2 = "Jag har ingen TV";
	
	public GenerateTestdata(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new IntegerParameter(false)});
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		int generate = 20;
		Integer numValue = (Integer) parameterArray[0];
		if (numValue != null) {
		    generate = numValue.intValue();
		}
		ServerSession session = context.getSession();
		for(int idx = 0; idx < generate; ++idx)
		{
			session.storeMessage(new UnstoredMessage(s_subject, s_body1));
			session.storeMessage(new UnstoredMessage(s_subject, s_body2));
		}
	}
}
