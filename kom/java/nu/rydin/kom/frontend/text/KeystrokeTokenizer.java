/*
 * Created on Jul 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KeystrokeTokenizer implements Cloneable
{
    public static class Token
    {
        private final int m_kind;
        
        private final char m_char;
        
        public Token(int kind, char ch)
        {
            m_kind = kind;
            m_char = ch;
        }
        
        public int getKind()
        {
            return m_kind;
        }
        
        public char getChar()
        {
            return m_char;
        }
    }
    private final KeystrokeTokenizerDefinition.State m_root;
    
    private KeystrokeTokenizerDefinition.State m_current;
    
    public KeystrokeTokenizer(KeystrokeTokenizerDefinition.State root) 
    {
        m_root = root;
        m_current = root;
    }
    
    public KeystrokeTokenizer deepCopy()
    {
        return new KeystrokeTokenizer(m_root.deepCopy());
    }
    
    public Token feedCharacter(char ch)
    {
        KeystrokeTokenizerDefinition.State state = m_current.getNextState(ch);
        if(state == null)
        {
            m_current = m_root;
            return new Token(KeystrokeTokenizerDefinition.LITERAL, ch);
        }
        if(!state.isTerminal())
        {
            m_current = state;
            return null;
        }
        m_current = m_root;
        return new Token(state.getKind(), ch);
    }
    
    public KeystrokeTokenizerDefinition getDefinition()
    {
        return new KeystrokeTokenizerDefinition(m_root);
    }
}
