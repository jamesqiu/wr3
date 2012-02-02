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
	 * 上框架codes
	 */
	private Row topCodes;
	/**
	 * 左框架codes
	 */
	private Col leftCodes;
	/**
	 * 提供上框架codes（Table.head()）和左框架codes（Table.col(0)）的Table
	 */
	private Table topLeftCodes;
	/**
	 * 填充框架的数据
	 */
	private List<Table> datas = new ArrayList<Table>();
	
	/**
	 * 设置Frame的上框架codes。
	 * 优先级高于 {@link #data(Table)}
	 * @param row 可为null，表示不限制topCodes（但row中的cell不能为null）, 
	 * 			可用 {@link #data(Table)} 来设定，或者取所有datas的head并集。
	 */
	public FrameTable top(Row row) {

		this.topCodes = row;
		return this;
	}
	
	/**
	 * 设置Frame的左框架codes。
	 * 优先级高于 {@link #data(Table)}
	 * @param col 可为null，表示不限制leftCodes, 可用 {@link #data(Table)} 来设定，
	 * 			或者用所有datas中对应topCodes.get(0)的列。
	 * 		
	 * @return
	 */
	public FrameTable left(Col col) {

		this.leftCodes = col;
		return this;
	}
	
	/**
	 * 更改 {@link BaseTable#data(Table)} 的行为; 
	 * 使用table的head和第1列为框架;
	 * 优先级低于 {@link #top(Row)}, {@link #left(Col)}
	 * @param table 提供Frame的 topCodes 和 leftCodes
	 */
	@Override
	public FrameTable data(Table table) {
		
		if (table!=null) this.topLeftCodes = table;
		return this;
	}
	
	/**
	 * 增加一个填充frame的数据，可通过多次调用增加多个
	 * @param data
	 * @return
	 */
	public FrameTable put(Table data) {
		
		if (data!=null) datas.add(data);
		return this;
	}
	
	/**
	 * 生成框架codes
	 */
	@Override
	Row initCodes() {
		
		return topCodes;
	};
	
	private Table frame = new Table();
	/**
	 * 生成框架data.
	 * @pre: 设置了 top(), left(), data(), 多次调用了 put()
	 * @post: 把多有put()进来的数据填入frame返回
	 */
	@Override
	Table initData() {

		// 生成空的框架Frame
		genFrame();
		// 往空Frame中填data
		fillFrame();
		// 把frame作为原始data Table
		super.data(frame);
		
		return frame;
	}

	private void genFrame() {
		
		// 生成 topCodes
		genTopCodes();
		// 生成 leftCodes
		genLeftCodes();
		// 生成null单元格
		genNullDatas();
		
	}
	
	/**
	 * 往空Frame中填所有datas。
	 * @pre: 数据为空的 frame
	 * @post: 填充好数据的 frame
	 */
	private void fillFrame() {
		
		// 如果没put数据，可用data()的值作为数据
		if (datas.size()==0 && topLeftCodes!=null) {
			datas.add(topLeftCodes);
		}
			
		for (Table data : datas) {
			fillData(data);
		}
	}

	private void genTopCodes() {
		
		// 已经设置了则不用生成
		if (topCodes!=null) {
			return;
		}
		
		// 使用 data 生成 
		if (topLeftCodes!=null) {
			topCodesFromData1();
			return;
		}
		
		// 使用 datas 生成
		if (datas.size()>0) {
			topCodesFromDatas();
			return;
		}		
		
		topCodes = new Row();
	}
	
	/**
	 * @pre: topCodes 已经生成
	 * @post: leftCodes ok.
	 */
	private void genLeftCodes() {
		
		// 已经设置了则不用生成
		if (leftCodes!=null) {
			return;
		}
		
		// 使用 data 生成 
		if (topLeftCodes!=null) {
			leftCodesFromData1();
			return;
		}
		
		// 使用 datas 生成
		if (datas.size() > 0) {
			leftCodesFromDatas();
			return;
		}
		
		leftCodes = new Row().asCol();
	}

	/**
	 * @pre: topCodes, leftCodes ok
	 * @post: 得到数据为空的框架
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
	 * 从data的head，变小写作为 topCodes.
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
	 * 从datas的多个Table的取head，忽略大小写排重，生成topCodes.
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
	 * 取 data 的第1列作为 leftCodes.
	 */
	private void leftCodesFromData1() {
		
		if (topLeftCodes.cols() == 0) {
			leftCodes = new Row().asCol();
			return;
		}
		
		leftCodes = topLeftCodes.col(0); // 没做copy，直接引用		
	}
	
	/**
	 * 以topCodes.cell(0)进行比对，取datas中对应列的并集，
	 * 并用null填充到最大行数, 得到leftCodes。
	 */
	private void leftCodesFromDatas() {
		
		if (topCodes.size()==0) {
			leftCodes = new Row().asCol();
			return;
		}
		
		int rowsMax = 0; // datas 中各 Table 的最大行数
		String code0 = topCodes.cell(0).value(); // 第1列code
		
		// set: 和 topCodes[0] 对应的所有data第1列cell的并集
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for (Table data : datas) {
			if (data.cols() == 0) continue;
			// 只取第1列和 topCodes[0] 来比较
			if (data.head(0).value().equalsIgnoreCase(code0)) {
				for (int i = 0, n = data.rows(); i < n; i++) {
					Cell c = data.cell(i, 0);
					String s = (c==null) ? null : lowerString(c);
					set.add(s);
				}
			}
			rowsMax = Math.max(data.rows(), rowsMax);
		}
		// 先取出并集中的元素
		Row row = new Row();
		for (String s : set) {
			row.add(new Cell(s));
		}
		// 其他的按照最大行数填为 null, 保证 leftCodes 的长度
		for (int i = set.size(); i < rowsMax; i++) 
			row.add(null);
		
		leftCodes = row.asCol();		
	}
	
	/**
	 * 往空Frame中填一个data;
	 * 使用字符串对比来定位
	 * called by {@link #fillFrame()}
	 */
	private void fillData(Table data) {
		
		int rows = data.rows();
		int cols = data.cols();
		
		Row leftCodesRow = leftCodes.asRow();
		for (int col = 0; col < cols; col++) {
			 // 得到该列在frame中的top坐标
			int colIndex = Rowx.index(data.head(col), topCodes);
			if (colIndex==-1) continue; 			
			for (int row = 0; row < rows; row++) {
				// 得到该cell在frame中的left坐标
				int rowIndex = -1;
				if (Rowx.index(data.head(0), topCodes)!=0) {
					// -- 如果该data的第1列不是head(0), 不受leftCodes限制
					rowIndex = row;
				} else if (leftCodesRow.cell(row)==null) {
					// -- 如果leftCodes的该行为null，不受leftCodes限制
					rowIndex = row;
				} else {
					// -- 用leftCodes进行定位
					rowIndex = Rowx.index(data.cell(row, 0), leftCodesRow);					
				}
				if (rowIndex==-1) continue;
				frame.cell(rowIndex, colIndex, data.cell(row, col));
			}			
		}
	}
	
	/**
	 * 得到Cell值的小写字符串
	 * @param c，不能为null
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
			.left(Row.createByTypes("1月", "1.5", "2月", "3月").asCol())
			.put(data$1)
			.put(data$2)
		;
		System.out.println(t.result());	
		System.out.println(t.html());
	}

}
