/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class BraindeadMessageEditor implements MessageEditor
{
	public UnstoredMessage edit(Context context, long replyTo)
		throws KOMException, InterruptedException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter formatter = context.getMessageFormatter();			
		try
		{
			// Read subject
			//
			out.print(formatter.format("write.message.subject"));
			out.flush();
			String subject = in.readLine();
			
			// Read message body
			//
			out.println(formatter.format("write.message.writetext"));
			StringBuffer sb = new StringBuffer();
			for(;;)
			{
				String line = in.readLine();
				if(line.length() == 0)
					break;
				sb.append(line);
				sb.append('\n');
			}
			return new UnstoredMessage(subject, sb.toString());
		}
		catch(IOException e)
		{
			throw new KOMException(formatter.format("error.reading.user.input"), e);		
		}
	}
}
