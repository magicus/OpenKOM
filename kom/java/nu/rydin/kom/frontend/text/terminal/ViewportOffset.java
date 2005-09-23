/*
 * Created on Feb 7, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.terminal;

/**
 * @author Pontus Rydin
 */
public class ViewportOffset extends TerminalDecorator
{
    private final int m_colOffs;
    private final int m_lineOffs;
    
    public ViewportOffset(TerminalController underlying, int lineOffs, int colOffs)
    {
        super(underlying);
        m_lineOffs = lineOffs;
        m_colOffs = colOffs;
    }
    
    public void setCursor(int line, int column)
    {
        m_underlying.setCursor(line + m_lineOffs, column + m_colOffs);
    }
    public void setScrollRegion(int start, int end)
    {
        m_underlying.setScrollRegion(start + m_lineOffs, end + m_lineOffs);
    }

}
