/*
 * Created on 2004-09-04
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * FilenameParameter represents a parameter that accepts a filename. A filename
 * is for this purpose defined as a single word.
 * 
 * The "return type" for a FilenameParameter is a String.
 * 
 * @author Henrik Schröder
 */
public class FilenameParameter extends StringParameter 
{
    public FilenameParameter(String missingObjectQuestionKey,
            boolean isRequired) 
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public FilenameParameter(boolean isRequired) 
    {
        super("parser.parameter.filename.ask", isRequired);
    }

	public char getSeparator()
	{
		return ' ';
	}
    
    protected String getUserDescriptionKey() 
    {
        return "parser.parameter.filename.description";
    }
}
