/*
 * Created on Jan 18, 2008
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.LocalTextNumberParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ListReaders extends AbstractCommand
{
    public ListReaders(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new LocalTextNumberParameter(false) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
    throws KOMException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        NameAssociation list[];
        if (null == parameterArray[0])
        {
            list = context.getSession().listReaders(null);
        }
        else
        {
            MessageLocator ml = new MessageLocator(-1);
            ml.setLocalnum(((Integer) parameterArray[0]).intValue());
            list = context.getSession().listReaders(ml);
        }
                
        int len = list.length;
        if (0 == len)
        {
            out.println(formatter.format("list.readers.none"));
            return;
        }
        
        for (int i = 0; i < len; ++i)
        {
            out.println(context.formatObjectName(list[i].getName(), list[i].getId()));
        }
        out.println();
        out.println(formatter.format("list.readers.sum", len));
    }
}
