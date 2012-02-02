package wr3;

import java.util.ArrayList;
import java.util.List;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * Row����һ��Cell.
 * Row��Ԫ���ǻ���Cell����null, ��ʼ����Ϊ0.
 * </pre>
 * @author jamesqiu 2009-1-1
 *
 */
public class Row implements Comparable<Row>{

	private List<Cell> cells = new ArrayList<Cell>();

	/**
	 * ���г�ʼ��
	 */
	public Row() {
	}

	/**
	 * �õ�����Ϊn�������Row
	 * @param n
	 */
	public Row(int n) {
		cells.clear();
		int[] nn = Numberx.randoms(n);
		for (int i : nn) {
			cells.add(new Cell(i));
		}
	}

	/**
	 * ʹ���ض���cell��ʼ��һ��Row
	 * @param n
	 * @param cell
	 */
	public Row(int n, Cell cell) {

		cells.clear();
		for (int i = 0; i < n; i++) {
			if (cell==null) {
				cells.add(null);
			} else {
				cells.add(new Cell(cell));
			}
		}
	}

	/**
	 * ��List<Cell>����ʼ����Row.
	 * @param cells ���ú�cells���ı�, ֮����Ϊ����cells��ֵҲ����ı䱾Row.
	 */
	public Row(List<Cell> cells) {
		if (cells==null) return;
		this.cells = copyCells(cells);
	}

	/**
	 * ��Object[]����ʼ����Row.
	 * @param cells ��: {null, Cell.create(10), 100L, 3.1416d, "cn����"}
	 */
	public Row(Object[] cells) {
		if (cells==null) return;

		this.cells.clear();
		for (Object object : cells) {
			if (object==null) {
				this.cells.add(null);
			} else {
				Class<?> type = object.getClass();
				Cell cell;
				if (type==Cell.class) {
					cell = new Cell((Cell)object);//.copy();
				} else if (type==Integer.class) {
					cell = Cell.create(((Integer)object).intValue());
				} else if (type==Long.class) {
					cell = Cell.create(((Long)object).longValue());
				} else if (type==Double.class) {
					cell = Cell.create(((Double)object).doubleValue());
				} else {
					cell = Cell.create(object.toString());
				}
				this.cells.add(cell);
			}
		}
	}

	/**
	 * �õ���Row��һ��clone
	 * @return
	 */
	public Row copy() {
		return new Row(cells);
	}

	public static Row create(int n) {
		return new Row(n);
	}

	public static Row create(int n, Cell cell) {
		return new Row(n, cell);
	}

	public static Row create(List<Cell> cells) {
		return new Row(cells);
	}

	public static Row create(Cell[] cells) {
		return new Row(cells);
	}

	public static Row createByStrings(String[] strings) {

		Row row = new Row();

		if (strings==null) return row;

		for (String string : strings) {
			row.add(new Cell(string));
		}
		return row;
	}

	/**
	 * ʹ�ø������͵Ĳ����Զ���ʼ��һ��Row.
	 * <pre>
	 * usage:
	 *   Row.createByTypes(0, "cn", null, 3.14)
	 * </pre>
	 * @param objs
	 * @return
	 */
	public static Row createByTypes(Object ... objs) {

		Row row = new Row(objs);
		return row;
	}

	/**
	 * alias of {@link #createHead(int)}
	 * @param n
	 * @return
	 */
	public static Row head(int n) {
		return createHead(n);
	}

	/**
	 * �õ�һ��ָ�����ȵ�head.
	 * @param n
	 * @return {"c0", "c1", "c2", ...}
	 */
	public static Row createHead(int n) {
		Row head = new Row();
		if (n < 0) n = 0;
		for (int i = 0; i < n; i++) {
			head.add(new Cell("c"+i));
		}
		return head;
	}

	/**
	 * �õ�size����length
	 */
	public int size() {
		return cells.size();
	}

	public int length() {
		return size();
	}

	public int width() {
		return size();
	}

	/**
	 * alias of {@link #get(int)}
	 * @param index
	 * @return
	 */
	public Cell cell(int index) {
		return get(index);
	}

	/**
	 * �õ���Rowָ��index��Cell.
	 * @param index 0Ϊ��һ��Ԫ��,-1����(size()-1)Ϊ���һ��Ԫ��
	 * @return
	 */
	public Cell get(int index) {

		if (isEmpty()) return null;
		index = Numberx.safeIndex(index, size());
		return cells.get(index);
	}

	/**
	 * �õ���Row.cells��һ��copy
	 * @return
	 */
	public List<Cell> getCells() {
		return copyCells(cells);
	}

	/**
	 * ��һ��List<Cell>�����copy
	 * @param cells ��Ϊnull
	 * @return
	 */
	private List<Cell> copyCells(List<Cell> cells) {
		/**
		 * ����һ�е�ʵ��, ��copy�˸�cell��ָ��,�ı�cells��ı�Row.
		 * 	this.cells = new ArrayList<Cell>(cells);
		 * �����÷�ֻ������copyԭʼ���ͺ�final������String.
		 */
		List<Cell> copy = new ArrayList<Cell>();
		for (Cell cell : cells) {
			if (cell==null) {
				copy.add(null);
			} else {
				copy.add(new Cell(cell));
			}
		}
		return copy;
	}

	/**
	 * ΪRow����һ��cell
	 * @param cell ���ӵ���Rowβ��
	 */
	public Row add(Cell cell) {
		cells.add(cell);
		return this;
	}

	/**
	 * ��ָ��λ��֮ǰ����һ��Cell
	 * @param index 0��ʾ���ڵ�һ��λ��
	 * @param cell Ҫ�����Cell
	 */
	public Row add(int index, Cell cell) {

		index = Numberx.safeIndex(index, size());
		cells.add(index, cell);
		return this;
	}

	/**
	 * alias of {@link #set(int, Cell)}
	 * @param index
	 * @param cell
	 */
	public Row cell(int index, Cell cell) {
		set(index, cell);
		return this;
	}

	/**
	 * �ı�ָ��index��cell.
	 * @param index
	 * @param cell
	 */
	public Row set(int index, Cell cell) {

		index = Numberx.safeIndex(index, size());
		cells.set(index, cell);
		return this;
	}

	/**
	 * ɾ��ָ��index��cell. ��Cell�Ŀ�Row��������.
	 * @param index
	 */
	public Row remove(int index) {

		if (size()==0) return this;

		index = Numberx.safeIndex(index, size());
		cells.remove(index);
		return this;
	}

	/**
	 * <pre>
	 * ȡ��Row��һ������[fromIndex, toIndex)
	 * {11,22,33,44,55}
	 * subrow(0,1): {11,22}
	 * subrow(0,4): {11,22,33,44,55}
	 * subrow(3,3): {44}
	 * subrow(4,1)==subrow(1,4): {22,33,44,55}
	 * subrow(4,4): {55}
	 * subrow(0,-1): {11,22,33,44,55}
	 * subrow(-2,-1): {44}
	 * </pre>
	 * @param fromIndex ����,0��ʾ��һ��, -1��ʾ���һ��
	 * @param toIndex ����,0��ʾ��һ��, -1��ʾ���һ��
	 * @return
	 */
	public Row subrow(int fromIndex, int toIndex) {
//		int size = size();
//		fromIndex = Numberx.safeIndex(fromIndex, size);
//		toIndex = Numberx.safeIndex(toIndex, size);
//		if (fromIndex>toIndex) {
//			int tmp = fromIndex;
//			fromIndex = toIndex;
//			toIndex = tmp;
//		}
		int[] rt = subIndex(fromIndex, toIndex, size());
		fromIndex = rt[0];
		toIndex = rt[1];
		return new Row(cells.subList(fromIndex, toIndex+1));
	}

	static int[] subIndex(int fromIndex, int toIndex, int length) {
		fromIndex = Numberx.safeIndex(fromIndex, length);
		toIndex = Numberx.safeIndex(toIndex, length);
		if (fromIndex>toIndex) {
			int tmp = fromIndex;
			fromIndex = toIndex;
			toIndex = tmp;
		}
		return new int[]{fromIndex, toIndex};
	}

	/**
	 * ����һ��row�����ݺϲ�
	 */
	public Row join(Row row1) {

		if (isNull(row1)) return this;

		cells.addAll(row1.getCells());
		return this;
	}

	/**
	 * ����һ��row��������Ϊdouble������.
	 * this += row1;
	 * @return �ı������ݵı�Row
	 */
	public Row plus(Row row1) {
		return compute(plus, row1);
	}


	/**
	 * ����һ��row��������Ϊdouble������.
	 * this -= row1;
	 * @return �ı������ݵı�Row
	 */
	public Row minus(Row row1) {
		return compute(minus, row1);
	}

	/**
	 * ʹ�����������һ��Row��������
	 * @param operator
	 * @param row1
	 * @return
	 */
	public Row compute(Operator operator, Row row1) {

		if (isNull(row1) || !isSameSize(row1)) return this;

		List<Cell> cells1 = row1.getCells();
		int size = size();
		for (int i = 0; i < size; i++) {
			double d = cells.get(i).doubleValue();
			double d1 = cells1.get(i).doubleValue();
			cells.set(i, Cell.create(operator.run(d, d1)));
		}
		return this;
	}

	/**
	 * �����
	 * @author jamesqiu 2009-1-5
	 */
	interface Operator {
		double run(double d1, double d2);
	}

	Operator plus = new Operator() {
		public double run(double d1, double d2) {
			return d1+d2;
		}
	};

	Operator minus = new Operator() {
		public double run(double d1, double d2) {
			return d1-d2;
		}
	};

	// Operator multiply, div

	/**
	 * <pre>
	 * �õ�һ������cellת��Ϊint�Ŀ���.
	 * Row��Ϊnull��cell, ����nullֵ.
	 * </pre>
	 * @param TYPE int.class, long.class, double.class, String.class
	 * @return
	 */
	public Row asInt() {
		return as(int.class);
	}

	/**
	 * @see #asInt()
	 * @return
	 */
	public Row asLong() {
		return as(long.class);
	}

	/**
	 * @see #asInt()
	 * @return
	 */
	public Row asDouble() {
		return as(double.class);
	}

	/**
	 * @see #asInt()
	 * @return
	 */
	public Row asString() {
		return as(String.class);
	}

	/**
	 * @see #asInt()
	 * @return
	 */
	public Row asPercent() {
		return as(Row.class); // �ò���int, long, double, String��class�Կ�
	}

	Row as(Class<?> type) {
		// todo: ����nullԪ��
		Row row1 = new Row();
		for (Cell cell : getCells()) {
			if (cell==null) {
				row1.add((Cell)null);
			} else {
				row1.add(cell.as(type));
			}
		}
		return row1;
	}

	/**
	 * �ѱ�RowתΪCol
	 * @return Row��Col��ͼ
	 */
	public Col asCol() {
		return new Col(this);
	}

	/**
	 * �ѱ�RowתΪList, ����Ϊ: Integer | Long | Double | String | null
	 * @return
	 */
	public List<Object> asList() {
		List<Object> list = new ArrayList<Object>();
		for (Cell cell : getCells()) {
			if (cell==null) {
				list.add(null);
			} else {
				list.add(cell.data());
			}
		}
		return list;
	}

	/**
	 * String as List.toString()
	 */
	public String toString() {
		return cells.toString();
	}

	/**
	 * ��Rowת��ΪCSV�ļ���һ��, ��","�ָ���ע�ⲻ����", "�ָ�
	 * <pre>
	 *
	 * </pre>
	 * @return
	 */
	public String toCsv() {

		int n = size();
		String[] csv = new String[n];
		for (int i = 0; i < n; i++) {
			Cell cell = cells.get(i);
			csv[i] = (cell==null) ? null : cell.toCsv();
		}
		return Stringx.join(csv, ",");
	}

	/**
	 * json String
	 * @return
	 */
	public String toJson() {

		int n = size();
		String[] json = new String[n];
		for (int i = 0; i < n; i++) {
			Cell cell = cells.get(i);
			json[i] = (cell==null) ? null : cell.toJson();
		}
		return '[' + Stringx.join(json, ", ") + ']';
	}

	/**
	 * <pre>
	 * as &lt;table&gt; with one &lt;tr&gt; html string
	 * </pre>
	 * @param id id of &lt;table id=".."&gt;
	 * @return
	 */
	public String toHtml(String id) {

		String tag = "table";
		StringBuilder sb = new StringBuilder();
		sb.append(tagStart(tag, id));
		sb.append(toHtmlTr());
		sb.append(tagEnd(tag));
		return sb.toString();
	}

	static String tagStart(String tag, String id) {
		if (Stringx.nullity(id)) {
			id = "";
		} else {
			id = " id=\""+id+"\"";
		}
		return "<" + tag + id + ">";
	}

	static String tagEnd(String tag) {
		return "</" + tag + ">";
	}

	/**
	 * as one &lt;tr&gt; html string
	 * @return
	 */
	public String toHtmlTr() {

		StringBuilder sb = new StringBuilder("<tr>");

		int n = size();
		for (int i = 0; i < n; i++) {
			Cell cell = cells.get(i);
			sb.append("<td>");
			sb.append((cell==null) ? null : cell.value());
			sb.append("</td>");
		}

		sb.append("</tr>");
		return sb.toString();
	}

	/**
	 * <pre>
	 * to &lt;ul&gt;&lt;li&gt; html string
	 * </pre>
	 * @return
	 */
	public String toHtmlList(String id) {

		String tag = "ul";
		StringBuilder sb = new StringBuilder();
		sb.append(tagStart(tag, id));
		int n = size();
		for (int i = 0; i < n; i++) {
			Cell cell = cells.get(i);
			sb.append("<li>");
			sb.append((cell==null) ? null : cell.value());
			sb.append("</li>");
		}
		sb.append(tagEnd(tag));
		return sb.toString();
	}

	private boolean isSameSize(Row row) {

		if (this.size()!=row.size()) {
			new IllegalArgumentException("Two rows are not save size.").printStackTrace();
			return false;
		}
		return true;
	}

	private boolean isNull(Row row) {
		if (row==null) {
			new NullPointerException("Row is null.").printStackTrace();
			return true;
		}
		return false;
	}

	private boolean isEmpty() {
		if (size()==0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj==null || !(obj instanceof Row)) return false;

		Row row2 = (Row) obj;
		for (int i = 0, n = size(); i < n; i++) {
			// ����null�����
			if ((cell(i)==null) ^ (row2.cell(i)==null)) return false;
			if (cell(i)==null && row2.cell(i)==null) continue;
			// ��null
			if (!(cell(i).equals(row2.cell(i)))) return false;
		}
		return true;
	}

	/**
	 * ���ش˷�����Row������Map��key.
	 *
	 * 1�������null��ȡ"".����
	 * 2��r1.equals(r2) => r1.hashCode=r2.hashCode; ��֮δ�أ������������
	 *    new Row(others) �� Row[new Cell(-36*17), others]��hashCode���
	 *    Row[..., "<null>", ...] �� Row[..., null, ...]��hashCode���
	 */
	@Override
	public int hashCode() {
		int rt = 17;
		for (Cell cell : cells) {
			int c = cell==null ? "<null>".hashCode() : cell.hashCode();
			rt = 37*rt + c;
		}
		return rt;
	}

	/**
	 * ʵ�ִ˷�����, Row�ɽ��бȽ���������TreeSet���С�
	 * �ȽϹ���
	 * 1��null��С��
	 * 2����Row(0)��ʼ�Ƚϣ��ȳ���С���ɣ���ͬ���һ����
	 */
	public int compareTo(Row o) {

		if (o==null) return 1;
		int n1 = size();
		int n2 = o.size();
		int n = Math.min(n1, n2);
		for (int i = 0; i < n; i++) {
			// cellΪnull
			if (cell(i)==null && o.cell(i)==null) continue;
			if (cell(i)==null) return -1;
			if (o.cell(i)==null) return 1;
			// cell��null
			int rt = cell(i).compareTo(o.cell(i));
			if (rt==0) continue;
			return rt;
		}
		return n1-n2;
	}
}
