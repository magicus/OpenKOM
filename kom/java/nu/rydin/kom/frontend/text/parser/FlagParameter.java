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
public class FlagParameter extends EnumParameter
{
    public FlagParameter(String missingObjectQuestionKey, String[] flagLabels, boolean isRequired)
    {
        super(missingObjectQuestionKey, "parser.parameter.flag.header", missingObjectQuestionKey, flagLabels, false, null, isRequired);
    }

    public FlagParameter(boolean isRequired, String[] flagLabels)
    {
        this("parser.parameter.flag.ask", flagLabels, isRequired);
    }
    
    public FlagParameter(String missingObjectQuestionKey, String[] flagLabels, boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, "parser.parameter.flag.header", missingObjectQuestionKey, flagLabels, false, null, isRequired, def);
    }

    public FlagParameter(boolean isRequired, String[] flagLabels, DefaultStrategy def)
    {
        this("parser.parameter.flag.ask", flagLabels, isRequired, def);
    }


    protected String getUserDescriptionKey()
    {
        return "parser.parameter.flag.description";
    }
}