/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ReevaluateDefaultEvent extends Event 
{
	private long m_confId = -1;
	
	public ReevaluateDefaultEvent() 
	{
		super();
	}

	public ReevaluateDefaultEvent(long user) 
	{
		super(user);
	}
	
	public ReevaluateDefaultEvent(long user, long conf)
	{
		super(user);
		m_confId = conf;
	}
}
