package wr3.table;

import java.util.List;
import java.util.Map;


import tool.DataGen;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Tablex;

/**
 * 旋转表，对原始数据表进行旋转
 * @author IBM
 *
 */
public class RotateTable extends BaseTable {

	private RotateTable() { }
	
	public static RotateTable create() {
		
		return new RotateTable();
	}
	
	@Override
	public RotateTable id(String id) {
		super.id(id);
		return this;
	}

	@Override
	public RotateTable data(Table table) {
		super.data(table);
		return this;
	}

	@Override
	public RotateTable dd(int headIndex, Map<?, ?> dd) {
		super.dd(headIndex, dd);
		return this;
	}

	@Override
	public RotateTable dd(String headCode, Map<?, ?> dd) {
		super.dd(headCode, dd);
		return this;
	}

	@Override
	public RotateTable meta(List<?> metaList) {
		super.meta(metaList);
		return this;
	}

	@Override
	public RotateTable meta(Map<?, ?> metaMap) {
		super.meta(metaMap);
		return this;
	}

	@Override
	public RotateTable view(String templateFilename) {
		super.view(templateFilename);
		return this;
	}
	
	@Override
	public RotateTable filter(CellFilter filter) {
		super.filter(filter);
		return this;
	}

	@Override
	Table processData(Table table) {
		
		return Tablex.rotate(table);
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		DataGen.create("h2");
		DbServer dbs = DbServer.create("h2");
		
		Table data = dbs.query("select * from loan where orgid='001' ");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));
		
		RotateTable table = RotateTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			;
//		System.out.println(data);
//		System.out.println(table.result());
//		System.out.println(table.toString());
		System.out.println(table.html());
	}
}
