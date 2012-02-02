package wr3.table;

import wr3.Cell;

/**
 * 对wr3.Table的每一个Cell进行处理并返回处理后产生的新Cell.
 * 注意：该接口放在app下Tomcat的classloader找不到。
 * @author jamesqiu 2009-9-1
 */
public interface CellFilter {

	/**
	 * 对表头的cell进行处理，返回处理结果。
	 * @param col
	 * @param cell
	 * @return 新的Cell
	 */
	Cell process(int col, Cell cell);
	
	/**
	 * 对指定行列的cell进行处理，返回处理结果。
	 * @param row 
	 * @param col
	 * @param cell
	 * @return 新的Cell
	 */
	Cell process(int row, int col, Cell cell);
	
}
