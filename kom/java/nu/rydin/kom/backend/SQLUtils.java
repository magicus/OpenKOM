/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	
	public static String[] extractStrings(ResultSet rs, int index)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
			l.add(rs.getString(index));
		int top = l.size();
		String[] answer = new String[top];
		l.toArray(answer);
		return answer;
	}
	
	public static NameAssociation[] extractNames(ResultSet rs, int idIndex, int nameIndex)
	throws SQLException
	{
		List l = new ArrayList();
		while(rs.next())
			l.add(new NameAssociation(rs.getLong(idIndex), rs.getString(nameIndex)));
		int top = l.size();
		NameAssociation[] answer = new NameAssociation[top];
		l.toArray(answer);
		return answer;
	}	
}
