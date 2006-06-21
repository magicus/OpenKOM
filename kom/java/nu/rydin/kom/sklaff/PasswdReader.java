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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import nu.rydin.kom.backend.NameUtils;

/**
 * @author Pontus Rydin
 */
public class PasswdReader
{
    private static class UserDetails
    {
        private String fullName;
        private Timestamp lastLogin;
 
        public UserDetails(String fullName, Timestamp lastLogin)
        {
            super();
            this.fullName = fullName;
            this.lastLogin = lastLogin;
        }
 
        public String getFullName()
        {
            return fullName;
        }
        public Timestamp getLastLogin()
        {
            return lastLogin;
        }
    }    

    public static Map<Integer, PasswdEntry> read(String userFile, String passwdFile)
    throws IOException
    {
        // First, read user file
        //
        HashMap<String, UserDetails> userDetails = new HashMap<String, UserDetails>();
        BufferedReader rdr = new BufferedReader(new FileReader(userFile));
        try
        {
            for(String line = null; (line = rdr.readLine()) != null;)
            {
                line = CharConvert.convert(line);
                StringTokenizer st = new StringTokenizer(line, ":");
                String id = st.nextToken();
                String login = st.nextToken();
                String fullName = NameUtils.stripSuffix(st.nextToken());
                userDetails.put(id, new UserDetails(fullName, 
                        new Timestamp(Long.parseLong(login) * (long) 1000)));
            }
        }
        finally
        {
            rdr.close();
        }
        
        // Then read passwd file...
        Map<Integer, PasswdEntry> answer = new TreeMap<Integer, PasswdEntry>();
        rdr = new BufferedReader(new FileReader(passwdFile));
        try
        {
            for(String line = null; (line = rdr.readLine()) != null;)
            {
                line = CharConvert.convert(line);
                StringTokenizer st = new StringTokenizer(line, ":");
                String id = st.nextToken();
                String passwd = st.nextToken();
                String nbr = st.nextToken();
                UserDetails ud = (UserDetails) userDetails.get(nbr);
                if(ud == null)
                {
                    System.err.println("User " + id + " is not in sklaff user table, skipping");
                    continue;
                }                
                st.nextToken(); // Skip group
                String name = st.nextToken();
                answer.put(new Integer(nbr), new PasswdEntry(id, passwd, ud.getFullName(), 
                        ud.getLastLogin()));
            }
            return answer;
            
        }
        finally
        {
            rdr.close();
        }
    }
}
