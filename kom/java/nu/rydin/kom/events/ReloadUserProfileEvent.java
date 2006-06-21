/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReloadUserProfileEvent extends SingleUserEvent
{
	public ReloadUserProfileEvent(long targetUser)
	{
		super(-1, targetUser);
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent(this);
	}
}
