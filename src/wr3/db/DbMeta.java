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
 * ͨ�����ݿ�Connection�õ�Metadata��Ϣ.
 * ����function, columns, column type, data type.
 * ��,����ļ�meta��DbServer�л�ȡ(product, database, tables, views)
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
	 * �õ�sql��ѯ����ĸ�columns��Ϣ
	 * @param sql
	 * @return
	 */
	public Table columnsOfResultset(String sql) {
		// TODO, can use DbServer to implements.
		return null;
	}

	/**
	 * �õ�һ���������columns��Ϣ
	 * @param tablename ע�����ִ�Сд����Щ���ݿ�Ա�����Сд���С�
	 * @return {�к�, ����, ����, ��С, JDBC���ͺ� ,JDBC����}
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
	 * ��SQL:2003�Ĺؼ���
	 * @return ��","�ָ�����з���keywords.
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
	 * �õ�ָ�����͵����к����б�
	 * @param t FUNCTION.String | .Number | .System | .Datetime
	 * @return ��","�ָ������function����.
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
	 * ���ݷ������͵õ�JDBC���ͺ�
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
	 * �õ����ݿ����з������Ͷ�Ӧ��JDBC����.
	 * ���ܶ����������<->JDBC����.
	 * ���ݿ�A,B֮��Ķ������޷�һһȷ����, ��Ϊ:
	 *  ���ݿ�A����<*--1>JDBC����<1--*>���ݿ�B����
	 *  Hibernate����dialect�����ֹ�ָ��Ϊ1<->1��Ӧ
	 * </pre>
	 * @return {��������, JDBC���ͺ�, JDBC����}
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
	 * �õ����ݿ���create sql.
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
	 * ��Ҫָ����С������, ��varchar(10), numeric(10,2)
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
	 * �õ������ݿ���������ԵĽ���DDL���
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
			// �õ���Ӧ�ķ�������, ��û�о�ʹ�ñ����ݿ������
			try {
				typename = dialect.getTypeName(typecode);
			} catch (HibernateException e) {
				typename = type;
			}
			// ��Ҫ�滻���ʹ�Сֵ��
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
				// ��Ҫ������ʹ�Сֵ��
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
	 * �������ݿ�����, ����columns Table�еõ������DDL
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
	 * ����JDBC���ͺ�,�õ�JDBC��������.
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
