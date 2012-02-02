package wr3;

import java.io.*;
import java.util.*;

import wr3.util.Numberx;

/**
 * <pre>
 * wr3 basic configuration.
 * 从 ${wr3.home}/conf/base.properties 中读取基本配置信息
 * wr3.home设置方法：
 *   1) 从java启动参数 -Dwr3.home=e:/tomcat/webapps/wr3 中设置;
 *   2) 从其他先运行的java类中使用System.setProperty("wr3.home", ".");
 *   3) 从web.xml的AppFilter <init-parameter>中配置;
 *  注：不能从OS环境变量中获取
 *
 * usage:<code>
 *   BaseConfig.getBool("DBAdaptor.debug");
 *   BaseConfig.get("wr3.home");  //取wr3.home:
 * </code>
 * </pre>
 * @author jamesqiu 2008-11-25
 */
public class BaseConfig implements ResourceInterface {

	private static BaseConfig instance = new BaseConfig();
	/**
	 * config file name with full path,
	 * like "./conf/BaseConfig.properties", "c:/wr3/conf/base.properties"
	 */
	private static String filename;
	/**
	 * 用于内存中存储config信息, 使用Properties，可读可写
	 */
	private static Properties config = new Properties ();

	// 基本配置文件的路径
	public final static String WR3HOME  = "wr3.home";
	// webapp的contextPath，如："/wr3", 只在web程序的AppFilter#init中设置
	public final static String CONTEXT_PATH  = "contextPath";
	// 基本配置文件的文件名
	public final static String FILENAME = "conf/base.properties";

	/**
	 * get config setting as string.
	 * @param key key name of String value in properties file.
	 * @return null if not found
	 */
	public static String get (String key) {
		load();
		return config.getProperty(key);
	}

	/**
	 * get config setting not null string.
	 * @param key key name of String value in properties file.
	 * @return relevent String value of given key. return "" if not found.
	 */
	public static String getStr (String key) {

		String value = get(key);
		if (value==null) return "";
		return value.trim ();
	}

	/**
	 * get config setting as int
	 * @param key key name of integer value in properties file.
	 * @return relevent int value of given key. return -1 if not found.
	 */
	public static int getInt (String key) {

		String value = get(key);
		return Numberx.toInt(value.trim(), -1);
	}

	/**
	 * get config setting as boolean (true|false)
	 * @param key key name of bool value in properties file.
	 * @return relevent bool value of given key. return false if not found.
	 */
	public static boolean getBool (String key) {

		String value = get(key);
		return "true".equalsIgnoreCase(value.trim());
	}

	//--------- 几个util方法 -----------

	/**
	 * 得到 "wr3.home"，如：'e:/tomcat/webapps/wr3'
	 * @return
	 */
	public static String wr3home() { return get(WR3HOME); }

	/**
	 * 得到当前webapp的contextPath，如"/wr3"
	 * @return
	 */
	public static String contextPath() { return get(CONTEXT_PATH); }

	/**
	 * @see #contextPath()，如：'/wr3', ''
	 * @return
	 */
	public static String webapp() { return contextPath(); }

	/**
	 * 得到app.path, 如：'f:/dev3/classes/'
	 */
	public static String appPath() { return get("app.path"); }

	/**
	 * 得到app.package，如：'app'
	 */
	public static String appPackage() { return get("app.package"); }


	/**
	 * config setting
	 * @param key
	 * @param value
	 */
	public static void set (String key, String value) {
		load();
		config.setProperty(key, value);
	}

	public static void setStr (String key, String value) {
		set(key, value);
	}

	public static void setInt (String key, int value) {
		set(key, "" + value);
	}

	public static void setBool (String key, boolean value) {
		set(key, "" + value);
	}

	/**
	 * check if has key in GlobalProperty.
	 * @return if has key, return true;
	 */
	public static boolean has (String key) {
		return (get(key) != null);
	}

	/**
	 * @return If has "key=string_value" in .properties file, return true.
	 */
	public static boolean hasStr (String key, String value) {
		return getStr(key).equals(value);
	}

	/**
	 * @return If has "key=int_value" in .properties file, return true.
	 */
	public static boolean hasInt(String key, int value) {
		return getInt(key)==value;
	}

	public static boolean hasBool (String key, boolean value) {
		return getBool(key)==value;
	}

	/**
	 * 调用此方法进行自动转载（变化则重新解析，不变则cache中取）
	 */
	private static void load () {
		init();
		ResourceCache.create(instance);
	}

	/**
	 * <pre>
	 * set baseconfig file name, run once.
	 * use "./conf/base.properties" if "wr3.home" not define.
	 * 可从 java -Dwr3.home 或之前 System.setProperty() 中得到wr3home.
	 * <b>不</b>从OS环境参数取（ System.getenv("wr3.home") ）
	 * </pre>
	 */
	private static void init() {

		if (filename!=null) return;

		// 从启动-D或者之前 System.setProperty() 中得到wr3home, 并设置到BaseConfig中
		String wr3home = System.getProperty(WR3HOME);
		if (wr3home==null) wr3home = ".";
		config.setProperty("wr3.home", wr3home);
		// 取得到BaseConfig的配置文件路径
		filename = (wr3home + "/" + FILENAME);
	}

	public static String info() {
		return config.toString();
	}

	// 实现如下接口方法
	public String filename() {

		return filename;
	}

	/**
	 * 对 BaseConfig.properties 文件的装载.
	 */
	public ResourceInterface parse() {
		System.out.printf("--[wr3 BaseConfig.parse()]: %s\n", filename);
		try {
			InputStream in = new FileInputStream(filename);
			config.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ResourceCache.regist(instance); // 在ConfigCache做登记
		return null;
	}
}
