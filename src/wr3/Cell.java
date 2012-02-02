package wr3;

import wr3.util.Csv;
import wr3.util.Json;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * wr3内部数据模型的最小单位，代表表格的一个元素，类似于&lt;TABLE>的&lt;TD>.
 * -------------------------------------------------------
 * 1) 每个Table含多个Row，每个Row含多个Cell, 内部存储为:
 *  Table |--> Row
 *        |--> Row
 *        |--> Row |--> Cell
 *                 |--> Cell
 *                 |--> Cell
 *  但有Column列视图.
 * 2) 一个Table中, 每个Row的个数是固定的, 即列是定长的;
 * 3) 一个Table中, 每个Row的缺省值为null, 非null的Row中, 每个Cell的缺省值是null;
 * 4) 取表头head:
 *      Row rhead = Table.head();
 *    取数据行row:
 *      Row row = Table.row(0);
 *    取数据cell:
 *      Cell cell = Table.cell(0,0);
 *      Cell cell = row.cell(0);
 *      Cell cell = row.cell("age");
 *      Cell cell = col.cell(0);
 *    取数据列column视图:
 *      Column names = Table.column(0);
 *      Column ages  = Table.column("age");
 * -------------------------------------------------------
 * usage:
 *   cell1 = new Cell(1);
 *   cell2 = new Cell("cn中文");
 *
 * 注意:  Cell初始化之后即不可改变, 且内容不会为null.
 *        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * </pre>
 * @author jamesqiu 2008-12-13
 *
 */
public final class Cell implements Comparable<Cell>{

	/**
	 * Cell的数据, 基本类型的包装, 设置之后不能改变, 任何情况都不为null
	 */
	private Object data;

	/**
	 * 使用int, long, double, String, Cell进行初始化
	 */
	public Cell() {
		this("");
	}

	public Cell(int i) {
		data = Integer.valueOf(i);
	}

	public Cell(long l) {
		data = Long.valueOf(l);
	}

	public Cell(double d) {
		data = Double.valueOf(d);
	}

	public Cell(String s) {
		if (s==null) s = "";
		data = s;
	}

	/**
	 * 把其他cell的data拷贝给本Cell
	 * @param cell 之后其改变不会影响本Cell.
	 */
	public Cell(Cell cell) {
		if (cell==null) {
			data = "";
		} else {
			data = cell.data;
		}
	}

	public static Cell create(int i) {
		return new Cell(i);
	}

	public static Cell create(long l) {
		return new Cell(l);
	}

	public static Cell create(double d) {
		return new Cell(d);
	}

	public static Cell create(String s) {
		return new Cell(s);
	}

	public static Cell create(Cell cell) {
		return new Cell(cell);
	}

	/**
	 * 用未知类型的对象初始化Cell,
	 * @param o 必须是int/long/double/String/Cell，o被转换为(""+o)
	 * @return Cell或者null
	 */
	public static Cell createByObject(Object o) {

		if (o instanceof Integer) {
			return new Cell(((Integer)o).intValue());
		} else if (o instanceof Long) {
			return new Cell(((Long)o).longValue());
		} else if (o instanceof Double) {
			return new Cell(((Double)o).doubleValue());
		} else if (o instanceof String) {
			return new Cell(((String)o));
		} else if (o instanceof Cell) {
			return new Cell((Cell)o);
		} else {
			return new Cell(""+o);
		}
	}

	/**
	 * 类型处理, 基本: Integer, Long, Double和String
	 * @return
	 */
	public Class<?> type() {
		return data.getClass();
	}

	public boolean isNumber() {
		return Numberx.isNumber(data);
	}

	public boolean isString() {
		return Stringx.isString(data);
	}

	/**
	 * 得到Cell的值
	 * @return
	 */
	public String value() {
		return data.toString();
	}

	public int intValue() {
		if (type()==Integer.class) {
			return ((Integer)data).intValue();
		} else {
			return (int)longValue();
		}
	}

	public long longValue() {
		if (type()==Long.class) {
			return ((Long)data).longValue();
		} else {
			return Math.round(doubleValue());
		}
	}

	public double doubleValue() {
		return  Numberx.toDouble(value(), 0d);
	}

	public String percentValue() {
		return Numberx.toPercent(doubleValue());
	}

	/**
	 * alias of {@link #value()}
	 */
	public String toString() {
		return value();
	}

	public String toJson() {
		return Json.toJson(data);
	}

	public String toCsv() {
		return Csv.toCsv(data);
	}

	/**
	 * 得到data,改变此返回值不会改变Cell
	 * @return
	 */
	public Object data() {
		return data;
	}

	/**
	 * 得到转换类型后的新Cell
	 * @return
	 */
	public Cell asInt() {
		return Cell.create(intValue());
	}

	public Cell asLong() {
		return Cell.create(longValue());
	}

	public Cell asDouble() {
		return Cell.create(doubleValue());
	}

	public Cell asString() {
		return Cell.create(value());
	}

	public Cell asPercent() {
		return Cell.create(percentValue());
	}

	Cell as(Class<?> type) {
		if (type==int.class) {
			return asInt();
		} else if (type==long.class) {
			return asLong();
		} else if (type==double.class) {
			return asDouble();
		} else if (type==String.class) {
			return asString();
		} else {
			return asPercent();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj==this) return true;
		if (obj==null || !(obj instanceof Cell)) return false;

		return data.equals(((Cell)obj).data);
	}

	/**
	 * 覆盖该方法后，Cell才能作为Map的key。
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}

	/**
	 * 实现Cell的比较。
	 * 规则：
	 * 1、null最小；
	 * 2、同类去data比;
	 * 3、不同类但都是Number，转换为Double比；
	 * 4、不同类有非Number, 取value()比
	 */
	public int compareTo(Cell o) {

		if (o==null) return 1;
		Class<?> type1 = type();
		Class<?> type2 = o.type();
		if (type1==type2) {
			if (type1==Integer.class)
				return ((Integer)data).compareTo((Integer)(o.data));
			if (type1==Long.class)
				return ((Long)data).compareTo((Long)(o.data));
			if (type1==Double.class)
				return ((Double)data).compareTo((Double)(o.data));
			if (type1==String.class)
				return ((String)data).compareTo((String)(o.data));
		} else if (isNumber() && o.isNumber()) {
			return ((Double)doubleValue()).compareTo((Double)(o.doubleValue()));
		} else {
			return value().compareTo(o.value());
		}
		return 0;
	}

	/**
	 * <pre>
	 * 本Cell和另一Cell相加，返回新Cell，本Cell不改变。
	 * 1、和null相加不改变
	 * 2、有非Number型则进行字符串相加；
	 * 3、Number型进行数字相加，不同类型则向高类型进行提升；
	 * </pre>
	 * @param o
	 * @return
	 */
	public Cell add(Cell o) {

		if (o==null) return new Cell(this);

		if (isString() || o.isString()) {
			return new Cell(value() + o.value());
		}
		if (type()==Double.class || o.type()==Double.class) {
			return new Cell(doubleValue() + o.doubleValue());
		}
		if (type()==Long.class || o.type()==Long.class) {
			return new Cell(longValue() + o.longValue());
		}
		return new Cell(intValue() + o.intValue());
	}

	public Cell add(int v) { return add(new Cell(v)); }
	public Cell add(long v) { return add(new Cell(v)); }
	public Cell add(double v) { return add(new Cell(v)); }
	public Cell add(String v) { return add(new Cell(v)); }
}
