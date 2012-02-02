package wr3.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.Cell;
import wr3.Col;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.text.Template;
import wr3.util.Rowx;
import wr3.util.Stringx;
import wr3.web.Params;
import static wr3.util.Stringx.*;

/**
 * 注意：
 * 1、不同维度或者指标的code不能设成一样；不同指标的meta不能设成一样；
 * @author jamesqiu 2009-11-18
 */
public class CubeTable extends BaseTable {

	public static CubeTable create() {
		
		return new CubeTable();
	}
	
	/**
	 * 在web上使用的时候把request params传入进来。
	 * @param params
	 * @return
	 */
	public static CubeTable create(Params params) {
		
		CubeTable instance = new CubeTable();
		instance.params(params);
		return instance;
	}
		
	@Override
	public CubeTable id(String id) {
		super.id(id);
		return this;
	}
	
	@Override
	public CubeTable data(Table table) {
		super.data(table);
		return this;
	};
	
	@Override
	public CubeTable view(String templateFilename) {
		super.view(templateFilename);
		return this;
	}

	@Override
	public CubeTable filter(CellFilter filter) {
		super.filter(filter);
		return this;
	}

	@Override
	public CubeTable dd(int headIndex, Map<?, ?> dd) {
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public CubeTable dd(String headCode, Map<?, ?> dd) {
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public CubeTable meta(List<?> metaList) {
		super.meta(metaList);
		return this;
	}

	@Override
	public CubeTable meta(Map<?, ?> metaMap) {
		super.meta(metaMap);
		return this;
	}
	
	// ----------------- CubeTable's methods -----------------//

	/**
	 * 维度指标布局
	 */
	private enum Position {TOP, LEFT, NONE}; // 标识指标数大于1的时候放在上还是左
	
	private class Layout {
		List<String> topCodes; 		List<Integer> topIndex;
		List<String> leftCodes; 	List<Integer> leftIndex;
		List<String> measureCodes; 	List<Integer> measureIndex;
		Row measureMetas = new Row();
		Position measurePosition = Position.NONE;
		
		@Override
		public String toString() {
			return Stringx.printf("\n top: %s(%s)\n left: %s(%s)\n" +
					" measureCodes: %s(%s)\n measureMetas: %s\n measurePosition: %s\n",
					topCodes, topIndex, leftCodes, leftIndex,
					measureCodes, measureIndex, measureMetas, measurePosition);
		}
	}
	private Layout layout = new Layout();
	
	/**
	 * 设置上边维度
	 * @param code 代表维度的字段代码
	 * @return 
	 */
	public CubeTable top(String... codes) {
	
		layout.topCodes = Stringx.ss2set(codes);
		super.needProcess();
		return this;
	}
	
	/**
	 * 设置左边维度
	 * @param code 代表维度的字段代码
	 * @return
	 */
	public CubeTable left(String... codes) {
		
		layout.leftCodes = Stringx.ss2set(codes);
		super.needProcess();
		return this;
	}
		
	/**
	 * 设置计数指标
	 * @param onTop 指标列是否在上显示，缺省为true。
	 * @param codes 指标列的代码列表，如："month", "name"。
	 * @return
	 */
	public CubeTable measure(boolean onTop, String... codes) {
		
		layout.measureCodes = Stringx.ss2set(codes);
		layout.measurePosition = onTop ? Position.TOP : Position.LEFT;
		super.needProcess();
		return this;
	}
	/**
	 * @see #measure(boolean, String...)
	 * @param codes
	 * @return
	 */
	public CubeTable measure(String... codes) {
		return measure(true, codes);
	}
	
	/**
	 * 使用表达式方便设置维度、指标布局，如: 
	 *   "month \\ name, [hq, dq]",
	 *   "month, name \\ [hq, dq]",
	 *   "month, name, [hq, dq] \\",
	 *   "\\ month, [qh]"
	 *   
	 * @param express 维度、指标布局的表达式
	 * @return
	 * @pre 
	 * @post 设置 topCodes, leftCodes, measureCodes
	 */
	public CubeTable layout(String express) {
		
		CubeLayoutParser p = CubeLayoutParser.create(express);
		top(p.top());
		left(p.left());
		measure(p.measureOnTop(), p.measure());
		
		super.needProcess();
		return this;		
	}
	
	private boolean debug = false;
	public CubeTable debug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	@Override
	Table processData(Table data) {
		
		if (valid(data)) {
			// 生成空的框架Frame
			genFrame(data);
			// 往空Frame中填data
			fillFrame(data);	
//			// 合计
//			addSum();
		}
		return frame;
	}

	@Override
	void processTemplate(Template t) {
		
//		t.set("topMeta", topMeta);
//		t.set("leftMeta", leftMeta);
		t.set("measureMeta", layout.measureMetas);
		
		t.set("cubeHtml", cubeHtml);
	}

	/**
	 * data --处理--> frame:
	 * ┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈
	 *┊          ┊
	 *┊  corner  ┊┈┈frame_top┈┈┈┈┈┈┈┈
	 *┊          ┊    (指标数>1)
	 *┊┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈
	 *┊   ┊     ┊
	 *┊frame_left┊    (指标数据区)
	 *┊   ┊     ┊
	 *┊   ┊(指  ┊
	 *┊   ┊ 标  ┊
	 *┊   ┊ 数  ┊
	 *┊   ┊ >1) ┊
	 *┊   ┊     ┊
	 */
	private Table frame;
	
	private Table frame_corner;
	private Table frame_top; // 需要旋转过来看
	private Table frame_left;	
	
	private void genFrame(final Table data) {
	
		// 检测处理设置的 top / left / measure / ...，重设layout
		refineLayout(data);
		
		// 得到上维度相关，如：[1月，2月，3月]
		frame_top = Rowx.uniq(selectCols(data, layout.topCodes));	
		// 得到左维度相关，如：[机构1，机构2，机构3]
		frame_left = Rowx.uniq(selectCols(data, layout.leftCodes));
		
		// 有指标的情况
		if (layout.measurePosition==Position.TOP) {
			frame_top = multiplyMeasure(frame_top);
		} else if (layout.measurePosition==Position.LEFT) {
			frame_left = multiplyMeasure(frame_left);
		}
		
		// 把左上角画斜线的部分称为corner
		int corner_rows = layout.topCodes.size();
		int corner_cols = layout.leftCodes.size();
		if (layout.measurePosition==Position.TOP) {
			corner_rows++; 
		} else if (layout.measurePosition==Position.LEFT) {
			corner_cols++;
		}
		frame_corner = new Table().head(Row.createHead(corner_cols));
		for (int i = 0; i < corner_rows; i++) {
			frame_corner.add(new Row(corner_cols, new Cell("")));
		}		
		
		// 逐行构建frame
		int top_cols = frame_top.rows(); // 上维度宽度
		// 有指标, 没有上维度的情况: 至少有1列来放指标合计值
		if (layout.measureCodes.size()>0 && top_cols==0) {
			top_cols = 1; 
		}
		
		// --- frame.head
		int frame_cols = corner_cols + top_cols;
		frame = new Table().head(Row.createHead(frame_cols));
		
		// --- 上frame的rows
		for (int i = 0, n = frame_top.cols(); i < n; i++) {
			Row row = frame_corner.row(i);
			row.join(frame_top.col(i).asRow());
			frame.add(row);
		}
		
		// --- 左frame的rows
		for (int i = 0, n = frame_left.rows(); i < n; i++) {
			Row row = frame_left.row(i);
			frame.add(row);
		}
		// ------ 如果没有左维度（frame_left=[]）且有指标，占1行
		if (frame_left.rows()==0 && layout.measureCodes.size()>0) {
			frame.add(new Row());
		}
		
		if (debug) {
			System.out.printf(" -genFrame():\n -layout=%s -corner=%s -top=%s -left=%s -frame=%s\n", 
				layout, frame_corner, frame_top, frame_left,frame);
		}
		
		// 计算frame的cosspan,rowspan
		spans = new CubeTableSpans()
			.corner(frame_corner)
			.top(frame_top)
			.left(frame_left);
	}
	
	/**
	 * 把data的所有指标列中的数据累加填充到frame中
	 * @param data
	 */
	private void fillFrame(final Table data) {
		
		measureTypes = new LinkedHashMap<Integer, Boolean>();
		
		for (int i : layout.measureIndex) {
			measureTypes.put(i, isNumberType(data, i));
			// 处理1个指标列, i: 指标列的列index
			for (int j = 0, m = data.rows(); j < m; j++) {
				// 处理指标列中的1个cell
				fill(data, j, i);
			}
		}	

		cubeHtml = cubeHtml(spans);
	}
	
	private CubeTableSpans spans;
	private String cubeHtml;
	
	private String cubeHtml(CubeTableSpans spans) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0, rows = frame.rows(); i < rows; i++) {
			sb.append("<tr>\n");
			for (int j = 0, cols = frame.cols(); j < cols; j++) {
				Cell v = frame.cell(i, j);
				if (spans.contains(i, j)) {
					String a = spans.get(i, j);
					if (a==null) {
						continue;
					} else {					
						String css_class = isCorner(i,j) ? "corner" : "frame";
						sb.append(printf("<th %s class=\"%s\">%s</th>", 
								a, css_class, v));
					}
				} else {
					sb.append(printf("<td class=\"numberFormat\">%s</td>", 
							(v==null ? "" : v)));
				}
			}
			sb.append("\n</tr>\n");
		}
		
		return sb.toString();
	}
	
	private boolean isCorner(int i, int j) {
		if (i!=0 || j!=0) return false;
		if (frame_corner.cols()==0 || frame_corner.rows()==0) return false;
		return true;
	}

	/**
	 * 把原始数据中1个指定行、列编号的cell填充到frame中
	 * @param data 原始数据Table
	 * @param row0 原始数据表Cell行号
	 * @param col0 原始数据表Cell列号
	 */
	private void fill(Table data, int row0, int col0) {
		
		// 找到top坐标col1。从本row找到top维度code及指标meta
		Row row_top = new Row();
		for (int i : layout.topIndex) {
			row_top.add(data.cell(row0, i));
		}
		if (layout.measurePosition==Position.TOP) {
			row_top.add(data.head(col0)); 
		}
		int col1 = indexOf(row_top, frame_top) + frame_left.cols();
		
		// 找到left坐标row1。从本row找到left维度code及指标meta
		Row row_left = new Row();
		for (int i : layout.leftIndex) {
			row_left.add(data.cell(row0, i));
		}
		if (layout.measurePosition==Position.LEFT) {
			row_left.add(data.head(col0));
		}
		int row1 = indexOf(row_left, frame_left) + frame_top.cols();
//		System.out.printf("(row1,col1)=(%d,%d)\n", row1, col1);
		
		// 填充数据到frame的[col1, row1]坐标
		//（如果该列是数据则累加，有字符串则计count，null忽略不计）
		Cell cell = frame.cell(row1, col1);
		if (cell==null) cell = new Cell(0);

		if (measureTypes.get(col0)) {
			// 数字类型（或全null）
			frame.cell(row1, col1, cell.add(data.cell(row0, col0)));
		} else {
			// 非数字类型
			frame.cell(row1, col1, cell.add(1));
		}
//		System.out.printf(" row_top=%s\n row_left=%s\n", row_top, row_left);
	}

	// 指标列的类型：（指标index，指标是否数字类型）
	private Map<Integer, Boolean> measureTypes;	
	/**
	 * 判断该列是否Number类型，如果有String则false，如果只有Number和null则true
	 * @param data
	 * @param colIndex
	 * @return 
	 */
	private boolean isNumberType(Table data, int colIndex) {
		
		for (int i = 0, n = data.rows(); i < n; i++) {
			Cell cell = data.cell(i, colIndex);
			if (cell==null) continue;
			if (data.cell(i, colIndex).isString()) return false;
		}
		return true;
	}
	
	/**
	 * 查找row在table中的位置
	 * @param row
	 * @param table
	 * @return
	 */
	private int indexOf(Row row, Table table) {
		for (int i = 0, n = table.rows(); i < n; i++) {
			if (row.equals(table.row(i))) return i; 
		}		
		return 0;
	}

	private boolean valid(Table data) {
		
		// 必须至少1列1行（上、左维度和1个指标）且有数据
		if (data==null || data.cols()<1 || data.rows()<1) return false;
		
		return true;
	}
	
	/**
	 * 检测处理设置的 top / left / measure codes，重设layout
	 * 
	 * @param data 提供meta
	 * @pre layout中leftCodes, topCodes, measureCodes 不为null
	 * @post 改写 layout 中的值。
	 */
	private void refineLayout(final Table data) {
		
		layout.topIndex = new ArrayList<Integer>();
		layout.leftIndex = new ArrayList<Integer>();
		layout.measureIndex = new ArrayList<Integer>();
		// - top
		refine(layout.topCodes, layout.topIndex);
		// - left
		refine(layout.leftCodes, layout.leftIndex);
		// - measure
		refine(layout.measureCodes, layout.measureIndex);		
		// --- 指标meta
		initMeasureMetas(data);
		// --- "指标"占位位置
		initMeasurePosition();
	}
	
	/**
	 * 去除非法设置的code。
	 * @param codes 已经去重的codes
	 * @param flag 借用flag表示top/left/measure
	 * @return
	 */
	private void refine(List<String> codes, List<Integer> index) {
		
		Row codes0 = super.codes();
		for (String s : new ArrayList<String>(codes)) {
			int i = Rowx.index(new Cell(s), codes0);
			if (i==-1) {
				codes.remove(s);
			} else {
				index.add(i);				
			}
		}
	}
	
	/**
	 * 从data的head中找到meta，并设置layout.measureMetas
	 * @param data
	 */
	private void initMeasureMetas(final Table data) {
		
		Row codes0 = super.codes();
		Row measureMetas = new Row();
		for (String s : layout.measureCodes) {
			int index = Rowx.index(new Cell(s), codes0);
			measureMetas.add(data.head(index));
		}
		layout.measureMetas = measureMetas;
	}
	
	private void initMeasurePosition() {
		
		// 没有指标或仅1个指标不占位
		if (layout.measureCodes.size()<=1) {
			layout.measurePosition = Position.NONE;
		}
	}
	
	/**
	 * top * measureCodes 或者 left * measureCodes
	 * @param dims 维度Table，每列是一个维度
	 * @return 增加了指标列，并扩展了row数目（*= measureCodes.length）的Table
	 * @pre 肯定有至少1个指标项
	 */
	private Table multiplyMeasure(Table dims) {
		
		Table rt = new Table();
		
		int size = layout.measureCodes.size();
		
		// 没有维度的情况
		if (dims.cols()==0) {
			rt.head(Row.createByTypes("指标"));
			for (int i = 0; i < size; i++) {
				Row row = Row.createByTypes(layout.measureMetas.get(i));
				rt.add(row);
			}
			return rt;
		}
		
		// 有维度的情况
		Row head = dims.head().add(new Cell("指标"));
		rt.head(head);

		for (int i = 0, n = dims.rows(); i < n; i++) {
			for (int j = 0; j < size; j++) {
				Row row = new Row();
				for (int col = 0, cols = dims.cols(); col < cols; col++) {
					row.add(dims.cell(i, col));
				}
				row.add(layout.measureMetas.get(j));
				rt.add(row);
			}
		}
//		System.out.println("multiplyMeasure=" + rt);
		return rt;
	}
	
	/**
	 * 通过列代码选择列
	 * @param data 原始数据表
	 * @param codes 列代码列表
	 * @param ref 被改变的 topCodes 或者 leftCodes 引用
	 * @return 列数组
	 */
	private Col[] selectCols(final Table data, final List<String> codes) {
		
		List<Col> cols = new ArrayList<Col>();
		for (String code : codes) {
			int codeIndex = Rowx.index(new Cell(code), super.codes());
			if (codeIndex==-1) continue;
			cols.add(data.col(codeIndex));
		}
		return (Col[]) cols.toArray(new Col[0]);
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		String dbname = "h2";
		DataGen.create(dbname);
		DbServer dbs = DbServer.create(dbname);
//		String sql = "select name, month, sum(hq) hq, sum(dq) dq from deposit" +
//				"	group by name, month" +
//				"	order by name, month";
		String sql = "select * from deposit";
		Table data = dbs.query(sql);
		
		CubeTable table = CubeTable.create()
//			.top("name")
//			.left("month")
//			.measure("")
			.layout("name, [dq] \\ orgid, orgid1, Month")
//			.layout("month")
//			.sum(true)
			.data(data)
			.debug(true)
			.meta(Arrays.asList("月份", "机构", "姓名", "活期额", "定期额"))
		;
		System.out.printf("data=%s\nresult=%s", data, table.result());

		System.out.println(table.data(data).html());
	}
}
