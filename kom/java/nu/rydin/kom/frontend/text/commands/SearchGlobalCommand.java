/*
 * Created on Sep 11, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.structs.GlobalMessageSearchResult;

/**
 * @author Henrik Schröder
 */
public abstract class SearchGlobalCommand extends SearchCommand
{
    
    public SearchGlobalCommand(String fullName, CommandLineParameter[] signature, long permissions)
    {
        super(fullName, signature, permissions);
    }
    
    protected void preparePrinting(Context context) throws KOMException  
    {
        m_resultPrinter = MessageSearchResultPrinterFactory.createMessageSearchResultPrinter(context, GlobalMessageSearchResult.class);
    }
    
}
