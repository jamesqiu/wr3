package wr3;

import java.util.ArrayList;
import java.util.List;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * wr3���ݵĻ�����ʾ��ʽ, �ɶ�Ӧ�����ݿ�table��html�е�<table>
 * usage:
 * 	table = new Table();
 * 	// ���ͷ
 * 	table.head(Row.head(5));
 * 	table.head(1, new Cell("age"));
 * 	// ���row
 * 	table.add(new Row(5));
 * 	table.add(0, new Row(5));
 * 	table.add(1, new Row(5));
 * 	// ��С
 * 	table.width();
 * 	table.height();
 * 	table.rows();
 * 	table.cols();
 * 	// ȡhead
 * 	table.head();
 * 	table.head(1);
 * 	// ȡrow, cell
 * 	table.row(0);
 * 	table.cell(-1, -1);
 * 	// ��cell
 * 	table.cell(0, 0, new Cell(101));
 * 	// ��row
 * 	table.row(-1, new Row({1.1, 2.2, 3.3, 4.4, 5.5}));
 * 	// ɾrow, col
 * 	table.rmrow(0);
 * 	table.rmcol(-1);
 * 	// ȡ��������
 * 	table.subrow(0,-2);
 * 	table.subcol(3,1);
 * 	table.subtable(0,5,0,3);
 * 	// ���
 * 	table.toJson();
 * 	table.toHtml();
 *
 * ע��:
 *   (1) Table�Ŀ���ǹ̶���, ��head�Ŀ��Ϊ׼.
 *   (2) Table��Row��Ϊnull(null�в��벻��)
 *   (3) Table��Cell����Ϊnull
 *   (4) Table��headΪnull, ��width()Ϊ0, head(i)Ϊnull
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
	 * �õ�һ�����������Table
	 * @param cols	# of columns, or width
	 * @param rows # of rows, or height
	 */
	public Table(int cols, int rows) {
		head0(cols);
		data0(cols, rows);
	}

	/**
	 * �õ�ֻ��head, û�����ݵ�һ��Table.
	 * @param cols # of columns, or width
	 */
	public Table(int cols) {
		head0(cols);
	}

	// C0,...,Cn
	private void head0(int cols) {
		head = Row.createHead(cols);
	}

	// ÿ��Ϊ���������Table
	private void data0(int width, int height) {
		Row row;
		for (int i = 0; i < height; i++) {
			row = new Row(width);
			rows.add(row);
		}
	}

	/**
	 * ����head, ���ԭ���Ѿ���head, ��Ҫ��Сһ��.
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
	 * ͨ��String[]����head
	 * @param head
	 * @return
	 */
	public Table head(String[] head) {
		return head(Row.createByStrings(head));
	}

	/**
	 * �õ�head
	 * @return
	 */
	public Row head() {

		if (head==null) return null;

		return head.copy();
	}

	/**
	 * �õ�head����ֵ
	 * @param i
	 */
	public Cell head(int index) {

		if (head==null) return null;

		index = Numberx.safeIndex(index, cols());
		return head.cell(index);
	}

	/**
	 * ����head
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
	 * Table�Ŀ��/����Ŀ
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
	 * Talbe�ĸ߶�/����Ŀ
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
	 * ����һ������β.
	 * @param row0
	 */
	public Table add(Row row0) {
		if (row0==null) return this;
		rows.add(adjust(row0));
		return this;
	}

	/**
	 * �ڵ�index��֮ǰ����һ��
	 * @param index 0��ʾ��һ��,-1��ʾ���һ��
	 * @param row0 Ҫ�������
	 */
	public Table add(int index, Row row0) {
		if (row0==null) return this;

		index = Numberx.safeIndex(index, height());
		rows.add(index, adjust(row0));
		return this;
	}

	/**
	 * �����׷��һ�У�����г��Ȳ��ԣ�������
	 * @param col ���ı�
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
	 * �ڵ�index��֮ǰ����һ��, ����г��Ȳ��ԣ�������
	 * @param index
	 * @param col ���ı䡣
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
	 * ��Table��������.
	 * @param index 0��һ��,-1���һ��
	 */
	public Table rmrow(int index) {

		if (rows()==0) return this;

		index = Numberx.safeIndex(index, rows());
		rows.remove(index);
		return this;
	}

	/**
	 * Remove Col with given index.
	 * ��Table��������.
	 * @param index 0��һ����,-1���һ��
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
	 * ����һ���µ�Row, ��row0����������
	 * ����row0�ĳ���Ϊwidth(), ����Ĳ�null, �����Ľض�.
	 * @param row0
	 * @return ���ȷ��ϱ�Table��Row
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
	 * �õ�ָ����,�е�Cell
	 * @param row 0��ʾ��һ��, -1��ʾ���һ��
	 * @param col 0��ʾ��һ��, -1��ʾ���һ��
	 * @return
	 */
	public Cell cell(int row, int col) {

		if (rows()==0 || cols()==0) return null;

		row = Numberx.safeIndex(row, rows());
		col = Numberx.safeIndex(col, cols());
		return rows.get(row).cell(col);
	}

	/**
	 * ����ָ����,�е�Cellֵ
	 * @param row 0��ʾ��һ��, -1��ʾ���һ��
	 * @param col 0��ʾ��һ��, -1��ʾ���һ��
	 * @param cell0 �趨ֵ
	 */
	public Table cell(int row, int col, Cell cell0) {

		if (rows()==0 || cols()==0) return this;

		row = Numberx.safeIndex(row, rows());
		col = Numberx.safeIndex(col, cols());
		rows.get(row).set(col, cell0);
		return this;
	}

	/**
	 * �õ�ָ���кŵ�Row��һ��copy,
	 * @param index �к�, 0��ʾ��һ��, -1��ʾ���һ��
	 * @return Table��index�е�Row��clone
	 */
	public Row row(int index) {

		if (rows()==0) return null;

		index = Numberx.safeIndex(index, rows());
		return rows.get(index).copy();
	}

	/**
	 * ����ָ���кŵ�Row������.
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
	 * �õ�ָ���кŵ�Col��һ��copy.
	 * @param index �к�, 0��ʾ��һ��, -1��ʾ���һ��
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
	 * ����һ���µ�Table, ��Table�Ĳ���row,
	 * @param fromIndex ����,0��ʾ��һ��, -1��ʾ���һ��
	 * @param toIndex ����,0��ʾ��һ��, -1��ʾ���һ��
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
	 * �õ�ԭTable��һ��������
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
	 * ���Ϊ���е��ı�
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
	 * ���ΪString[][]����head��
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
	 * ���ΪList<List<?>>����head
	 * ����Clojure�ȴ���
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
	 * ���Ϊ��id��html &lt;table&gt;
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
	 * ���Ϊ����id�������html &lt;table&gt;
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
	 * ���Ϊjson�ַ�������:
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
