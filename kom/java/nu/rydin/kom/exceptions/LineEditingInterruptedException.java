/*
 * Created on 2004-okt-09
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse
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
