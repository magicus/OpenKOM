/*
 * Created on 2004-aug-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Magnus Ihse
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FlagParameter extends CommandLineParameter {

    public FlagParameter(String missingObjectQuestionKey, boolean isRequired) {
        super(missingObjectQuestionKey, isRequired);
    }

    public FlagParameter(boolean isRequired) {
        super("parser.parameter.flag.ask", isRequired);
    }

    protected String getUserDescriptionKey() {
        return "parser.parameter.flag.description";
    }

    protected Match innerMatch(String matchingPart, String remainder) {
		String cooked = cookString(matchingPart);
		
		if (cooked.length() > 0) {
			// well, this _could_ be a flag... check it later
			return new Match(true, matchingPart, remainder, cooked);
		} else {
			return new Match(false, null, null, null);
		}
    }

    public Object resolveFoundObject(Context context, Match match) throws KOMException {
		String[] flagLabels = context.getFlagLabels();
		String flagName = match.getMatchedString();
		int imatch = -1;
		int top = flagLabels.length;
		for(int idx = 0; idx < top; ++idx)
		{
			if(flagLabels[idx] != null && NameUtils.match(flagName, flagLabels[idx]))
			{
				// Ambigous?
				//
				if(imatch != -1) {
				    // FIXME:Ihse: We should resolve the ambiguity instead!
				    throw new AmbiguousNameException();
				}
				imatch = idx;
			}
		}
		if(imatch == -1)
		{
		    throw new ObjectNotFoundException(context.getMessageFormatter().format("manipulate.flag.nonexistent", flagName));
		}        
        
		return new Integer(imatch);
    }
}
