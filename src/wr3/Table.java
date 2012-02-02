package wr3;

import java.util.ArrayList;
import java.util.List;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * wr3数据的基本表示形式, 可对应于数据库table及html中的<table>
 * usage:
 * 	table = new Table();
 * 	// 设表头
 * 	table.head(Row.head(5));
 * 	table.head(1, new Cell("age"));
 * 	// 添加row
 * 	table.add(new Row(5));
 * 	table.add(0, new Row(5));
 * 	table.add(1, new Row(5));
 * 	// 大小
 * 	table.width();
 * 	table.height();
 * 	table.rows();
 * 	table.cols();
 * 	// 取head
 * 	table.head();
 * 	table.head(1);
 * 	// 取row, cell
 * 	table.row(0);
 * 	table.cell(-1, -1);
 * 	// 改cell
 * 	table.cell(0, 0, new Cell(101));
 * 	// 改row
 * 	table.row(-1, new Row({1.1, 2.2, 3.3, 4.4, 5.5}));
 * 	// 删row, col
 * 	table.rmrow(0);
 * 	table.rmcol(-1);
 * 	// 取部分区域
 * 	table.subrow(0,-2);
 * 	table.subcol(3,1);
 * 	table.subtable(0,5,0,3);
 * 	// 输出
 * 	table.toJson();
 * 	table.toHtml();
 *
 * 注意:
 *   (1) Table的宽度是固定的, 以head的宽度为准.
 *   (2) Table的Row不为null(null行插入不了)
 *   (3) Table的Cell可以为null
 *   (4) Table的head为null, 则width()为0, head(i)为null
 * </pre>
 * @author jamesqiu 2009-1-2
 * @see Cell, Row
 */
public class Table {

	private Row head = new Row();
	private List<Row> rows = new ArrayList<Row>();

	public Table() {
	}

	/**
	 * 得到一个随机整数的Table
	 * @param cols	# of columns, or width
	 * @param rows # of rows, or height
	 */
	public Table(int cols, int rows) {
		head0(cols);
		data0(cols, rows);
	}

	/**
	 * 得到只有head, 没有数据的一个Table.
	 * @param cols # of columns, or width
	 */
	public Table(int cols) {
		head0(cols);
	}

	// C0,...,Cn
	private void head0(int cols) {
		head = Row.createHead(cols);
	}

	// 每行为随机整数的Table
	private void data0(int width, int height) {
		Row row;
		for (int i = 0; i < height; i++) {
			row = new Row(width);
			rows.add(row);
		}
	}

	/**
	 * 设置head, 如果原来已经有head, 需要大小一致.
	 * @param head
	 */
	public Table head(Row head) {
		if (head==null) return this;
		if (width()>0 && width()!=head.width()) {
			new IllegalArgumentException("head width not fit.").printStackTrace();
			return this;
		}
		this.head = head.copy();
		return this;
	}

	/**
	 * 通过String[]设置head
	 * @param head
	 * @return
	 */
	public Table head(String[] head) {
		return head(Row.createByStrings(head));
	}

	/**
	 * 得到head
	 * @return
	 */
	public Row head() {

		if (head==null) return null;

		return head.copy();
	}

	/**
	 * 得到head的数值
	 * @param i
	 */
	public Cell head(int index) {

		if (head==null) return null;

		index = Numberx.safeIndex(index, cols());
		return head.cell(index);
	}

	/**
	 * 设置head
	 * @param index
	 * @param cell
	 * @return
	 */
	public Table head(int index, Cell cell) {

		if (head==null || cols()==0) return this;

		index = Numberx.safeIndex(index, cols());
		head.cell(index, cell);
		return this;
	}

	/**
	 * Table的宽度/列数目
	 * @return
	 */
	public int width() {
		if (head==null) return 0;
		return head.width();
	}

	/**
	 * alias of {@link #width()}
	 * @return
	 */
	public int cols() {
		return width();
	}

	/**
	 * Talbe的高度/行数目
	 * @return
	 */
	public int height() {
		if (rows==null) return 0;
		return rows.size();
	}

	/**
	 * alias of {@link #height()}
	 * @return
	 */
	public int rows() {
		return height();
	}

	/**
	 * 增加一行至表尾.
	 * @param row0
	 */
	public Table add(Row row0) {
		if (row0==null) return this;
		rows.add(adjust(row0));
		return this;
	}

	/**
	 * 在第index行之前插入一行
	 * @param index 0表示第一行,-1表示最后一行
	 * @param row0 要插入的行
	 */
	public Table add(int index, Row row0) {
		if (row0==null) return this;

		index = Numberx.safeIndex(index, height());
		rows.add(index, adjust(row0));
		return this;
	}

	/**
	 * 在最后追加一列，如果列长度不对，不处理。
	 * @param col 不改变
	 * @return
	 */
	public Table addcol(Col col) {

		if (col==null) return this;
		Row colAsRow = col.asRow();
		if (colAsRow.size()!=rows()) return this;

		head.add(col.head());

		for (int i = 0, n = rows(); i < n; i++) {
			rows.get(i).add(colAsRow.cell(i));
		}

		return this;
	}

	/**
	 * 在第index列之前插入一列, 如果列长度不对，不处理。
	 * @param index
	 * @param col 不改变。
	 * @return
	 */
	public Table addcol(int index, Col col) {

		if (col==null) return this;
		Row colAsRow = col.asRow();
		if (colAsRow.size()!=rows()) return this;

		index = Numberx.safeIndex(index, cols());
		head.add(index, col.head());

		for (int i = 0, n = rows(); i < n; i++) {
			rows.get(i).add(index, colAsRow.cell(i));
		}

		return this;
	}

	/**
	 * Remove Row with given index.
	 * 空Table不报错返回.
	 * @param index 0第一行,-1最后一行
	 */
	public Table rmrow(int index) {

		if (rows()==0) return this;

		index = Numberx.safeIndex(index, rows());
		rows.remove(index);
		return this;
	}

	/**
	 * Remove Col with given index.
	 * 空Table不报错返回.
	 * @param index 0第一个列,-1最后一列
	 */
	public Table rmcol(int index) {

		if (cols()==0) return this;

		index = Numberx.safeIndex(index, cols());
		head.remove(index);
		for (Row row : rows) {
			row.remove(index);
		}
		return this;
	}

	/**
	 * 返回一个新的Row, 从row0调整而来。
	 * 调整row0的长度为width(), 不足的补null, 超出的截断.
	 * @param row0
	 * @return 长度符合本Table的Row
	 */
	private Row adjust(Row row0) {
		int width0 = row0.width();
		int width1 = width();
		Row row1 = new Row();
		for (int i = 0; i < width1; i++) {
			if (i<width0) {
				row1.add(row0.cell(i));
			} else {
				row1.add((Cell)null);
			}
		}
		return row1;
	}

	/**
	 * 得到指定行,列的Cell
	 * @param row 0表示第一行, -1表示最后一行
	 * @param col 0表示第一列, -1表示最后一列
	 * @return
	 */
	public Cell cell(int row, int col) {

		if (rows()==0 || cols()==0) return null;

		row = Numberx.safeIndex(row, rows());
		col = Numberx.safeIndex(col, cols());
		return rows.get(row).cell(col);
	}

	/**
	 * 设置指定行,列的Cell值
	 * @param row 0表示第一行, -1表示最后一行
	 * @param col 0表示第一列, -1表示最后一列
	 * @param cell0 设定值
	 */
	public Table cell(int row, int col, Cell cell0) {

		if (rows()==0 || cols()==0) return this;

		row = Numberx.safeIndex(row, rows());
		col = Numberx.safeIndex(col, cols());
		rows.get(row).set(col, cell0);
		return this;
	}

	/**
	 * 得到指定行号的Row的一份copy,
	 * @param index 行号, 0表示第一行, -1表示最后一行
	 * @return Table第index行的Row的clone
	 */
	public Row row(int index) {

		if (rows()==0) return null;

		index = Numberx.safeIndex(index, rows());
		return rows.get(index).copy();
	}

	/**
	 * 设置指定行号的Row的数据.
	 * @param index
	 * @param row0
	 */
	public Table row(int index, Row row0) {

		if (row0==null || rows()==0) return this;

		index = Numberx.safeIndex(index, rows());
		rows.set(index, adjust(row0));
		return this;
	}

	/**
	 * 得到指定列号的Col的一份copy.
	 * @param index 列号, 0表示第一列, -1表示最后一列
	 * @return
	 */
	public Col col(int index) {

		int cols = cols();
		if (cols==0) return null;

		index = Numberx.safeIndex(index, cols);
		Row row = new Row();
		for (int i = 0, n = rows(); i < n; i++) {
			row.add(cell(i, index));
		}
		return row.asCol().head(head(index));
	}

	/**
	 * 返回一个新的Table, 含Table的部分row,
	 * @param fromIndex 包含,0表示第一个, -1表示最后一个
	 * @param toIndex 包含,0表示第一个, -1表示最后一个
	 * @return
	 */
	public Table subrow(int fromIndex, int toIndex) {
		int[] rt = Row.subIndex(fromIndex, toIndex, rows());
		fromIndex = rt[0];
		toIndex = rt[1];
		Table table1 = new Table();
		table1.head(head);
		for (int i = fromIndex; i <= toIndex; i++) {
			table1.add(row(i));
		}
		return table1;
	}

	public Table subcol(int fromIndex, int toIndex) {
		Table table1 = new Table();
		table1.head(head.subrow(fromIndex, toIndex));
		for (Row row : rows) {
			table1.add(row.subrow(fromIndex, toIndex));
		}
		return table1;
	}

	/**
	 * 得到原Table的一个拷贝。
	 * @return
	 */
	public Table copy() {

		Table copy = new Table();
		copy.head(head.copy());
		for (int i = 0; i < rows(); i++) {
			copy.add(row(i).copy());
		}
		return copy;
	}

	/**
	 * 输出为换行的文本
	 */
	public String toString() {

		StringBuilder sb = new StringBuilder();
		char sp = '\n';
		sb.append(head).append(sp);
		for (Row row : rows) {
			sb.append(row).append(sp);
		}
		return sb.toString();
	}

	/**
	 * 输出为String[][]，无head。
	 * @return
	 */
	public String[][] toArray() {

		int rows = rows();
		int cols = cols();
		String[][] rt = new String[rows][cols];

		for (int i = 0; i < rows; i++) {
			rt[i] = row(i).asString().asList().toArray(new String[cols]);
		}
		return rt;
	}

	/**
	 * 输出为List<List<?>>，带head
	 * 便于Clojure等处理
	 * @author jamesqiu 2011-4-20
	 * @return
	 */
	public List<List<?>> toList() {

		List<List<?>> rt = new ArrayList<List<?>>();
		rt.add(head().asList());
		int rows = rows();
		for (int i = 0; i < rows; i++) {
			rt.add(row(i).asList());
		}
		return rt;
	}

	/**
	 * 输出为带id的html &lt;table&gt;
	 * @param id
	 * @return
	 */
	public String toHtml(String id) {
		String tag = "table";
		StringBuilder sb = new StringBuilder();
		sb.append(Row.tagStart(tag, id));
		// <thead>
		sb.append("<thead>");
		for (int i = 0; i < cols(); i++) {
			sb.append("<th>").append(head(i)).append("</th>");
		}
		sb.append("</thead>");
		// <tbody>
		sb.append("<tbody>");
		for (Row row : rows) {
			sb.append(row.toHtmlTr());
		}
		sb.append("</tbody>");

		sb.append(Row.tagEnd(tag));
		return sb.toString();

	}

	/**
	 * 输出为不带id的最简略html &lt;table&gt;
	 * @return
	 */
	public String toHtml1() {
		StringBuilder sb = new StringBuilder("<table>");
		for (Row row : rows) {
			sb.append("<tr>");
			for (int i=0; i<row.size(); i++) {
				sb.append("<td>").append(row.cell(i)).append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();

	}

	/**
	 * <pre>
	 * 输出为json字符串，如:
	 * {head: ["c1", "c2", "c3],
	 *  data:[[11, 22, 33],
	 *        [21, 22, 33],
	 *        [31, 32, 33]]}
	 * </pre>
	 * @return
	 */
	public String toJson() {
		return "{" + headJson() + ",\n" + dataJson() + "}";
	}

	/**
	 * head: ["c1", "c2", "c3]
	 */
	private String headJson() {

		String json = (cols()==0) ? "[]" : head.toJson();
		return "head: " + json;
	}

	/**
	 *  data:[[11, 22, 33],
	 *        [21, 22, 33],
	 *        [31, 32, 33]]
	 */
	private String dataJson() {
		int n = rows();
		String[] json = new String[n];
		for (int i = 0; i < n; i++) {
			json[i] = rows.get(i).toJson();
		}
		return " data:[" + Stringx.join(json, ",\n       ") + ']';
	}
}
