package wr3.table;

import wr3.Cell;

/**
 * �Խ��з�����е�ÿһ��cell���д���Ķ��ƻ�����
 * @author jamesqiu 2009-10-3
 */
public interface GroupFilter {

	/**
	 * ��cell���д���
	 * @param cell ���ı�
	 * @return ������cell
	 */
	Cell process(Cell cell);
}
