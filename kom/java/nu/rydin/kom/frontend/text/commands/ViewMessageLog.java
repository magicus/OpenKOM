/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class ViewMessageLog extends AbstractCommand
{
    public ViewMessageLog(String fullName)
    {
        super(fullName);
    }

    public abstract void execute(Context context, String[] parameters)
            throws KOMException, IOException, InterruptedException;
    
    public void innerExecute(Context context, String[] parameters, short kind)
    throws KOMException, IOException, InterruptedException
    {
        
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int num = -1;
		if(parameters.length == 0)
			num = 20;
		else
		{
			try
			{
				num = Integer.parseInt(parameters[0]);
			}
			catch(NumberFormatException e)
			{
				throw new NumberFormatException(formatter.format("view.chat.log.invalid.number"));
			}
			if(num > 1000) // TODO: Read from configuration
			    throw new UserException(formatter.format("view.char.log.overflow"));
		}
		
		// Fetch data
		//
		MessageLogItem[] messages = context.getSession().getMessagesFromLog(kind, num);

		// Print data
		//		
		int top = messages.length;
		for(int idx = top - 1; idx >= 0; --idx)
		{
		    StringBuffer sb = new StringBuffer(top * 100); 
		    MessageLogItem each = messages[idx];
		    
		    // Are we the sender? 
		    //
		    if(each.isSent())
		    {
		        sb.append("> ");
		        NameAssociation[] recs = each.getRecipients();
		        int nRecs = recs.length;
		        for(int recIdx = 0; recIdx < nRecs; ++recIdx)
		        {
		            sb.append(recs[recIdx].getName());
		            if(recIdx < nRecs - 1)
		                sb.append(", ");
		        }
		    }
		    else
		    {
		        sb.append("< ");
		        sb.append(each.getAuthorName());
		    }
		    sb.append(": ");
		    sb.append(each.getBody());
		    
		    // Wordwrap and print
		    //
		    WordWrapper ww = context.getWordWrapper(sb.toString());
		    String line = null;
		    while((line = ww.nextLine()) != null)
		        out.println(line);
		}
    }
    
    public boolean acceptsParameters()
    {
        return true;
    }
}
