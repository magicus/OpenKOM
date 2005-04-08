/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.ClientSettings;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CharacterSetParameter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeCharacterset extends AbstractCommand
{
	public ChangeCharacterset(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new CharacterSetParameter(true, getCharacterSets()) }, permissions );
	}
	
    public void execute(Context context, Object[] parameterArray) throws KOMException, IOException
	{
        Integer index = (Integer) parameterArray[0];
        String charSet = getCharacterSets()[index.intValue()];
        
		context.getSession().updateCharacterset(charSet);
		// Either we got a new charset or we resetting the old one.
		//
		context.getOut().setCharset(charSet);
		context.getIn().setCharset(charSet);
	}

    public static String[] getCharacterSets()
    {
        StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
        ArrayList list = new ArrayList();
        
        while(st.hasMoreTokens())
        {
        	String name = st.nextToken();
        	list.add(name);
        }
        
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }
}
