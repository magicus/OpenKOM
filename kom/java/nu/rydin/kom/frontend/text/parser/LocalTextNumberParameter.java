/*
 * Created on Sep 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * LocalTextNumberParameter represents a parameter that only accepts a 
 * local textnumber.
 * 
 * The "return type" for a LocalTextNumberParameter is a positive Integer.
 * 
 * @author Henrik Schröder
 */
public class LocalTextNumberParameter extends CommandLineParameter
{
    public LocalTextNumberParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        this(missingObjectQuestionKey, isRequired, null);
    }

    public LocalTextNumberParameter(boolean isRequired)
    {
        this(isRequired, null);
    }
    
    public LocalTextNumberParameter(String missingObjectQuestionKey,
            boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, isRequired, def);
    }

    public LocalTextNumberParameter(boolean isRequired, DefaultStrategy def)
    {
        super("parser.parameter.localtextnumber.ask", isRequired, def);
    }


    protected String getUserDescriptionKey()
    {
        return "parser.parameter.localtextnumber.description";
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String cooked = matchingPart.trim();

        try
        {
            int number = Integer.parseInt(cooked);
            if (number > 0)
            {
                return new Match(true, matchingPart, remainder, new Integer(number));
            }
        } catch (NumberFormatException e)
        {
        }
        return new Match(false, null, null, null);
    }
}