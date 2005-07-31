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
 * ConferenceParameter represents a parameter that only accepts (shortened) conference names.
 * 
 * The "return type" for a ConferenceParameter is a NameAssociation object.
 * 
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 */
public class ConferenceParameter extends NamedObjectParameter
{

    public ConferenceParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public ConferenceParameter(boolean isRequired)
    {
        super("parser.parameter.conference.ask", isRequired);
    }

    public Object resolveFoundObject(Context context, Match match)
            throws KOMException, IOException, InterruptedException
    {
        return NamePicker.resolveName(match.getParsedObject().toString(),
                NameManager.CONFERENCE_KIND, context);
    }

    protected boolean isValidName(String name)
    {
        return NameUtils.isValidConferenceName(name);
    }
    
    protected String getUserDescriptionKey()
    {
        return "parser.parameter.conference.description";
    }
}