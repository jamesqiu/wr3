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
 * 对关系数据库的一个SQL操作实例.
 * 封装通过jdbc/jndi的Connection对数据库的所有操作.
 * 注意: <span class=zd>
 * (1) 务必调用close()方法关闭资源.
 * (2) DbServer的大部分方法是非线程安全的(有类变量statement, result),
 *     类的多个线程不要同时调用同一个DbServer对象的方法,
 *     应该每个线程使用自己的DbServer对象.</span>
 * usage:
 *  // 得到Table结果
 *  ds = DbServer.create();
 *  table = ds.data("select top 100 * from cust");
 *  table = ds.subdata("select * form cust", from, to);
 *  ds.close();
 *
 *  // 自行处理每行结果
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
	 * 取出结果为Table时,缺省记录行数目最大限制.
	 */
	final static int MAX_ROWS = 10000;
	private int maxRows = MAX_ROWS;

	/**
	 * 对数据源的Connection, 通过connect()打开, close()关闭
	 */
	private Connection conn;
	/**
	 * Connection的SQL statement
	 */
	private Statement  statement;
	/**
	 * Statement的一个游标结果表格
	 */
	private ResultSet  result;
	private ResultSetMetaData meta;

	public DbServer() {
	}

	/**
	 * 连接缺省的dataSource
	 * @return
	 */
	public static DbServer create() {
		return create("");
	}

	/**
	 * 连接指定名称的dataSource(DbConfig中定义)
	 * @param dbname
	 * @return
	 */
	public static DbServer create(String dbname) {
		DbServer server = new DbServer();
		server.connect(dbname);
		return server;
	}

	/**
	 * 直接设置Connection, 不使用dbname
	 * @param conn
	 */
	public static DbServer create(Connection conn) {
		DbServer server = new DbServer();
		server.conn = conn;
		return server;
	}

	/**
	 * 打开Connection.
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
	 * 关闭Connection
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
	 * 获取数据库Connection
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
	 * 执行查询操作, 结果放入Table.
	 * 如果maxRows设置过大, 自动限制结果为MAX_ROWS以内.
	 * @param sql
	 * @return 存放结果的Table
	 */
	public Table query(String sql) {
		return query(sql, 1, maxRows);
	}

	/**
	 * <pre>
	 * 执行查询操作, 把指定行index的结果放入Table.
	 *  query(sql, 1, 10);  // 取前10行
	 * </pre>
	 * @param sql
	 * @param beginIndex 1 first row, 超出范围则报错
	 * @param n 目标记录数, 但真正返回的记录数可能小于n; n>=0, n=0时只取表列名.
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
	 * 执行"增/删/改"操作, 结果为影响的行数
	 * @param sql
	 * @return 0 表示没有操作任何数据
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
	 * 执行存储过程.
	 * @param sqls
	 * @return 如果是结果集, 返回Table对象; 如果是update, 返回处理成功的记录数.
	 */
	public Table procedure(String sqls) {

		if (conn==null || Stringx.nullity(sqls))
			return new Table();
		if (!initStatement()) return new Table();
		// 执行query得到ResultSet
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
     * 调用 ResultsetFilter 处理sql的查询结果, 表头作为String[]处理,
     * 每一行作为ResultSet处理.
	 * @param sql sql语句
	 * @param filter ResultsetFilter的实现类对象
	 */
	public void process(String sql, ResultsetFilter filter) {

		if (!initStatement()) return;
		// 执行query得到ResultSet
		try {
			result = statement.executeQuery(sql);
			process(filter);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用filter处理已经ok的ResultSet, this.result已经就绪.
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

		// 关闭资源
		closeResultset();
		closeStatement();
	}

	/**
     * 调用RowFilter处理sql的查询结果, 把表头和每一行作为Row处理.
	 * @param sql
	 * @param filter
	 */
	public void process(String sql, RowFilter filter) {
		// 通过 ResultsetFilter 的实现类, 把表头和每一行转为Row来处理
		process(sql, new Rs2RowFilter(filter));
	}

	/**
	 * 使用 ResultsetFilter 包装, 把表头和每一行转为Row让RowFilter处理.
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
	 * 得到数据库产品的名称, 如: "DB2/NT"
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
	 * 得到数据库产品的version
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
	 * 得到当期数据库库名称(catalog)
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
	 * 得到当前数据库的所有table.
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
	 * 得到当前数据库的所有view
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
	 * 获取一个当前数据库的Meta对象
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
	 * 可用于在执行真正操作之前确定sql结果数目是否过大.
	 * </pre>
	 * @param sql the query sql sentence
	 */
	public int resultRows(String sql) {

		ResultRows filter = new ResultRows();
		process(sql, filter);
		return filter.i;
	}


	/**
	 * 得到ResultSet大小的 RowFilter/ResultsetFilter 实现
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

		// Oracle数据库单独处理
		if (product().equalsIgnoreCase ("Oracle")) {
			String sql = "SELECT table_name FROM user_catalog " +
					"WHERE table_type='" + types[0] + "'";
			Table table = query(sql);
			for (int i = 0; i < table.rows(); i++) {
				list.add(table.cell(i,0).value());
			}
			return list;
		}
		// 其他数据库
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
	 * 注意:
	 *   scrollable在sqlserver2000上大数据量时非常缓慢, 没有采用.
	 *   在其他jdbc上没有测试.
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
	 * 从Resultset中取指定位置和数量的数据行, 形成Table返回.
	 *  asTable(1, 10);  // 取前10行
	 *  asTable(n, n+10);  // 取从第n行开始的10行
	 * </pre>
	 * @param beginIndex 1表示第一行
	 * @param n 预计要取的记录行数目,
	 * @return
	 * @throws SQLException
	 */
	private Table asTable(int beginIndex, int n) throws SQLException {
		// 使参数在安全范围内.
		if (beginIndex < 1) beginIndex = 1;
		n = Numberx.safeRange(n, 0, maxRows);
		int endIndex = beginIndex + n - 1;

		Rs2tableFilter filter = new Rs2tableFilter(beginIndex, endIndex);
		process(filter);

		return filter.table;
	}

	/**
	 * 把Resultset的指定数据行转换为Table的filter
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
			if (cindex < beginIndex) return true; // 还没开始, 直接返回
			if (cindex > endIndex) return false; // 结束
			// 需要处理的数据行
			Row row = new Row();
			for (int j = 1; j <= cols; j++) {
				String type = null;
				String value = null;
				Cell cell = null;
				try {
					//type = meta.getColumnTypeName(j); // 会是"NULL", 但不会为null
					// 上面语句改成如下版本：2011-4-20 jamesqiu
					type = DbMeta.jdbcType(meta.getColumnType(j));
					value = result.getString(j);
					if (ColumnType.isLong(type)) {
						// 整型
						cell = Cell.create(Numberx.toLong(value, 0));
					} else if (ColumnType.isDouble(type)) {
						// 小数型
						cell = Cell.create(Numberx.toDouble(value, 0.0));
					} else {
						// 其他类型
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
	 * 对ResultSet的结果进行encode.
	 * @param s
	 * @return
	 */
	private String encode(String s) {
		// todo: 根据不同数据库的配置进行编码.
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
