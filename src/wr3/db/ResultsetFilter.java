package wr3.db;

import java.sql.ResultSet;

/**
 * <pre>
 * ResultSet�����ݹ������ӿڣ����DbServerʹ�á�
 * ���������Լ��Ĺ��췽���������������е��á�
 * 
 * usage:
 *  server = DbServer.create ("abs");
 *  server.process (sql, new ResultsetFilter() { // ʹ��������
 *  	public void head(String[] head) {...}
 *  	public void row(ResultSet result) {...}
 *  });
 *  server.close();
 * </pre>
 * @author jamesqiu 2007-7-31
 * @see DbServer#process(String, RowFilter)
 * @see LineFilter  (������ı��ļ�)
 */
public interface ResultsetFilter {
	
	void head(String[] head);
	
	boolean row(ResultSet result);
}
