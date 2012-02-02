package wr3.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.syntax.Types;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Stringx;

/**
 * <pre>
 * 通过数据库Connection得到Metadata信息.
 * 包括function, columns, column type, data type.
 * 库,表级别的简单meta在DbServer中获取(product, database, tables, views)
 * usage:
 *   dbs = DbServer.create();
 *   meta = dbs.meta();
 *   meta = DbMeta.create(conn);
 *   table = meta.columns();
 *   dbs.close();
 *
 * </pre>
 * @author jamesqiu 2009-1-17
 *
 */
public class DbMeta {

	private DatabaseMetaData meta;

	private DbMeta() { }

	public static DbMeta create(Connection conn) {
		DbMeta o = new DbMeta();
		if (conn==null) return o;
		try {
			o.meta = conn.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 得到sql查询结果的各columns信息
	 * @param sql
	 * @return
	 */
	public Table columnsOfResultset(String sql) {
		// TODO, can use DbServer to implements.
		return null;
	}

	/**
	 * 得到一个表的所有columns信息
	 * @param tablename 注意区分大小写，有些数据库对表名大小写敏感。
	 * @return {列号, 列名, 类型, 大小, JDBC类型号 ,JDBC类型}
	 */
	public Table columns(String tablename) {

		Table table = new Table();

		if (meta==null) return table;

		String[] colnames = {
				"ORDINAL_POSITION",
				"COLUMN_NAME",
				"TYPE_NAME",
				"COLUMN_SIZE",
				"DATA_TYPE",
				"JDBC_TYPE" };

		Row head = new Row();
		for (int i = 0; i < colnames.length; i++) {
			head.add(new Cell(colnames[i]));
		}
		table.head(head);

		try {
			ResultSet result = meta.getColumns(null, null, tablename, null);
			while (result.next()) {
				Row row = new Row();
				row.add(new Cell(result.getInt(colnames[0])));
				row.add(new Cell(result.getString(colnames[1])));
				row.add(new Cell(result.getString(colnames[2])));
				row.add(new Cell(result.getInt(colnames[3])));
				row.add(new Cell(result.getInt(colnames[4])));
				row.add(new Cell(jdbcType(result.getInt(colnames[4]))));
				table.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * 非SQL:2003的关键字
	 * @return 用","分割的所有方言keywords.
	 */
	public String keywords() {

		if (meta==null) return "";

		try {
			return meta.getSQLKeywords();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public enum FUNCTION {String, Number, Datetime, System};
	/**
	 * 得到指定类型的所有函数列表
	 * @param t FUNCTION.String | .Number | .System | .Datetime
	 * @return 用","分割的所有function名称.
	 */
	public String functions(FUNCTION t) {

		if (meta==null) return "";

		try {
			if (t==FUNCTION.String) return meta.getStringFunctions();
			if (t==FUNCTION.Number) return meta.getNumericFunctions();
			if (t==FUNCTION.Datetime) return meta.getTimeDateFunctions();
			if (t==FUNCTION.System) return meta.getSystemFunctions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 根据方言类型得到JDBC类型号
	 * @param type
	 * @return
	 */
	public int datatype(String type) {

		try {
			ResultSet result = meta.getTypeInfo();
			while (result.next()) {
	            String typeName = result.getString("TYPE_NAME");
	            if (typeName.equalsIgnoreCase(type)) {
	            	int dataType = result.getInt("DATA_TYPE");
	            	return dataType;
	            }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Types.UNKNOWN;
	}

	/**
	 * <pre>
	 * 得到数据库所有方言类型对应的JDBC类型.
	 * 可能多个方言类型<->JDBC类型.
	 * 数据库A,B之间的对于是无法一一确定的, 因为:
	 *  数据库A类型<*--1>JDBC类型<1--*>数据库B类型
	 *  Hibernate是在dialect包中手工指定为1<->1对应
	 * </pre>
	 * @return {方言类型, JDBC类型号, JDBC类型}
	 */
	public Table datatypes() {

		Table table = new Table();

		if (meta==null) return table;

		String[] colnames = {
				"TYPE_NAME",
				"DATA_TYPE",
				"JDBC_TYPE"};

		Row head = new Row();
		for (int i = 0; i < colnames.length; i++) {
			head.add(new Cell(colnames[i]));
		}
		table.head(head);

		try {
			ResultSet result = meta.getTypeInfo();
			while (result.next()) {
				Row row = new Row();
	            String typeName = result.getString(colnames[0]);
	            // Get the java.sql.Types type to which this database-specific type is mapped
	            int dataType = result.getInt(colnames[1]);
	            String jdbcType = jdbcType(dataType);
				row.add(new Cell(typeName));
				row.add(new Cell(dataType));
				row.add(new Cell(jdbcType));
				table.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return table;
	}

	/**
	 * 得到数据库表的create sql.
	 * @param tablename
	 * @return
	 */
	public String ddl(String tablename) {

		Table table = columns(tablename);
		StringBuilder sb = new StringBuilder("create table " + tablename + " (\n");
		int rows = table.rows();
		for (int i = 0; i < rows; i++) {
			String colname = table.cell(i, 1).value();
			String type = table.cell(i, 2).value();
			int size = table.cell(i, 3).intValue();
			sb.append("\t"+colname+" "+type);
			if (needSize(type)) sb.append("("+size+")");
			if (i!=(rows-1)) sb.append(",\n");
		}
		sb.append("\n)");
		return sb.toString();
	}

	/**
	 * 需要指定大小的类型, 如varchar(10), numeric(10,2)
	 * @param type
	 * @return
	 */
	private boolean needSize(String type) {
		type = type.toLowerCase();
		return Stringx.in(type, new String[]{
			"char", "varchar", "nvarchar",
			"numeric",
			"bit", "interval"
		});
	}

	/**
	 * 得到本数据库表其他方言的建表DDL语句
	 * @param tablename
	 * @param dialect
	 * @return
	 */
	public String ddl(String tablename, Dialect dialect) {

		Table table = columns(tablename);
		StringBuilder sb = new StringBuilder("create table " + tablename + " (\n");
		int rows = table.rows();
		for (int i = 0; i < rows; i++) {
			String colname = table.cell(i, 1).value();
			sb.append("\t"+colname+" ");
			String type = table.cell(i, 2).value();
			int size = table.cell(i, 3).intValue();
			int typecode = table.cell(i, 4).intValue();
			String typename;
			// 得到对应的方言类型, 若没有就使用本数据库的类型
			try {
				typename = dialect.getTypeName(typecode);
			} catch (HibernateException e) {
				typename = type;
			}
			// 需要替换类型大小值的
			if (typename.indexOf("$l")!=-1) {
				sb.append(Stringx.replace(typename, "$l", ""+size));
			} else if (typename.indexOf("$p,$s")!=-1) {
				sb.append(Stringx.replace(typename, "$p,$s", ""+size+",0"));
			} else if (typename.indexOf("$p, $s")!=-1) {
				sb.append(Stringx.replace(typename, "$p, $s", ""+size+", 0"));
			} else if (typename.equals("char(1)")) {
				sb.append(Stringx.replace(typename, "(1)", "("+size+")"));
			} else {
				sb.append(typename);
				// 需要添加类型大小值的
				if (needSize(typename)) {
					sb.append("("+size+")");
				}
			}
			if (i!=(rows-1)) sb.append(",\n");
		}
		sb.append("\n)");
		return sb.toString();
	}

	/**
	 * 不用数据库连接, 仅从columns Table中得到所需的DDL
	 * @param tablename
	 * @param columns
	 * @param dialect
	 * @return
	 */
	public static String ddl(String tablename, Table columns, Dialect dialect) {
		// TODO
		return null;
	}

	private static Map<Integer, String> map = new HashMap<Integer, String>();
	static {
        // Get all field in java.sql.Types
        Field[] fields = java.sql.Types.class.getFields();
        for (int i=0; i<fields.length; i++) {
            String name = fields[i].getName();
            Integer value = null;
			try {
				value = (Integer)fields[i].get(null);
				map.put(value, name);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
        }
	};
	/**
	 * 根据JDBC类型号,得到JDBC类型名称.
	 * This method returns the name of a JDBC type.
     * Returns null if dataType is not recognized.
	 * @param dataType
	 * @return
	 */
    public static String jdbcType(int dataType) {
        // Return the JDBC type name
        return (String)map.get(Integer.valueOf(dataType));
    }

}
