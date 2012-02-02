package wr3;

import java.io.*;
import java.util.*;

import wr3.util.Numberx;

/**
 * <pre>
 * wr3 basic configuration.
 * �� ${wr3.home}/conf/base.properties �ж�ȡ����������Ϣ
 * wr3.home���÷�����
 *   1) ��java�������� -Dwr3.home=e:/tomcat/webapps/wr3 ������;
 *   2) �����������е�java����ʹ��System.setProperty("wr3.home", ".");
 *   3) ��web.xml��AppFilter <init-parameter>������;
 *  ע�����ܴ�OS���������л�ȡ
 *
 * usage:<code>
 *   BaseConfig.getBool("DBAdaptor.debug");
 *   BaseConfig.get("wr3.home");  //ȡwr3.home:
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
	 * �����ڴ��д洢config��Ϣ, ʹ��Properties���ɶ���д
	 */
	private static Properties config = new Properties ();

	// ���������ļ���·��
	public final static String WR3HOME  = "wr3.home";
	// webapp��contextPath���磺"/wr3", ֻ��web�����AppFilter#init������
	public final static String CONTEXT_PATH  = "contextPath";
	// ���������ļ����ļ���
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

	//--------- ����util���� -----------

	/**
	 * �õ� "wr3.home"���磺'e:/tomcat/webapps/wr3'
	 * @return
	 */
	public static String wr3home() { return get(WR3HOME); }

	/**
	 * �õ���ǰwebapp��contextPath����"/wr3"
	 * @return
	 */
	public static String contextPath() { return get(CONTEXT_PATH); }

	/**
	 * @see #contextPath()���磺'/wr3', ''
	 * @return
	 */
	public static String webapp() { return contextPath(); }

	/**
	 * �õ�app.path, �磺'f:/dev3/classes/'
	 */
	public static String appPath() { return get("app.path"); }

	/**
	 * �õ�app.package���磺'app'
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
	 * ���ô˷��������Զ�ת�أ��仯�����½�����������cache��ȡ��
	 */
	private static void load () {
		init();
		ResourceCache.create(instance);
	}

	/**
	 * <pre>
	 * set baseconfig file name, run once.
	 * use "./conf/base.properties" if "wr3.home" not define.
	 * �ɴ� java -Dwr3.home ��֮ǰ System.setProperty() �еõ�wr3home.
	 * <b>��</b>��OS��������ȡ�� System.getenv("wr3.home") ��
	 * </pre>
	 */
	private static void init() {

		if (filename!=null) return;

		// ������-D����֮ǰ System.setProperty() �еõ�wr3home, �����õ�BaseConfig��
		String wr3home = System.getProperty(WR3HOME);
		if (wr3home==null) wr3home = ".";
		config.setProperty("wr3.home", wr3home);
		// ȡ�õ�BaseConfig�������ļ�·��
		filename = (wr3home + "/" + FILENAME);
	}

	public static String info() {
		return config.toString();
	}

	// ʵ�����½ӿڷ���
	public String filename() {

		return filename;
	}

	/**
	 * �� BaseConfig.properties �ļ���װ��.
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
		ResourceCache.regist(instance); // ��ConfigCache���Ǽ�
		return null;
	}
}
