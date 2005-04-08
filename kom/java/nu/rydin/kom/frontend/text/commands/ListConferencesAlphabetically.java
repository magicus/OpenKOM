/*
 * Created on Sep 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListConferencesAlphabetically extends AbstractCommand
{

	public ListConferencesAlphabetically(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}

	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		ListConferences.printConferenceList(context, context.getSession().listConferencesByName());
	}
}

