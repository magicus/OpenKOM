/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import nu.rydin.kom.InvalidChoiceException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.ClientSettings;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeCharacterset extends AbstractCommand
{
	public ChangeCharacterset(String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}
	
    public void execute(Context context, Object[] parameterArray) throws KOMException, IOException, InterruptedException
	{
		LineEditor in = context.getIn();
		KOMWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		String charSet = out.getCharset();
		try
		{
			StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
			ArrayList list = new ArrayList();
			
			// List supported character sets with a sample string
			//
			while(st.hasMoreTokens())
			{
				String name = st.nextToken();
				list.add(name);
				out.print(list.size());
				out.print(". ");
				PrintUtils.printLeftJustified(out, name, 20);
				out.print(' ');
				out.setCharset(name);
				// out.println(formatter.format("change.charset.samplestring"));
				out.println();
			}
			
			// Pick one!
			//
			out.setCharset(charSet);
			out.print(formatter.format("change.charset.choose", list.size()));
			out.flush();
			String input = in.readLine();
			
			// Return if empty response
			//
			if(input.trim().length() == 0)
				return;
			int idx = 0;
			try
			{
				idx = Integer.parseInt(input);
			}
			catch(NumberFormatException e)
			{
				throw new InvalidChoiceException();
			}
			if(idx < 1 || idx > list.size())
				throw new InvalidChoiceException();
			charSet = (String) list.get(idx - 1);
			
			// Store in user profile
			//
			context.getSession().updateCharacterset(charSet);
		}
		finally
		{
			// Either we got a new charset or we resetting the old one.
			//
			out.setCharset(charSet);
			in.setCharset(charSet);
		}
	}
}
