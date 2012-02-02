package tool;

import java.util.List;
import java.util.Map;

import wr3.BaseConfig;
import wr3.db.DbConfig;
import wr3.util.Filex;

import static wr3.util.Stringx.rightback;

import static wr3.util.Regex.find;

/**
 * <pre>
 * 查看DataSource.groovy的内容. 
 * 无参数则列出所有dbnames, 有参数则列出指定的dbname的配置信息.
 * </pre>
 * @author jamesqiu 2009-4-12
 *
 */
public class DbConfigTool {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("");
		if (args.length==0) {
			dbnames();
		} else {
			dbname(args[0]);
		}

	}
	
	static DbConfig config = new DbConfig();
	
	static void dbnames() {
		List<String> list = config.dbnames();
		String defaultDbname = defaultDbname();
		for (String dbname : list) {
			if (dbname.equals("dataSource")) continue;
			Map<String, String> map = config.get(dbname);
			if (dbname.equals(defaultDbname)) {
				System.out.println(dbname + " (* DEFAULT)");
			} else if ("jndi".equalsIgnoreCase(map.get("driver"))) {
				System.out.println(dbname + " (JNDI)");
			} else {
				System.out.println(dbname);
			}
		}
		defaultDbname();
	}

	static String defaultDbname() {
		String wr3home = BaseConfig.get("wr3.home");
		String filename2 = wr3home + "/conf/DataSource.groovy";
		String text = Filex.read(filename2);
		String find = find(text, "dataSource[ ]+=[ ]+[\\w]+");
		if (find!=null) {
			return rightback(find, "=").trim();
		} else {
			return null;
		}
	}
	
	static void dbname(String name) {
		Map<String, String> map = config.get(name);
		if ("jndi".equalsIgnoreCase(map.get("driver"))) {
			System.out.println(map.get("name"));
		} else {
			System.out.println(map.get("driver"));
			System.out.println(map.get("url"));
			System.out.println(map.get("username") + " : " + map.get("password"));
		}
	}

}
