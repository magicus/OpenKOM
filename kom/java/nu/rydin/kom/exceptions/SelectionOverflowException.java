/*
 * Created on Jan 16, 2005
 */
package nu.rydin.kom.exceptions;

/**
 * @author Pontus Rydin
 */
public class SelectionOverflowException extends UserException
{
    static final long serialVersionUID = 2005;
    
    private long[] partialSelection; 
    
    public SelectionOverflowException(long[] partialSelection)
    {
        this.partialSelection = partialSelection;
    }

    public long[] getPartialSelection()
    {
        return this.partialSelection;
    }
}
