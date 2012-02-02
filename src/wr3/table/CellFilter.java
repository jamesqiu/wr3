package wr3.table;

import wr3.Cell;

/**
 * ��wr3.Table��ÿһ��Cell���д������ش�����������Cell.
 * ע�⣺�ýӿڷ���app��Tomcat��classloader�Ҳ�����
 * @author jamesqiu 2009-9-1
 */
public interface CellFilter {

	/**
	 * �Ա�ͷ��cell���д������ش�������
	 * @param col
	 * @param cell
	 * @return �µ�Cell
	 */
	Cell process(int col, Cell cell);
	
	/**
	 * ��ָ�����е�cell���д������ش�������
	 * @param row 
	 * @param col
	 * @param cell
	 * @return �µ�Cell
	 */
	Cell process(int row, int col, Cell cell);
	
}
