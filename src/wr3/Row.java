package wr3;

import java.util.ArrayList;
import java.util.List;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * Row代表一行Cell.
 * Row的元素是基本Cell或者null, 初始长度为0.
 * </pre>
 * @author jamesqiu 2009-1-1
 *
 */
public class Row implements Comparable<Row>{

	private List<Cell> cells = new ArrayList<Cell>();

	/**
	 * 进行初始化
	 */
	public Row() {
	}

	/**
	 * 得到长度为n的随机数Row
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
	 * 使用特定的cell初始化一个Row
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
	 * 用List<Cell>来初始化本Row.
	 * @param cells 调用后cells不改变, 之后人为更改cells的值也不会改变本Row.
	 */
	public Row(List<Cell> cells) {
		if (cells==null) return;
		this.cells = copyCells(cells);
	}

	/**
	 * 用Object[]来初始化本Row.
	 * @param cells 如: {null, Cell.create(10), 100L, 3.1416d, "cn中文"}
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
	 * 得到本Row的一个clone
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
	 * 使用各种类型的参数自动初始化一个Row.
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
	 * 得到一个指定长度的head.
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
	 * 得到size或者length
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
	 * 得到本Row指定index的Cell.
	 * @param index 0为第一个元素,-1或者(size()-1)为最后一个元素
	 * @return
	 */
	public Cell get(int index) {

		if (isEmpty()) return null;
		index = Numberx.safeIndex(index, size());
		return cells.get(index);
	}

	/**
	 * 得到本Row.cells的一份copy
	 * @return
	 */
	public List<Cell> getCells() {
		return copyCells(cells);
	}

	/**
	 * 对一个List<Cell>的深度copy
	 * @param cells 不为null
	 * @return
	 */
	private List<Cell> copyCells(List<Cell> cells) {
		/**
		 * 如下一行的实现, 仅copy了各cell的指针,改变cells会改变Row.
		 * 	this.cells = new ArrayList<Cell>(cells);
		 * 这种用法只能用于copy原始类型和final对象如String.
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
	 * 为Row增加一个cell
	 * @param cell 增加到本Row尾部
	 */
	public Row add(Cell cell) {
		cells.add(cell);
		return this;
	}

	/**
	 * 在指定位置之前插入一个Cell
	 * @param index 0表示插在第一个位置
	 * @param cell 要插入的Cell
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
	 * 改变指定index的cell.
	 * @param index
	 * @param cell
	 */
	public Row set(int index, Cell cell) {

		index = Numberx.safeIndex(index, size());
		cells.set(index, cell);
		return this;
	}

	/**
	 * 删除指定index的cell. 无Cell的空Row不报错返回.
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
	 * 取本Row的一个子行[fromIndex, toIndex)
	 * {11,22,33,44,55}
	 * subrow(0,1): {11,22}
	 * subrow(0,4): {11,22,33,44,55}
	 * subrow(3,3): {44}
	 * subrow(4,1)==subrow(1,4): {22,33,44,55}
	 * subrow(4,4): {55}
	 * subrow(0,-1): {11,22,33,44,55}
	 * subrow(-2,-1): {44}
	 * </pre>
	 * @param fromIndex 包含,0表示第一个, -1表示最后一个
	 * @param toIndex 包含,0表示第一个, -1表示最后一个
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
	 * 和另一个row的数据合并
	 */
	public Row join(Row row1) {

		if (isNull(row1)) return this;

		cells.addAll(row1.getCells());
		return this;
	}

	/**
	 * 和另一个row的数据作为double逐个相加.
	 * this += row1;
	 * @return 改变了数据的本Row
	 */
	public Row plus(Row row1) {
		return compute(plus, row1);
	}


	/**
	 * 和另一个row的数据作为double逐个相减.
	 * this -= row1;
	 * @return 改变了数据的本Row
	 */
	public Row minus(Row row1) {
		return compute(minus, row1);
	}

	/**
	 * 使用运算符和另一个Row进行运算
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
	 * 运算符
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
	 * 得到一个所有cell转换为int的拷贝.
	 * Row中为null的cell, 保持null值.
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
		return as(Row.class); // 用不是int, long, double, String的class皆可
	}

	Row as(Class<?> type) {
		// todo: 处理null元素
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
	 * 把本Row转为Col
	 * @return Row的Col视图
	 */
	public Col asCol() {
		return new Col(this);
	}

	/**
	 * 把本Row转为List, 内容为: Integer | Long | Double | String | null
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
	 * 把Row转换为CSV文件的一行, 用","分隔，注意不能用", "分隔
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
			// 考虑null的情况
			if ((cell(i)==null) ^ (row2.cell(i)==null)) return false;
			if (cell(i)==null && row2.cell(i)==null) continue;
			// 非null
			if (!(cell(i).equals(row2.cell(i)))) return false;
		}
		return true;
	}

	/**
	 * 重载此方法后，Row可用作Map的key.
	 *
	 * 1、如果是null，取"".代替
	 * 2、r1.equals(r2) => r1.hashCode=r2.hashCode; 反之未必，如特殊情况：
	 *    new Row(others) 和 Row[new Cell(-36*17), others]的hashCode相等
	 *    Row[..., "<null>", ...] 和 Row[..., null, ...]的hashCode相等
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
	 * 实现此方法后, Row可进行比较排序，用在TreeSet等中。
	 * 比较规则：
	 * 1、null最小；
	 * 2、从Row(0)开始比较，比出大小即可，相同则后一个。
	 */
	public int compareTo(Row o) {

		if (o==null) return 1;
		int n1 = size();
		int n2 = o.size();
		int n = Math.min(n1, n2);
		for (int i = 0; i < n; i++) {
			// cell为null
			if (cell(i)==null && o.cell(i)==null) continue;
			if (cell(i)==null) return -1;
			if (o.cell(i)==null) return 1;
			// cell非null
			int rt = cell(i).compareTo(o.cell(i));
			if (rt==0) continue;
			return rt;
		}
		return n1-n2;
	}
}
