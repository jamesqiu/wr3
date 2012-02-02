package wr3.db;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wr3.BaseConfig;
import wr3.GroovyConfig;
import wr3.ResourceCache;
import wr3.util.Stringx;

/**
 * <pre>
 * 从配置文件:
 *  ${wr3.home}/conf/Driver.groovy
 *  ${wr3.home}/conf/DataSource.groovy
 * 得到数据库配置信息(driver,url,username,password)
 * <usage>
 *  config = new DbConfig();
 *  props = config.get();		// 得到缺省的数据库配置
 *  props = config.get("abs");	// 得到"abs"的数据库配置
 *  map = config.drivers();		// 得到所有drivers的信息
 *  list = config.dbnames();	// 得到所有的数据库配置名称
 *   
 * </pre>
 * @author jamesqiu 2009-1-9
 *
 */

public class DbConfig {

	private GroovyConfig config1;	// Driver.groovy
	private GroovyConfig config2;	// DataSource.groovy
	
	public DbConfig() {
		init();
	}
	
	/**
	 * 得到缺省dataSource的数据源配置属性
	 * @return
	 */
	public Map<String, String> get() {
		return get(null);
	}
	
	/**
	 * 得到指定名称的数据源配置属性
	 * @param dbname
	 * @return jdbc或者jndi配置属性
	 */
	public Map<String, String> get(String dbname) {
		
		if (Stringx.nullity(dbname)) dbname = "dataSource";
		
		Map<String, String> rt = new LinkedHashMap<String, String>();
		String driver = config2.getString(dbname+".driver");
		
		if(driver==null) return rt;
		
		rt.put("driver", driver);
		String[] props;
		if ("jndi".equals(driver)) {
			props = new String[]{"name"};
		} else {
			props = new String[]{"url", "username", "password"};
		}
		for (String prop : props) {
			rt.put(prop, config2.getString(dbname+"."+prop));
		}
		return rt;
	}
	
	/**
	 * 得到配置文件中的所有数据源配置属性
	 * @return 
	 */
	public List<String> dbnames() {

		return Arrays.asList(config2.keys());
	}
	
	/**
	 * 得到所有Driver配置的列表;
	 * @return
	 */
	public Map<String, String> drivers() {
		
		Map<String,String> vars = new LinkedHashMap<String, String>();

		String[] jdbc = config1.keys("jdbc");
		String[] jndi = config1.keys("jndi");
		
		//得到jdbc的下一级变量
		for (String key : jdbc) {
			// 如果是jdbc.url, 得到下一级变量
			if (key.equals("url")) {
				String[] jdbc_url = config1.keys("jdbc.url");
				for (String url : jdbc_url) {
					String name1 = "jdbc.url."+url;
					vars.put(name1, ""+config1.get(name1));
				}
				continue;
			}
			String name1 = "jdbc."+key;
			vars.put(name1, ""+config1.get(name1));
		}
		// 得到jndi的下一级变量
		for (String name : jndi) {
			String name1 = "jndi."+name;
			vars.put(name1, ""+config1.get(name1));
		}
		return vars;
	}
	
	/**
	 * 读配置文件并存储与{@link ResourceCache}中 .
	 */
	private void init() {

		String wr3home = BaseConfig.get("wr3.home");
		String filename1 = wr3home + "/conf/Driver.groovy";
		String filename2 = wr3home + "/conf/DataSource.groovy";
		
		config1 = GroovyConfig.create(filename1);

		Map<String, Object> vars = new LinkedHashMap<String, Object>();
		vars.put("jdbc", config1.get("jdbc"));
		vars.put("jndi", config1.get("jndi"));
		
		config2 = GroovyConfig.create(filename2, vars);
	}
	
	public static void main(String[] args) {
		DbConfig ds = new DbConfig();
		System.out.println(ds.get());
		System.out.println(ds.get("abs_grails"));
		System.out.println(ds.get("apmis"));
		System.out.println(ds.drivers());
		System.out.println(ds.dbnames());
	}
}
