/*
 * Created on Oct 7, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.sql.Timestamp;
import java.util.TimeZone;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserInfo extends NamedObject
{
	public static final int ADDRESS1 		= 0x00000001;
	public static final int ADDRESS2 		= 0x00000002;
	public static final int ADDRESS3 		= 0x00000004;
	public static final int ADDRESS4 		= 0x00000008;
	public static final int PHONENO1 		= 0x00000010;
	public static final int PHONENO2 		= 0x00000020;
	public static final int EMAIL1	 		= 0x00000040;
	public static final int EMAIL2	 		= 0x00000080;
	public static final int URL		 		= 0x00000100;
	public static final int CHARSET	 		= 0x00000200;
	public static final int LOCALE	 		= 0x00000400;
	public static final int TIMEZONE		= 0x00000800;
	
	private final String m_userid;
	private String m_address1;
	private String m_address2;
	private String m_address3;
	private String m_address4;
	private String m_phoneno1;
	private String m_phoneno2;
	private String m_email1;
	private String m_email2;
	private String m_url;
	private String m_charset;
	private final long[] m_flags = new long[4];
	private final long m_rights;
	private String m_locale;
	private TimeZone m_timeZone;
	private Timestamp m_created;
	private Timestamp m_lastlogin;
	
	private int m_changeMask = 0;
	
	public UserInfo(long id, String name, String userid, String address1, String address2,
		String address3, String address4, String phoneno1, String phoneno2, String email1,
		String email2, String url, String charset, long flags1, long flags2, long flags3, long flags4,
		long rights, String locale, String timeZone, Timestamp created, Timestamp lastlogin)
	{
		super(id, name);
		m_userid	= userid;
		m_address1 	= address1;
		m_address2 	= address2;
		m_address3 	= address3;
		m_address4 	= address4;
		m_phoneno1 	= phoneno1;
		m_phoneno2 	= phoneno2;
		m_email1 	= email1;
		m_email2 	= email2;
		m_url 		= url;
		m_charset	= charset;
		m_flags[0] 	= flags1;
		m_flags[1] 	= flags2;
		m_flags[2] 	= flags3;
		m_flags[2] 	= flags4;
		m_rights	= rights;
		m_locale 	= locale;
		m_timeZone	= timeZone != null ? TimeZone.getTimeZone(timeZone) : TimeZone.getDefault();
		m_created 	= created;
		m_lastlogin = lastlogin;
	}
	
	public String getAddress1()
	{
		return m_address1;
	}

	public String getAddress2()
	{
		return m_address2;
	}

	public String getAddress3()
	{
		return m_address3;
	}

	public String getAddress4()
	{
		return m_address4;
	}

	public String getEmail1()
	{
		return m_email1;
	}

	public String getEmail2()
	{
		return m_email2;
	}

	public long getFlags1()
	{
		return m_flags[0];
	}
	
	public long getFlags2()
	{
		return m_flags[1];
	}
	
	public long getFlags3()
	{
		return m_flags[2];
	}
	
	public long getFlags4()
	{
		return m_flags[3];
	}	
	
	public long[] getFlags()
	{
		return m_flags;
	}
	
	public long getRights()
	{
		return m_rights;
	}
	
	public boolean hasRights(long mask)
	{	
		return (m_rights & mask) == mask;
	}

	public String getLocale()
	{
		return m_locale;
	}
	
	public TimeZone getTimeZone()
	{
	    return m_timeZone;
	}

	public String getPhoneno1()
	{
		return m_phoneno1;
	}

	public String getPhoneno2()
	{
		return m_phoneno2;
	}

	public String getUrl()
	{
		return m_url;
	}

	public String getUserid()
	{
		return m_userid;
	}
	
	public String getCharset()
	{
		return m_charset;
	}
	
	public Timestamp getCreated()
	{
		return m_created;
	}

	public Timestamp getLastlogin()
	{
		return m_lastlogin;
	}
	
	public void setAddress1(String string)
	{
		if(!string.equals(m_address1))
		{
			m_changeMask |= ADDRESS1;
			m_address1 = string;
		}
	}

	public void setAddress2(String string)
	{
		if(!string.equals(m_address2))
		{
			m_changeMask |= ADDRESS2;
			m_address2 = string;
		}
	}

	public void setAddress3(String string)
	{
		if(!string.equals(m_address3))
		{
			m_changeMask |= ADDRESS3;
			m_address3 = string;
		}
	}

	public void setAddress4(String string)
	{
		if(!string.equals(m_address4))
		{
			m_changeMask |= ADDRESS4;
			m_address4 = string;
		}
	}

	public void setCharset(String string)
	{
		if(!string.equals(m_charset))
		{
			m_changeMask |= CHARSET;
			m_charset = string;
		}
	}

	public void setEmail1(String string)
	{
		if(!string.equals(m_email1))
		{
			m_changeMask |= EMAIL1;
			m_email1 = string;
		}
	}
	
	public void setEmail2(String string)
	{
		if(!string.equals(m_email2))
		{
			m_changeMask |= EMAIL2;
			m_email2 = string;
		}
	}
	
	public void setLocale(String string)
	{
		if(!string.equals(m_locale))
		{
			m_changeMask |= LOCALE;
			m_locale = string;
		}
	}

	public void setPhoneno1(String string)
	{
		if(!string.equals(m_phoneno1))
		{
			m_changeMask |= PHONENO1;
			m_phoneno1 = string;
		}
	}

	public void setPhoneno2(String string)
	{
		if(!string.equals(m_phoneno2))
		{
			m_changeMask |= PHONENO2;
			m_phoneno2 = string;
		}
	}

	public void setUrl(String string)
	{
		if(!string.equals(m_url))
		{
			m_changeMask |= URL;
			m_url = string;
		}
	}
	
	public void setTimeZone(TimeZone tz)
	{
		if(!tz.equals(m_timeZone))
		{
			m_changeMask |= TIMEZONE;
			m_timeZone = tz;
		}
	}
	
	public boolean testFlags(int flagword, long mask)
	{
	    return (m_flags[flagword] & mask) == mask;
	}
}
