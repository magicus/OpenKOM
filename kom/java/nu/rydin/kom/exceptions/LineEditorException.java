/*
 * Created on Oct 3, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class LineEditorException extends SystemException
{
    static final long serialVersionUID = 2005;
    
    private final String m_line;
    private final int m_pos;
    
    public LineEditorException(String line, int pos)
    {
        m_line = line;
        m_pos = pos;
    }
    
    public String getLine()
    {
        return m_line;
    }
    
    public int getPos()
    {
        return m_pos;
    }
}
