/*
 * Created on 2004-aug-19
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;


/**
 * @author Magnus Ihse Bursie
 */
public class CharacterSetParameter extends EnumParameter
{
    public CharacterSetParameter(String missingObjectQuestionKey, String[] flagLabels, boolean isRequired)
    {
        super(missingObjectQuestionKey, "parser.parameter.charset.header", missingObjectQuestionKey, flagLabels, false, "charset", isRequired);
    }

    public CharacterSetParameter(boolean isRequired, String[] flagLabels)
    {
        this("parser.parameter.charset.ask", flagLabels, isRequired);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.charset.description";
    }
}