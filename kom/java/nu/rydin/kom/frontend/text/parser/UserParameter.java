/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.InvalidChoiceException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class UserParameter extends NamedObjectParameter
{

	public UserParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		super(missingObjectQuestionKey, isRequired);
	}

	public UserParameter(boolean isRequired)
	{
		super("parser.parameter.user.ask", isRequired);
	}

	public Object resolveFoundObject(Context context, Match match) throws KOMException, IOException, InterruptedException
	{
		return NamePicker.resolveName(match.getParsedObject().toString(), NameManager.USER_KIND, context);
	}

    protected String getUserDescriptionKey() {
        return "parser.parameter.user.description";
    }
}
