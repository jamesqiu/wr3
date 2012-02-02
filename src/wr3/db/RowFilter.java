package wr3.db;

import wr3.Row;

/**
 * <pre>
 * ResultSet�����ݹ������ӿڣ����DbServerʹ�á�
 * ���������Լ��Ĺ��췽���������������е��á�
 * 
 * usage:
 *  server = DbServer.create ("abs");
 *  server.process (sql, new DataFilter() { // ʹ��������
 *  	public void process(Row row) {...}
 *  });
 *  server.close();
 * </pre>
 * @author jamesqiu 2007-7-31
 * @see DbServer#process(String, RowFilter)
 * @see LineFilter  (������ı��ļ�)
 */
public interface RowFilter {
	/**
	 * ����ResultSet��row��ÿ���ֶ�һ��String��
	 * ��һ�δ�������ֶ����ƣ�֮�����ֶ�ֵ
	 * @return false��ֹͣ����֮�����.
	 */
	boolean process (Row row);
}