/*
 * Created on Feb 7, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.text.terminal;

import nu.rydin.kom.frontend.text.KeystrokeTokenizer;

/**
 * @author Pontus Rydin
 */
public class TerminalDecorator implements TerminalController
{
    protected final TerminalController m_underlying;
    
    public TerminalDecorator(TerminalController underlying)
    {
        m_underlying = underlying;
    }
    
    
    public void backward(int n)
    {
        m_underlying.backward(n);
    }
    public void bottom()
    {
        m_underlying.bottom();
    }
    public void broadcastMessageBody()
    {
        m_underlying.broadcastMessageBody();
    }
    public void broadcastMessageHeader()
    {
        m_underlying.broadcastMessageHeader();
    }
    public boolean canBackward()
    {
        return m_underlying.canBackward();
    }
    public boolean canBottom()
    {
        return m_underlying.canBottom();
    }
    public void cancelScrollRegion()
    {
        m_underlying.cancelScrollRegion();
    }
    public void reverseVideo()
    {
        m_underlying.reverseVideo();
    }
    public boolean canDown()
    {
        return m_underlying.canDown();
    }
    public boolean canEndOfLine()
    {
        return m_underlying.canEndOfLine();
    }
    public boolean canEraseLine()
    {
        return m_underlying.canEraseLine();
    }
    public boolean canEraseScreen()
    {
        return m_underlying.canEraseScreen();
    }
    public boolean canEraseToEndOfLine()
    {
        return m_underlying.canEraseToEndOfLine();
    }
    public boolean canEraseToStartOfLine()
    {
        return m_underlying.canEraseToStartOfLine();
    }
    public boolean canForward()
    {
        return m_underlying.canForward();
    }
    public boolean canScrollDown()
    {
        return m_underlying.canScrollDown();
    }
    public boolean canScrollUp()
    {
        return m_underlying.canScrollUp();
    }
    public boolean canSetCursor()
    {
        return m_underlying.canSetCursor();
    }
    public boolean canSetScrollRegion()
    {
        return m_underlying.canSetScrollRegion();
    }
    public boolean canStartOfLine()
    {
        return m_underlying.canStartOfLine();
    }
    public boolean canTop()
    {
        return m_underlying.canTop();
    }
    public boolean canUp()
    {
        return m_underlying.canUp();
    }
    public void chatMessageBody()
    {
        m_underlying.chatMessageBody();
    }
    public void chatMessageHeader()
    {
        m_underlying.chatMessageHeader();
    }
    public void down(int n)
    {
        m_underlying.down(n);
    }
    public void editorLineNumber()
    {
        m_underlying.editorLineNumber();
    }
    public void endOfLine()
    {
        m_underlying.endOfLine();
    }
    public boolean equals(Object obj)
    {
        return m_underlying.equals(obj);
    }
    public void eraseLine()
    {
        m_underlying.eraseLine();
    }
    public void eraseScreen()
    {
        m_underlying.eraseScreen();
    }
    public void eraseToEndOfLine()
    {
        m_underlying.eraseToEndOfLine();
    }
    public void eraseToStartOfLine()
    {
        m_underlying.eraseToStartOfLine();
    }
    public void forward(int n)
    {
        m_underlying.forward(n);
    }
    public KeystrokeTokenizer getKeystrokeTokenizer()
    {
        return m_underlying.getKeystrokeTokenizer();
    }
    public int hashCode()
    {
        return m_underlying.hashCode();
    }
    public void highlight()
    {
        m_underlying.highlight();
    }
    public void quotedHighlight() 
    {
        m_underlying.quotedHighlight();        
    }
    public void input()
    {
        m_underlying.input();
    }
    public void messageBody()
    {
        m_underlying.messageBody();
    }
    public void messageFooter()
    {
        m_underlying.messageFooter();
    }
    public void messageHeader()
    {
        m_underlying.messageHeader();
    }
    public void messageSubject()
    {
        m_underlying.messageSubject();
    }
    public void normal()
    {
        m_underlying.normal();
    }
    public void output()
    {
        m_underlying.output();
    }
    public void prompt()
    {
        m_underlying.prompt();
    }
    public void quotedMessageBody()
    {
        m_underlying.quotedMessageBody();
    }
    public void reset()
    {
        m_underlying.reset();
    }
    public void scrollDown(int lines)
    {
        m_underlying.scrollDown(lines);
    }
    public void scrollUp(int lines)
    {
        m_underlying.scrollUp(lines);
    }
    public void setCursor(int line, int column)
    {
        m_underlying.setCursor(line, column);
    }
    public void setScrollRegion(int start, int end)
    {
        m_underlying.setScrollRegion(start, end);
    }
    public void startOfLine()
    {
        m_underlying.startOfLine();
    }
    public void top()
    {
        m_underlying.top();
    }
    public String toString()
    {
        return m_underlying.toString();
    }
    public void up(int n)
    {
        m_underlying.up(n);
    }
    
    public void printWithAttributes(String s)
    {
        m_underlying.printWithAttributes(s);
    }

    public void header()
    {
        m_underlying.header();
    }
}
