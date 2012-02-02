package wr3.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import wr3.Cell;
import wr3.Col;
import wr3.Row;
import wr3.Table;

/**
 * Row/Col 的util函数
 * @author jamesqiu 2009-11-10
 */
public class Rowx {

	/**
	 * 查找cell在row中的位置，对比value，不区分大小写,
	 * 所以new Cell(10), new Cell(10L), new Cell("10")的位置一样。
	 * @param cell
	 * @param row
	 * @return 从0开始，-1表示没有找到
	 */
	public static int index(Cell cell, Row row) {
		
		if (cell==null || row==null) return -1;
		for (int i = 0, n = row.size(); i < n; i++) {
			Cell celli = row.cell(i);
			if (celli==null) return -1;
			if (cell.value().equalsIgnoreCase(celli.value())) return i;
		}
		return -1;
	}
	
	/**
	 * 把Row相同的Cell合并去除重复
	 * @param row 原始
	 * @return
	 */
	public static Row uniq(final Row row) {
		
		Row rt = new Row();
		if (row==null || row.size()==0) return rt;
			
		Set<Cell> set = new LinkedHashSet<Cell>();
		for (int i = 0, n = row.size(); i < n; i++) {
			Cell cell = row.get(i);
			cell = cell==null ? null : new Cell(cell);
			set.add(cell);
		}
		for (Cell cell : set) {
			rt.add(cell);
		}
		
		return rt;
	}
	
	/**
	 * <pre>
	 * 把Table中指定列相同的合并去重排序。相对于group by c1, c2,..
	 * c0, c2
	 * ------
	 * 01  aa
	 * 01  aa
	 * 01  bb
	 * 01  bb
	 * 02  aa
	 * 02  cc
	 * 
	 * </pre>
	 * @param table
	 * @param cols
	 * @return 多列的Table
	 */
	public static Table uniq(final Col... cols) {
		
		int n = cols.length; // n=2
		if (n==0) return new Table();
		
		Table rt = new Table();
		Row head = new Row();
		for (int j = 0; j < n; j++) {
			head.add(cols[j].head());
		}
		rt.head(head);
		
		int len = cols[0].asRow().size(); // len=6
//		Set<Row> set = new LinkedHashSet<Row>();
		Set<Row> set = new TreeSet<Row>();
		for (int i = 0; i < len; i++) {
			Row row = new Row();
			for (int j = 0; j < n; j++) {
				row.add(cols[j].asRow().cell(i));
			}
			set.add(row);
		}
		for (Row row : set) {
			rt.add(row);
		}
		return rt;
	}
	
	/**
	 * 把Row相同的Cell分组计算count(*)
	 * @param row 原始
	 * @return 返回2列的Table，第1列是Cell，第2列是count(*)
	 */
	public static Table group(final Row row) {
		
		
		Table rt = new Table().head(Row.createByTypes("cell", "count"));
		if (row==null || row.size()==0) return rt;
			
		Map<Cell, Integer> map = new LinkedHashMap<Cell, Integer>();
		for (int i = 0, n = row.size(); i < n; i++) {
			Cell cell = row.get(i);
			int count = map.containsKey(cell) ? map.get(cell)+1 : 1;
			cell = cell==null ? null : new Cell(cell);
			map.put(cell, count);
		}
		for (Entry<Cell, Integer> e : map.entrySet()) {
			Cell cell = e.getKey();
			int count = e.getValue();
			rt.add(Row.createByTypes(cell, count));
		}
		
		return rt;
	}
	
	/**
	 * 对Row所有cell进行累加。
	 * null和非Number类型不参与计算，
	 * Number类型按就高进行类型提升(Integer->Long->Double).
	 * @param row 不会被更改
	 * @return 没有Number类型的Cell返回new Cell(), 
	 * 		没有Double类型成员返回Long类型Cell, 否则返回Double类型Cell
	 */
	public static Cell sum(final Row row) {
		
		int n = row.size();
		if (row==null || n==0) return new Cell();
		
		Cell cell = new Cell(0);
		for (int i = 0; i < n; i++) {
			Cell cell1 = row.cell(i);
			if (cell1==null || !cell1.isNumber()) continue;
			cell = cell.add(cell1);
		}
		
		return cell;		
	}
	
}
