/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.util.ArrayList;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.Logger;

/**
 * EllipsisParameter represents a parameter that only accepts a list of it's 
 * wrapped type.
 * 
 * The "return type" for a EllipsisParameter is an Object[] of the "return type"
 * of the wrapped type.
 * 
 * @author Henrik Schröder
 * @author <a href="mailto:pontus@rydin.nu">Pontus Rydin</a>
 */
public class EllipsisParameter extends CommandLineParameter
{
    private final CommandLineParameter innerParameter;
    
    private final char separator;
    
    public EllipsisParameter(String missingObjectQuestionKey, boolean isRequired, CommandLineParameter innerParameter)
    {
        this(missingObjectQuestionKey, isRequired, innerParameter, ':');
    }
    
    public EllipsisParameter(String missingObjectQuestionKey, boolean isRequired, CommandLineParameter innerParameter, char separator)
    {
        super(missingObjectQuestionKey, isRequired);
        this.separator = separator;
        this.innerParameter = innerParameter;
        if (!innerParameter.isRequired())
        {
            //Uh-oh, this will cause an infinite loop on parsing. Die violently.
            Logger.fatal(this, "Ellipses CANNOT contain optional parameters!");
            throw new KOMRuntimeException("Ellipses CANNOT contain optional parameters!");
        }        
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String innerCommandLine = matchingPart;
        
        ArrayList innerMatches = new ArrayList();
        Match innerMatch;
        
        // As long as the last innerCommandLine matched, keep trying.
        do
        {
            innerMatch = innerParameter.match(innerCommandLine);
            if (innerMatch.isMatching())
            {
                innerMatches.add(innerMatch);
                innerCommandLine = innerMatch.getRemainder();
            }
        }
        while (innerMatch.isMatching());
        
        // If we got any inner matches, return a true match with an array of the
        // inner parsed objects as its parsed object.
        // If not, return a false match.
        if (innerMatches.size() > 0)
        {
            Object[] parsedObjects = new Object[innerMatches.size()];
            for (int i = 0; i < innerMatches.size(); i++)
            {
                parsedObjects[i] = ((Match)innerMatches.get(i)).getParsedObject();
            }
            return new Match(true, matchingPart, remainder, parsedObjects);
        }
        else
        {
            return new Match(false, null, null, null);
        }
    }
        
    public Object resolveFoundObject(Context context, Match match)
    throws KOMException, IOException, InterruptedException
    {
        Object[] parsedObjects = (Object[])match.getParsedObject();
        Object[] result = new Object[parsedObjects.length];
        for (int i = 0; i < parsedObjects.length; i++)
        {
            String parsed = (String) parsedObjects[i];
            result[i] = this.innerParameter.resolveFoundObject(context, new Match(true, parsed, "", parsed)); 
        }
        return result;
    }
	
    public String getUserDescription(Context context) 
    {
		MessageFormatter fmt = context.getMessageFormatter();
		String result = fmt.format(innerParameter.getUserDescriptionKey()) + innerParameter.getSeparator() + " ...";
        if (isRequired()) 
            return "<" + result + ">";
        else
            return "[" + result + "]";
    }
    
    protected String getUserDescriptionKey()
    {
        return "parser.parameter.ellipsis.description";
    }
    
    public char getSeparator()
    {
        return this.separator;
    }
}
