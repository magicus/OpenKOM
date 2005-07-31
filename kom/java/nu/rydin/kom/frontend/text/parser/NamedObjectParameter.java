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
 * NamedObjectParameter represents a parameter that only accepts (shortened) object names.
 * 
 * The "return type" for a NamedObjectParameter is a NameAssociation object.
 * 
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 * @author Henrik Schröder
 */
public class NamedObjectParameter extends CommandLineParameter
{
    public NamedObjectParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public NamedObjectParameter(boolean isRequired)
    {
        super("parser.parameter.namedobject.ask", isRequired);
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        //Sanity-check.
        if (!this.isValidName(matchingPart.trim()))
        {
            return new Match(false, null, null, null);
        }
        
        String cooked = cookString(matchingPart);

        if (cooked.length() > 0)
        {
            // well, this _could_ be a name... check it later
            return new Match(true, matchingPart, remainder, cooked);
        }

        return new Match(false, null, null, null);
    }

    protected boolean isValidName(String name)
    {
        return NameUtils.isValidName(name);
    }

    public Object resolveFoundObject(Context context, Match match)
            throws KOMException, IOException, InterruptedException
    {
        return NamePicker.resolveName(match.getParsedObject().toString(),
                NameManager.UNKNOWN_KIND, context);
    }

    protected int getSeparatorPos(String commandLine)
    {
        int separatorPos = -1;
        boolean ignoreMode = false;
        char separator = getSeparator();
        char[] cla = commandLine.toCharArray();
        for (int i = 0; i < cla.length; i++)
        {
            if ((cla[i] == separator) && !ignoreMode)
            {
                separatorPos = i;
                break;
            } else if (cla[i] == '(')
            {
                ignoreMode = true;
            } else if (cla[i] == ')')
            {
                ignoreMode = false;
            }
        }
        return separatorPos;
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.namedobject.description";
    }
}