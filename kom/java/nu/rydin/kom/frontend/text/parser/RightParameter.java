/*
 * Created on 2004-aug-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author Magnus Ihse
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
