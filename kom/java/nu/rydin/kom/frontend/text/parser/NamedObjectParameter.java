/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.InvalidChoiceException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;


/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class NamedObjectParameter extends CommandLineParameter
{
	public NamedObjectParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		super(missingObjectQuestionKey, isRequired);
	}

	public NamedObjectParameter(boolean isRequired)
	{
		super("parser.parameter.namedobject.ask", isRequired);
	}

	protected Match innerMatch(String matchingPart, String remainder)
	{
		String cooked = cookString(matchingPart);
		
		if (cooked.length() > 0) {
			// well, this _could_ be a name... check it later
			return new Match(true, matchingPart, remainder, cooked);
		} else {
			return new Match(false, null, null, null);
		}
	}

	public Object resolveFoundObject(Context context, Match match) throws IOException, InterruptedException
	{
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		
		try {
			return innerResolve(context, match); 
		}
		catch (UnexpectedException e)
		{
			out.println(fmt.format("nu.rydin.kom.UnexpectedException.format"));
			out.flush();
			return null;
		}
		catch (OperationInterruptedException e)
		{
			out.println(fmt.format("nu.rydin.kom.OperationInterruptedException.format"));
			out.flush();
			return null;
		}
		catch (InvalidChoiceException e)
		{
			out.println(fmt.format("nu.rydin.kom.InvalidChoiceException.format"));
			out.flush();
			return null;
		}	    
	}

    protected int getSeparatorPos(String commandLine) 
    {
        int separatorPos = -1;
        boolean ignoreMode = false;
        char separator = getSeparator();
        char[] cla = commandLine.toCharArray();
        for (int i = 0; i < cla.length; i++) {
            if ((cla[i] == separator) && !ignoreMode)
            {
                separatorPos = i;
            }
            else if (cla[i] == '(')
            {
                ignoreMode = true;
            }
            else if (cla[i] == ')')
            {
                ignoreMode = false;
            }
        }
        return separatorPos;
    }
	
	protected Object innerResolve(Context context, Match match) throws UnexpectedException, IOException, InterruptedException, OperationInterruptedException, InvalidChoiceException
	{
		try
		{
			NameAssociation assoc = NamePicker.resolveName(match.getParsedObject().toString(), NameManager.UNKNOWN_KIND, context);
			return assoc;
		}
		catch (ObjectNotFoundException e)
		{
			PrintWriter out = context.getOut();
			MessageFormatter fmt = context.getMessageFormatter();

			out.println(fmt.format("parser.parameter.namedobject.notfound", match.getMatchedString()));
			out.flush();
			return null;
		}
	}

    protected String getUserDescriptionKey() {
        // TODO Auto-generated method stub
        return "parser.parameter.namedobject.description";
    }
}
