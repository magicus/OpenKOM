/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SQLUtils
{
	public static long[] extractLongs(ResultSet rs, int index)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
			l.add(new Long(rs.getLong(index)));
		int top = l.size();
		long[] answer = new long[top];
		for(int idx = 0; idx < top; ++idx)
			answer[idx] = ((Long) l.get(idx)).longValue();
		return answer;
	}
	
	public static int[] extractInts(ResultSet rs, int index)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
			l.add(new Integer(rs.getInt(index)));
		int top = l.size();
		int[] answer = new int[top];
		for(int idx = 0; idx < top; ++idx)
			answer[idx] = ((Integer) l.get(idx)).intValue();
		return answer;
	}


	public static Name[] extractStrings(ResultSet rs, int nameIndex, int visibilityIndex, String pattern)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
		{
		    String name = rs.getString(nameIndex);
		    if(NameUtils.match(pattern, name, false))
		        l.add(new Name(name, rs.getShort(visibilityIndex)));
		}
		int top = l.size();
		Name[] answer = new Name[top];
		l.toArray(answer);
		return answer;
	}
	
	public static NameAssociation[] extractNames(ResultSet rs, int idIndex, int nameIndex, int visibilityIndex, String pattern)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
		{
		    String name = rs.getString(nameIndex);
		    if(NameUtils.match(pattern, name, false))
		        l.add(new NameAssociation(rs.getLong(idIndex), new Name(name, rs.getShort(visibilityIndex))));
		}
		int top = l.size();
		NameAssociation[] answer = new NameAssociation[top];
		l.toArray(answer);
		return answer;
	}	
}
