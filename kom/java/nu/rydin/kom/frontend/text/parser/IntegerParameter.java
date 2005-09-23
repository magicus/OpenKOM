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
public class IntegerParameter extends CommandLineParameter {

    public IntegerParameter(String missingObjectQuestionKey,
            boolean isRequired) {
        this(missingObjectQuestionKey, isRequired, null);
    }

    public IntegerParameter(boolean isRequired) {
        this(isRequired, null);
    }
    
    public IntegerParameter(String missingObjectQuestionKey,
            boolean isRequired, DefaultStrategy def) {
        super(missingObjectQuestionKey, isRequired, def);
    }

    public IntegerParameter(boolean isRequired, DefaultStrategy def) {
        super("parser.parameter.integer.ask", isRequired, def);
    }
    

    protected String getUserDescriptionKey() {
        return "parser.parameter.integer.description";
    }

    protected Match innerMatch(String matchingPart, String remainder) {
		String cooked = cookString(matchingPart);
		try
		{
			int number = Integer.parseInt(cooked);
			return new Match(true, matchingPart, remainder, new Integer(number));
		}
		catch (NumberFormatException e)
		{
			return new Match(false, null, null, null);
		}
    }
}
