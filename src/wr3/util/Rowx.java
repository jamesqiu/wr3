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
 * Row/Col ��util����
 * @author jamesqiu 2009-11-10
 */
public class Rowx {

	/**
	 * ����cell��row�е�λ�ã��Ա�value�������ִ�Сд,
	 * ����new Cell(10), new Cell(10L), new Cell("10")��λ��һ����
	 * @param cell
	 * @param row
	 * @return ��0��ʼ��-1��ʾû���ҵ�
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
	 * ��Row��ͬ��Cell�ϲ�ȥ���ظ�
	 * @param row ԭʼ
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
	 * ��Table��ָ������ͬ�ĺϲ�ȥ�����������group by c1, c2,..
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
	 * @return ���е�Table
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
	 * ��Row��ͬ��Cell�������count(*)
	 * @param row ԭʼ
	 * @return ����2�е�Table����1����Cell����2����count(*)
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
	 * ��Row����cell�����ۼӡ�
	 * null�ͷ�Number���Ͳ�������㣬
	 * Number���Ͱ��͸߽�����������(Integer->Long->Double).
	 * @param row ���ᱻ����
	 * @return û��Number���͵�Cell����new Cell(), 
	 * 		û��Double���ͳ�Ա����Long����Cell, ���򷵻�Double����Cell
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
