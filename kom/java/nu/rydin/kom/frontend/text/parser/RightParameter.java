/*
 * Created on 2004-aug-31
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author Magnus Ihse
 */
public class RightParameter extends EnumParameter
{

    public RightParameter(String missingObjectQuestionKey, String[] rightLabels, boolean isRequired)
    {
        super(missingObjectQuestionKey, "parser.parameter.right.header", missingObjectQuestionKey, rightLabels, isRequired);
    }

    public RightParameter(boolean isRequired, String[] rightLabels)
    {
        this("parser.parameter.right.ask", rightLabels, isRequired);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.right.description";
    }    
}
