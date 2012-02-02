package wr3.table;

import wr3.Cell;

/**
 * 对进行分组的列的每一个cell进行处理的定制化处理。
 * @author jamesqiu 2009-10-3
 */
public interface GroupFilter {

	/**
	 * 对cell进行处理
	 * @param cell 不改变
	 * @return 处理后的cell
	 */
	Cell process(Cell cell);
}
