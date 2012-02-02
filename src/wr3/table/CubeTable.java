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
 * ע�⣺
 * 1����ͬά�Ȼ���ָ���code�������һ������ָͬ���meta�������һ����
 * @author jamesqiu 2009-11-18
 */
public class CubeTable extends BaseTable {

	public static CubeTable create() {
		
		return new CubeTable();
	}
	
	/**
	 * ��web��ʹ�õ�ʱ���request params���������
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
	 * ά��ָ�겼��
	 */
	private enum Position {TOP, LEFT, NONE}; // ��ʶָ��������1��ʱ������ϻ�����
	
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
	 * �����ϱ�ά��
	 * @param code ����ά�ȵ��ֶδ���
	 * @return 
	 */
	public CubeTable top(String... codes) {
	
		layout.topCodes = Stringx.ss2set(codes);
		super.needProcess();
		return this;
	}
	
	/**
	 * �������ά��
	 * @param code ����ά�ȵ��ֶδ���
	 * @return
	 */
	public CubeTable left(String... codes) {
		
		layout.leftCodes = Stringx.ss2set(codes);
		super.needProcess();
		return this;
	}
		
	/**
	 * ���ü���ָ��
	 * @param onTop ָ�����Ƿ�������ʾ��ȱʡΪtrue��
	 * @param codes ָ���еĴ����б��磺"month", "name"��
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
	 * ʹ�ñ��ʽ��������ά�ȡ�ָ�겼�֣���: 
	 *   "month \\ name, [hq, dq]",
	 *   "month, name \\ [hq, dq]",
	 *   "month, name, [hq, dq] \\",
	 *   "\\ month, [qh]"
	 *   
	 * @param express ά�ȡ�ָ�겼�ֵı��ʽ
	 * @return
	 * @pre 
	 * @post ���� topCodes, leftCodes, measureCodes
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
			// ���ɿյĿ��Frame
			genFrame(data);
			// ����Frame����data
			fillFrame(data);	
//			// �ϼ�
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
	 * data --����--> frame:
	 * ������������������������������������������
	 *��          ��
	 *��  corner  ������frame_top����������������
	 *��          ��    (ָ����>1)
	 *��������������������������������������������
	 *��   ��     ��
	 *��frame_left��    (ָ��������)
	 *��   ��     ��
	 *��   ��(ָ  ��
	 *��   �� ��  ��
	 *��   �� ��  ��
	 *��   �� >1) ��
	 *��   ��     ��
	 */
	private Table frame;
	
	private Table frame_corner;
	private Table frame_top; // ��Ҫ��ת������
	private Table frame_left;	
	
	private void genFrame(final Table data) {
	
		// ��⴦�����õ� top / left / measure / ...������layout
		refineLayout(data);
		
		// �õ���ά����أ��磺[1�£�2�£�3��]
		frame_top = Rowx.uniq(selectCols(data, layout.topCodes));	
		// �õ���ά����أ��磺[����1������2������3]
		frame_left = Rowx.uniq(selectCols(data, layout.leftCodes));
		
		// ��ָ������
		if (layout.measurePosition==Position.TOP) {
			frame_top = multiplyMeasure(frame_top);
		} else if (layout.measurePosition==Position.LEFT) {
			frame_left = multiplyMeasure(frame_left);
		}
		
		// �����Ͻǻ�б�ߵĲ��ֳ�Ϊcorner
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
		
		// ���й���frame
		int top_cols = frame_top.rows(); // ��ά�ȿ��
		// ��ָ��, û����ά�ȵ����: ������1������ָ��ϼ�ֵ
		if (layout.measureCodes.size()>0 && top_cols==0) {
			top_cols = 1; 
		}
		
		// --- frame.head
		int frame_cols = corner_cols + top_cols;
		frame = new Table().head(Row.createHead(frame_cols));
		
		// --- ��frame��rows
		for (int i = 0, n = frame_top.cols(); i < n; i++) {
			Row row = frame_corner.row(i);
			row.join(frame_top.col(i).asRow());
			frame.add(row);
		}
		
		// --- ��frame��rows
		for (int i = 0, n = frame_left.rows(); i < n; i++) {
			Row row = frame_left.row(i);
			frame.add(row);
		}
		// ------ ���û����ά�ȣ�frame_left=[]������ָ�꣬ռ1��
		if (frame_left.rows()==0 && layout.measureCodes.size()>0) {
			frame.add(new Row());
		}
		
		if (debug) {
			System.out.printf(" -genFrame():\n -layout=%s -corner=%s -top=%s -left=%s -frame=%s\n", 
				layout, frame_corner, frame_top, frame_left,frame);
		}
		
		// ����frame��cosspan,rowspan
		spans = new CubeTableSpans()
			.corner(frame_corner)
			.top(frame_top)
			.left(frame_left);
	}
	
	/**
	 * ��data������ָ�����е������ۼ���䵽frame��
	 * @param data
	 */
	private void fillFrame(final Table data) {
		
		measureTypes = new LinkedHashMap<Integer, Boolean>();
		
		for (int i : layout.measureIndex) {
			measureTypes.put(i, isNumberType(data, i));
			// ����1��ָ����, i: ָ���е���index
			for (int j = 0, m = data.rows(); j < m; j++) {
				// ����ָ�����е�1��cell
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
	 * ��ԭʼ������1��ָ���С��б�ŵ�cell��䵽frame��
	 * @param data ԭʼ����Table
	 * @param row0 ԭʼ���ݱ�Cell�к�
	 * @param col0 ԭʼ���ݱ�Cell�к�
	 */
	private void fill(Table data, int row0, int col0) {
		
		// �ҵ�top����col1���ӱ�row�ҵ�topά��code��ָ��meta
		Row row_top = new Row();
		for (int i : layout.topIndex) {
			row_top.add(data.cell(row0, i));
		}
		if (layout.measurePosition==Position.TOP) {
			row_top.add(data.head(col0)); 
		}
		int col1 = indexOf(row_top, frame_top) + frame_left.cols();
		
		// �ҵ�left����row1���ӱ�row�ҵ�leftά��code��ָ��meta
		Row row_left = new Row();
		for (int i : layout.leftIndex) {
			row_left.add(data.cell(row0, i));
		}
		if (layout.measurePosition==Position.LEFT) {
			row_left.add(data.head(col0));
		}
		int row1 = indexOf(row_left, frame_left) + frame_top.cols();
//		System.out.printf("(row1,col1)=(%d,%d)\n", row1, col1);
		
		// ������ݵ�frame��[col1, row1]����
		//������������������ۼӣ����ַ������count��null���Բ��ƣ�
		Cell cell = frame.cell(row1, col1);
		if (cell==null) cell = new Cell(0);

		if (measureTypes.get(col0)) {
			// �������ͣ���ȫnull��
			frame.cell(row1, col1, cell.add(data.cell(row0, col0)));
		} else {
			// ����������
			frame.cell(row1, col1, cell.add(1));
		}
//		System.out.printf(" row_top=%s\n row_left=%s\n", row_top, row_left);
	}

	// ָ���е����ͣ���ָ��index��ָ���Ƿ��������ͣ�
	private Map<Integer, Boolean> measureTypes;	
	/**
	 * �жϸ����Ƿ�Number���ͣ������String��false�����ֻ��Number��null��true
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
	 * ����row��table�е�λ��
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
		
		// ��������1��1�У��ϡ���ά�Ⱥ�1��ָ�꣩��������
		if (data==null || data.cols()<1 || data.rows()<1) return false;
		
		return true;
	}
	
	/**
	 * ��⴦�����õ� top / left / measure codes������layout
	 * 
	 * @param data �ṩmeta
	 * @pre layout��leftCodes, topCodes, measureCodes ��Ϊnull
	 * @post ��д layout �е�ֵ��
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
		// --- ָ��meta
		initMeasureMetas(data);
		// --- "ָ��"ռλλ��
		initMeasurePosition();
	}
	
	/**
	 * ȥ���Ƿ����õ�code��
	 * @param codes �Ѿ�ȥ�ص�codes
	 * @param flag ����flag��ʾtop/left/measure
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
	 * ��data��head���ҵ�meta��������layout.measureMetas
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
		
		// û��ָ����1��ָ�겻ռλ
		if (layout.measureCodes.size()<=1) {
			layout.measurePosition = Position.NONE;
		}
	}
	
	/**
	 * top * measureCodes ���� left * measureCodes
	 * @param dims ά��Table��ÿ����һ��ά��
	 * @return ������ָ���У�����չ��row��Ŀ��*= measureCodes.length����Table
	 * @pre �϶�������1��ָ����
	 */
	private Table multiplyMeasure(Table dims) {
		
		Table rt = new Table();
		
		int size = layout.measureCodes.size();
		
		// û��ά�ȵ����
		if (dims.cols()==0) {
			rt.head(Row.createByTypes("ָ��"));
			for (int i = 0; i < size; i++) {
				Row row = Row.createByTypes(layout.measureMetas.get(i));
				rt.add(row);
			}
			return rt;
		}
		
		// ��ά�ȵ����
		Row head = dims.head().add(new Cell("ָ��"));
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
	 * ͨ���д���ѡ����
	 * @param data ԭʼ���ݱ�
	 * @param codes �д����б�
	 * @param ref ���ı�� topCodes ���� leftCodes ����
	 * @return ������
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
			.meta(Arrays.asList("�·�", "����", "����", "���ڶ�", "���ڶ�"))
		;
		System.out.printf("data=%s\nresult=%s", data, table.result());

		System.out.println(table.data(data).html());
	}
}
