/*
 * Created on Oct 19, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadMessage extends AbstractReadMessage
{
	public ReadMessage(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(true), new ConferenceParameter(false)}, permissions, null);	
	}
	
    protected MessageLocator getMessageToRead(Context context, Object[] parameterArray)
    {
        MessageLocator textNum = (MessageLocator) parameterArray[0];
	    if(parameterArray.length > 1 && parameterArray[1] != null)
            textNum.setConference(((NameAssociation) parameterArray[1]).getId());
        return textNum;
    }
}
