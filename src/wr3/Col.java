package wr3;

import java.util.List;

/**
 * <pre>
 * Table的一列, 和Row的结构完全一样, 是Row的另一个视图, 任何改变会同步.
 * 对于Col的一切操作可以: 
 * 	1) 先转换为Row或者新建Row; 
 * 	2) 对Row操作; 
 * 	3) 再转换为Col
 * </pre> 
 * @author jamesqiu 2009-1-4
 */
public class Col {
	
	private Cell head = new Cell("c0");
	private Row row;
	
	public Col(Row row) {
		this.row = row;
	}
	
	/**
	 * 转换本Col为Row
	 * @return
	 */
	public Row asRow() {
		return row;
	}
	
	/**
	 * 转换Col为List<Object>
	 * @return
	 */
	public List<Object> asList() {
		return row.asList();
	}
	
	public Cell head() {
		return head;
	}
	
	public Col head(Cell head) {
		if (head!=null) {
			this.head = head; 
		}
		return this;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		char sep = '\n';
		sb.append(head).append(sep);
		for (int i = 0; i < row.size(); i++) {
			sb.append(row.cell(i)).append(sep);
		}
		return sb.toString();
	}
}
