package wr3.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Stringx;

/**
 * <pre>
 * 计算CubeTable进行html输出所需要的colspan,rowspan
 * usage:
 	// 进行设置初始化
 	spans = new CubeTableSpans()
		.corner(frame_corner)
		.top(frame_top)
		.left(frame_left);
	// 取出结果进行使用
	if (spans.contains(i, j)) {
		s = spans.get(i, j);
	}		
 * </pre>
 * @author jamesqiu 2009-11-25
 */
public class CubeTableSpans {

	private Table frame_corner;
	private Table frame_top;
	private Table frame_left;
	
	private Map<String, String> spans; // 输出结果
	
	public CubeTableSpans() {
		spans = new TreeMap<String, String>();
	}
		
	public CubeTableSpans corner(Table frame_corner) {
		this.frame_corner = frame_corner;
		processCorner();
		return this;
	}
	
	public CubeTableSpans top(Table frame_top) {
		this.frame_top = frame_top;
		processTop();
		return this;
	}
	
	public CubeTableSpans left(Table frame_left) {
		this.frame_left = frame_left;
		processLeft();
		return this;
	}
	
	public boolean contains(int row, int col) {
		return spans.containsKey(key(row, col));
	}
	
	public String get(int row, int col) {
		if (contains(row, col)) {
			return spans.get(key(row, col));
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() { 
		if (spans==null) return "{}";
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String>e : spans.entrySet()) {
			sb.append('[').append(e.getKey()).append("]: ")
			.append(e.getValue()).append('\n');
		}
		return sb.toString();
	}
	
	// 处理corner
	private void processCorner() {
		
		if (frame_corner==null) frame_corner = new Table();
		int rows = frame_corner.rows();
		int cols = frame_corner.cols();
//		if (rows<=1 && cols<=1) return;
		if (rows==0 || cols==0) return;
		
		String value;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (i==0 && j==0) {
					value = span(rows, cols);
				} else {
					value = null;
				}
				put(i, j, value);
			}
		}
	}
	
	// 处理top
	private void processTop() {
		
		if (!valid(frame_top)) frame_top = new Table();
		
		for (int j = 0, n = frame_top.cols(); j < n; j++) {
			topRow(frame_top.col(j).asRow(), j);
		}		
	}
	
	private void processLeft() {
		
		if (!valid(frame_left)) frame_left = new Table();

		for (int j = 0, n = frame_left.cols(); j < n; j++) {
			leftRow(frame_left.col(j).asRow(), j);
		}
		
	}
	
	private boolean valid(Table table) {
		
		if (table==null) return false;
		if (table.rows()==0 || table.cols()==0) return false;
		return true;
	}

	/**
	 * 处理一个Row, 如: [aa, aa, aa, bb] -> 
	 * {[0,0]: colspan=3, [0,1]=[0,2]=null, [0,3]: colspan=1}
	 * ┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈
	 *┊          ┊AA  AA  AA  BB
	 *┊  corner  ┊┈┈frame_top┈┈┈┈┈┈┈┈
	 *┊          ┊    (指标数>1)
	 *┊┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈
	 *┊   ┊     ┊
	 *┊frame_left┊    (指标数据区)
	 *┊   ┊     ┊
	 *┊001┊(指  ┊
	 *┊001┊ 标  ┊
	 *┊002┊ 数  ┊
	 *┊002┊ >1) ┊
	 *┊002┊     ┊
	 */
	private void topRow(Row row, int targetRowIndex) {
		
		List<Integer> list = count(row);
		int targetColIndex = frame_corner.cols(); // 注意
		for (int n : list) {
			for (int j = 0; j < n; j++) {
				if (j==0) {
					put(targetRowIndex, targetColIndex, ("nowrap colspan="+n));
				} else {
					put(targetRowIndex, targetColIndex, null);
				}
				targetColIndex++;
			}
		}
	}
	private void leftRow(Row row, int targetColIndex) {
		
		List<Integer> list = count(row);
		int targetRowIndex = frame_corner.rows(); // 注意
		for (int n : list) {
			for (int j = 0; j < n; j++) {
				if (j==0) {
					put(targetRowIndex, targetColIndex, ("nowrap rowspan="+n));
				} else {
					put(targetRowIndex, targetColIndex, null);
				}
				targetRowIndex++;
			}
		}
	}
	
	// 处理一个Row, 如: [aa, aa, aa, bb] -> [3, 1]
	private List<Integer> count(Row row) {
		
		List<Integer> list = new ArrayList<Integer>();
		int size = row.size();
		if (size==0) return list;
		
		Cell c0 = row.cell(0);
		list.add(1);
		for (int i = 1; i < size; i++) {
			Cell c1 = row.cell(i);
			if (equals(c0, c1)) {
				int index = list.size()-1;
				list.set(index, list.get(index)+1);
			} else {
				list.add(1);
			}
			c0 = c1;
		}
		
//		System.out.println("row=" + row + ",list=" + list);
		return list;
	}
	
	// 判断两个cell(可能为null)是否相等
	private boolean equals(Cell c0, Cell c1) {
		if (c0==null && c1==null) return true;
		if (c0==null || c1==null) return false;
		return c0.equals(c1);
	}
	
	// 设置指定行/列的span
	private void put(int row, int col, String value) {
		
		String key = key(row, col);
		spans.put(key, value);
	}

	// 行,列号组成的复合key (String类型)
	static String key(int row, int col) {
		
		return Stringx.printf("%d,%d", row, col);
	}
	
	// 组成 "rowspan=n colspan=m" 形式字符串
	static String span(int rows, int cols) {
		
		String r = "";
		String c = "";
		if (rows>1) r = "rowspan=" + rows;
		if (cols>1) c = "colspan=" + cols;
		
		if (r.equals("") || c.equals("")) return r+c;
		return r + " " + c;
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		Table corner = new Table().head(Row.head(2));
		corner.add(Row.create(2, new Cell()));
		corner.add(Row.create(2, new Cell()));
		
		Table top = new Table().head(Row.head(2))
			.add(Row.createByTypes("aa","001"))
			.add(Row.createByTypes("aa","001"))
			.add(Row.createByTypes("aa","002"))
			.add(Row.createByTypes("bb","001"))
		;
		
		Table left = new Table().head(Row.head(2))
			.add(Row.createByTypes("1月","活期"))
			.add(Row.createByTypes("1月","定期"))
			.add(Row.createByTypes("2月","活期"))
			.add(Row.createByTypes("2月","定期"))
			.add(Row.createByTypes("3月","活期"))
			.add(Row.createByTypes("3月","定期"))
		;
			
		CubeTableSpans o = new CubeTableSpans()
			.corner(corner)
			.top(top).left(left)
		;
		System.out.println(o);
	}
}
