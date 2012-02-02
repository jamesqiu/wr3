package wr3.db;

import java.util.ArrayList;
import java.util.List;

import wr3.util.Stringx;

/**
 * 辅助生成sql语言的类
 * @author jamesqiu 2010-4-23
 */
public class Sqlx {

	/**
	 * 
	 * @param tableName
	 * @param fields 如："org, typ, sum(v1), count(*)"
	 * @return 如: 
	 * <pre> select org, typ, sum(v1), count(*)
	 * 	from tableName 
	 * 	group by org, typ 
	 * 	order by org, typ, sum(v1), count(*) </pre>
	 */
	public static String aggre(String tableName, String fields) {
		
		if (Stringx.nullity(tableName)) return "select 'nothing'";
		if (Stringx.nullity(fields)) return aggre(tableName);
		
		String filt = filt(fields);
		
		return Stringx.printf("select %s\n  from %s\n  group by %s\n  order by %s desc", 
				fields, tableName, filt, fields);
	}
	
	private static String filt(String fields) {
		
		String[] ff = Stringx.split(fields, ",");
		List<String> ff2 = new ArrayList<String>();
		
		for (String f : ff) {
			String f2 = Stringx.trim(f).toLowerCase();
			if (f2.startsWith("sum(") || f2.startsWith("avg(") || f2.startsWith("max(") || f2.startsWith("count")) {
				continue;
			} else {
				ff2.add(f);
			}
		}
		return Stringx.join(ff2, ",");
	}
	
	public static String aggre(String tableName) {
		
		if (Stringx.nullity(tableName)) return "select 'nothing'";
		
		return Stringx.printf("select count(*) from %s", tableName);
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("usage: \n" + "    Sqlx tableName \"a, b, sum(c), count(*)\"\n");
			return;
		}
		String tableName = args[0];
		String fields = (args.length>1) ? args[1] : null;
		System.out.println(aggre(tableName, fields));
	}
}
