/*
 * Created on Sep 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.SettingKeys;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeUserPresentationConference extends AbstractCommand
{
	public ChangeUserPresentationConference(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) });	
	}

	public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
	    context.getSession().changeSetting(SettingKeys.USER_PRESENTATIONS, 
	            ((NameAssociation) parameters[0]).getId());
    }
}