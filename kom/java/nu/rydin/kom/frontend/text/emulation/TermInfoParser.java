/*
 * Created on Jul 20, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.emulation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import nu.rydin.kom.exceptions.KOMRuntimeException;

public class TermInfoParser
{
	private HashMap<String, String> keyValueMap;
	public TermInfoParser(String input) throws KOMRuntimeException 
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(input));
			keyValueMap = new HashMap<String, String>();
			String data;
			while (null != (data = br.readLine()))
			{
				data = data.trim();
				
				// Simple filter. Filtering lines starting with a hash is obvious. If we come across a line that doesn't contain an equals sign,
				// we're probably looking at either the description ("ansi|ansi/pc-term compatible") or a capability line ("OTbs, am, mc5i, mir, ...")
				// which we're not really interested in since the user will have defined the capabilities of his/her terminal elsewhere.
				// 
				if (data.startsWith("#") ||
					(-1 == data.indexOf("=")))
				{
					continue;
				}
				String[] tokens = data.split(",");
				for (int i = 0; i < tokens.length; ++i)
				{
					String[] keyValuePair = tokens[i].split("=");
					if (1 == keyValuePair.length)
					{
						// Despite our efforts at filtering out things that are not key-value pairs, one has slipped through.
						//
						continue;
					}
					if ('.' == keyValuePair[0].charAt(0))
					{
						// The capability is commented out, so let's ignore it.
						//
						continue;
					}
					keyValueMap.put(keyValuePair[0], fiddleWithInputHelper(keyValuePair[1]));
				}
			}
			
			// We're done!
			//
			br.close();
		}
		catch (Exception e)
		{
			throw new KOMRuntimeException(e.getMessage());
		}
	}
	
	private String fiddleWithInputHelper (String input)
	{
		int i = 0;
		int sideStep;
		String temp = "";
		char c;
		while (i < input.length())
		{
			sideStep = 1;
			c = input.charAt(i);	// default behavior: pass through
			switch (c)
			{
				case '^':
					c = (char)(input.charAt(i+1)-'@');
					sideStep = 2;
					break;
				
				case '\\':
					sideStep = 2;
					switch (input.charAt(i+1))
					{
						case 'e':
						case 'E':
							c = '\u001b';
							break;
						case '^':
							c = '^';
							break;
						case '\\':
							c = '\\';
							break;
						case ',':
							c = ',';
							break;
						case ':':
							c = ':';
							break;
						case '0':
							//
							// TODO: Rewrite \0 handling.
							//
							// (Ugly code. May walk out of bounds. Needs to
							// be able to handle \0#, even though it's not a
							// legal sequence AFAIK (someone's bound to do it)).
							//
							if (!Character.isDigit(input.charAt(i+2)))
							{
								// We're just looking at \0 followed by a non-
								// digit. This must be replaced with \200.
								//
								c = '\u0080';
							}
							else
							{
								// It's a three-digit octal number. If it isn't,
								// we're fsck(1)'ed.
								//
								c = (char)(Integer.parseInt(input.substring(i+1, i+4), 8));
								sideStep = 4;
							}
							break;
					}

				case '$':
					int j = i;
					while ('>' != input.charAt(j))
					{
						++j;
					}
					sideStep = (j - i);
					break;

				default:
					// sideStep = 1;
					break;
					
			}
			temp += c;
			i += sideStep;
		}
		
		// Fulkod.
		//
		return temp.replaceAll("$", "");
	}
	
	public HashMap<String, String> getEncodingMap()
	{
		return keyValueMap;
	}
	
	public String getEncoding(String name)
	{
		return keyValueMap.get(name);
	}
}