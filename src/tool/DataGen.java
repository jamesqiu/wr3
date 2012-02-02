package tool;

import java.util.Arrays;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Stringx;
import static wr3.util.Numberx.random;
import static wr3.util.Numberx.randoms;

/**
 * <pre>
 * 在h2/hsqldb内存数据库中自动产生如下表及数据：
 * deposit:(存款表)
 *   month(月份),  orgid(机构代码),  name(客户名), hq(活期存款额), dq(定期存款额)
 *   ----------------------------------------------------------------------------
 *   1月,            001,              丁一,         30,        2000
 *   1月,            002,              丁一,         500,          1000
 *   2月,            001,              王二,         
 *   
 * loan:(贷款表)
 *   month(月份),  orgid(机构代码),  name(客户名),   amount(贷款额)
 *   --------------------------------------------------------
 *   1月，           002，             丁一,         30000
 *   
 * 
 * dd_org:(机构字典表)
 *   code,    name
 *   -------------
 *   001,     市分行
 *   002,     西城支行
 *   003,     东城支行
 *   004,     海淀支行
 * 
 * usage:
 *   dg = Table.create(); // 创建在内存对象中
 *   dg = Table.create("h2"); // 创建在内存数据库中
 *   dg.tables(); // {"deposit", "loan", dd_org"}
 *   dg.deposit(); // deposit table
 *   dg.loan(); // loan table
 * </pre>
 * 
 * @author jamesqiu 2009-8-29
 *
 */
public class DataGen {

	Table deposit = new Table();
	Table loan = new Table();
	Table dd_org = new Table();
	Table sys_infolder = new Table();
	
	final static String[] months = new String[] {
		"1月", "2月", "3月"	
	};
	
	final static String[] orgs = new String[] {
		"001", "002", "003", "004"	
	};
	final static String[] orgNames = new String[] {
		"市分行", "西城支行", "东城支行", "海淀支行"
	};
	
	final static String[] names = new String[] {
			"丁一", "孙二", "张三", "李四", "王五", "赵六"	
	};
	
	DbServer dbs;
	
	private DataGen() { }
	
	/**
	 * 生成数据，放在3个 wr.Table 中。
	 * @return
	 */
	public static DataGen create() {
		
		DataGen o = new DataGen();
		o.depositTable();
		o.loanTable();
		o.dd_orgTable();
		o.sys_infolderTable();
		
		return o;
	}
	
	/**
	 * 生成数据，放在3个 wr.Table 中及内存数据库中。
	 * @param dbname
	 * @return
	 */
	public static DataGen create(String dbname) {
		
		DataGen o = create();
		
		o.toDb(dbname);
		
		return o;
	}
	
	public Table deposit() {
		return deposit;
	}
	
	public Table loan() {
		return loan;
	}
	
	public Table dd_org() {
		return dd_org;
	}
	
	private void toDb(String dbname) {
		
		dbs = DbServer.create(dbname);
		// 只往数据库中插入一次（第一次生成的数据）
		if (dbs.tables().contains("SYS_INFOLDER")) return;
			
		String createDeposit = "create table deposit (\n" + 
				" month char(2),\n" + 
				" orgid char(3),\n" + 
				" name varchar(20),\n" + 
				" hq int,\n" + 
				" dq int);";
		String createLoan = "create table loan (\n" + 
				" month char(2),\n" + 
				" orgid char(3),\n" + 
				" name varchar(20),\n" + 
				" amount int);";
		String createDd_org = "create table dd_org (\n" + 
				" code varchar(20),\n" + 
				" name varchar(20));";
		String createSys_infolder = "CREATE TABLE sys_infolder (\n" + 
				" typ char (1)  NOT NULL ,\n" + 
				" cod varchar (100) NOT NULL ,\n" + 
				" nam varchar (200) NULL ,\n" + 
				" etc varchar (300) NULL)";
		
		dbs.update(createDeposit);
		dbs.update(createLoan);
		dbs.update(createDd_org);
		dbs.update(createSys_infolder);
		
		insertData("deposit", deposit);
		insertData("loan", loan);
		insertData("dd_org", dd_org);
		insertData("sys_infolder", sys_infolder);
	}
	
	private void insertData(String tableName, Table table) {
		
		for (int i = 0, n = table.rows(); i < n; i++) {
			int m = table.cols();
			String[] cols = new String[m];
			for (int j = 0; j < m; j++) {
				cols[j] = table.cell(i, j).value();
			}
			String sql = Stringx.printf("insert into %s values(%s);",
					tableName,
					Stringx.join(cols, ",", "'", "'"));
			dbs.update(sql);
		}
	}
	
	/**
	 * 产生存款表
	 * @return
	 */
	private void depositTable() {
	
		Row head = Row.createByStrings(new String[]{
				"month", "orgid", "name", "hq", "dq"});
		deposit.head(head);
		
		for (int i = 0, n = months.length; i < n; i++) {
			for (int j = 0, m = orgs.length; j < m; j++) {
				for (int k = 0, p = names.length; k < p; k++) {
					int[] rands1 = randoms(p, 1); // 随机选人
					if (rands1[k]==1) {
						Row row = new Row();
						row.add(new Cell(months[i]));
						row.add(new Cell(orgs[j]));
						row.add(new Cell(names[k]));
						row.add(new Cell(random(10000)));
						row.add(new Cell(random(10)*1000));
						deposit.add(row);
//						System.out.printf("%s, %s, %s, %d, %d\n", 
//							months[i], orgs[j], names[k], random(10000), random(10)*1000);
					}
				}
			}
		}
	}
	
	/**
	 * 产生贷款表
	 * @return
	 */
	private void loanTable() {
		
		Row head = Row.createByStrings(new String[]{
				"month", "orgid", "name", "amount"});
		loan.head(head);
		
		for (int i = 0, n = months.length; i < n; i++) {
			for (int j = 0, m = orgs.length; j < m; j++) {
				for (int k = 0, p = names.length; k < p; k++) {
					int[] rands1 = randoms(p, 1); // 随机选人
					if (rands1[k]==1) {
						Row row = new Row();
						row.add(new Cell(months[i]));
						row.add(new Cell(orgs[j]));
						row.add(new Cell(names[k]));
						row.add(new Cell(random(10)*10000));
						loan.add(row);
					}
				}
			}
		}
	}
	
	private void dd_orgTable() {
		
		Row head = Row.createByStrings(new String[]{
				"code", "name"});
		dd_org.head(head);
		
		for (int i = 0, n = orgs.length; i < n; i++) {
			Row row = new Row();
			row.add(new Cell(orgs[i]));
			row.add(new Cell(orgNames[i]));	
			dd_org.add(row);
		}
	}
	
	private void sys_infolderTable() {
		
		Row head = Row.createByStrings(new String[]{
				"typ", "cod", "nam", "etc"});
		sys_infolder.head(head);

		sys_infolder.add(Row.createByStrings(new String[] {
				"2", "deposit", "存款表", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"2", "loan", "贷款表", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"2", "dd_org", "机构字典表", ""}));

		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "deposit.month", "月份", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "deposit.orgid", "机构代码", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "deposit.name", "客户名", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "deposit.hq", "活期存款额", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "deposit.dq", "定期存款额", ""}));
		
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "loan.month", "月份", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "loan.orgid", "机构代码", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "loan.name", "客户名", ""}));
		sys_infolder.add(Row.createByStrings(new String[] {
				"3", "loan.amount", "贷款额", ""}));
				
	}
	
	/**
	 * 关闭内存数据库连接
	 */
	public void close() {
		dbs.close();
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
	
		String dbname = "h2";
		DataGen o = DataGen.create(dbname);
		System.out.println(o.deposit);
//		System.out.println(o.loan);
//		System.out.println(o.dd_org);
//		
		System.out.println(Arrays.asList("a", "c", "b"));
		System.out.println(DbServer.create("h2").query(
				"select substr(cod, 4+2), nam " +
				" from sys_infolder " +
				" where cod like 'loan.%' "));
	}
	
}
