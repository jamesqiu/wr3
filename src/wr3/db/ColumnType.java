package wr3.db;

import java.util.Arrays;
import java.util.List;

import wr3.Table;
import wr3.util.Stringx;

/**
 * 通过字段类型名称判断是否数字类型、整数类型、小数类型
 * @author jamesqiu 2009-9-24
 * @see com.webreport.rdb.DataType
 */
public class ColumnType {

	private final static List<String> longTypes = Arrays.asList(
			"int", 
			"integer",
			"smallint", 
			"tinyint",
			"bigint" );
	
	private final static List<String> doubleTypes = Arrays.asList(
			"number",
			"numeric", 
			"decimal", 
			"float", 
			"real", 
			"double",
			"money",
			"smallmoney",
			"double precision",
			"double precis" );
	
	/**
	 * 判断字段类型是否数字(Double或者Integer)
	 * @param columnTypeName
	 */
	public static boolean isNumber(String columnTypeName) {
		
		return isLong(columnTypeName) || isDouble(columnTypeName);
	}
	
	/**
	 * 判断字段类型是否无小数点整型(Integer, Long, ...),
	 * 为了防止溢出，使用Long而非Integer.
	 * @param columnTypeName
	 * @return
	 */
	public static boolean isLong(String columnTypeName) {
		
		if (Stringx.nullity(columnTypeName)) return false;
		
		return longTypes.contains(columnTypeName.toLowerCase());
	}
	
	/**
	 * 判断字段类型是否有小数点数字型(Double, Float, ...)
	 * @param columnTypeName
	 * @return
	 */
	public static boolean isDouble(String columnTypeName) {

		if (Stringx.nullity(columnTypeName)) return false;
		
		return doubleTypes.contains(columnTypeName.toLowerCase());
	}
	
	/**
	 * 辅助类，打印数据库表的字段类型
	 * @param dbname 
	 * @param tablename 数据库表名
	 */
	public static void helper(String dbname, String tablename) {
		
		DbServer dbs = DbServer.create(dbname);
		DbMeta meta = dbs.meta();
		Table table = meta.columns(tablename);
		for (int i = 0; i < table.rows(); i++) {
			boolean isNumber = isNumber(table.cell(i, 5).value());
			System.out.print(isNumber ? "-N-" : "---");
			System.out.println(table.row(i));
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("打印数据库表字段类型, usage: \n" + 
					"    ColumnType dbname tablename\n" +
					"    如：ColumnType abs_grails Types2");
			return;
		}
		helper(args[0], args[1]);
	}
}
