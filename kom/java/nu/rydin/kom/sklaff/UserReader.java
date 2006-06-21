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

import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author Pontus Rydin
 */
public class UserReader
{
    public static UserInfo readUser(String fileName, String userId, String name)
    throws IOException
    {
        BufferedReader rdr = new BufferedReader(new FileReader(fileName));
        try
        {
            StringBuffer buffer = new StringBuffer();
            HashMap<String, String> map = new HashMap<String, String>();
            String tag = null;
            boolean data = false;
            for(String line = null; (line = rdr.readLine()) != null;)
            {
                String translated = CharConvert.convert(line);
                if(line.startsWith("!["))
                {
                    String s = buffer.toString();
                    if(s.length() > 0)
                        map.put(tag, s);
                    tag = translated.substring(2, translated.length() - 1);
                    data = false;
                    buffer = new StringBuffer();
                }
                else
                {
                    if(data)
                        buffer.append(",");
                    buffer.append(translated);
                    data = true;
                }
            }
            
            // Valid users must at least have flags
            //
            String flags = (String) map.get("flags");
            String city = (String) map.get("ort");
            String zip = (String) map.get("postnr");
            String address2 = zip != null ? zip : "";
            if(city != null)
                address2 += " " + city; 
            return new UserInfo(
                    -1, 
                    new Name(name, Visibilities.PUBLIC, NameManager.USER_KIND), 
                    userId,
                    (String) map.get("address"),
                    address2,
                    null,
                    null,
                    (String) map.get("tele1"),
                    (String) map.get("tele2"),
                    (String) map.get("email1"),
                    (String) map.get("email2"),
                    (String) map.get("url"),
                    flags != null ? decodeCharSet(flags) : "ISO-8859-1",
                    flags != null ? decodeFlags(flags) : UserFlags.DEFAULT_FLAGS1,
                    0,
                    0,
                    0,
                    UserPermissions.NORMAL,
                    "sv_SE",
                    "Europe/Stockholm",
                    new Timestamp(System.currentTimeMillis()),
                    null
                    );
        }
        finally
        {
            rdr.close();
        }        
    }
    
    protected static long decodeFlags(String s)
    {
        s = s.toLowerCase();
        long flags = UserFlags.SHOW_ATTENDANCE_MESSAGES | UserFlags.KEEP_COPIES_OF_MAIL 
        	| UserFlags.EMPTY_LINE_FINISHES_CHAT | UserFlags.SHOW_NUM_UNREAD 
        	| UserFlags.PRIORITIZE_MAIL;
        if(s.indexOf("say = 1") != -1)
            flags |= UserFlags.ALLOW_CHAT_MESSAGES;
        if(s.indexOf("shout = 1") != -1)
            flags |= UserFlags.ALLOW_BROADCAST_MESSAGES;
        if(s.indexOf("present = 1") != -1)
            flags |= UserFlags.SHOW_ATTENDANCE_MESSAGES;
        if(s.indexOf("date = 1") != -1)
            flags |= UserFlags.ALWAYS_PRINT_FULL_DATE;
        if(s.indexOf("beep = 1") != -1)
            flags |= UserFlags.BEEP_ON_BROADCAST | UserFlags.BEEP_ON_CHAT;
        if(s.indexOf("presbeep = 1") != -1)
            flags |= UserFlags.BEEP_ON_ATTENDANCE;
        if(s.indexOf("ansi = 1") != -1)
            flags |= UserFlags.ANSI_ATTRIBUTES;
        if(s.indexOf("ansi = 1") != -1)
            flags |= UserFlags.ANSI_ATTRIBUTES;                
        if(s.indexOf("readowntexts = 1") != -1)
            flags |= UserFlags.NARCISSIST;                
        return flags;
    }
    
    protected static String decodeCharSet(String s)
    {
        if(s.indexOf("ibm = 1") != -1)
            return "Cp850";
        if(s.indexOf("mac = 1") != -1)
            return "MacRoman";
        return "ISO-8859-1";
    }
}
