package wr3.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tool.DataGen;
import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.text.Template;
import wr3.util.Tablex;

/**
 * 聚合表，对行、列进行sum、avg、max、min、count计算
 * @author IBM
 *
 */
public class AggregateTable extends BaseTable {

	/**
	 * 聚合计算的类型
	 * @author jamesqiu 2009-9-25
	 */
	public enum Type { SUM, AVG, MAX, MIN };
	/**
	 * 聚合计算结果的位置
	 * @author jamesqiu 2009-9-25
	 */
	public enum Position { FIRST, LAST, FIRST_AND_LAST };
	
	/**
	 * 放置聚合类型及计算结果的所有信息，每种Type只能一个
	 */
	private Map<Type, Position> positions = new LinkedHashMap<Type, Position>();
	
	public static AggregateTable create() {
		
		return new AggregateTable();
	}
		
	@Override
	public AggregateTable id(String id) {
		
		super.id(id);
		return this;
	}
	
	@Override
	public AggregateTable data(Table table) {
		
		super.data(table);
		return this;
	}

	@Override
	public AggregateTable view(String templateFilename) {
		
		super.view(templateFilename);
		return this;
	}

	@Override
	public AggregateTable filter(CellFilter filter) {
		
		super.filter(filter);
		return this;
	}

	@Override
	public AggregateTable dd(int headIndex, Map<?, ?> dd) {
		
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public AggregateTable dd(String headCode, Map<?, ?> dd) {
		
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public AggregateTable meta(List<?> metaList) {
		
		super.meta(metaList);
		return this;
	}

	@Override
	public AggregateTable meta(Map<?, ?> metaMap) {
		
		super.meta(metaMap);
		return this;
	}

	/**
	 * 聚合行的位置，如[0, 1, 5]，用于template中进行标识
	 */
	Row aggregateRows = new Row();
	
	@Override
	Table processData(Table data) {
		
		if (positions.size()==0) {
			aggregate(Type.SUM, Position.LAST);
		}
		
		Row sum = null, avg = null, max = null, min = null;
		if (positions.containsKey(Type.SUM)) { 
			sum = Tablex.sum(data); 
			label(sum, "合计值"); 
		}
		if (positions.containsKey(Type.AVG)) {
			avg = Tablex.avg(data);
			label(avg, "平均值");
		}
		if (positions.containsKey(Type.MAX)) {
			max = Tablex.max(data);
			label(max, "最大值");
		}
		if (positions.containsKey(Type.MIN)) {
			min = Tablex.min(data);
			label(min, "最小值");
		}
		
		setAggregateRows(data.rows());
		
		for (Entry<Type,Position> e : positions.entrySet()) {
			Type type = e.getKey();
			Position position = e.getValue();			
			Row row = null;
			// 类型
			switch (type) {
			case SUM: row = sum; break;
			case AVG: row = avg; break;
			case MAX: row = max; break;
			case MIN: row = min; break;
			default:  break;
			}
			// 位置
			switch (position) {
			case FIRST: 
				data.add(0, row); 
				break;
			case LAST: 
				data.add(row); 
				break;
			case FIRST_AND_LAST: 
				data.add(0, row); 
				data.add(row); 
				break;
			default: break;
			}
		}
		
		return data;
	}
	
	private void setAggregateRows(int rows) {
		int firstRows = 0;
		int lastRows = 0;
		for (Position p : positions.values()) {
			switch (p) {
			case FIRST:
				firstRows++;
				break;
			case LAST:
				lastRows++;
				break;
			case FIRST_AND_LAST:
				firstRows++;
				lastRows++;
				break;
			default:
				break;
			}
		}
		for (int i = 0; i < firstRows; i++) {
			aggregateRows.add(new Cell(i));
		}
		for (int i = 0; i < lastRows; i++) {
			aggregateRows.add(new Cell(i+rows+firstRows));
		}
	}
	
	@Override
	void processTemplate(Template t) {
		// 聚合行的位置，如[0, 1, 5]
		t.set("aggregates", aggregateRows);
	}

	/**
	 * 给聚合行加label
	 * used by {@link #processData(Table)}
	 * @param row
	 * @param label
	 */
	private void label(Row row, String label) {
		if (row.size()>0 && row.cell(0).equals(new Cell())) 
			row.cell(0, new Cell(label));
	}
	
	public AggregateTable sum() {
		
		return sum(Position.LAST);
	}
	
	public AggregateTable avg() {
		
		return avg(Position.LAST);
	}
	
	public AggregateTable max() {
		
		return max(Position.LAST);
	}
	
	public AggregateTable min() {
		
		return min(Position.LAST);
	}
		
	public AggregateTable sum(Position position) {
		
		return aggregate(Type.SUM, position);
	}
	
	public AggregateTable avg(Position position) {
		
		return aggregate(Type.AVG, position);
	}
	
	public AggregateTable max(Position position) {
		
		return aggregate(Type.MAX, position);
	}
	
	public AggregateTable min(Position position) {
		
		return aggregate(Type.MIN, position);
	}
	
	private AggregateTable aggregate(Type type, Position position) {
		
		positions.put(type, position);
		return this;
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {

		DataGen.create("h2");
		DbServer dbs = DbServer.create("h2");
//		System.out.println(dbs.meta().columns("LOAN"));
		
		Table data = dbs.query("select * from loan where orgid='001' ");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));
		
		AggregateTable table = AggregateTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			.sum(Position.FIRST)
			.sum(Position.FIRST_AND_LAST) // 会替换上面一个
			.avg()
			.max()
			.min(Position.FIRST)
			;
		System.out.println(table.html());
	}
	
}
