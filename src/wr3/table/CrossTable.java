package wr3.table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.text.Template;
import wr3.util.Rowx;

/**
 * 注意：作为维度（top，left）的数据不能是特殊字符串'\', 否则出错，可通过sql或者函数先处理data中的维度列。
 * @author user 2012-2-17
 */
public class CrossTable extends BaseTable {

	public static CrossTable create() {
		
		return new CrossTable();
	}
		
	@Override
	public CrossTable id(String id) {
		super.id(id);
		return this;
	}
	
	@Override
	public CrossTable data(Table table) {
		super.data(table);
		return this;
	};
	
	@Override
	public CrossTable view(String templateFilename) {
		super.view(templateFilename);
		return this;
	}

	@Override
	public CrossTable filter(CellFilter filter) {
		super.filter(filter);
		return this;
	}

	@Override
	public CrossTable dd(int headIndex, Map<?, ?> dd) {
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public CrossTable dd(String headCode, Map<?, ?> dd) {
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public CrossTable meta(List<?> metaList) {
		super.meta(metaList);
		return this;
	}

	@Override
	public CrossTable meta(Map<?, ?> metaMap) {
		super.meta(metaMap);
		return this;
	}
	
	// ----------------- CrossTable's methods -----------------//
	
	/**
	 * 设置上边维度
	 * @param code 代表维度的字段代码
	 * @return 
	 */
	private String topCode;
	public CrossTable top(String code) {
	
		this.topCode = code;
		return this;
	}
	
	/**
	 * 设置左边维度
	 * @param code 代表维度的字段代码
	 * @return
	 */
	private String leftCode;
	public CrossTable left(String code) {
		
		this.leftCode = code;
		return this;
	}
	
	/**
	 * 设置计数指标
	 * @param code 代表指标的字段代码
	 * @return
	 */
	private String measureCode;
	public CrossTable measure(String code) {
		
		this.measureCode = code;
		return this;
	}
	
	/**
	 * 设置是否进行行列合计
	 * @param toSum
	 * @return
	 */
	private boolean toSum = false;
	public CrossTable sum(boolean toSum) {
		this.toSum = toSum;
		return this;
	}
	
	/**
	 * data --处理--> frame
	 */
	Table frame = new Table();
	
	@Override
	Table processData(Table data) {
		
		if (valid(data)) {
			// 生成空的框架Frame
			genFrame(data);
			// 往空Frame中填data
			fillFrame(data);	
			// 合计
			addSum();
		}
		return frame;
	}
	
	@Override
	void processTemplate(Template t) {
		t.set("topMeta", topMeta);
		t.set("leftMeta", leftMeta);
		t.set("measureMeta", measureMeta);
	}

	private boolean valid(Table data) {
		
		// 必须至少3列（上、左维度和1个指标）且有数据
		if (data==null || data.cols()<3 || data.rows()<1) return false;
		
		return true;
	}
	
	int topIndex;		// 上维度index
	int leftIndex;		// 左维度index
	int measureIndex; 	// 指标index
	
	String topMeta;		// 上维度meta
	String leftMeta;    // 左维度meta   
	String measureMeta; // 指标meta   
	
	private void genFrame(final Table data) {
				
		topIndex = Rowx.index(new Cell(topCode), super.codes());
		leftIndex = Rowx.index(new Cell(leftCode), super.codes());
		measureIndex = Rowx.index(new Cell(measureCode), super.codes());
		if ( (topIndex == -1) && (leftIndex == -1) && (measureIndex == -1) ) {
			topIndex = 0;
			leftIndex = 1;
			measureIndex = 2;
		}
		
		// 设置用于显示的meta信息
		topMeta = data.head(topIndex).value();
		leftMeta = data.head(leftIndex).value();
		measureMeta = data.head(measureIndex).value();
		
		Row topSet = Rowx.uniq(data.col(topIndex).asRow());
		Row leftSet = Rowx.uniq(data.col(leftIndex).asRow());
		int cols = topSet.size() + 1;
		int rows = leftSet.size() + 1;
		
		// head
		frame.head(Row.head(cols));		
		// row_0
		frame.add(topSet.copy().add(0, new Cell("\\")));
		// row_1,...row_i,...row_n
		for (int i = 0; i < rows-1; i++) {
			Row row = Row.create(cols, null).cell(0, leftSet.cell(i));
			frame.add(row);
		}
	}
	
	private void fillFrame(final Table data) {
		
		Row top = frame.row(0);
		Row left = frame.col(0).asRow();
		for (int i = 0, n = data.rows(); i < n; i++) {
			Cell cell = data.cell(i, measureIndex);
			int col = Rowx.index(data.cell(i, topIndex), top);
			int row = Rowx.index(data.cell(i, leftIndex), left);
			frame.cell(row, col, Cell.create(cell));
		}
	}
	
	private void addSum() {
		
		if (!toSum) return;
		
		// 置底的行统计
		Row sumBottom = new Row().add(new Cell("合计"));
		for (int i = 1; i < frame.cols(); i++) {
			Cell sum = Rowx.sum(frame.col(i).asRow());
			sumBottom.add(sum);
		}
		frame.add(sumBottom);
		
		// 右边的列统计
		Row sumRight = new Row().add(new Cell("合计"));
		for (int i = 1; i < frame.rows(); i++) {
			Cell sum = Rowx.sum(frame.row(i));
			sumRight.add(sum);
		}
		frame.addcol(sumRight.asCol());
	}
		
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		String dbname = "h2";
		DataGen.create(dbname);
		DbServer dbs = DbServer.create(dbname);
		String sql = "select name, month, sum(amount) amount from loan" +
				"	group by name, month" +
				"	order by name, month";
		Table data = dbs.query(sql);
		
		CrossTable table = CrossTable.create()
//			.top("name")
//			.left("month")
//			.measure("amount")
			.sum(true)
			.data(data)
			.meta(Arrays.asList("姓名", "月份", "贷款额"))
		;
		System.out.printf("data=%s\nresult=%s", data, table.result());
		System.out.println(table.html());
	}
}
