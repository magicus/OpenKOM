/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

import java.io.PrintWriter;
import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class PrintUtils
{
	public static void printColumn(PrintWriter out, String message, int colLength)
	{
		out.print(message);
		printRepeated(out, ' ', colLength - message.length());
	}
	
	public static void printLabelledIfDefined(PrintWriter out, String label, int colLength, String value)
	{
		if(value == null || value.length() == 0)
			return;
		printLabelled(out, label, colLength, value);
	}

	public static void printLabelled(PrintWriter out, String label, int colLength, String value)
	{
		printColumn(out, label, colLength);
		out.println(value);
	}
	
	public static void printRepeated(PrintWriter out, char ch, int n)
	{
		for(int idx = 0; idx < n; ++idx)
			out.print(ch);
	}
	
	public static void printLeftJustified(PrintWriter out, String s, int width)
	{
		if(s.length() > width)
			s = s.substring(0, width);
		out.print(s);
		printRepeated(out, ' ', width - s.length());
	}
	
	public static void printRightJustified(PrintWriter out, String s, int width)
	{
		if(s.length() > width)
			s = s.substring(0, width);
		printRepeated(out, ' ', width - s.length());			
		out.print(s);
	}
	
	// TODO: ugly, fix later...
	public static String printDate(Timestamp d)
	{
		if(d != null)
			return d.toString();
		else
			return "aldrig";
	}
	
}
