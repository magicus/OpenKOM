/*
 * Created on Oct 7, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

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
	private final long m_flags;
	private final long m_rights;
	private String m_locale;
	
	private int m_changeMask = 0;
	
	public UserInfo(long id, String name, String userid, String address1, String address2,
		String address3, String address4, String phoneno1, String phoneno2, String email1,
		String email2, String url, String charset, long flags, long rights, String locale)
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
		m_flags 	= flags;
		m_rights	= rights;
		m_locale 	= locale;
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

	public long getFlags()
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
}