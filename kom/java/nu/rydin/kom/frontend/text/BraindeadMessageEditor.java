/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class BraindeadMessageEditor implements MessageEditor
{
    private final Context m_context;
    
    public BraindeadMessageEditor(Context context)
    {
        m_context = context;
    }
	public UnstoredMessage edit(long replyTo)
		throws KOMException, InterruptedException
	{
		PrintWriter out = m_context.getOut();
		LineEditor in = m_context.getIn();
		MessageFormatter formatter = m_context.getMessageFormatter();	
		String oldSubject=null;
		try
		{
			// if this is a reply, retrieve subject from original message
			//
			if(replyTo > 0) {
				oldSubject = m_context.getSession().innerReadMessage(replyTo).getMessage().getSubject();
			}
				
			// Read subject
			//
			out.print(formatter.format("write.message.subject"));
			out.flush();
			String subject = in.readLine(oldSubject);
			
			// Read message body
			//
			out.println(formatter.format("write.message.writetext"));
			StringBuffer sb = new StringBuffer();
			for(;;)
			{
				String line = in.readLine();
				if(line.equals("."))
					break;
				sb.append(line);
				sb.append('\n');
			}
			return new UnstoredMessage(subject, sb.toString());
		}
		catch(IOException e)
		{
			throw new GenericException(formatter.format("error.reading.user.input"));		
		}
	}
}
