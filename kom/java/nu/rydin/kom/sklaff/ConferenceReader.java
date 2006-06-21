/*
 * Created on Apr 12, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * @author Pontus Rydin
 */
public class ConferenceReader
{
    public static final int CONFERENCE_OFFSET = 100000;
    
    public static Map read(String confFile, boolean sklommon)
    throws IOException
    {
        Map<Integer, ConfEntry> answer = new TreeMap<Integer, ConfEntry>();
        BufferedReader rdr = new BufferedReader(new FileReader(confFile));
        try
        {
            for(String line = null; (line = rdr.readLine()) != null;)
            {
                line = CharConvert.convert(line);
                StringTokenizer st = new StringTokenizer(line, ":");
                String id = st.nextToken();
                String lastTest = st.nextToken();
                String owner = st.nextToken();
                if(sklommon)
                    st.nextToken();
                String time = st.nextToken();
                String type = st.nextToken();
                String life = st.nextToken();
                String replyConf = st.nextToken();
                String name = st.nextToken();
                int num = Integer.parseInt(id) + CONFERENCE_OFFSET;
                answer.put(new Integer(num), 
                        new ConfEntry(
                                name, 
                                Integer.parseInt(owner),
                                Integer.parseInt(type),
                                new Timestamp((long) Integer.parseInt(time) * 1000),
                                Integer.parseInt(replyConf) + CONFERENCE_OFFSET,
                                num));
            }
            return answer;
            
        }
        finally
        {
            rdr.close();
        }
    }

}
