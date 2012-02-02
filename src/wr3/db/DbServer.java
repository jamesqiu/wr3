package wr3.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * �Թ�ϵ���ݿ��һ��SQL����ʵ��.
 * ��װͨ��jdbc/jndi��Connection�����ݿ�����в���.
 * ע��: <span class=zd>
 * (1) ��ص���close()�����ر���Դ.
 * (2) DbServer�Ĵ󲿷ַ����Ƿ��̰߳�ȫ��(�������statement, result),
 *     ��Ķ���̲߳�Ҫͬʱ����ͬһ��DbServer����ķ���,
 *     Ӧ��ÿ���߳�ʹ���Լ���DbServer����.</span>
 * usage:
 *  // �õ�Table���
 *  ds = DbServer.create();
 *  table = ds.data("select top 100 * from cust");
 *  table = ds.subdata("select * form cust", from, to);
 *  ds.close();
 *
 *  // ���д���ÿ�н��
 *  ds = DbServer.create ("mydb");
 *  filter = new DataFilter() {
 *  	public void process(String[] data) { ... }
 *  };
 *  ds.process(sql, filter);
 *  ds.close();
 *
 * <style>.zd {color: red}</style>
 * </pre>
 * @author jamesqiu 2009-1-10
 */
public class DbServer {

	/**
	 * ȡ�����ΪTableʱ,ȱʡ��¼����Ŀ�������.
	 */
	final static int MAX_ROWS = 10000;
	private int maxRows = MAX_ROWS;

	/**
	 * ������Դ��Connection, ͨ��connect()��, close()�ر�
	 */
	private Connection conn;
	/**
	 * Connection��SQL statement
	 */
	private Statement  statement;
	/**
	 * Statement��һ���α������
	 */
	private ResultSet  result;
	private ResultSetMetaData meta;

	public DbServer() {
	}

	/**
	 * ����ȱʡ��dataSource
	 * @return
	 */
	public static DbServer create() {
		return create("");
	}

	/**
	 * ����ָ�����Ƶ�dataSource(DbConfig�ж���)
	 * @param dbname
	 * @return
	 */
	public static DbServer create(String dbname) {
		DbServer server = new DbServer();
		server.connect(dbname);
		return server;
	}

	/**
	 * ֱ������Connection, ��ʹ��dbname
	 * @param conn
	 */
	public static DbServer create(Connection conn) {
		DbServer server = new DbServer();
		server.conn = conn;
		return server;
	}

	/**
	 * ��Connection.
	 * @param dbname
	 */
	private void connect(String dbname) {
		DbSource ds = new DbSource();
		if (Stringx.nullity(dbname)) {
			conn = ds.connect();
		} else {
			conn = ds.connect(dbname);
		}
	}

	/**
	 * �ر�Connection
	 */
	public void close() {
		closeResultset();
		closeStatement();
		closeConnection();
		result = null;
		statement = null;
		conn = null;
	}

	private void closeResultset() {
		try {
			if (result != null)
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void closeStatement() {
		try {
			if (statement != null)
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void closeConnection() {
		try {
			if (conn != null) conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ȡ���ݿ�Connection
	 * @return
	 */
	public Connection connection() {
		return conn;
	}

	/**
	 * <pre>
	 * Set max rows of Table object.
	 * </pre>
	 * @param maxRows default value is MAXROWS, <=0 means MAX_ROWS limit, CAN'T > MAX_ROWS.
	 */
	public void maxRows (int maxRows) {
		if (maxRows <= 0 || maxRows > MAX_ROWS)
			maxRows = MAX_ROWS;
		this.maxRows = maxRows;
	}

	/**
	 * ִ�в�ѯ����, �������Table.
	 * ���maxRows���ù���, �Զ����ƽ��ΪMAX_ROWS����.
	 * @param sql
	 * @return ��Ž����Table
	 */
	public Table query(String sql) {
		return query(sql, 1, maxRows);
	}

	/**
	 * <pre>
	 * ִ�в�ѯ����, ��ָ����index�Ľ������Table.
	 *  query(sql, 1, 10);  // ȡǰ10��
	 * </pre>
	 * @param sql
	 * @param beginIndex 1 first row, ������Χ�򱨴�
	 * @param n Ŀ���¼��, ���������صļ�¼������С��n; n>=0, n=0ʱֻȡ������.
	 * @return
	 */
	public Table query(String sql, int beginIndex, int n) {

		if (conn==null || Stringx.nullity(sql))
			return new Table();

		if (!initStatement())
			return new Table();

		try {
			result = statement.executeQuery(sql);
			meta = result.getMetaData();
			Table table = asTable(beginIndex, n);
			return table;
		} catch (SQLException e) {
			e.printStackTrace();
			return new Table();
		}
	}

	/**
	 * ִ��"��/ɾ/��"����, ���ΪӰ�������
	 * @param sql
	 * @return 0 ��ʾû�в����κ�����
	 */
	public int update(String sql) {

		if (conn==null || Stringx.nullity(sql))
			return 0;
		if (!initStatement()) return 0;
		int n;
		try {
			n = statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		closeStatement();
		return n;
	}

	/**
	 * ִ�д洢����.
	 * @param sqls
	 * @return ����ǽ����, ����Table����; �����update, ���ش���ɹ��ļ�¼��.
	 */
	public Table procedure(String sqls) {

		if (conn==null || Stringx.nullity(sqls))
			return new Table();
		if (!initStatement()) return new Table();
		// ִ��query�õ�ResultSet
		try {
			boolean returnResultset = statement.execute(sqls);
			if (returnResultset) {
				result = statement.getResultSet();
				return asTable(1, maxRows);
			} else {
				int n = statement.getUpdateCount();
				return new Table(1, 1).cell(0, 0, new Cell(n));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new Table();
		}
	}

	/**
     * ���� ResultsetFilter ����sql�Ĳ�ѯ���, ��ͷ��ΪString[]����,
     * ÿһ����ΪResultSet����.
	 * @param sql sql���
	 * @param filter ResultsetFilter��ʵ�������
	 */
	public void process(String sql, ResultsetFilter filter) {

		if (!initStatement()) return;
		// ִ��query�õ�ResultSet
		try {
			result = statement.executeQuery(sql);
			process(filter);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ʹ��filter�����Ѿ�ok��ResultSet, this.result�Ѿ�����.
	 * @param filter
	 * @throws SQLException
	 */
	private void process(ResultsetFilter filter) throws SQLException {

		meta = result.getMetaData();
		// ---------- Table.head
		int cols = meta.getColumnCount();
		String[] head = new String[cols];
		for (int i = 0; i < cols; i++) {
			String colname = meta.getColumnName(i+1);
			colname = encode(colname);
			head[i] = colname;
		}
		filter.head(head);
		// ---------- Table.data
		while (result.next()) {
			boolean rt = filter.row(result);
			if (rt==false) break;
		}

		// �ر���Դ
		closeResultset();
		closeStatement();
	}

	/**
     * ����RowFilter����sql�Ĳ�ѯ���, �ѱ�ͷ��ÿһ����ΪRow����.
	 * @param sql
	 * @param filter
	 */
	public void process(String sql, RowFilter filter) {
		// ͨ�� ResultsetFilter ��ʵ����, �ѱ�ͷ��ÿһ��תΪRow������
		process(sql, new Rs2RowFilter(filter));
	}

	/**
	 * ʹ�� ResultsetFilter ��װ, �ѱ�ͷ��ÿһ��תΪRow��RowFilter����.
	 * @author jamesqiu 2009-2-2
	 * @see DbServer#process(String, ResultsetFilter)
	 * @see DbServer#process(String, RowFilter)
	 * @see RowFilter
	 */
	private static class Rs2RowFilter implements ResultsetFilter {

		RowFilter filter;
		int cols;

		public Rs2RowFilter(RowFilter filter) {
			this.filter = filter;
		}

		//
		public void head(String[] head) {
			cols = head.length;
			Row tr = Row.createByStrings(head);
			filter.process(tr);
		}

		public boolean row(ResultSet result) {
			Row row = new Row();
			for (int i = 1; i <= cols; i++) {
//				String type = meta.getColumnTypeName(n);
				String value = null;
				try {
					value = result.getString(i);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				row.add(new Cell(value));
			}
			return filter.process(row);
		}
	};

	/**
	 * �õ����ݿ��Ʒ������, ��: "DB2/NT"
	 */
	public String product() {

		if (conn==null) return "";

		try {
			return conn.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * �õ����ݿ��Ʒ��version
	 */
	public String version() {

		if (conn==null) return "";

		try {
			return conn.getMetaData().getDatabaseProductVersion();
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * �õ��������ݿ������(catalog)
	 * @return
	 */
	public String database() {

		if (conn == null) return "";

		try {
			return conn.getCatalog();
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * �õ���ǰ���ݿ������table.
	 * @return
	 */
	public List<String> tables() {
		try {
			return getTables(new String[]{"TABLE"});
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	/**
	 * �õ���ǰ���ݿ������view
	 * @return
	 */
	public List<String> views() {
		try {
			return getTables(new String[]{"VIEW"});
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	/**
	 * ��ȡһ����ǰ���ݿ��Meta����
	 * @return
	 */
	public DbMeta meta() {
		return DbMeta.create(conn);
	}

	/**
	 * <pre>
	 * gets the number of rows in a table,
	 * using the SQL statement 'SELECT COUNT(*)'.
	 * </pre>
	 * @param tableName
	 * @return the number of rows in a table
	 */
	public int rows(String tableName) {

		Table table = query("SELECT COUNT(*) FROM " + tableName);
		if (table.rows()==0) return 0;
		return table.cell(0,0).intValue();
	}


	/**
	 * <pre>
	 * get the number of rows in a sql query ResultSet.
	 * ��������ִ����������֮ǰȷ��sql�����Ŀ�Ƿ����.
	 * </pre>
	 * @param sql the query sql sentence
	 */
	public int resultRows(String sql) {

		ResultRows filter = new ResultRows();
		process(sql, filter);
		return filter.i;
	}


	/**
	 * �õ�ResultSet��С�� RowFilter/ResultsetFilter ʵ��
	 * @author jamesqiu 2009-1-12
	 */
	private static class ResultRows implements ResultsetFilter {
		int i = 0;
		public void head(String[] head) {}
		public boolean row(ResultSet result) { i++; return true; }
	}

	private List<String> getTables(String[] types) throws SQLException {

		List<String> list = new ArrayList<String>();

		if (conn==null) return list;

		// Oracle���ݿⵥ������
		if (product().equalsIgnoreCase ("Oracle")) {
			String sql = "SELECT table_name FROM user_catalog " +
					"WHERE table_type='" + types[0] + "'";
			Table table = query(sql);
			for (int i = 0; i < table.rows(); i++) {
				list.add(table.cell(i,0).value());
			}
			return list;
		}
		// �������ݿ�
		result = conn.getMetaData().getTables(null, null, "%", types);
		while (result.next()) {
			list.add(result.getString(3));
		}
		closeResultset();

		return list;
	}

	/**
	 * <pre>
	 * method: createStatement ()
	 * create a default statement: unscrollable & unupdatable
	 * ע��:
	 *   scrollable��sqlserver2000�ϴ�������ʱ�ǳ�����, û�в���.
	 *   ������jdbc��û�в���.
	 * </pre>
	 * @return true if init ok, false if init false.
	 */
	private boolean initStatement() {

		if (conn == null) return false;

		try {
			statement = conn.createStatement();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * <pre>
	 * ��Resultset��ȡָ��λ�ú�������������, �γ�Table����.
	 *  asTable(1, 10);  // ȡǰ10��
	 *  asTable(n, n+10);  // ȡ�ӵ�n�п�ʼ��10��
	 * </pre>
	 * @param beginIndex 1��ʾ��һ��
	 * @param n Ԥ��Ҫȡ�ļ�¼����Ŀ,
	 * @return
	 * @throws SQLException
	 */
	private Table asTable(int beginIndex, int n) throws SQLException {
		// ʹ�����ڰ�ȫ��Χ��.
		if (beginIndex < 1) beginIndex = 1;
		n = Numberx.safeRange(n, 0, maxRows);
		int endIndex = beginIndex + n - 1;

		Rs2tableFilter filter = new Rs2tableFilter(beginIndex, endIndex);
		process(filter);

		return filter.table;
	}

	/**
	 * ��Resultset��ָ��������ת��ΪTable��filter
	 * @author jamesqiu 2009-2-12
	 */
	private class Rs2tableFilter implements ResultsetFilter {

		Table table = new Table();
		int beginIndex;
		int endIndex;

		public Rs2tableFilter(int beginIndex, int endIndex) {
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
		}

		int cols;
		public void head(String[] head) {
			cols = head.length;
			table.head(Row.createByStrings(head));
		}

		int cindex = 0; // current index
		public boolean row(ResultSet result) {

			cindex++;
			if (cindex < beginIndex) return true; // ��û��ʼ, ֱ�ӷ���
			if (cindex > endIndex) return false; // ����
			// ��Ҫ�����������
			Row row = new Row();
			for (int j = 1; j <= cols; j++) {
				String type = null;
				String value = null;
				Cell cell = null;
				try {
					//type = meta.getColumnTypeName(j); // ����"NULL", ������Ϊnull
					// �������ĳ����°汾��2011-4-20 jamesqiu
					type = DbMeta.jdbcType(meta.getColumnType(j));
					value = result.getString(j);
					if (ColumnType.isLong(type)) {
						// ����
						cell = Cell.create(Numberx.toLong(value, 0));
					} else if (ColumnType.isDouble(type)) {
						// С����
						cell = Cell.create(Numberx.toDouble(value, 0.0));
					} else {
						// ��������
						cell = (value==null ? null : new Cell(value));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				row.add(cell);
			}
			table.add(row);
			return true;
		}
	}

	/**
	 * ��ResultSet�Ľ������encode.
	 * @param s
	 * @return
	 */
	private String encode(String s) {
		// todo: ���ݲ�ͬ���ݿ�����ý��б���.
		return s;
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		DbServer server = DbServer.create();
		String sql;

//		sql = "select * from cust limit 5";
		sql = "select top 5 * from cust";
		System.out.println(server.query(sql));
		System.out.println("cust rows1: " + server.rows("cust"));
		sql = "select * from cust";
		System.out.println("cust rows2: " + server.resultRows(sql));
		System.out.println(server.query(sql, 255941, 10));
		server.process(sql, new RowFilter() {
			int i = 0;
			public boolean process(Row row) {
				if (i>=255941) {
					System.out.println(row);
				}
				i++;
				return true;
			}
		});

		server = DbServer.create("abs_grails");
		sql = "select * from sys_infolder";
		server.maxRows(3);
		System.out.println(server.query(sql));
		System.out.println(server.query(sql, 2, 1));

		server.close();
	}
}
