/*
 * Created on 2004-09-04
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author Henrik Schröder
 */
public class FilenameParameter extends CommandLineParameter {

    public FilenameParameter(String missingObjectQuestionKey,
            boolean isRequired) {
        super(missingObjectQuestionKey, isRequired);
    }

    public FilenameParameter(boolean isRequired) {
        super("parser.parameter.filename.ask", isRequired);
    }

	public char getSeparator()
	{
		return ' ';
	}
    
    protected String getUserDescriptionKey() {
        return "parser.parameter.filename.description";
    }

    protected Match innerMatch(String matchingPart, String remainder) {
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
