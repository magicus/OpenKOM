/*
 * Created on Aug 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

import java.io.PrintWriter;

import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class FlagUtils
{
    public static void printFlags(PrintWriter out, MessageFormatter formatter, String[] flagLabels, long[] flags)
    {
		String on = formatter.format("list.flags.on");
		String off = formatter.format("list.flags.off"); 
		int top = flagLabels.length;
		for(int idx = 0; idx < top; ++idx)
		{
			if(flagLabels[idx] == null)
				continue;
			int flagWord = idx / 64;
			int flagMask = 1 << (idx % 64);
			out.print((flags[flagWord] & flagMask) == flagMask ? on : off);
			out.print("  ");
			out.println(flagLabels[idx]); 
		}
    }
    
    public static void printFlagsShort(PrintWriter out, MessageFormatter formatter, String[] flagLabels, long[] flags)
    {
		int top = flagLabels.length;
		boolean printed = false;
		for(int idx = 0; idx < top; ++idx)
		{
			if(flagLabels[idx] == null)
				continue;
			int flagWord = idx / 64;
			int flagMask = 1 << (idx % 64);
			if((flags[flagWord] & flagMask) == flagMask)
			{
			    if(printed)
			        out.print(", ");
			    out.print(flagLabels[idx]);
			    printed = true;
			}
		}
    }
}
