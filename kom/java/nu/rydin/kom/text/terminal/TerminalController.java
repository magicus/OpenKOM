/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.text.terminal;

import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;

public interface TerminalController extends DisplayController
{ 
    public void startOfLine(); 
    
    public void endOfLine();
    
    public void top();
    
    public void bottom();
    
    public void up(int n);
    
    public void down(int n);
    
    public void forward(int n);
    
    public void backward(int n);
    
    public void setCursor(int line, int column);
    
    public void scrollUp(int lines);
    
    public void scrollDown(int lines);
    
    public void setScrollRegion(int start, int end);
    
    public void cancelScrollRegion();
    
    public void eraseToEndOfLine();
    
    public void eraseScreen();
    
    public void eraseLine();
    
    public void reverseVideo();
    
    public void eraseToStartOfLine();
    
    public boolean canStartOfLine();
    
    public boolean canEndOfLine();
    
    public boolean canTop();
    
    public boolean canBottom();
    
    public boolean canUp();
    
    public boolean canDown();
    
    public boolean canForward();
    
    public boolean canBackward();	
        
    public boolean canSetCursor();
    
    public boolean canScrollUp();
    
    public boolean canScrollDown();
    
    public boolean canSetScrollRegion();
    
    public boolean canEraseToEndOfLine();
    
    public boolean canEraseLine();
    
    public boolean canEraseScreen();
    
    public boolean canEraseToStartOfLine();    
    
    public KeystrokeTokenizer getKeystrokeTokenizer();
}
