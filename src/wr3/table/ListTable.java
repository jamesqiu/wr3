package wr3.table;

import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Tablex;

/**
 * <pre>
 * 从数据库表或者SQL语句直接取出的结果集进行列表显示。
 * usage:
 *   ListTable.create().data(table).meta(list).html();
 *   
 * </pre>
 * @author jamesqiu 2009-8-30
 *
 */
public class ListTable extends BaseTable {

	private ListTable() {}
	
	public static ListTable create() {
		
		return new ListTable();
	}
	
	@Override
	public ListTable id(String id) {
		
		super.id(id);
		return this;
	}

	@Override
	public ListTable data(Table table) {

		super.data(table);
		return this;
	}

	@Override
	public ListTable meta(List<?> metaList) {

		super.meta(metaList);
		return this;
	}

	@Override
	public ListTable meta(Map<?, ?> metaMap) {

		super.meta(metaMap);
		return this;
	}
	
	@Override
	public ListTable dd(String headCode, Map<?, ?> dd) {
		
		super.dd(headCode, dd);
		return this;
	}
	
	@Override
	public ListTable dd(int headIndex, Map<?, ?> dd) {
		
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public ListTable view(String templateFilename) {

		super.view(templateFilename);
		return this;
	}

	@Override
	public ListTable filter(CellFilter filter) {
		
		super.filter(filter);
		return this;
	}
		
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		DataGen.create("h2");
		Table data = DbServer.create("h2").query("select top 15 * from loan -- where 1=2");
		List<Object> meta = Tablex.asList(
				DbServer.create("h2").query(
						"select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				DbServer.create("h2").query(
						"select * from dd_org"));

		ListTable lt = ListTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			;
		System.out.println(lt.html());
		System.out.println(lt);
		
		System.out.println(ListTable.create().data(data).result());
	}

}
