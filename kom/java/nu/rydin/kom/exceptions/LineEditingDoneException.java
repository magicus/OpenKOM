/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.frontend.text.KeystrokeTokenizer;

/**
 * @author Pontus Rydin
 */
public class LineEditingDoneException extends LineEditorException
{
    static final long serialVersionUID = 2005;
    
    private final KeystrokeTokenizer.Token m_token;

    public LineEditingDoneException(KeystrokeTokenizer.Token token, String line, int position)
    {
        super(line, position);
        m_token = token;
    }
    
    public KeystrokeTokenizer.Token getToken()
    {
        return m_token;
    }    
}
