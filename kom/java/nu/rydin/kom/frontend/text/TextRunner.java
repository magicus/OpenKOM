/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.util.HashMap;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TextRunner
{
	public static void main(String[] args)
	{
		try
		{
			ClientSession server = new ClientSession(System.in, System.out, false, false, "localhost", new HashMap<String, String>());
			server.run();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
