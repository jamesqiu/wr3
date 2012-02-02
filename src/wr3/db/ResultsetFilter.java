package wr3.db;

import java.sql.ResultSet;

/**
 * <pre>
 * ResultSet行数据过滤器接口，配合DbServer使用。
 * 可以增加自己的构造方法和其他方法进行调用。
 * 
 * usage:
 *  server = DbServer.create ("abs");
 *  server.process (sql, new ResultsetFilter() { // 使用匿名类
 *  	public void head(String[] head) {...}
 *  	public void row(ResultSet result) {...}
 *  });
 *  server.close();
 * </pre>
 * @author jamesqiu 2007-7-31
 * @see DbServer#process(String, RowFilter)
 * @see LineFilter  (处理大文本文件)
 */
public interface ResultsetFilter {
	
	void head(String[] head);
	
	boolean row(ResultSet result);
}
