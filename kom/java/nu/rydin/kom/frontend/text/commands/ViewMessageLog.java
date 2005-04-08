/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.IntegerParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class ViewMessageLog extends AbstractCommand
{
    public ViewMessageLog(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new IntegerParameter(false) }, permissions);
    }

    public abstract void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException;
    
    public void innerExecute(Context context, Object[] parameterArray)
    throws KOMException
    {
        
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        int num = -1;
		if(parameterArray[0] == null)
			num = 10;
		else
		{
		    num = ((Integer)parameterArray[0]).intValue();
		    // FIXME review limit should not be hardcoded. (Should it even exist?)
			if(num > 1000)
			{
			    throw new GenericException(formatter.format("view.chat.log.overflow"));
			}
		}

		//FIXME Fetch data in chunks.
		// Fetch data
		//
		MessageLogItem[] messages = this.fetch(context.getSession(), num);

		// Print data
		//		
		int top = messages.length;
		for(int idx = top - 1; idx >= 0; --idx)
		{
		    StringBuffer sb = new StringBuffer(top * 100); 
		    MessageLogItem each = messages[idx];
		    short kind = each.getKind();
		    
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
		        if(kind == MessageLogKinds.CHAT)
		            sb.append("< ");
		        sb.append(each.getAuthorName());
		    }
		    if(kind != MessageLogKinds.CONDENSED_BROADCAST)
		        sb.append(':');
		    sb.append(' ');
		    sb.append(each.getBody());
		    
		    // Wordwrap and print
		    //
		    WordWrapper ww = context.getWordWrapper(sb.toString());
		    String line = null;
		    while((line = ww.nextLine()) != null)
		        out.println(line);
		}
    }
    
    protected abstract MessageLogItem[] fetch(ServerSession session, int num)
    throws KOMException;
} 
