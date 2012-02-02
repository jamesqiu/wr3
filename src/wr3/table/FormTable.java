package wr3.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wr3.Cell;
import wr3.Table;

/**
 * <pre>
 * 从数据库表或者SQL语句直接取出的单条记录进行表单显示。
 * usage:
 *   FormTable.create().data(table).html();
 *   
 *   FormTable.create()
 *     .data(table).meta(mapColumn).dd("orgid", dict1).dd(-1, dict1)
 *     .view("table.ftl").html();
 * </pre>
 * @author jamesqiu 2009-8-27
 */
public class FormTable extends BaseTable {

	private FormTable() {		
	}
	
	public static FormTable create() {
		
		return new FormTable();
	}
	
	@Override
	public FormTable id(String id) {
		
		super.id(id);
		return this;
	}
	
	@Override
	public FormTable data(Table table) {
		
		super.data(table);
		return this;
	}
	
	@Override
	public FormTable meta(Map<?, ?> metaMap) {
		
		super.meta(metaMap);
		return this;
	}
	
	@Override
	public FormTable meta(List<?> metaList) {
		
		super.meta(metaList);
		return this;
	}
	
	@Override
	public FormTable dd(String headCode, Map<?, ?> dd) {
		
		super.dd(headCode, dd);
		return this;
	}
	
	@Override
	public FormTable dd(int headIndex, Map<?, ?> dd) {
		
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public FormTable view(String templateFilename) {
		
		super.view(templateFilename);
		return this;
	}
	
	@Override
	public FormTable filter(CellFilter filter) {
		
		super.filter(filter);
		return this;
	}
	
	// ---------------------- main() ----------------------
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		
		CellFilter filter = new CellFilter() {

			public Cell process(int col, Cell cell) {
				if (col>3)
					return Cell.create("头："+cell.value().toUpperCase());
				return null;
			}

			public Cell process(int row, int col, Cell cell) {
				return Cell.create(cell.intValue()*100);
			}
		};
		Map<?,?> m1 = new HashMap<String, Object>() {{
			put("c0", "org");
			put("c1", "name");
			put("c2", "余额");
			put("c6", 12345);
		}};
		List<String> m2 = Arrays.asList("贷款","活期","定期");
		FormTable ft = FormTable.create()
			.id("form")
			.data(new Table(10, 1))
			.meta(m2)
			.meta(m1)
			.filter(filter)
//			.view("f:/dev3/classes/wr3/table/FormTable.ftl")
			;
		System.out.println(ft.html());
		System.out.println(ft);
	}
	
}
