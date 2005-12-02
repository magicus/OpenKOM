/*
 * Created on Sep 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.statdaemon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.xml.rpc.ServiceException;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.soap.client.AlreadyLoggedInException;
import nu.rydin.kom.soap.client.AuthenticationException;
import nu.rydin.kom.soap.client.AuthenticatorService;
import nu.rydin.kom.soap.client.AuthenticatorServiceServiceLocator;
import nu.rydin.kom.soap.client.AuthorizationException;
import nu.rydin.kom.soap.client.LoginProhibitedException;
import nu.rydin.kom.soap.client.MessagePosterService;
import nu.rydin.kom.soap.client.MessagePosterServiceServiceLocator;
import nu.rydin.kom.soap.client.ObjectNotFoundException;
import nu.rydin.kom.soap.client.SecurityToken;
import nu.rydin.kom.soap.client.SessionExpiredException;
import nu.rydin.kom.soap.client.UnstoredMessage;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Pontus Rydin
 */
public class StatisticsDaemon
{
    private ResourceBundle resources;
    private MessageFormatter formatter;
    private Connection conn;
    private AuthenticatorService authenticator;
    private MessagePosterService messagePoster;
    private SecurityToken token;
    
    public StatisticsDaemon(ResourceBundle resources, MessageFormatter formatter)
    {
        super();
        this.resources = resources;
        this.formatter = formatter;
    }
    
    public void run()
    throws SQLException, UnexpectedException, AuthenticationException, AlreadyLoggedInException, LoginProhibitedException, RemoteException
    {
        // Get hold of database connection
        //
        try
        {
			Class.forName(resources.getString("jdbc.driver")).newInstance();
			// System.out.println(resources.getString("jdbc.connect"));
			conn = DriverManager.getConnection(resources.getString("jdbc.connect"));
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        catch(ClassNotFoundException e)
        {
            throw new UnexpectedException(-1, e);
        }
        catch(IllegalAccessException e)
        {
            throw new UnexpectedException(-1, e);
        }
        catch(InstantiationException e)
        {
            throw new UnexpectedException(-1, e);
        }
        
        // Connect to KOM
        //
        try
        {
            // Bind to endpoints
            //
	        AuthenticatorServiceServiceLocator authLoc = new AuthenticatorServiceServiceLocator();
	        this.authenticator = authLoc.getAuthenticator(new URL(resources.getString("authenticator.endpoint")));
	        MessagePosterServiceServiceLocator posterLoc = new MessagePosterServiceServiceLocator();
	        this.messagePoster = posterLoc.getMessagePoster(new URL(resources.getString("messageposter.endpoint")));
	        
	        // Log in 
	        //
	        this.token = authenticator.login(resources.getString("username"), resources.getString("password"));
        }
        catch(MalformedURLException e)
        {
            throw new UnexpectedException(-1, e);
        }
        catch(ServiceException e)
        {
            throw new UnexpectedException(-1, e);
        }
        catch(nu.rydin.kom.soap.client.UnexpectedException e)
        {
            throw new UnexpectedException(e.getUser(), e);
        }
        
        try
        {
	        // Enumerate operations
	        //
	        String operations = resources.getString("operations");
	        for(StringTokenizer st = new StringTokenizer(operations, ","); st.hasMoreTokens();)
	        {
	            String each = st.nextToken();
	            int p = each.indexOf(' ');
	            if(p == -1)
	            {
	                Logger.error(this, "Operation '" + each + "' is invalid");
	                continue;
	            }
	            String verb = each.substring(0, p).trim();
	            String arg = each.substring(p + 1).trim();
	            
	            // Find the corresponding method
	            //
	            Method m;
	            try
	            {
	                m = this.getClass().getMethod(verb, new Class[] { String.class });
	            }
	            catch(NoSuchMethodException e)
	            {
	                Logger.error(this, "Operation '" + each + "' is not implemented");
	                continue;
	            }
	            
	            // Call the method
	            //
	            try
	            {
	                m.invoke(this, new Object[] { arg });
	            }
	            catch(Exception e)
	            {
	                Logger.error(this, e);
	            }
	        }
        }
        finally
        {
            authenticator.discardToken(token);
        }
    }
    
    public void authorStatistics(String arg)
    throws SQLException, ObjectNotFoundException, nu.rydin.kom.soap.client.UnexpectedException, AuthorizationException, SessionExpiredException, NumberFormatException, RemoteException
    {        
        // Get subject and date range
        //
        int p = arg.indexOf(' ');
        if(p == -1)
            throw new IllegalArgumentException(arg);
        String range = arg.substring(0, p).trim();
        String subject = arg.substring(p + 1).trim();
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        
        // Print header
        //
        HeaderPrinter hp = new HeaderPrinter();
        hp.addHeader(formatter.format("authorstat.author"), 50, false);
        hp.addSpace(1);
        hp.addHeader(formatter.format("authorstat.messages"), 10, true);
        hp.printOn(out);
        
        // Calculate start date
        //
        Timestamp start = this.translateDate(range);
        
        // Create statement
        //
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT n.fullname, COUNT(*) c FROM messages m, names n " +
                "WHERE n.id = m.author AND m.created > ? " +
                "GROUP BY m.author " +
                "ORDER BY c DESC");
        stmt.setTimestamp(1, start);
        ResultSet rs = null;
        long total = 0;
        try
        {            
            rs = stmt.executeQuery();
            while(rs.next())
            {
                PrintUtils.printLeftJustified(out, NameUtils.stripSuffix(rs.getString(1)), 50);
                out.print(' ');
                long n = rs.getLong(2);
                total += n;
                PrintUtils.printRightJustified(out, Long.toString(n), 10);
                out.println();
            }
        }
        finally
        {
            if(rs != null)
                rs.close();
        }
        
        // Print total
        //
        PrintUtils.printRepeated(out, ' ', 51);
        PrintUtils.printRepeated(out, '=', 10);
        out.println();
        PrintUtils.printRepeated(out, ' ', 51);
        PrintUtils.printRightJustified(out, Long.toString(total), 10);
        out.println();
        
        System.out.println(sw.getBuffer());
        
        // Post as message
        //
        UnstoredMessage msg = new UnstoredMessage(sw.getBuffer().toString(), subject);
        messagePoster.storeMessage(token, Long.parseLong(resources.getString("conferenceid")), msg);
    }
    
    protected Timestamp translateDate(String arg)
    {
        // Arg is on the form "<n><u>" where n is a number and
        // u is a unit specifier, such as "d" for day, "w" for week
        // and "m" for month. E.g. "1w" or "12m".
        //
        long now = System.currentTimeMillis();
        int len = arg.length();
        char unit = arg.charAt(len - 1);
        int n = Integer.parseInt(arg.substring(0, len - 1));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        switch(unit)
        {
        case 'd':
            return new Timestamp(now - 24 * 1000);
        case 'w':
            return new Timestamp(now - 7 * 24 * 1000);
        case 'm':
            c.roll(Calendar.MONTH, -n);
            return new Timestamp(c.getTimeInMillis());
        case 'y':
            c.roll(Calendar.YEAR, -n);
            return new Timestamp(c.getTimeInMillis());            
        default:
            throw new IllegalArgumentException("Wrong unit: " + unit);
        }
    }
}
