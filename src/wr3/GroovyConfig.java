package wr3;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import wr3.util.CLI;
import wr3.util.Filex;
import wr3.util.GroovyUtil;
import wr3.util.Stringx;

/**
 * <pre>
 * use ConfigSlurper to read .groovy config file.
 * ConfigSlurp是基于Groovy的具有比.xml和.properties更强表达力的配置文件格式
 * <a href="http://groovy.codehaus.org/ConfigSlurper">web</a>
 * 1.
 * config file可以有多个变量(name, value)输入;
 * 输出可以有多个.
 * 2.
 * 记录每一个config文件最后更新时间，转载过的config文件无需再次转载。
 *
 * usage1: // 无输入，取输出
 * 	gc = GroovyConfig.create("config.groovy");
 * 	keys = gc.keys ();
 * 	gc.get (keys[0]);
 *
 * usage2: // 有输入，取输出
 *  gc = GroovyConfig.create("config.groovy", vars);
 *
 * usage3: // 有输入，取输出（更灵活）
 *	gc = GroovyConfig.create().set("k1","v1").vars(vars)
 * 		.load("config.groovy");
 * 	keys = gc.keys();
 * 	gc.get("query1.dbname");
 *
 * usage4: // 特殊用法，强制解析；
 *  gc = GroovyConfig.create().filename("config.groovy").parse();
 * </pre>
 *
 * @author james 2008-7-11
 * @see GroovyUtil
 */
public class GroovyConfig implements ResourceInterface {

	private String filename;		// config file name with full path, like "./config1.groovy", "c:/wr3/wr3.groovy"
	private ConfigSlurper slurper;	// Config slurp 解析器
	private ConfigObject  confobj;  // 解析执行后的结果

	/**
	 * 对config文件的输入变量名称
	 */
	private Map<String, Object> vars = new LinkedHashMap<String, Object>();

	private GroovyConfig () {
		slurper = new ConfigSlurper();
	}

	/**
	 * get instance without input
	 * @return
	 */
	public static GroovyConfig create() {
		return new GroovyConfig();
	}

	/**
	 * <pre>
	 * Get instance by given config groovy file.
	 * Use this to create config *without* any input vars,
	 * set() and setVars() is *useless* after create(filename).
	 * </pre>
	 * @param filename config groovy file fullpath
	 * @return instance ready for use.
	 */
	public static GroovyConfig create (String filename) {

		return create().load(filename);
	}

	/**
	 * <pre>
	 * get instance by given config file and vars.
	 * set() and setVars() is *useless* after create(filename).
	 * </pre>
	 * @author james 2008-7-15
	 * @param filename
	 * @param vars
	 * @return
	 */
	public static GroovyConfig create (String filename, Map<String, ?> vars) {

		return create().vars(vars).load(filename);
	}

	/**
	 * 调用此方法进行自动装载（变化则重新解析，不变则cache中取）
	 */
	public GroovyConfig load (String filename) {

		this.filename = filename;	// 使ResourceCache.create()能够获取文件名
		return (GroovyConfig) (ResourceCache.create(this));
	}

	/**
	 * 设置filename
	 * @param filename
	 * @return
	 */
	public GroovyConfig filename(String filename) {
		this.filename = filename;
		return this;
	}
	/**
	 * {@link ResourceInterface#filename()}接口的实现，得到ConfigSlurp文件名
	 */
	public String filename() {
		return filename;
	}

	/**
	 * <pre>
	 * {@link ResourceInterface#parse(String)}接口的实现，进行Groovy ConfigSlurp文件的真正解析
	 * parse groovy configslurp file *AFTER* set all vars.
	 * 注意：显式调用本方法将强制做一次文件解析，出于性能考虑，请尽量使用其他如下方法
	 * {@link #create(String)}, {@link #create(String, Map)}, {@link #load(String)}
	 * </pre>
	 * @see ResourceInterface#parse(String)
	 * @param filename
	 * @return
	 */
	public GroovyConfig parse () {

		System.out.printf("--[wr3 GroovyConfig.parse()]: %s\n", filename);

		if (!Filex.has(filename)) {
			new FileNotFoundException(Filex.fullpath(filename)).printStackTrace();
			return this;
		}

		try {
			URL url = new File(filename).toURI().toURL();
			if (vars.size() > 0) slurper.setBinding(vars);
			confobj = slurper.parse(url);	// this will spend about 125ms
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		ResourceCache.regist(this);	// 在ResourceCache做登记
		return this;
	}

	/**
	 * <pre>
	 * 注意：本方法必须在如下方法之前调用，否则无效：
	 * {@link #create(String)}, {@link #create(String, Map)}, {@link #load(String)}
	 * 再次调用 {@link #parse(String)}或者更改config文件时间戳可使set()值生效。
	 * </pre>
	 * @author james 2008-7-15
	 * @param key
	 * @param value
	 */
	public GroovyConfig set (String key, Object value) {

		vars.put(key, value);
		return this;
	}

	/**
	 * 注意事项同 {@link #set(String, Object)}
	 * @author james 2008-7-15
	 * @param vars
	 */
	public GroovyConfig vars (Map<String, ?> vars) {

		if (vars!=null) this.vars.putAll(vars);
		return this;
	}

	/**
	 * 返回第一级变量名字，不含下一级变量名
	 * @return all keys name
	 */
	public String[] keys () {

		if (confobj==null) return new String[0];

		Set<?> set = confobj.keySet();
		return set2array(set);
	}

	/**
	 * 得到config中指定对象的下一级keys.
	 * @param objname 对象名称, 如 "jdbc"
	 * @return
	 */
	public String[] keys(String objname) {

		if (Stringx.nullity(objname)) return new String[0];

		Object o = get(objname);
		if (o instanceof ConfigObject) {
			Set<?> set = ((ConfigObject)o).keySet();
			return set2array(set);
		} else {
			return new String[0];
		}

	}

	private String[] set2array(Set<?> set) {
		return (String[]) (set.toArray(new String[set.size()]));
	}

	/**
	 * <pre>
	 * get all config, can use like this:
	 * in java:
	 * 	config = groovyConfig.getAll();
	 * 	groovy.set("config", gc.getAll());
	 * in groovy:
	 * 	sql = config.query1.sql
	 * </pre>
	 * @return
	 */
	public ConfigObject getAll () {

		return confobj;
	}

	/**
	 * get value of given key
	 * @param key like: "query", "query1.sql", "query1.a.b.c"
	 * @return value of given key as Object, null if not exist.
	 */
	public Object get (String key) {

		if (key==null || confobj==null) return null;

		String[] keyparts = Stringx.split(key, ".");
		ConfigObject p = confobj;
		int n = keyparts.length;
		Object o = null;
		for (int i = 0; i < (n-1); i++) {
			o = p.getProperty (keyparts[i]);
			if (o==null ||
				!(o instanceof ConfigObject) || // 没有下级了
				((ConfigObject)o).size()==0 ) 	// 不存在的节点会返回{}
			{
				return null;
			}
			p = (ConfigObject) o;
		}

		o = p.getProperty(keyparts[n-1]);
		// 不存在的节点
		if ((o instanceof ConfigObject) &&
			((ConfigObject)o).size()==0) {
			return null;
		}

		return o;
	}

	/**
	 * get value as string
	 * @param key
	 * @return string value of given key if you sure it's String type
	 */
	public String getString (String key) {

		Object p = get(key);
		if (p==null) return null;

		if (p instanceof String) {
			return (String) p;
		} else { // maybe groovy GStringImpl
			return p.toString();
		}
	}

	/**
	 * @param key
	 * @return int value of given key if you sure it's Integer type
	 */
	public int getInt (String key) {

		Object p = get(key);
		if (p==null) return 0;

		return ((Integer) p).intValue();
	}

	/**
	 * @param key
	 * @return BigDecimal value of given key if you sure it's BigDecimal type
	 */
	public BigDecimal getDecimal (String key) {

		Object p = get(key);
		if (p==null) return null;

		return ((BigDecimal) p);
	}

	/**
	 * @param key
	 * @return boolean value of given key if you sure it's Boolean type
	 */
	public boolean getBool (String key) {

		Object p = get(key);
		if (p==null) return false;

		return ((Boolean) p).booleanValue();
	}

	// ---------------------- main() ----------------------
	/**
	 * 提高启动速度：
	 * java -Xbootclasspath/a:lib\groovy-all-1.5.7.jar \
	 *   wr3.util.GroovyConfig -f test\GroovyConfig.groovy
	 * 或者
	 * java -Xverify:none wr3.util.GroovyConfig -f test\GroovyConfig.groovy
	 */
	public static void main(String[] args) {

		long t0 = System.currentTimeMillis();

		CLI cli = new CLI()
			.set("i", "input", true, "[-i key=value] like: \"table=cust\" 必须带引号]")
			.set("f", "file", true, "[-f conffile] like: -f test/config.groovy]")
			.set("k", "key", true, "[-k key] like: \"query1.dbname\"]")
			.parse(args);

		if (args.length == 0) {
			cli.help("GroovyConfig [-i key=value] -f config.groovy -k q1.sql");
			return;
		}

		if (cli.has("file")) {
			String filename = cli.get("file");
			GroovyConfig config = GroovyConfig.create();
			if (cli.has("input")) {
				String input = cli.get("input");
				String key = Stringx.left(input, "=");
				String val = Stringx.right(input, "=");
				config.set(key, val);
			}
			config.load(filename);
			if (cli.has("key")) {
				String key = cli.get("key");
				System.out.println(config.get(key));
			} else {
				String[] keys = config.keys();
				System.out.println("keys: " + Stringx.join(keys, ", "));
				System.out.println(config.getAll());
			}
		}

		long t1 = System.currentTimeMillis();
		System.out.println("time use: " + (t1 - t0));
	}
}
