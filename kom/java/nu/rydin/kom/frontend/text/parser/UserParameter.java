/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 * @author Henrik Schröder
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

    public Object resolveFoundObject(Context context, Match match)
            throws KOMException, IOException, InterruptedException
    {
        return NamePicker.resolveName(match.getParsedObject().toString(),
                NameManager.USER_KIND, context);
    }

    protected boolean isValidName(String name)
    {
        return NameUtils.isValidUserName(name);
    }
    
    protected String getUserDescriptionKey()
    {
        return "parser.parameter.user.description";
    }
}