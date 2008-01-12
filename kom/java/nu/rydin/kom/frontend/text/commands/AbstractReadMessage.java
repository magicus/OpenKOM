/*
 * Created on Jan 11, 2008
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:magnus.neck@abc.se>Magnus Neck</a>
 */
public abstract class AbstractReadMessage extends AbstractCommand
{
	private final String m_noMessageErrorMessageResource;

    public AbstractReadMessage(String fullName, CommandLineParameter[] signature, long permissions, String noMessageErrorMessageResource)
	{
		super(fullName, signature, permissions);
		m_noMessageErrorMessageResource = noMessageErrorMessageResource;
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
	    MessageLocator textNum = getMessageToRead(context, parameterArray);
		
	    if (textNum == MessageLocator.NO_MESSAGE)
	    {
	        context.getOut().println(context.getMessageFormatter().format(m_noMessageErrorMessageResource));
	    }
	    else {
	        try
	        {
	            // Retrieve message
	            //
	            Envelope env = context.getSession().readMessage(textNum);

	            context.getMessagePrinter().printMessage(context, env);			
	        }
	        catch(ObjectNotFoundException e)
	        {
	            throw new GenericException(context.getMessageFormatter().format("read.message.not.found"));
	        }
	    }
	}

    protected abstract MessageLocator getMessageToRead(Context context, Object[] parameterArray);

}
