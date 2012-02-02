package app;

import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.split;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.GroovyConfig;
import wr3.db.DbServer;
import wr3.table.CubeTable;
import wr3.text.Template;
import wr3.web.Appx;
import wr3.web.Params;
import wr3.web.Render;
import wr3.web.Session;

/**
 * 从Cube.groovy读配置，从request param读layout express，生成CubeTable
 * @author jamesqiu 2009-11-30
 */
public class Cube {

	public Params params;	// 变量名必须为"params"
	public Session session;	// 变量名必须为"session"
	
	// 生成内存随机数据表
	private final static String dbname = "h2";
	private DbServer dbs = DbServer.create(dbname);
	static {
		DataGen.create(dbname);
	}
	
	/**
	 * 输出CubeTable结果
	 * @return
	 */
	public Render index() {

		String layout = params.get("layout");
		if (nullity(layout)) layout = config().getString("layout");
		
		CubeTable table = CubeTable.create(params)
			.layout(layout)
			.data(data())
			.debug(true)
			.meta(metas())
		;

		return Render.body(table.html());
	}

	// 执行config文件中的sql获取原始数据。
	private wr3.Table data() {
		
		DataGen.create(dbname);
		String sql = config().getString("sql");
		if (nullity(sql)) sql = "select * from deposit";
		return dbs.query(sql);
	}
	
	// 从config文件中获取meta
	private List<String> metas() {
		String ss = config().getString("metas");
		return Arrays.asList(split(ss, ","));
	}
	
	// 得到config文件
	private GroovyConfig config() {
		
		// // "f:/dev3/classes/app/Cube.groovy";
		String configfile = Appx.filepath(getClass(), ".groovy"); 
		return GroovyConfig.create(configfile);
	}


	/**
	 * todo: 是否能够更简单：
	 * Render.params("cn中文");
	 * 
	 * 得到本action对应的模板输出
	 * @return
	 */
	public Render view() {
	
		Template t = Appx.view(this); // 调用此方法需有params域
		if (t==null) {
			return Render.text("没有视图模板文件");
		} else {
			return Render.text(t.set("msg", "cn中文").toString());			
		}
	}
	
	/**
	 * 得到数据集字段代码和meta
	 * @return
	 */
	public Render codes() {
		
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("month", "月份");
		map.put("orgid", "机构");
		map.put("name", "姓名");
		map.put("hq", "活期额");
		map.put("dq", "定期额");
		return Render.json(map); // json对象不能保证顺序
		// for (p in json) println(p + ": " + json[p])
	}
}
