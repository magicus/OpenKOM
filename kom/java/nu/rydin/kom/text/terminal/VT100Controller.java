/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.text.terminal;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.ANSIDisplayController;
import nu.rydin.kom.frontend.text.AmbiguousPatternException;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;
import nu.rydin.kom.frontend.text.KeystrokeTokenizerDefinition;
import nu.rydin.kom.frontend.text.constants.Keystrokes;

/**
 * @author Pontus Rydin
 */
public class VT100Controller extends ANSITerminalController
{
	
    public VT100Controller(PrintWriter out)
    {
        super(out);
    }
    
    public void setScrollRegion(int start, int end)
    {
        this.printPreamble();
        m_writer.print(start + 1);
        m_writer.print(';');
        m_writer.print(end + 1);
        m_writer.print('r');
    }
    
    public boolean canSetScrollRegion()
    {
        return true;
    }    
}
