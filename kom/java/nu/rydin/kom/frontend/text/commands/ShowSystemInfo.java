/*
 * Created on Sep 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.boot.Bootstrap;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.CacheInformation;
import nu.rydin.kom.structs.SystemInformation;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowSystemInfo extends AbstractCommand
{
    private static final int WIDTH = 25;
    
	public ShowSystemInfo(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        SystemInformation info = context.getSession().getSystemInformation();        
                
        // Print memory status.
        // Clean up before we display it
        //
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long freeMem = rt.freeMemory() / 1024;
        long totalMem = rt.totalMemory() / 1024;
        
        // Print headers
        //
        HeaderPrinter hp = new HeaderPrinter();
        hp.addSpace(WIDTH);
        hp.addHeader(formatter.format("system.allocated.memory"), 10, true);
        hp.addHeader(formatter.format("system.used.memory"), 10, true);
        hp.addHeader(formatter.format("system.free.memory"), 10, true);
        hp.printOn(out);
  
        // Print values
        //
        PrintUtils.printLeftJustified(out, formatter.format("system.info.memory"), WIDTH);
        PrintUtils.printRightJustified(out, Long.toString(totalMem), 10);
        PrintUtils.printRightJustified(out, Long.toString(totalMem - freeMem), 10);
        PrintUtils.printRightJustified(out, Long.toString(freeMem), 10);
        out.println();
        out.println();
                
        // Print object counts
        //
        hp = new HeaderPrinter();
        hp.addHeader(formatter.format("system.info.object"), WIDTH, false);
        hp.addHeader(formatter.format("system.info.count"), 10, true);
        hp.printOn(out);
        PrintUtils.printLeftJustified(out, formatter.format("system.info.conferences"), WIDTH);
        PrintUtils.printRightJustified(out, Long.toString(info.getNumConferences()), 10);
        out.println();
        PrintUtils.printLeftJustified(out, formatter.format("system.info.users"), WIDTH);
        PrintUtils.printRightJustified(out, Long.toString(info.getNumUsers()), 10);
        out.println();
        PrintUtils.printLeftJustified(out, formatter.format("system.info.messages"), WIDTH);
        PrintUtils.printRightJustified(out, Long.toString(info.getNumMessages()), 10);
        out.println();
        out.println();
        
        // Print cache stats
        //
        hp = new HeaderPrinter();
        hp.addHeader(formatter.format("system.info.cache"), WIDTH, false);
        hp.addHeader(formatter.format("system.info.cache.accesses"), 10, true);
        hp.addHeader(formatter.format("system.info.cache.hits"), 10, true);
        hp.addHeader("%", 3, true);
        hp.printOn(out);
        this.printCacheInfo(out, formatter, formatter.format("system.info.names"), info.getNameCache());
        this.printCacheInfo(out, formatter, formatter.format("system.info.conferences"), info.getConferenceCache());
        this.printCacheInfo(out, formatter, formatter.format("system.info.users"), info.getUserCache());
        
        // Print uptime
        //
        out.println();
        long uptime = System.currentTimeMillis() - Bootstrap.getBootTime();
        long upMins = uptime / 60000;
        long upHours = upMins / 60;
        long upDays = upHours / 24;
        StringBuffer sb = new StringBuffer(100);
        if(upDays > 0)
        {
            sb.append(Long.toString(upDays));
            sb.append(' ');
            sb.append(formatter.format("time.days"));
            sb.append(", ");
        }
        if(upHours > 0)
        {
            sb.append(Long.toString(upHours % 24));
            sb.append(' ');
            sb.append(formatter.format("time.hours"));
            sb.append(", ");
        }
        sb.append(Long.toString(upMins % 60));
        sb.append(' ');
        sb.append(formatter.format("time.minutes"));        
        PrintUtils.printLabelled(out, formatter.format("system.info.uptime"), WIDTH, sb.toString());
        
        // Print system mode
        //
        PrintUtils.printLabelled(out, formatter.format("system.info.mode"), WIDTH, 
                info.isLoginAllowed() ? formatter.format("system.info.multiser") : 
                formatter.format("system.info.singleuser"));
        
        // Number of threads
        //
        PrintUtils.printLabelled(out, formatter.format("system.info.threads"), WIDTH, 
                Integer.toString(Thread.enumerate(new Thread[1000])));
        
        // Java VM
        //
        PrintUtils.printLabelled(out, formatter.format("system.info.vm"), WIDTH, 
                System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
                
        // Operating system
        //
        PrintUtils.printLabelled(out, formatter.format("system.info.os"), WIDTH, 
                System.getProperty("os.name") + " " + System.getProperty("os.version"));
        out.println();        
    }
    
    private void printCacheInfo(PrintWriter out, MessageFormatter formatter, String label, CacheInformation ci)
    {
        PrintUtils.printLeftJustified(out, label, WIDTH);
        PrintUtils.printRightJustified(out, Long.toString(ci.getNumAccesses()), 10);
        PrintUtils.printRightJustified(out, Long.toString(ci.getNumHits()), 10);
        PrintUtils.printRightJustified(out, Long.toString((long) (ci.getHitRatio() * 100)), 3);
        out.println('%');
    }
}

