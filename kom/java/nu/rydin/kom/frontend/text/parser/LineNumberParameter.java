/*
 * Created on 2004-aug-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;

/**
 * @author Magnus Ihse
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineNumberParameter extends IntegerParameter {

    public LineNumberParameter(String missingObjectQuestionKey,
            boolean isRequired) {
        super(missingObjectQuestionKey, isRequired);
    }

    public LineNumberParameter(boolean isRequired) {
        super("parser.parameter.linenumber.ask", isRequired);
    }

    public Object resolveFoundObject(Context context, Match match) {
        assert (context instanceof EditorContext);
        int line = ((Integer) (match.getParsedObject())).intValue();
		Buffer buffer = ((EditorContext) context).getBuffer();
		if(line < 1 || line > buffer.size()) {
		    // FIXME:Ihse: Handle this in a better way
		    return null;
			//throw new BadParameterException();
		}
		return match.getParsedObject();
    }
    
    protected String getUserDescriptionKey() {
        return "parser.parameter.linenumber.description";
    }
}
