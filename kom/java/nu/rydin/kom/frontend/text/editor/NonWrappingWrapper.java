/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NonWrappingWrapper implements WordWrapper
{
    private final BufferedReader m_reader;
    
    public NonWrappingWrapper(String content)
    {
        m_reader = new BufferedReader(new StringReader(content));
    }

    public String nextLine()
    {
        try
        {
            String answer = m_reader.readLine();
            if(answer == null)
                return null;
            return answer + '\n';
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
