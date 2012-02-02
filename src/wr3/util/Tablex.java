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
 * wr3.Cell, wr3.Row, wr3.Table的辅助类
 * @author jamesqiu 2009-8-30
 *
 * @see wr3.Table
 * @see wr3.Row
 * @see wr3.Cell
 */
public class Tablex {

	/**
	 * 把单列的Table转换为一个List
	 * @param table cols()>=1的Table, 会把其第一列转换为List
	 */
	public static List<Object> asList(Table table) {
		
		return asList(table, 0);
	}

	/**
	 * 把Table指定的列转换为一个List
	 * @param table cols()>=1的Table, 会把其第一列转换为List
	 */
	public static List<Object> asList(Table table, int colIndex) {
		
		if (table==null || table.cols()<1) return new ArrayList<Object>();
		
		return table.col(colIndex).asList();
	}
	
	/**
	 * 把2列的Table转换为一个List, 如果超过2列，取前两列。
	 * @return
	 */
	public static Map<Object, Object> asMap(Table table) {
		
		return asMap(table, 0, 1);
	}

	/**
	 * 把Table指定的某2列分别作为key和val转换为一个Map。
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
	 * 得到table0的旋转表table1：
	 *   table0.head 变成 table1 的第1列
	 *   table0.rows 变成 table1 的第2..n列
	 *   table1.head 为 ["", "r0", "r1", ... , "rn"]
	 * </pre>
	 * @param table0 原来的表，调用后不改变。
	 * @return
	 */
	public static Table rotate(Table table0) {
		
		if (table0==null) return null;
		
		int rows0 = table0.rows();
		int cols0 = table0.cols();

		// 新表多出来一列显示原head
		int cols1 = rows0+1;
		int rows1 = cols0;
		
		Table table1 = new Table(cols1, rows1);
		// 第1列：使用原head
		for (int j = 0; j < cols0; j++) {
			table1.cell(j, 0, table0.head(j));
		}
		
		// 第1列之后的数据：使用原data
		for (int i = 0; i < rows0; i++) {
			for (int j = 0; j < cols0; j++) {
				Cell cell = table0.cell(i, j);
				table1.cell(j, i+1, cell);
			}
		}
		
		// head：使用"", r0, ...
		table1.head(0, new Cell());
		for (int i = 0; i < rows0; i++) {
			table1.head(i+1, new Cell("r"+i));
		}
		
		return table1;
	}
	
	/**
	 * 通过filter增加一个分组列为首列，如分数分组
	 * @param table 被改变（增加分组列为首列）
	 * @param colIndex 进行分组的列号
	 * @param filter 自定义分组filter
	 * @return 
	 */
	public static Table group(Table table, int colIndex, GroupFilter filter) {
		
		if (table==null || filter==null) return table;
		
		int rows = table.rows();
		int cols = table.cols();
		colIndex = Numberx.safeIndex(colIndex, cols);
		
		Row row = new Row(); // 要转换为col的row
		for (int i = 0; i < rows; i++) {
			Cell cell = filter.process(table.cell(i,colIndex));
			row.add(cell);
		}
		
		Col col = row.asCol().head(new Cell("分组列"));
		table.addcol(0, col);
		return table;
	}
	
	/**
	 * {@link #sum(Table, List)} 的alias
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
	 * 计算各列数据的合计值。
	 * @param table
	 * @param rows 参与计算的列，如：{0,3,-1}
	 * @return 计算后的合计行，不改变传入的参数。
	 */
	public static Row sum(Table table, List<Integer> rows) {
	
		final List<Integer> rowsIndex = safeIndex(table, rows); 
		
		/**
		 * 该列如果有null项，不参与合计，但不影响该列其他Number类型cell的合计；
		 * 如果有String类型，整列不合计；
		 */
		CellFilter sumFilter = new CellFilter() {
			
			double sum;
			Class<?> type;
			
			public Cell process(int col, Cell cell) { return null; }
			
			public Cell process(int row, int col, Cell cell) {
				// 每列初始化，独立处理
				if (row==0) { 
					sum = 0d;
					type = null;
				}
				// 该列之前存在String类型的Cell则整列都不合计
				if (type==String.class) return new Cell();
				
				if (cell==null || !rowsIndex.contains(row)) {
					// 有null的不参与合计，返回之前的计算结果；
					return type==Double.class ? new Cell(sum) : new Cell((long)sum);
				} else if (cell.isNumber()) {
					// 数字列进行计算
					if (cell.type()==Double.class || type==Double.class) {
						// 此Cell为Double类型或该列之前已经有Double类型的Cell
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
	 * 处理table或者rows为null的情况，并把如-1转为n-1
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
	 * 计算各列数据的合计值。
	 * @param table
	 * @return 计算后的合计行，不改变传入的table。
	 */
	public static Row sum(Table table) {
		
		return sum(table, rowsIndex(table));
	}
	
	/**
	 * 得到table所有行的行号列表
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
	 * 计算各列数据的平均值。调用 {@link #sum(Table)}
	 * @param table
	 * @return
	 */
	public static Row avg(Table table) {
		
		Row rt = new Row();
		
		if (table==null || table.cols()==0) return rt;
		// 空结果集
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
		 * 通过比较选出最大
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
		 * 通过比较选出最小
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
	 * 对Table的各列进行聚合处理的通用代码。
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
	 * 进行筛选Max/Min的Filter, 抽象出来使用 MaxMinCompare 接口避免重复代码。
	 * @author jamesqiu 2009-9-26
	 */
	private static class MaxMinFilter implements CellFilter {

		MaxMinCompare filter;
		public MaxMinFilter(MaxMinCompare filter) {
			this.filter = filter;
		}
		
		public Cell process(int col, Cell cell) { return null; }

		// 进行数据选择
		double d0;
		Cell cellReturn;
		
		public Cell process(int row, int col, Cell cell) {
			// 每列独立分别进行初始化
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
	 * 进行最大或者最小比较，DRY：不写重复的max()和min().
	 * @author jamesqiu 2009-9-26
	 * used by {@link MaxMinFilter}, max(Table), min(Table)
	 */
	private interface MaxMinCompare {		
		/**
		 * 得到比较的原始值
		 * @return
		 */
		double initValue();
		/**
		 * 进行比较的结果
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
