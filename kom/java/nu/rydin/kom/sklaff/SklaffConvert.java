/*
 * Created on Apr 12, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;

import nu.rydin.kom.backend.CacheManager;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.MessageNotFoundException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.structs.MessageAttribute;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author Pontus Rydin
 */
public class SklaffConvert
{ 
    private static final int DEBUG_MESSAGES = -1;
    private static final int DEBUG_MAIL = -1;
    
    private static PreparedStatement addMsg;
    private static PreparedStatement addMsgOcc;
    private static PreparedStatement findMail;
    private static Statement stmt;
    private static MessageManager mm;
    private static NameManager nm;
    private static ConferenceManager cm;
    private static Map confXref = new TreeMap();
    private static Map userXref = new TreeMap();

    public static void main(String[] args)
    throws Exception
    {
        if(args.length != 3)
        {
            System.err.println("Usage SklaffConvert <database> <passwdfile> <sklaffroot>");
            return;
        }
        String db = args[0];
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/" + db + "?user=" + db + "&password=" + db);
		conn.setAutoCommit(false);
        addMsg = conn.prepareStatement(
                "INSERT INTO messages(created, author, author_name, reply_to, thread, kind, subject, body) " +
                "VALUES(?,?,?,?,?,?,?,?)");
        addMsgOcc = conn.prepareStatement(
                "INSERT INTO messageoccurrences(localnum, conference, message, action_ts, kind, user, user_name) " +
                "VALUES(?,?,?,?,?,?,?)");
        findMail = conn.prepareStatement(
                "SELECT mo.message, mo.localnum FROM messageoccurrences mo, messages m " +
                "WHERE m.id = mo.message AND m.author = ? AND mo.conference = ? AND m.created = ?");
        stmt = conn.createStatement();
        mm = new MessageManager(conn);
        nm = new NameManager(conn);
        cm = new ConferenceManager(conn, nm);
		convert(conn, args[1], args[2]);
        conn.commit();
        conn.close();
    }
    
    protected static void convert(Connection conn, String passwdFile, String sklaffRoot)
    throws Exception
    {
        boolean sklommon = true;
		UserManager um = new UserManager(conn, CacheManager.instance(), nm);
		MessageManager mm = new MessageManager(conn);
		ConferenceManager cm = new ConferenceManager(conn, nm);
        convertUsers(passwdFile, sklaffRoot, um);
        convertConferences(sklaffRoot, sklommon, cm, conn);
        convertMemberships(sklaffRoot, conn);
        stmt.execute("CREATE INDEX created_1 ON messages(created)");
        convertAllMessages(sklaffRoot, conn);
		convertAllMail(sklaffRoot, conn);
		updateConferenceDates(conn);
		stmt.execute("ALTER TABLE messages DROP INDEX created_1");
    }
    
    protected static void convertUsers(String passwdFile, String sklaffRoot, UserManager um) 
    throws IOException, AmbiguousNameException, NoSuchAlgorithmException, SQLException, DuplicateNameException  
    {
        PasswordGenerator pg = new PasswordGenerator();
        System.out.println("Converting users...");
        PrintWriter pw = new PrintWriter(new FileWriter("newpasswd.txt"));
        Map m = PasswdReader.read(sklaffRoot + "/etc/user", passwdFile);
        for(Iterator itor = m.entrySet().iterator(); itor.hasNext();)
        {
            Map.Entry entry = (Map.Entry) itor.next();
            Integer n = (Integer) entry.getKey();
            PasswdEntry pwdE = (PasswdEntry) entry.getValue();
            try
            {
                String passwd = pg.generate(8);
	            UserInfo ui = UserReader.readUser(sklaffRoot + "/user/" + n + "/sklaffrc", pwdE.getId(), 
	                    pwdE.getName());
	            if(ui == null)
	            {
	                System.err.println(pwdE.getId() + " does not have a valid sklaffrc file");
	                continue;
	            }
	            System.out.print(ui.getName());
	            System.out.print("...");
	            System.out.flush();
	            long id = um.addUser(
	                    ui.getUserid(),
	                    passwd,
	                    ui.getName(),
	                    ui.getAddress1(),
	                    ui.getAddress2(),
	                    ui.getAddress3(),
	                    ui.getAddress4(),
	                    ui.getPhoneno1(),
	                    ui.getPhoneno2(),
	                    ui.getEmail1(),
	                    ui.getEmail2(),
	                    ui.getUrl(),
	                    ui.getCharset(),
	                    ui.getLocale(),
	                    ui.getFlags1(),
	                    ui.getFlags2(),
	                    ui.getFlags3(),
	                    ui.getFlags4(),
	                    ui.getRights());
	            System.out.println("Added!");
	            System.out.flush();
	            userXref.put(n, new Long(id));
	            pw.print(ui.getUserid());
	            pw.print(':');
	            pw.print(ui.getEmail1());
	            pw.print(':');
	            pw.print(passwd);
	            pw.print(':');
	            pw.println(ui.getName());
            }
            catch(FileNotFoundException e)
            {
                System.err.println(pwdE.getId() + " does not appear to be a sklaff user");
            }
        }
        pw.close();
    }
    
    protected static void convertConferences(String sklaffRoot, boolean sklommon, ConferenceManager cm, Connection conn)
    throws IOException, AmbiguousNameException, SQLException, DuplicateNameException
    {
        PreparedStatement updateCreated = conn.prepareStatement(
                "UPDATE conferences SET created = ? WHERE id = ?");
        ArrayList fixups = new ArrayList();
        Map m = ConferenceReader.read(sklaffRoot + "/etc/conf", sklommon);
        for(Iterator itor = m.entrySet().iterator(); itor.hasNext();)
        {
            Map.Entry entry = (Map.Entry) itor.next();
            Integer n = (Integer) entry.getKey();
            ConfEntry ce = (ConfEntry) entry.getValue();
            if(ce.getOwner() == -1)
            {
                System.out.println(ce.getName() + " is deleted. Skipping!");
                continue;
            }
            System.out.println(ce.getName());
            
            // A conference with a non-empty confrc is deemed protected
            //
            File confrc = new File(sklaffRoot + "/db/" + n + "/confrc");
            boolean prot = confrc.length() > 0;
            long replyConf = -1;
            if(ce.getReplyConf() != 0)
            {
                replyConf = sklaffConf2openkom(ce.getReplyConf());
                if(replyConf == -1)
                    fixups.add(n);
            }
            long admin = ((Long) userXref.get(new Integer(ce.getOwner()))).longValue();
            long id = cm.addConference(
                    ce.getName(),
                    admin,
                    prot ? 0: ConferencePermissions.NORMAL_PERMISSIONS,
                    0, 
                    (short) 0, // TODO: Support invisible conferences
                    replyConf
                    );
            
            // Update time of last text
            //
            updateCreated.setTimestamp(1, ce.getLastText());
            updateCreated.setLong(2, id);
            updateCreated.executeUpdate();
            confXref.put(n , new Long(id));
        }       
        
        // Deal with reply conference fixups
        //
        System.out.println("Repairing conference forward references...");
        for(Iterator itor = fixups.iterator(); itor.hasNext();)
        {
            Integer sklaffId = (Integer) itor.next();
            long id = sklaffConf2openkom(sklaffId);
            ConfEntry ce = (ConfEntry) m.get(sklaffId);
            long replyConf = sklaffConf2openkom(ce.getReplyConf());
            cm.changeReplyToConference(id, replyConf);
        }        
    }
    
    protected static void convertMemberships(String sklaffRoot, Connection conn)
    throws Exception
    {
        System.out.println("Converting memberships...");
        for(Iterator itor = userXref.entrySet().iterator(); itor.hasNext();)
        {
            Map.Entry each = (Map.Entry) itor.next();
            int sklaffId = ((Integer) each.getKey()).intValue();
            System.out.println("Sklaffid: " + sklaffId);
            long openkomId = ((Long) each.getValue()).longValue();
            convertMembership(sklaffRoot, sklaffId, openkomId, conn);
        }
    }
    
    protected static void convertMembership(String sklaffRoot, int sklaffUser, long openkomUser, Connection conn)
    throws Exception
    {
        // System.out.println("Handling memberships for slkaff user: " + sklaffUser);
        PreparedStatement ps = conn.prepareStatement(
                "REPLACE INTO memberships(conference, user, priority, flags, active, permissions, negation_mask, markers) VALUES(?,?,?,?,1,?,0,?)");
        BufferedReader rdr = new BufferedReader(new FileReader(sklaffRoot + "/user/" + sklaffUser + "/confs"));
        try
        {
            int prio = 0;
            for(String line = null; (line = rdr.readLine()) != null;)
            {
                line = CharConvert.convert(line);
                StringTokenizer st = new StringTokenizer(line, ":");
                String id = st.nextToken();
                //System.out.println("id=" + id);
                String unread = st.hasMoreTokens() ? st.nextToken() : null;
                long confId = "0".equals(id) 
                	? openkomUser  	// mailbox
                	: sklaffConf2openkom(Integer.parseInt(id) + ConferenceReader.CONFERENCE_OFFSET);
                if(confId == -1)
                {
                    System.err.println("Conference id=" + confId + " is deleted. Skipping membership");
                    continue;
                }
                
                // Create OpenKOM membership record
                //
                ps.setLong(1, confId);
                ps.setLong(2, openkomUser);
                ps.setInt(3, prio++);
                ps.setLong(4, 0);
                ps.setLong(5, ConferencePermissions.NORMAL_PERMISSIONS);
                ps.setString(6, unread);
                ps.executeUpdate();
            }
        }
        finally
        {
            rdr.close();
        }

    }
    
    protected static void convertAllMessages(String sklaffRoot, Connection conn)
    throws Exception
    {
        System.out.println("Converting messages...");
        
        // Load messages
        //
        ArrayList fixups = new ArrayList();
        for(Iterator itor = confXref.keySet().iterator(); itor.hasNext();)
        {
            int sklaffConf = ((Integer) itor.next()).intValue();
            convertMessagesInConference(sklaffRoot, sklaffConf, fixups, conn);
        }
        handleFixups(fixups, conn);
    }
    
    protected static void convertAllMail(String sklaffRoot, Connection conn)
    throws Exception
    {
        // Load messages
        //
        System.out.println("Converting inbound mail...");
        ArrayList fixups = new ArrayList();
        for(Iterator itor = userXref.keySet().iterator(); itor.hasNext();)
        {
            int sklaffConf = ((Integer) itor.next()).intValue();
            convertIncomingMail(sklaffRoot, sklaffConf, fixups, conn);
        }        
        System.out.println("Converting outbound mail...");
        for(Iterator itor = userXref.keySet().iterator(); itor.hasNext();)
        {
            int sklaffConf = ((Integer) itor.next()).intValue();
            convertOutgoingMail(sklaffRoot, sklaffConf, fixups, conn);
        }
        handleFixups(fixups, conn);
    }
       
    protected static void handleFixups(ArrayList fixups, Connection conn)
    throws Exception
    { 
        System.out.println("Repairing forward-refering reply-links...");
        // Handle reply fixups
        //
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE messages SET reply_to = ? WHERE id = ?");
        MessageManager mm = new MessageManager(conn);
        int top = fixups.size();
        for(int idx = 0; idx < top; ++idx)
        {
            long[] each = (long[]) fixups.get(idx);
            long id = each[0];
            int sklaffReplyTo = (int) each[1];
            long replyToConf = each[2];
            try
            {
                long replyTo = mm.getGlobalMessageId(replyToConf, sklaffReplyTo);
                ps.setLong(1, replyTo);
                ps.setLong(2, id);
                ps.executeUpdate();
            }
            catch(MessageNotFoundException e)
            {
                System.err.println("Message (" + replyToConf + "," + sklaffReplyTo + ") not found, skipping");
            }
        }
    }
    
    protected static void convertMessagesInConference(String sklaffRoot, int sklaffConf, ArrayList replyFixups, Connection conn)
    throws Exception
    {
        long conf = sklaffConf2openkom(sklaffConf);
        File confDir = new File(sklaffRoot + "/db/" + (sklaffConf - ConferenceReader.CONFERENCE_OFFSET));
        File[] messages = confDir.listFiles();
        if(messages == null)
        {
            System.err.println("No messages found in conference " + (sklaffConf - ConferenceReader.CONFERENCE_OFFSET));
            return;
        }
        int top = messages.length;
        if(DEBUG_MESSAGES != -1)
            top = Math.min(top, DEBUG_MESSAGES);
        for(int idx = 0; idx < top; ++idx)
        {
            String file = messages[idx].getAbsolutePath();
            if(file.endsWith("confrc") || file.endsWith(".result") || file.endsWith(".users"))
                continue;
            MessageEntry me = readMessage(file, true);
            convertMessage(me, sklaffConf, conf, false, replyFixups, conn);
        }
    }
    
    protected static void convertIncomingMail(String sklaffRoot, int sklaffUser, ArrayList replyFixups, Connection conn)
    throws Exception
    {
        long user = ((Long) userXref.get(new Integer(sklaffUser))).longValue();
        File confDir = new File(sklaffRoot + "/mbox/" + sklaffUser);
        File[] messages = confDir.listFiles();
        int top = messages.length;
        if(DEBUG_MAIL != -1)
            top = Math.min(top, DEBUG_MAIL);
        
        // Deal with mail sent to us
        //
        for(int idx = 0; idx < top; ++idx)
        {
            String file = messages[idx].getAbsolutePath();
            if(file.endsWith("mailbox") || file.endsWith(".result") || file.endsWith(".users"))
                continue;
            MessageEntry me = readMessage(file, true);
            if(me == null)
                continue;
            
            // Convert only mail sent to us
            //
            if(me.getAuthor() != sklaffUser)
            {
                long id = convertMessage(me, sklaffUser, user, true, replyFixups, conn);   
                // System.out.println("Created message: " + file + " linked to: " + id);
            }
        }
    }    
    

    protected static void convertOutgoingMail(String sklaffRoot, int sklaffUser, ArrayList replyFixups, Connection conn)
    throws Exception
    {
        long user = ((Long) userXref.get(new Integer(sklaffUser))).longValue();
        String userName = nm.getNameById(user).getName();
        File confDir = new File(sklaffRoot + "/mbox/" + sklaffUser);
        File[] messages = confDir.listFiles();
        int top = messages.length;
        if(DEBUG_MAIL != -1)
            top = Math.min(top, DEBUG_MAIL);
                
        // Deal with mail sent from us
        //
        for(int idx = 0; idx < top; ++idx)
        {
            String file = messages[idx].getAbsolutePath();
            if(file.endsWith("mailbox") || file.endsWith(".result") || file.endsWith(".users"))
                continue;
            MessageEntry me = readMessage(file, true);
            if(me == null)
                continue;            
            
            // Convert only mail sent from us
            //
            if(me.getAuthor() != sklaffUser)
                continue;
            
            // Try to find message in party's mailbox
            //
            long party = sklaffUser2openkom(me.getReplyToUser());
            findMail.setLong(1, user);
            findMail.setLong(2, party);
            findMail.setTimestamp(3, me.getCreated());
            ResultSet rs = findMail.executeQuery();
            long id;
            if(rs.next())
            {
                // Link to message in party's mailbox
                //
                id = rs.getLong(1);
                int localnum = rs.getInt(2);
               // System.out.println("Found message: " + file + " linked to: " + id + ", party=(" + me.getReplyToUser() + "," + localnum);
                addMsgOcc.setInt(1, me.getNum());
                addMsgOcc.setLong(2, user);
                addMsgOcc.setLong(3, id);
                addMsgOcc.setTimestamp(4, me.getCreated());
                addMsgOcc.setShort(5, MessageManager.ACTION_COPIED);
                addMsgOcc.setLong(6, user);
                addMsgOcc.setString(7, userName);
                addMsgOcc.executeUpdate();
            }
            else
            {
                id = convertMessage(me, sklaffUser, user, true, replyFixups, conn);      
            }
            try
            {
                String payload = MessageAttribute.constructUsernamePayload(party, nm.getNameById(party).getName());
                mm.addMessageAttribute(id, MessageAttributes.MAIL_RECIPIENT, payload);
            }
            catch(ObjectNotFoundException e)
            {
                System.out.println("Party #" + party + " not found for user " + sklaffUser);
            }
            rs.close();
        }
    }

    
    protected static void updateConferenceDates(Connection conn)
    throws Exception
    {
	    // Update date for latest text
	    //
	    System.out.println("Updating dates for latest texts...");
	    PreparedStatement updateConf = conn.prepareStatement(
	            "UPDATE conferences SET lasttext = ? WHERE id = ?");
	    PreparedStatement getLatest = conn.prepareStatement(
	            "SELECT c.id, MAX(mo.action_ts) FROM conferences c, messageoccurrences mo " +
	            "WHERE c.id = mo.conference GROUP BY c.id");
	    ResultSet rs = getLatest.executeQuery();
	    while(rs.next())
	    {
	        long id = rs.getLong(1);
	        Timestamp ts = rs.getTimestamp(2);
	        updateConf.setTimestamp(1, ts);
	        updateConf.setLong(2, id);
	        updateConf.executeUpdate();
	    }
	    rs.close();
	    updateConf.close();
	    getLatest.close();
    }
    
    protected static long convertMessage(MessageEntry me, int sklaffConf, long conf, boolean mail, ArrayList replyFixups, Connection conn)
    throws Exception
    {     
        if(me == null)
            return -1;
        
        // Convert all ids into OpenKOM ids
        //
        Long authorObj = (Long) userXref.get(new Integer(me.getAuthor()));
        if(authorObj == null)
        {
            System.out.println("Message " + sklaffConf + '/' + me.getNum() + ": Author " + me.getAuthor() + " does not exist. Skipping!");
            return -1;
        }
        long author = authorObj.longValue();
        long replyTo = -1;
        long replyToAuthor = -1;
        long replyConf = -1;
        boolean needFixup = false;
        if(me.getReplyTo() != 0)
        {
            // Reply to mail? Reply conference is author's mailbox.
            //
            if(mail && !confXref.containsKey(new Integer(me.getReplyToConf())))
                replyConf = sklaffConf2openkom(me.getAuthor());
            else  
	            replyConf = me.getReplyToConf() == 0
	            	? conf
	            	: sklaffConf2openkom(me.getReplyToConf());
            // System.out.println("Looking up: (" + me.getReplyTo() + "," + replyConf + ")");
            try
            {
                replyTo = mm.getGlobalMessageId(replyConf, me.getReplyTo());
            }
            catch(MessageNotFoundException e)
            {
                needFixup = true;
            }
            Long replyToAuthorObj = (Long) userXref.get(new Integer(me.getReplyToUser()));
            if(replyToAuthorObj != null)
                replyToAuthor = replyToAuthorObj.longValue();
            else
            {
                System.out.println("Message " + sklaffConf + '/' + me.getNum() + ": Original author " + me.getReplyToUser() + " does not exist. Not stored as reply");
                replyTo = -1;
            }
            
        }
        
        // Resolve author names
        //e
        String authorName = nm.getNameById(author).getName();
        	
        // Add to message table
        //
        addMsg.setTimestamp(1, me.getCreated());
        addMsg.setLong(2, author);
        addMsg.setString(3, authorName);
        if(replyTo != -1)
            addMsg.setLong(4, replyTo);
        else
            addMsg.setNull(4, Types.BIGINT);
        addMsg.setLong(5, -1); // Thread. TODO!
        addMsg.setShort(6, (short) 0);
        addMsg.setString(7, me.getSubject());
        addMsg.setString(8, me.getBody());
        addMsg.executeUpdate();
        long id = ((com.mysql.jdbc.PreparedStatement) addMsg).getLastInsertID();
        
        // Add fixup if needed
        //
        if(needFixup)
            replyFixups.add(new long[] { id, me.getReplyTo(), replyConf });
            
        
        // Add message occurrence
        //
        // System.out.println("Storing: (" + me.getNum() + "," + conf + ")->" + id);
        addMsgOcc.setInt(1, me.getNum());
        addMsgOcc.setLong(2, conf);
        addMsgOcc.setLong(3, id);
        addMsgOcc.setTimestamp(4, me.getCreated());
        addMsgOcc.setShort(5, (short) 0);
        addMsgOcc.setLong(6, author);
        addMsgOcc.setString(7, authorName);
        addMsgOcc.executeUpdate();
        return id;
    }
    
    protected static MessageEntry readMessage(String messageFile, boolean readBody)
    throws Exception
    {
        BufferedReader rdr = new BufferedReader(new FileReader(messageFile));
        try
        {
            // Parse header
            //
            String header = rdr.readLine();
            StringTokenizer st = new StringTokenizer(header, ":");
            int num = Integer.parseInt(st.nextToken());
            int sklaffAuthor = Integer.parseInt(st.nextToken());
            Timestamp created = new Timestamp(Long.parseLong(st.nextToken()) * 1000);
            int replyTo = Integer.parseInt(st.nextToken());
            int replyToConf = Integer.parseInt(st.nextToken());
            if(replyToConf != 0)
                replyToConf += ConferenceReader.CONFERENCE_OFFSET;
            int replyToUser = Integer.parseInt(st.nextToken());
            int numLines = Integer.parseInt(st.nextToken());
           
            // Read subject
            //
            String subject = CharConvert.convert(rdr.readLine());
            if(subject == null)
            {
                System.err.println("Message file " + messageFile + " ends prematurely");
                return null;
            }
            
            // Read body
            //
            String body = null;
            if(readBody)
            {
	            StringBuffer sb = new StringBuffer();
	            for(int idx = 0; idx < numLines; ++idx)
	            {
	                String line = CharConvert.convert(rdr.readLine());
	                if(line == null)
	                {
	                    System.out.println("Warning: Premature end of message!");
	                    break;
	                }
	                sb.append(line);
	                sb.append('\n');
	            }
	            body = sb.toString();
            }
            return new MessageEntry(
                    num,
                    sklaffAuthor,
                    created,
                    replyTo,
                    replyToConf,
                    replyToUser,
                    numLines,
                    subject,
                    body);
        }
        catch(NoSuchElementException e)
        {
            System.err.println("Message file " + messageFile + " has a corrupt header");
            return null;
        }
        catch(NumberFormatException e)
        {
            System.err.println("Message file " + messageFile + " has a corrupt header");
            return null;
        }        
        finally
        {
            rdr.close();
        }        
    }
    
    protected static long sklaffConf2openkom(int sklaffConf)
    {
        return sklaffConf2openkom(new Integer(sklaffConf));
    }
    
    protected static long sklaffConf2openkom(Integer sklaffConf)
    {
        Long obj = (Long) confXref.get(sklaffConf);
        if(obj != null)
            return obj.longValue();
        
        // Not found. It could be a mailbox. Check user xref.
        //
        obj = (Long) userXref.get(sklaffConf);
        return obj != null ? obj.longValue() : -1; 
    }
    
    protected static long sklaffUser2openkom(int sklaffUser)
    {
        return sklaffUser2openkom(new Integer(sklaffUser));
    }
    
    protected static long sklaffUser2openkom(Integer sklaffUser)
    {
        Long obj = (Long) userXref.get(sklaffUser);
        return obj != null ? obj.longValue() : -1;
    }
    
}
