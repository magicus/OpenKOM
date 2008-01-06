/*
 * Created on Jul 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.rydin.kom.exceptions.AmbiguousPatternException;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KeystrokeTokenizerDefinition implements Cloneable
{
    public static final int NON_TERMINAL 	= 1;
    public static final int LITERAL 		= 2;
    
    public static class State
    {
        private final int m_tokenKind;
        
        private Map<Character, State> m_nextStates = new HashMap<Character, State>();
        
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
        
        public State deepCopy()
        {
            State copy = new State(m_tokenKind);
            HashMap<Character, State> map = new HashMap<Character, State>(m_nextStates.size());
            for (Entry<Character, State> each : m_nextStates.entrySet())
            {
                map.put(each.getKey(), each.getValue().deepCopy());
            }
            copy.m_nextStates = map;
            return copy;
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
    
    public KeystrokeTokenizerDefinition(State root)
    {
        m_rootState = root;
    }
    
    public KeystrokeTokenizerDefinition deepCopy()
    {
        try
        {
            KeystrokeTokenizerDefinition copy = (KeystrokeTokenizerDefinition) this.clone();
            copy.m_rootState = m_rootState.deepCopy();
            return copy;
        }
        catch(CloneNotSupportedException e)
        {
            // Yes it is! (Getting here means things are screwed up like crazy!)
            //
            throw new RuntimeException(e);
        }
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
                // We're at end but there's already a state here? Ambiguous!
                //
                if(idx == top - 1)
                    throw new AmbiguousPatternException(pattern);
            }
            else
            {
                // No state here! Create a new one.
                //
                next = idx == top - 1
                	? new State(kind)  	// Terminal
                	: new State();		// Non-terminal
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
