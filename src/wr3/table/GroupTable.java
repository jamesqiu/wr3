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
 * ���鱨����ĳһ�ֶν��з���С��
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
	 * ��ָ���н��з���. �� {@link #group(int, GroupFilter)} ����
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
	 * ��ָ���н��пͻ������飬�磺����Σ�����λ��֡�
	 * �� {@link #group(int)} ���⡣
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
	 * �õ�Tableָ���еķ���ɨ������
	 * @param data
	 * @param colIndex
	 * @return �磺{a:[1,3,5], b:[0,2]}
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
	 * �����е�λ�ã���[1, 3, 5]������template�н��б�ʶ
	 */
	Row groupRows;
	
	@Override
	Table processData(Table data0) {

		if (data0.cols()==0) return data0;
		
		if (groupFilter!=null) {
			Tablex.group(data0, colIndex, groupFilter);
			this.colIndex = 0; // �����б�Ϊ����
		}
		
		Table data1 = new Table().head(data0.head());
		
		// ����<�ܼ���>
		Row sum = Tablex.sum(data0);
		sum.cell(0, new Cell("�ܼ�"));
		data1.add(sum);
		groupRows = new Row();
		groupRows.add(new Cell(0));

		// ����<����С����>
		Map<String, List<Integer>> map = groupRowsIndex(data0, colIndex);
		for ( Entry<String, List<Integer>> e : map.entrySet()) {
			String s = e.getKey();
			List<Integer> rows = e.getValue();
			sum = Tablex.sum(data0, rows);
			// ��ʾ<����С����>
			sum.cell(colIndex, Cell.create(s));
			data1.add(sum);
			groupRows.add(new Cell(data1.rows()-1));
			for (Integer rowIndex : rows) {
				Row row = data0.row(rowIndex);
				// �����ڸ�����Ϊ""
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
					return (cell.value()=="a") ? new Cell("A��") : new Cell("B��");
				}
			})
		;
		
		System.out.println(data);
		System.out.println(t.result());
	}
	
}
