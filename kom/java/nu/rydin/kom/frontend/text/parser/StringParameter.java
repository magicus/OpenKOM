/*
 * Created on Sep 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class StringParameter extends CommandLineParameter
{
    public StringParameter(String missingObjectQuestionKey, boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired, null);
    }
    
    public StringParameter(String missingObjectQuestionKey, boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, isRequired, def);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.string.description";
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
		String cooked = matchingPart.trim();
		if (!"".equals(cooked))
		{
		    return new Match(true, matchingPart, remainder, cooked);
		}
		else
		{
		    return new Match(false, null, null, null);
		}		
    }
}
