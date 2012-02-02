package wr3.db;

import java.util.Arrays;
import java.util.List;

import wr3.Table;
import wr3.util.Stringx;

/**
 * ͨ���ֶ����������ж��Ƿ��������͡��������͡�С������
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
	 * �ж��ֶ������Ƿ�����(Double����Integer)
	 * @param columnTypeName
	 */
	public static boolean isNumber(String columnTypeName) {
		
		return isLong(columnTypeName) || isDouble(columnTypeName);
	}
	
	/**
	 * �ж��ֶ������Ƿ���С��������(Integer, Long, ...),
	 * Ϊ�˷�ֹ�����ʹ��Long����Integer.
	 * @param columnTypeName
	 * @return
	 */
	public static boolean isLong(String columnTypeName) {
		
		if (Stringx.nullity(columnTypeName)) return false;
		
		return longTypes.contains(columnTypeName.toLowerCase());
	}
	
	/**
	 * �ж��ֶ������Ƿ���С����������(Double, Float, ...)
	 * @param columnTypeName
	 * @return
	 */
	public static boolean isDouble(String columnTypeName) {

		if (Stringx.nullity(columnTypeName)) return false;
		
		return doubleTypes.contains(columnTypeName.toLowerCase());
	}
	
	/**
	 * �����࣬��ӡ���ݿ����ֶ�����
	 * @param dbname 
	 * @param tablename ���ݿ����
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
			System.out.println("��ӡ���ݿ���ֶ�����, usage: \n" + 
					"    ColumnType dbname tablename\n" +
					"    �磺ColumnType abs_grails Types2");
			return;
		}
		helper(args[0], args[1]);
	}
}
