package wr3.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.Cell;
import wr3.Col;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Rowx;

public class FrameTable extends BaseTable {

	public static FrameTable create() {
		
		return new FrameTable();
	}
		
	@Override
	public FrameTable id(String id) {
		super.id(id);
		return this;
	}
	
	@Override
	public FrameTable view(String templateFilename) {
		super.view(templateFilename);
		return this;
	}

	@Override
	public FrameTable filter(CellFilter filter) {
		super.filter(filter);
		return this;
	}

	@Override
	public FrameTable dd(int headIndex, Map<?, ?> dd) {
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public FrameTable dd(String headCode, Map<?, ?> dd) {
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public FrameTable meta(List<?> metaList) {
		super.meta(metaList);
		return this;
	}

	@Override
	public FrameTable meta(Map<?, ?> metaMap) {
		super.meta(metaMap);
		return this;
	}
	
	// ----------------- FrameTable's methods -----------------//
	/**
	 * �Ͽ��codes
	 */
	private Row topCodes;
	/**
	 * ����codes
	 */
	private Col leftCodes;
	/**
	 * �ṩ�Ͽ��codes��Table.head()��������codes��Table.col(0)����Table
	 */
	private Table topLeftCodes;
	/**
	 * ����ܵ�����
	 */
	private List<Table> datas = new ArrayList<Table>();
	
	/**
	 * ����Frame���Ͽ��codes��
	 * ���ȼ����� {@link #data(Table)}
	 * @param row ��Ϊnull����ʾ������topCodes����row�е�cell����Ϊnull��, 
	 * 			���� {@link #data(Table)} ���趨������ȡ����datas��head������
	 */
	public FrameTable top(Row row) {

		this.topCodes = row;
		return this;
	}
	
	/**
	 * ����Frame������codes��
	 * ���ȼ����� {@link #data(Table)}
	 * @param col ��Ϊnull����ʾ������leftCodes, ���� {@link #data(Table)} ���趨��
	 * 			����������datas�ж�ӦtopCodes.get(0)���С�
	 * 		
	 * @return
	 */
	public FrameTable left(Col col) {

		this.leftCodes = col;
		return this;
	}
	
	/**
	 * ���� {@link BaseTable#data(Table)} ����Ϊ; 
	 * ʹ��table��head�͵�1��Ϊ���;
	 * ���ȼ����� {@link #top(Row)}, {@link #left(Col)}
	 * @param table �ṩFrame�� topCodes �� leftCodes
	 */
	@Override
	public FrameTable data(Table table) {
		
		if (table!=null) this.topLeftCodes = table;
		return this;
	}
	
	/**
	 * ����һ�����frame�����ݣ���ͨ����ε������Ӷ��
	 * @param data
	 * @return
	 */
	public FrameTable put(Table data) {
		
		if (data!=null) datas.add(data);
		return this;
	}
	
	/**
	 * ���ɿ��codes
	 */
	@Override
	Row initCodes() {
		
		return topCodes;
	};
	
	private Table frame = new Table();
	/**
	 * ���ɿ��data.
	 * @pre: ������ top(), left(), data(), ��ε����� put()
	 * @post: �Ѷ���put()��������������frame����
	 */
	@Override
	Table initData() {

		// ���ɿյĿ��Frame
		genFrame();
		// ����Frame����data
		fillFrame();
		// ��frame��Ϊԭʼdata Table
		super.data(frame);
		
		return frame;
	}

	private void genFrame() {
		
		// ���� topCodes
		genTopCodes();
		// ���� leftCodes
		genLeftCodes();
		// ����null��Ԫ��
		genNullDatas();
		
	}
	
	/**
	 * ����Frame��������datas��
	 * @pre: ����Ϊ�յ� frame
	 * @post: �������ݵ� frame
	 */
	private void fillFrame() {
		
		// ���ûput���ݣ�����data()��ֵ��Ϊ����
		if (datas.size()==0 && topLeftCodes!=null) {
			datas.add(topLeftCodes);
		}
			
		for (Table data : datas) {
			fillData(data);
		}
	}

	private void genTopCodes() {
		
		// �Ѿ���������������
		if (topCodes!=null) {
			return;
		}
		
		// ʹ�� data ���� 
		if (topLeftCodes!=null) {
			topCodesFromData1();
			return;
		}
		
		// ʹ�� datas ����
		if (datas.size()>0) {
			topCodesFromDatas();
			return;
		}		
		
		topCodes = new Row();
	}
	
	/**
	 * @pre: topCodes �Ѿ�����
	 * @post: leftCodes ok.
	 */
	private void genLeftCodes() {
		
		// �Ѿ���������������
		if (leftCodes!=null) {
			return;
		}
		
		// ʹ�� data ���� 
		if (topLeftCodes!=null) {
			leftCodesFromData1();
			return;
		}
		
		// ʹ�� datas ����
		if (datas.size() > 0) {
			leftCodesFromDatas();
			return;
		}
		
		leftCodes = new Row().asCol();
	}

	/**
	 * @pre: topCodes, leftCodes ok
	 * @post: �õ�����Ϊ�յĿ��
	 */
	private void genNullDatas() {
		
		int rows = (leftCodes!=null) ? leftCodes.asRow().size() : 0;
		// head
		frame.head(topCodes);
		// data
		int cols = topCodes.size();
		for (int i = 0; i < rows; i++) {
			frame.add(Row.create(cols, null));
		}
	}

	/**
	 * ��data��head����Сд��Ϊ topCodes.
	 */
	private void topCodesFromData1() {
		
		Row head = topLeftCodes.head();
		topCodes = new Row();
		for (int i = 0, n = head.size(); i < n; i++) {
			String s = lowerString(topLeftCodes.head(i));
			topCodes.add(new Cell(s));
		}		
	}

	/**
	 * ��datas�Ķ��Table��ȡhead�����Դ�Сд���أ�����topCodes.
	 */
	private void topCodesFromDatas() {
		
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for (Table data : datas) {
			if (data.cols()==0) continue;
			for (int i = 0, n = data.cols(); i < n; i++) {
				set.add(lowerString(data.head(i)));
			}
		}
		
		Row row = new Row();
		for (String s : set) {
			row.add(new Cell(s));
		}
		topCodes = row;
	}
	
	/**
	 * ȡ data �ĵ�1����Ϊ leftCodes.
	 */
	private void leftCodesFromData1() {
		
		if (topLeftCodes.cols() == 0) {
			leftCodes = new Row().asCol();
			return;
		}
		
		leftCodes = topLeftCodes.col(0); // û��copy��ֱ������		
	}
	
	/**
	 * ��topCodes.cell(0)���бȶԣ�ȡdatas�ж�Ӧ�еĲ�����
	 * ����null��䵽�������, �õ�leftCodes��
	 */
	private void leftCodesFromDatas() {
		
		if (topCodes.size()==0) {
			leftCodes = new Row().asCol();
			return;
		}
		
		int rowsMax = 0; // datas �и� Table ���������
		String code0 = topCodes.cell(0).value(); // ��1��code
		
		// set: �� topCodes[0] ��Ӧ������data��1��cell�Ĳ���
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for (Table data : datas) {
			if (data.cols() == 0) continue;
			// ֻȡ��1�к� topCodes[0] ���Ƚ�
			if (data.head(0).value().equalsIgnoreCase(code0)) {
				for (int i = 0, n = data.rows(); i < n; i++) {
					Cell c = data.cell(i, 0);
					String s = (c==null) ? null : lowerString(c);
					set.add(s);
				}
			}
			rowsMax = Math.max(data.rows(), rowsMax);
		}
		// ��ȡ�������е�Ԫ��
		Row row = new Row();
		for (String s : set) {
			row.add(new Cell(s));
		}
		// �����İ������������Ϊ null, ��֤ leftCodes �ĳ���
		for (int i = set.size(); i < rowsMax; i++) 
			row.add(null);
		
		leftCodes = row.asCol();		
	}
	
	/**
	 * ����Frame����һ��data;
	 * ʹ���ַ����Ա�����λ
	 * called by {@link #fillFrame()}
	 */
	private void fillData(Table data) {
		
		int rows = data.rows();
		int cols = data.cols();
		
		Row leftCodesRow = leftCodes.asRow();
		for (int col = 0; col < cols; col++) {
			 // �õ�������frame�е�top����
			int colIndex = Rowx.index(data.head(col), topCodes);
			if (colIndex==-1) continue; 			
			for (int row = 0; row < rows; row++) {
				// �õ���cell��frame�е�left����
				int rowIndex = -1;
				if (Rowx.index(data.head(0), topCodes)!=0) {
					// -- �����data�ĵ�1�в���head(0), ����leftCodes����
					rowIndex = row;
				} else if (leftCodesRow.cell(row)==null) {
					// -- ���leftCodes�ĸ���Ϊnull������leftCodes����
					rowIndex = row;
				} else {
					// -- ��leftCodes���ж�λ
					rowIndex = Rowx.index(data.cell(row, 0), leftCodesRow);					
				}
				if (rowIndex==-1) continue;
				frame.cell(rowIndex, colIndex, data.cell(row, col));
			}			
		}
	}
	
	/**
	 * �õ�Cellֵ��Сд�ַ���
	 * @param c������Ϊnull
	 * @return 
	 */
	private String lowerString(Cell c) {
		return c.value().toLowerCase();
	}
		
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		String dbname = "h2";
		DataGen.create(dbname);
		DbServer dbs = DbServer.create(dbname);
		String sql$1 = "" +
				"select month, sum(amount) amount from loan " +
				"  group by month order by month";
		String sql$2 = "" +
				"select month, sum(hq) hq, sum(dq) dq from deposit " +
				"  group by month order by month";
		Table data$1 = dbs.query(sql$1);
		Table data$2 = dbs.query(sql$2);
		System.out.println(data$1);
		System.out.println(data$2);
		
//		List<Object> meta = Tablex.asList(
//				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
//		Map<Object, Object> dd = Tablex.asMap(
//				dbs.query("select * from dd_org"));
		
		FrameTable t = FrameTable.create()
//			.top(Row.createByTypes("month", "orgid", "name", "hq", "amount", "dq"))
			.left(Row.createByTypes("1��", "1.5", "2��", "3��").asCol())
			.put(data$1)
			.put(data$2)
		;
		System.out.println(t.result());	
		System.out.println(t.html());
	}

}
