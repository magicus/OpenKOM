/*
 * Created on 2004-aug-08
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author Magnus Ihse
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RawParameter extends CommandLineParameter {

    public RawParameter(String missingObjectQuestionKey, boolean isRequired) {
        super(missingObjectQuestionKey, isRequired);
    }

    protected Match innerMatch(String matchingPart, String remainder) {
        // We'll always match. We're RAW. Raaaaw-hiiiide.
        return new Match(true, matchingPart, remainder, matchingPart);
    }

    public char getSeparator() {
        // Okay, this is a bit ugly. But since we'll never find this in the
        // string,
        // match() will call innerMatch with the whole rest of the string as
        // matchingPart,
        // which is exactly what we want.
        return '\0';
    }
}