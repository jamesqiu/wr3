package wr3.db;

import wr3.Row;

/**
 * <pre>
 * ResultSet行数据过滤器接口，配合DbServer使用。
 * 可以增加自己的构造方法和其他方法进行调用。
 * 
 * usage:
 *  server = DbServer.create ("abs");
 *  server.process (sql, new DataFilter() { // 使用匿名类
 *  	public void process(Row row) {...}
 *  });
 *  server.close();
 * </pre>
 * @author jamesqiu 2007-7-31
 * @see DbServer#process(String, RowFilter)
 * @see LineFilter  (处理大文本文件)
 */
public interface RowFilter {
	/**
	 * 处理ResultSet的row，每个字段一个String，
	 * 第一次传入的是字段名称，之后是字段值
	 * @return false则停止处理之后的行.
	 */
	boolean process (Row row);
}