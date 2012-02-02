package wr3;

import java.util.List;

/**
 * <pre>
 * Table��һ��, ��Row�Ľṹ��ȫһ��, ��Row����һ����ͼ, �κθı��ͬ��.
 * ����Col��һ�в�������: 
 * 	1) ��ת��ΪRow�����½�Row; 
 * 	2) ��Row����; 
 * 	3) ��ת��ΪCol
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
	 * ת����ColΪRow
	 * @return
	 */
	public Row asRow() {
		return row;
	}
	
	/**
	 * ת��ColΪList<Object>
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
