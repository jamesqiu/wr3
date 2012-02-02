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
 * �������ļ�:
 *  ${wr3.home}/conf/Driver.groovy
 *  ${wr3.home}/conf/DataSource.groovy
 * �õ����ݿ�������Ϣ(driver,url,username,password)
 * <usage>
 *  config = new DbConfig();
 *  props = config.get();		// �õ�ȱʡ�����ݿ�����
 *  props = config.get("abs");	// �õ�"abs"�����ݿ�����
 *  map = config.drivers();		// �õ�����drivers����Ϣ
 *  list = config.dbnames();	// �õ����е����ݿ���������
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
	 * �õ�ȱʡdataSource������Դ��������
	 * @return
	 */
	public Map<String, String> get() {
		return get(null);
	}
	
	/**
	 * �õ�ָ�����Ƶ�����Դ��������
	 * @param dbname
	 * @return jdbc����jndi��������
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
	 * �õ������ļ��е���������Դ��������
	 * @return 
	 */
	public List<String> dbnames() {

		return Arrays.asList(config2.keys());
	}
	
	/**
	 * �õ�����Driver���õ��б�;
	 * @return
	 */
	public Map<String, String> drivers() {
		
		Map<String,String> vars = new LinkedHashMap<String, String>();

		String[] jdbc = config1.keys("jdbc");
		String[] jndi = config1.keys("jndi");
		
		//�õ�jdbc����һ������
		for (String key : jdbc) {
			// �����jdbc.url, �õ���һ������
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
		// �õ�jndi����һ������
		for (String name : jndi) {
			String name1 = "jndi."+name;
			vars.put(name1, ""+config1.get(name1));
		}
		return vars;
	}
	
	/**
	 * �������ļ����洢��{@link ResourceCache}�� .
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
