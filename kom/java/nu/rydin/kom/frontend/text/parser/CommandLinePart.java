/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.exceptions.InvalidChoiceException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 */
public abstract class CommandLinePart
{
	public abstract boolean isRequired();
	
	public abstract char getSeparator();
	
	protected abstract Match innerMatch(String matchingPart, String remainder);

	/**
	 * Note: commandLine must not be NULL.
	 * 
	 * @param commandLine 
	 * @return
	 */
	public Match match(String commandLine)
	{
		String matchingPart;
		String remainder;
		Match result = null;
		
		// Trim leading whitespace
		while (commandLine.length() > 0 && Character.isWhitespace(commandLine.charAt(0))) {
			commandLine = commandLine.substring(1);
		}
		
		if (commandLine.length() == 0) {
			result = new Match(false, null, null, null);
		}
		else
		{
			int separatorPos = getSeparatorPos(commandLine); 
	
			if (separatorPos == -1) {
				matchingPart = commandLine;
				remainder = "";
			} else {
				matchingPart = commandLine.substring(0, separatorPos);
				remainder = commandLine.substring(separatorPos + 1, commandLine.length());
			}
			result = innerMatch(matchingPart, remainder);
		}
		if (!result.isMatching() && !isRequired())
		{
		    //If we couldn't get a match, but this parameter was not required, 
		    //we should return a Match object that says it matched, but with
		    //the original commandline intact as the remainder.
		    return new Match(true, "", commandLine, null);
		}
		else
		{
		    return result;
		}
	}

    protected int getSeparatorPos(String commandLine) 
    {
        int top = commandLine.length();
        boolean quoted = false;
        char separator = this.getSeparator();
        for(int idx = 0; idx < top; ++idx)
        {
            char each = commandLine.charAt(idx);
            if(each == '\'')
           	    quoted = !quoted;
            else if(each == separator && !quoted)
       	        return idx;
        }
        return -1;
    }

    public abstract Match fillInMissingObject(Context context) 
    throws IOException, InterruptedException, OperationInterruptedException, InvalidChoiceException;
	
	public Object resolveFoundObject(Context context, Match match) 
	throws IOException, InterruptedException, KOMException
	{
		return match.getParsedObject();
	}

	public static String cookString(String matchingPart)
	{
		String name = NameUtils.normalizeName(matchingPart);
		int top = name.length();
		if(top == 0)
		    return "";
		return top > 1 && name.charAt(0)== '\'' && name.charAt(top - 1) == '\''
		    ? name.substring(1, top - 1)
		    : name;
	}
}
