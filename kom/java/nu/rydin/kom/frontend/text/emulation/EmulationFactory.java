/*
 * Created on Jul 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.emulation;

import java.util.HashMap;
import java.util.ResourceBundle;
import nu.rydin.kom.exceptions.GenericException;
/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class EmulationFactory 
{
	private static final HashMap<String, Emulator> knownEmulators;

	static
	{
		knownEmulators = new HashMap<String, Emulator>();
	}

	public static Emulator getEmulator(String name) throws GenericException
	{
		if (!knownEmulators.containsKey(name))
		{
			try
			{
				String path = ResourceBundle.getBundle("nu.rydin.kom.frontend.text.emulation").getString(name);
				knownEmulators.put(name, new Emulator(name, path));
			}
			catch (Exception e)
			{
				throw new GenericException (e.getMessage());
			}
		}
		
		return knownEmulators.get(name); 
	}
}
