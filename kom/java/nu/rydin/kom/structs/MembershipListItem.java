/*
 * Created on Nov 9, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MembershipListItem implements Serializable
{
	private NameAssociation m_conference;
	
	private int m_unread;

	public MembershipListItem(NameAssociation conference, int unread)
	{
		m_conference= conference;
		m_unread	= unread;	
	}
	
	public NameAssociation getConference()
	{
		return m_conference;
	}

	public int getUnread()
	{
		return m_unread;
	}
}
