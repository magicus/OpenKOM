/*
 * Created on Jul 21, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Henrik Schröder
 */
public class Logger extends Object
{
	private org.apache.log4j.Logger logger;

	static
	{
		try
		{
			Logger.initialize();
		}
		catch (Exception e)
		{
			System.err.println("Exception when initializing logging...");
			e.printStackTrace();
		}
	}

	public static void initialize() throws Exception
	{
		//The name of the logging config file shall be log.xml, log.xml
		//is the name of the config file, the whole config file and
		//nothing but the config file, woe unto thee if there be no
		//config file named thusly, and woe unto your children, and your
		//children's children, and you children's children's children and so on
		//and so forth for seven and one generations or until you don't think
		//it's very funny anymore.

		//TODO: TRÖTT I NÄSAN. ORKAR INTE FIXA JUST NU.
		DOMConfigurator.configureAndWatch("D:/eclipse/workspace/kom/java/log.xml");

		/*
		 // First, see if there are any appenders on the root category.
		 //
		 Enumeration appenders = org.apache.log4j.Logger.getRoot().getAllAppenders();
		 if(!(appenders instanceof NullEnumeration))
		 {
		 // Using already-supplied configuration
		 return;
		 }

		 // Create a hardcoded default configuration.
		 //
		 Document document = Logger.createDefaultConfiguration();

		 if(document != null)
		 {
		 DOMConfigurator.configure(document.getDocumentElement());
		 }
		 else
		 {
		 BasicConfigurator.configure();
		 }
		 */
	}
	
	public static boolean isDebugEnabled(Object invoker)
	{
		return Logger.getInstance(invoker).isDebugEnabled();
	}

	public static void debug(Object invoker)
	{
		Logger.getInstance(invoker).debug(null);
	}

	public static void debug(Object invoker, Object message)
	{
		Logger.getInstance(invoker).debug(message);
	}

	public static void debug(Object invoker, Throwable throwable)
	{
		Logger.getInstance(invoker).debug(null, throwable);
	}

	public static void debug(Object invoker, Object message, Throwable throwable)
	{
		Logger.getInstance(invoker).debug(message, throwable);
	}
	
	public static void info(Object invoker)
	{
		Logger.getInstance(invoker).info(null);
	}

	public static void info(Object invoker, Object message)
	{
		Logger.getInstance(invoker).info(message);
	}

	public static void info(Object invoker, Throwable throwable)
	{
		Logger.getInstance(invoker).info(null, throwable);
	}

	public static void info(Object invoker, Object message, Throwable throwable)
	{
		Logger.getInstance(invoker).info(message, throwable);
	}

	public static void warn(Object invoker)
	{
		Logger.getInstance(invoker).warn(null);
	}

	public static void warn(Object invoker, Object message)
	{
		Logger.getInstance(invoker).warn(message);
	}

	public static void warn(Object invoker, Throwable throwable)
	{
		Logger.getInstance(invoker).warn(null, throwable);
	}

	public static void warn(Object invoker, Object message, Throwable throwable)
	{
		Logger.getInstance(invoker).warn(message, throwable);
	}

	public static void error(Object invoker)
	{
		Logger.getInstance(invoker).error(null);
	}

	public static void error(Object invoker, Object message)
	{
		Logger.getInstance(invoker).error(message);
	}

	public static void error(Object invoker, Throwable throwable)
	{
		Logger.getInstance(invoker).error(null, throwable);
	}

	public static void error(Object invoker, Object message, Throwable throwable)
	{
		Logger.getInstance(invoker).error(message, throwable);
	}

	public static void fatal(Object invoker)
	{
		Logger.getInstance(invoker).fatal(null);
	}

	public static void fatal(Object invoker, Object message)
	{
		Logger.getInstance(invoker).fatal(message);
	}

	public static void fatal(Object invoker, Throwable throwable)
	{
		Logger.getInstance(invoker).fatal(null, throwable);
	}

	public static void fatal(Object invoker, Object message, Throwable throwable)
	{
		Logger.getInstance(invoker).fatal(message, throwable);
	}

	private static org.apache.log4j.Logger getInstance(Object invoker)
	{
		Class invokerClass = (invoker instanceof Class) ? (Class)invoker
				: invoker.getClass();
		return (org.apache.log4j.Logger)org.apache.log4j.Logger
				.getInstance(invokerClass.getName());
	}

	private static Document createDefaultConfiguration() throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document defaultConfiguration = db.newDocument();

		Element element = defaultConfiguration.createElementNS(
				"http://jakarta.apache.org/log4j/", "log4j:configuration");

		defaultConfiguration.appendChild(element);
		Element appender = defaultConfiguration.createElement("appender");
		Element layout = defaultConfiguration.createElement("layout");
		Element category = defaultConfiguration.createElement("category");
		Element root = defaultConfiguration.createElement("root");

		Element param = defaultConfiguration.createElement("param");
		appender.setAttribute("name", "STDOUT");
		appender.setAttribute("class", ConsoleAppender.class.getName());

		param = defaultConfiguration.createElement("param");
		appender.appendChild(param);
		param.setAttribute("name", "Target");
		param.setAttribute("value", ConsoleAppender.SYSTEM_OUT);

		appender.appendChild(layout);
		layout.setAttribute("class", "org.apache.log4j.PatternLayout");

		param = defaultConfiguration.createElement("param");
		layout.appendChild(param);
		param.setAttribute("value", "[%-5p][%c] %x%m%n");
		param.setAttribute("name", "ConversionPattern");

		Element priority = defaultConfiguration.createElement("priority");
		root.appendChild(priority);
		priority.setAttribute("value", "warn");

		Element appenderRef = defaultConfiguration
				.createElement("appender-ref");
		root.appendChild(appenderRef);
		appenderRef.setAttribute("ref", "STDOUT");

		element.appendChild(appender);
		element.appendChild(category);
		element.appendChild(root);

		return defaultConfiguration;
	}
}
