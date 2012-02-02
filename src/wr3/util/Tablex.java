package wr3.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wr3.Cell;
import wr3.Col;
import wr3.Row;
import wr3.Table;
import wr3.table.CellFilter;
import wr3.table.GroupFilter;

/**
 * wr3.Cell, wr3.Row, wr3.Table�ĸ�����
 * @author jamesqiu 2009-8-30
 *
 * @see wr3.Table
 * @see wr3.Row
 * @see wr3.Cell
 */
public class Tablex {

	/**
	 * �ѵ��е�Tableת��Ϊһ��List
	 * @param table cols()>=1��Table, ������һ��ת��ΪList
	 */
	public static List<Object> asList(Table table) {
		
		return asList(table, 0);
	}

	/**
	 * ��Tableָ������ת��Ϊһ��List
	 * @param table cols()>=1��Table, ������һ��ת��ΪList
	 */
	public static List<Object> asList(Table table, int colIndex) {
		
		if (table==null || table.cols()<1) return new ArrayList<Object>();
		
		return table.col(colIndex).asList();
	}
	
	/**
	 * ��2�е�Tableת��Ϊһ��List, �������2�У�ȡǰ���С�
	 * @return
	 */
	public static Map<Object, Object> asMap(Table table) {
		
		return asMap(table, 0, 1);
	}

	/**
	 * ��Tableָ����ĳ2�зֱ���Ϊkey��valת��Ϊһ��Map��
	 * @param table
	 * @param indexKey
	 * @param indexVal
	 * @return
	 */
	public static Map<Object, Object> asMap(Table table, int indexKey, int indexVal) {
		
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		if (table==null || table.cols()<1) return map;
		
		List<Object> keys = asList(table, indexKey);
		List<Object> vals = asList(table, indexVal);

		for (int i = 0, n = keys.size(); i < n; i++) {
			map.put(keys.get(i), vals.get(i));
		}
		
		return map;
	}
	
	/**
	 * <pre>
	 * �õ�table0����ת��table1��
	 *   table0.head ��� table1 �ĵ�1��
	 *   table0.rows ��� table1 �ĵ�2..n��
	 *   table1.head Ϊ ["", "r0", "r1", ... , "rn"]
	 * </pre>
	 * @param table0 ԭ���ı����ú󲻸ı䡣
	 * @return
	 */
	public static Table rotate(Table table0) {
		
		if (table0==null) return null;
		
		int rows0 = table0.rows();
		int cols0 = table0.cols();

		// �±�����һ����ʾԭhead
		int cols1 = rows0+1;
		int rows1 = cols0;
		
		Table table1 = new Table(cols1, rows1);
		// ��1�У�ʹ��ԭhead
		for (int j = 0; j < cols0; j++) {
			table1.cell(j, 0, table0.head(j));
		}
		
		// ��1��֮������ݣ�ʹ��ԭdata
		for (int i = 0; i < rows0; i++) {
			for (int j = 0; j < cols0; j++) {
				Cell cell = table0.cell(i, j);
				table1.cell(j, i+1, cell);
			}
		}
		
		// head��ʹ��"", r0, ...
		table1.head(0, new Cell());
		for (int i = 0; i < rows0; i++) {
			table1.head(i+1, new Cell("r"+i));
		}
		
		return table1;
	}
	
	/**
	 * ͨ��filter����һ��������Ϊ���У����������
	 * @param table ���ı䣨���ӷ�����Ϊ���У�
	 * @param colIndex ���з�����к�
	 * @param filter �Զ������filter
	 * @return 
	 */
	public static Table group(Table table, int colIndex, GroupFilter filter) {
		
		if (table==null || filter==null) return table;
		
		int rows = table.rows();
		int cols = table.cols();
		colIndex = Numberx.safeIndex(colIndex, cols);
		
		Row row = new Row(); // Ҫת��Ϊcol��row
		for (int i = 0; i < rows; i++) {
			Cell cell = filter.process(table.cell(i,colIndex));
			row.add(cell);
		}
		
		Col col = row.asCol().head(new Cell("������"));
		table.addcol(0, col);
		return table;
	}
	
	/**
	 * {@link #sum(Table, List)} ��alias
	 * @param table
	 * @param rows
	 * @return
	 */
	public static Row sum(Table table, int... rows) {
		
		List<Integer> rowsIndex = new ArrayList<Integer>();	
		for (int row : rows) {
			rowsIndex.add(row);
		}
		return sum(table, rowsIndex);
	}
	
	/**
	 * ����������ݵĺϼ�ֵ��
	 * @param table
	 * @param rows ���������У��磺{0,3,-1}
	 * @return �����ĺϼ��У����ı䴫��Ĳ�����
	 */
	public static Row sum(Table table, List<Integer> rows) {
	
		final List<Integer> rowsIndex = safeIndex(table, rows); 
		
		/**
		 * ���������null�������ϼƣ�����Ӱ���������Number����cell�ĺϼƣ�
		 * �����String���ͣ����в��ϼƣ�
		 */
		CellFilter sumFilter = new CellFilter() {
			
			double sum;
			Class<?> type;
			
			public Cell process(int col, Cell cell) { return null; }
			
			public Cell process(int row, int col, Cell cell) {
				// ÿ�г�ʼ������������
				if (row==0) { 
					sum = 0d;
					type = null;
				}
				// ����֮ǰ����String���͵�Cell�����ж����ϼ�
				if (type==String.class) return new Cell();
				
				if (cell==null || !rowsIndex.contains(row)) {
					// ��null�Ĳ�����ϼƣ�����֮ǰ�ļ�������
					return type==Double.class ? new Cell(sum) : new Cell((long)sum);
				} else if (cell.isNumber()) {
					// �����н��м���
					if (cell.type()==Double.class || type==Double.class) {
						// ��CellΪDouble���ͻ����֮ǰ�Ѿ���Double���͵�Cell
						type = Double.class;
						sum += cell.doubleValue();						
						return new Cell(sum);					
					} else {
						type = Long.class;
						sum += cell.longValue();
						return new Cell((long)sum);
					}
				} else {
					type = String.class;
					return new Cell();
				}
			}
		};
		
		return handler(table, sumFilter);
	}
	
	/**
	 * used by {@link #sum(Table, List)}
	 * ����table����rowsΪnull�������������-1תΪn-1
	 * @param table
	 * @param rows
	 * @return
	 */
	private static List<Integer> safeIndex(Table table, List<Integer> rows) {
		
		List<Integer> rt = new ArrayList<Integer>();		
		if (rows==null || table==null) return rt;
		
		int size = table.rows();
		for (Integer i : rows) {
			rt.add(Numberx.safeIndex(i, size));
		}
		
		return rt;
	}
	
	/**
	 * ����������ݵĺϼ�ֵ��
	 * @param table
	 * @return �����ĺϼ��У����ı䴫���table��
	 */
	public static Row sum(Table table) {
		
		return sum(table, rowsIndex(table));
	}
	
	/**
	 * �õ�table�����е��к��б�
	 * used by {@link #sum(Table)}
	 * @param table
	 * @return
	 */
	private static List<Integer> rowsIndex(Table table) {
		
		List<Integer> rt = new ArrayList<Integer>();
		if (table==null) return rt;
		
		for (int i = 0, n = table.rows(); i < n; i++) {
			rt.add(i);
		}
		return rt;
	}

	/**
	 * ����������ݵ�ƽ��ֵ������ {@link #sum(Table)}
	 * @param table
	 * @return
	 */
	public static Row avg(Table table) {
		
		Row rt = new Row();
		
		if (table==null || table.cols()==0) return rt;
		// �ս����
		if (table.rows()==0) {
			for (int i = 0, n = table.cols(); i < n; i++) {
				rt.add(new Cell());
			}
			return rt;
		}
		
		Row sum = sum(table);
		int rows = table.rows();
		for (int i = 0, n = sum.size(); i < n; i++) {
			Cell c0 = sum.cell(i);
			Cell c1;
			if (c0.isNumber()) {
				c1 = new Cell(c0.doubleValue() / rows);
			} else {
				c1 = new Cell();
			}
			rt.add(c1);
		}
		
		return rt;
	}
		
	public static Row max(Table table) {
	
		/**
		 * ͨ���Ƚ�ѡ�����
		 */
		MaxMinCompare maxCompare = new MaxMinCompare() {
			public double initValue() {
				return (double) Long.MIN_VALUE;
			}
			public boolean compare(double d, double d0) {
				return d > d0;
			}
		};
		
		return handler(table, new MaxMinFilter(maxCompare));		
	}
	
	
	public static Row min(Table table) {
	
		/**
		 * ͨ���Ƚ�ѡ����С
		 */
		MaxMinCompare minCompare = new MaxMinCompare() {
			public double initValue() {
				return (double) Long.MAX_VALUE;
			}
			public boolean compare(double d, double d0) {
				return d < d0;
			}
		};
		
		return handler(table, new MaxMinFilter(minCompare));
	}
	
	/**
	 * ��Table�ĸ��н��оۺϴ����ͨ�ô��롣
	 * @param table
	 * @param filter
	 */
	private static Row handler(Table table, CellFilter filter) {
		
		Row row = new Row();
	
		if (table==null) return row;
		
		int rows = table.rows();
		int cols = table.cols();
		
		for (int ci = 0; ci < cols; ci++) {
			Cell cell = new Cell();
			for (int ri = 0; ri < rows; ri++) {
				cell = filter.process(ri, ci, table.cell(ri, ci));
			}
			row.add(cell);
		}
		return row;
	}

	/**
	 * ����ɸѡMax/Min��Filter, �������ʹ�� MaxMinCompare �ӿڱ����ظ����롣
	 * @author jamesqiu 2009-9-26
	 */
	private static class MaxMinFilter implements CellFilter {

		MaxMinCompare filter;
		public MaxMinFilter(MaxMinCompare filter) {
			this.filter = filter;
		}
		
		public Cell process(int col, Cell cell) { return null; }

		// ��������ѡ��
		double d0;
		Cell cellReturn;
		
		public Cell process(int row, int col, Cell cell) {
			// ÿ�ж����ֱ���г�ʼ��
			if (row==0) {
				d0 = filter.initValue();
				cellReturn = new Cell();
			}
			if (cell==null || !cell.isNumber()) return cellReturn;
			
			double d = cell.doubleValue();
			if (filter.compare(d, d0)) {
				d0 = d;
				cellReturn = new Cell(cell);
			}
			return cellReturn;
		}
	}
	
	/**
	 * ������������С�Ƚϣ�DRY����д�ظ���max()��min().
	 * @author jamesqiu 2009-9-26
	 * used by {@link MaxMinFilter}, max(Table), min(Table)
	 */
	private interface MaxMinCompare {		
		/**
		 * �õ��Ƚϵ�ԭʼֵ
		 * @return
		 */
		double initValue();
		/**
		 * ���бȽϵĽ��
		 * @param d
		 * @param d0
		 * @return
		 */
		boolean compare(double d, double d0);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		Table t = new Table(10,5);
		System.out.println(t);
		System.out.println(asList(t, 2));
		System.out.println(asMap(t, -1, 1));
		System.out.println("\n"+rotate(t));
		
		Table t2 = new Table(10,0);
		System.out.println(t2);
		System.out.println(sum(t2));
		
		System.out.println(sum(t));
		System.out.println(avg(t));
	}
}
