/*
 * Created on Jul 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KeystrokeTokenizerDefinition 
{
    public static final int NON_TERMINAL 	= 1;
    public static final int LITERAL 		= 2;
    
    public static class State
    {
        private final int m_tokenKind;
        
        private Map m_nextStates = new HashMap();
        
        public State()
        {
            m_tokenKind = NON_TERMINAL;
        }
        
        public State(int tokenKind)
        {
            m_tokenKind = tokenKind;
        }
        
        public void addState(char ch, State state)
        {
            m_nextStates.put(new Character(ch), state);
        }
        
        public boolean isTerminal()
        {
            return m_tokenKind != NON_TERMINAL;
        }
        
        public int getKind()
        {
            return m_tokenKind;
        }
        
        public State getNextState(char ch)
        {
            return (State) m_nextStates.get(new Character(ch));
        }
    }
    
    private State m_rootState = new State();
    
    public KeystrokeTokenizerDefinition(String[] patterns, int[] kinds)
    throws AmbiguousPatternException
    {
        int top = patterns.length;
        for(int idx = 0; idx < top; ++idx)
            this.addPattern(patterns[idx], kinds[idx]);
    }
    
    public void addPattern(String pattern, int kind)
    throws AmbiguousPatternException
    {
        int top = pattern.length();
        State state = m_rootState;
        for(int idx = 0; idx < top; ++idx)
        {
            char ch = pattern.charAt(idx);
            State next = state.getNextState(ch);
            
            // Did we get a state here?
            //
            if(next != null)
            {
                // We're at end but there's already a state here? Amboguous!
                //
                if(idx == top - 1)
                    throw new AmbiguousPatternException(pattern);
            }
            else
            {
                // No state here! Create a new one.
                //
                next = idx == top - 1
                	? new State(kind)
                	: new State();
                state.addState(ch, next);
            }
            state = next;
        }
    }
    
    public KeystrokeTokenizer createKeystrokeTokenizer()
    {
        return new KeystrokeTokenizer(m_rootState);
    }
}
