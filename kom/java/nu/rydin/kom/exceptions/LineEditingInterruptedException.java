/*
 * Created on 2004-okt-09
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineEditingInterruptedException extends
        OperationInterruptedException
{
    private String m_partialLine;
    
    public LineEditingInterruptedException(String partialLine)
    {
        m_partialLine = partialLine;
    }
    
    public String getPartialLine()
    {
        return m_partialLine;
    }
}
