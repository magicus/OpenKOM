/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.StringTokenizer;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NameUtils
{
    public static String normalizeNameKeepParanteses(String name)
    {
		int top = name.length();
		StringBuffer buffer = new StringBuffer(top);
		for(StringTokenizer st = new StringTokenizer(name, " "); st.hasMoreTokens();)
		{
		    buffer.append(st.nextToken().toUpperCase());
		    if(st.hasMoreTokens())
		        buffer.append(' ');
		}
		return buffer.toString();
    }
	/**
	 * Removes non-significant parts of a name. E.g. 
	 * "(The) KOM System (general discussions)" becomes "KOM SYSTEM".
	 * "Foo ((((((((is a) ))) bar" becomes "FOO ))) BAR"
	 * 
	 * @param name The name to normalize
	 * @return The normalized name
	 */
	public static String normalizeName(String name)
	{
		int top = name.length();
		StringBuffer buffer = new StringBuffer(top);
		boolean significant = true;
		boolean skipSpace = true;
		for(int idx = 0; idx < top; ++idx)
		{
			char ch = name.charAt(idx);
			if(significant)
			{
				if(ch == ' ')
				{
					if(skipSpace)
						continue;
					skipSpace = true;
				}
				else
					skipSpace = false;
				if(ch == '(')
					significant = false;
				else
					buffer.append(Character.toUpperCase(ch));
			}
			else
			{
				if(ch == ')')
				{
					significant = true;
					skipSpace = true;
				}
			}
		}
		return buffer.toString();
	}
	
	public static String[] splitName(String name)
	{
		StringTokenizer st = new StringTokenizer(normalizeName(name));
		int top = st.countTokens();
		String[] answer = new String[top];
		for(int idx = 0; idx < top; ++idx)
			answer[idx] = st.nextToken();
		return answer;
	}
	
	public static String[] splitNameKeepParenteses(String name)
	{
		StringTokenizer st = new StringTokenizer(name);
		int top = st.countTokens();
		String[] answer = new String[top];
		for(int idx = 0; idx < top; ++idx)
			answer[idx] = st.nextToken().toUpperCase();
		return answer;
	}
	
	public static String assembleName(String[] names)
	{
		StringBuffer sb = new StringBuffer(200);
		int top = names.length;
		for(int idx = 0; idx < top; ++idx)
		{
			sb.append(names[idx]);
			if(idx < top - 1)
				sb.append(' ');		
		}
		return sb.toString();
	}
	
	public static boolean match(String pattern, String candidate)
	{
		String[] patternParts = splitName(normalizeName(pattern));
		String[] candidateParts = splitName(normalizeName(candidate));
		int pTop = patternParts.length;
		int cTop = patternParts.length;
		
		// Pattern has more parts than candidate? Not a match!
		//
		if(pTop > cTop)
			return false;
		
		// Check if every token in "pattern" is a substring of every token in "candidate"
		//
		for(int idx = 0; idx < pTop; ++idx)
		{
			if(!candidateParts[idx].startsWith(patternParts[idx]))
				return false;
		}
		
		// If we made it this far, everything matched!
		//
		return true;
	}
	
	public static String stripSuffix(String name)
	{
		int p = name.indexOf('/'); 
		return p == -1 ? name : name.substring(0, p > 0 ? p - 1 : 0).trim();
	}
	
	public static String addSuffix(String name, String suffix)
	{
		name = stripSuffix(name);
		return suffix.length() != 0 ? name + " /" + suffix : name;
	}
}
