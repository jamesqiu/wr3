package wr3.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.text.Template;
import wr3.util.Tablex;

/**
 * 分组报表，按某一字段进行分组小计
 * @author jamesqiu 2009-10-2
 */
public class GroupTable extends BaseTable {

	public static GroupTable create() {
		
		return new GroupTable();
	}
		
	@Override
	public GroupTable id(String id) {
		super.id(id);
		return this;
	}
	
	@Override
	public GroupTable data(Table table) {
		super.data(table);
		return this;
	}

	@Override
	public GroupTable view(String templateFilename) {
		super.view(templateFilename);
		return this;
	}

	@Override
	public GroupTable filter(CellFilter filter) {
		super.filter(filter);
		return this;
	}

	@Override
	public GroupTable dd(int headIndex, Map<?, ?> dd) {
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public GroupTable dd(String headCode, Map<?, ?> dd) {
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public GroupTable meta(List<?> metaList) {
		super.meta(metaList);
		return this;
	}

	@Override
	public GroupTable meta(Map<?, ?> metaMap) {
		super.meta(metaMap);
		return this;
	}
	
	private int colIndex = 0;
	/**
	 * 对指定列进行分组. 和 {@link #group(int, GroupFilter)} 互斥
	 * @param colIndex
	 * @return
	 */
	public GroupTable group(int colIndex) {
		
		this.groupFilter = null;
		this.colIndex = colIndex;
		return this;
	}
	
	private GroupFilter groupFilter;
	/**
	 * 对指定列进行客户化分组，如：分组段，年龄段划分。
	 * 和 {@link #group(int)} 互斥。
	 * @param colIndex
	 * @param filter
	 * @return
	 */
	public GroupTable group(int colIndex, GroupFilter filter) {
		
		if (filter==null) return group(colIndex);
		
		this.colIndex = colIndex;
		this.groupFilter = filter;
		return this;
	}

	/**
	 * 得到Table指定列的分组扫描结果。
	 * @param data
	 * @param colIndex
	 * @return 如：{a:[1,3,5], b:[0,2]}
	 */
	private Map<String,List<Integer>> groupRowsIndex(Table data, int colIndex) {
		
		Map<String,List<Integer>> map = new LinkedHashMap<String,List<Integer>>();
		if (data==null) return map;
		
		for (int i = 0, n = data.rows(); i < n; i++) {
			String v = data.cell(i, colIndex).value();
			if (!map.containsKey(v)) {
				map.put(v, Arrays.asList(i));
			} else {
				List<Integer> rowsIndex = new ArrayList<Integer>(map.get(v));
				rowsIndex.add(i);
				map.put(v, rowsIndex);
			}
		}
		
		return map;
	}
	
	@Override
	Row processCodes(Row codes) {
		
		if (groupFilter!=null && codes!=null) {
			codes.add(0, new Cell("c_group"));
		}
		return codes;
	};
	
	/**
	 * 分组行的位置，如[1, 3, 5]，用于template中进行标识
	 */
	Row groupRows;
	
	@Override
	Table processData(Table data0) {

		if (data0.cols()==0) return data0;
		
		if (groupFilter!=null) {
			Tablex.group(data0, colIndex, groupFilter);
			this.colIndex = 0; // 分组列变为首列
		}
		
		Table data1 = new Table().head(data0.head());
		
		// 增加<总计行>
		Row sum = Tablex.sum(data0);
		sum.cell(0, new Cell("总计"));
		data1.add(sum);
		groupRows = new Row();
		groupRows.add(new Cell(0));

		// 增加<分组小计行>
		Map<String, List<Integer>> map = groupRowsIndex(data0, colIndex);
		for ( Entry<String, List<Integer>> e : map.entrySet()) {
			String s = e.getKey();
			List<Integer> rows = e.getValue();
			sum = Tablex.sum(data0, rows);
			// 标示<分组小计行>
			sum.cell(colIndex, Cell.create(s));
			data1.add(sum);
			groupRows.add(new Cell(data1.rows()-1));
			for (Integer rowIndex : rows) {
				Row row = data0.row(rowIndex);
				// 分组内各行设为""
				row.cell(colIndex, new Cell());
				data1.add(row);
			}
		}
		
		return data1;
	}

	@Override
	void processTemplate(Template t) {
		
		t.set("groups", groupRows);
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		Table data = new Table().head(Row.createHead(4))
			.add(Row.createByTypes("a", 40, "hello", 20))
			.add(Row.createByTypes("b", 2, null, 3))
			.add(Row.createByTypes("b", 20, 3.1415, 30))
			.add(Row.createByTypes("b", 60, 10000L, 10))
			;
		
		GroupTable t = GroupTable.create()
			.data(data)
			.group(0, new GroupFilter() {
				public Cell process(Cell cell) {
					return (cell.value()=="a") ? new Cell("A组") : new Cell("B组");
				}
			})
		;
		
		System.out.println(data);
		System.out.println(t.result());
	}
	
}
