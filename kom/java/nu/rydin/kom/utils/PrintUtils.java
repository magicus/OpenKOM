/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

import java.io.PrintWriter;

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

	public static void printIndented(PrintWriter out, String s, int width, int indentWidth)
	{
	    StringBuffer buffer = new StringBuffer(width);
	    for (int i = 0; i < indentWidth; ++i)
	        buffer.append(' ');
	    printIndented (out, s, width, buffer.toString());
	}
	
	public static void printIndented(PrintWriter out, String s, int width, int initialIndentWidth, int indentWidth)
	{
	    StringBuffer buffer1 = new StringBuffer(width);
	    for (int i = 0; i < initialIndentWidth; ++i)
	        buffer1.append(' ');
	    StringBuffer buffer2 = new StringBuffer(width);
	    for (int i = 0; i < indentWidth; ++i)
	        buffer2.append(' ');
	    printIndented (out, s, width, buffer1.toString(), buffer2.toString());
	}
	
	public static void printIndented(PrintWriter out, String s, int width, String indent)
	{
	    printIndented(out, s, width, indent, indent);
	}
	
	public static void printIndented(PrintWriter out, String s, int width, String initialIndent, String indent)
	{
	    String[] p = s.split(" ");
	    int indentLength = indent.length(); 
	    int rowLength = initialIndent.length();
	    int wordLength;
	    String thisRow = initialIndent;
	    String thisWord;
	    for (int i = 0; i < p.length; ++i)
	    {
	        thisWord = p[i];
	        wordLength = thisWord.length() + 1;
	        if (rowLength + wordLength < width)
	        {
	            rowLength += wordLength;
	            thisRow += thisWord;
	            thisRow += " ";
	        }
	        else
	        {
	            out.println(thisRow);
	            thisRow = indent + thisWord + " ";
	            rowLength = /* indentLength + */ wordLength;
	            continue; //
	        }
	        if (thisWord.endsWith("\r") || thisWord.endsWith("\n")) //
	        {	//
	            out.print(thisRow);
	            thisRow = indent;
	            rowLength = 0 /* indentLength */;
	            continue;
	        }
	    }
	    if (rowLength != indentLength)
	    {
	        out.println(thisRow);
	    }
	}
}
