package wr3;

import wr3.util.Csv;
import wr3.util.Json;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * wr3�ڲ�����ģ�͵���С��λ���������һ��Ԫ�أ�������&lt;TABLE>��&lt;TD>.
 * -------------------------------------------------------
 * 1) ÿ��Table�����Row��ÿ��Row�����Cell, �ڲ��洢Ϊ:
 *  Table |--> Row
 *        |--> Row
 *        |--> Row |--> Cell
 *                 |--> Cell
 *                 |--> Cell
 *  ����Column����ͼ.
 * 2) һ��Table��, ÿ��Row�ĸ����ǹ̶���, �����Ƕ�����;
 * 3) һ��Table��, ÿ��Row��ȱʡֵΪnull, ��null��Row��, ÿ��Cell��ȱʡֵ��null;
 * 4) ȡ��ͷhead:
 *      Row rhead = Table.head();
 *    ȡ������row:
 *      Row row = Table.row(0);
 *    ȡ����cell:
 *      Cell cell = Table.cell(0,0);
 *      Cell cell = row.cell(0);
 *      Cell cell = row.cell("age");
 *      Cell cell = col.cell(0);
 *    ȡ������column��ͼ:
 *      Column names = Table.column(0);
 *      Column ages  = Table.column("age");
 * -------------------------------------------------------
 * usage:
 *   cell1 = new Cell(1);
 *   cell2 = new Cell("cn����");
 *
 * ע��:  Cell��ʼ��֮�󼴲��ɸı�, �����ݲ���Ϊnull.
 *        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * </pre>
 * @author jamesqiu 2008-12-13
 *
 */
public final class Cell implements Comparable<Cell>{

	/**
	 * Cell������, �������͵İ�װ, ����֮���ܸı�, �κ��������Ϊnull
	 */
	private Object data;

	/**
	 * ʹ��int, long, double, String, Cell���г�ʼ��
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
	 * ������cell��data��������Cell
	 * @param cell ֮����ı䲻��Ӱ�챾Cell.
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
	 * ��δ֪���͵Ķ����ʼ��Cell,
	 * @param o ������int/long/double/String/Cell��o��ת��Ϊ(""+o)
	 * @return Cell����null
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
	 * ���ʹ���, ����: Integer, Long, Double��String
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
	 * �õ�Cell��ֵ
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
	 * �õ�data,�ı�˷���ֵ����ı�Cell
	 * @return
	 */
	public Object data() {
		return data;
	}

	/**
	 * �õ�ת�����ͺ����Cell
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
	 * ���Ǹ÷�����Cell������ΪMap��key��
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}

	/**
	 * ʵ��Cell�ıȽϡ�
	 * ����
	 * 1��null��С��
	 * 2��ͬ��ȥdata��;
	 * 3����ͬ�൫����Number��ת��ΪDouble�ȣ�
	 * 4����ͬ���з�Number, ȡvalue()��
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
	 * ��Cell����һCell��ӣ�������Cell����Cell���ı䡣
	 * 1����null��Ӳ��ı�
	 * 2���з�Number��������ַ�����ӣ�
	 * 3��Number�ͽ���������ӣ���ͬ������������ͽ���������
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
