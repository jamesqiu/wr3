package wr3.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

import wr3.web.Appx;

/**
 * Class Util: classloader, method invoke, property, path list, ...
 * @author jamesqiu 2007-7-20
 */
public class Classx {

	/**
	 * �õ���ľ���Ŀ¼, ��������jar�ļ�����Ŀ¼, �������ļ���
	 * get real path of a class file.
	 */
	@SuppressWarnings("unchecked")
	public static String getPath (Class cls) {

		// "wr3.util.Classx", "java.lang.String"
		String clsname = cls.getName();
		// "wr3/util/Classx.class", "java/lang/String"
		String resname = clsname.replace('.', '/') + ".class";
		// "/F:/dev3/classes/wr3/util/Classx.class"
		// "file:/E:/jdk16/jre/lib/rt.jar!/java/lang/String.class"
		URL url = loader(cls).getResource(resname);
		String res = Filex.url2uri(url).getPath();
		if (res==null) { // jar�ļ�Ŀ¼
			res = url.getPath().replaceAll("%20", " ");
		}
		if (res.startsWith("/")) {
			// "/F:/dev3/classes/wr3/util"
			return Stringx.leftback(res, "/");
		} else {
			// "/E:/jdk16/jre/lib/rt.jar"
			return Stringx.between(res, "file:", "!");
		}
	}

	/**
	 * �õ���classͬĿ¼ͬ����ͬ��׺����Դ��·��.
	 * �磺 filepath(getClass(), ".ftl") --> "wr3/util/Classx.ftl"
	 * @param cls һ���� getClass()
	 * @param postfix ��׺�� ".ftl", ".properties", ע���©��"."
	 * @return
	 *
	 * @see Appx#filepath(Class, String)
	 */
	public static String filepath(Class<?> cls, String postfix) {

		// "wr3.util.Classx", "java.lang.String"
		String path = cls.getName();
		// "wr3/util/Classx.ftl", "java/lang/String.ftl"
		path = path.replace('.', '/');
		if (postfix!=null) path += postfix; // "Cube.groovy"

		return path;
	}

	/**
	 * �˷�������ϵͳ��ͷ�ϵͳ��õ�ClassLoader�Ĳ�ͬ.
	 * @param cls
	 * @return
	 */
	public static ClassLoader loader(Class<?> cls) {
		ClassLoader rt = cls.getClassLoader();
		return (rt==null) ? ClassLoader.getSystemClassLoader() : rt;
	}

	/**
	 * <pre>
	 * �õ�һ��jar���л���classpath�µ���Դ��url;
	 * ע�⣺clsΪϵͳ��ʱ��name�����Ƿ�ϵͳ�࣬ѡclsԽ�ӽ��û���Խ�á�
	 *       ����classpath�µ���Դ�Ҳ�����
	 * </pre>
	 * @param cls һ�����getClass()����,
	 * 		����static������ֱ���������Filex.class��
	 * @param name ���磺"wr3/table/FormTable.ftl", "wr3/Table.class"
	 *             ��·����class loader "/", ���Բ�����д "/"
	 * @return
	 * @see #inputStream(Class, String)
	 */
	public static URL url(Class<?> cls, String name) {
		// ��loaderת�ز���"/"��ʼ����classװ�ؿ���"/"��ʼ������·����
		return loader(cls).getResource(name);
	}

	/**
	 * @see Filex#resource(Class, String)
	 * @see #url(Class, String)
	 * @param cls һ����getClass(), ������ Classx.class
	 * @param name �� {@link #url(Class, String)} ��name��һ���������Ǿ��Ի������·��
	 * @return
	 */
	public static String resource(Class<?> cls, String name) {
		return Filex.resource(cls, name);
	}

	/**
	 * @see Filex#resource(Object, String)
	 * @param o
	 * @param name
	 * @return
	 */
	public static String resource(Object o, String name) {
		return Filex.resource(o, name);
	}

	/**
	 * �õ�һ��jar���л���classpath�µ���Դ��InputStream
	 * @param cls һ�����getClass()���ɡ�
	 * @param name ���磺"wr3/table/FormTable.ftl", "wr3/Table.class"
	 * @return
	 * @see Classx#url(Class, String)
	 */
	public static InputStream inputStream(Class<?> cls, String name) {

//		return url(cls, name).openStream();
		return loader(cls).getResourceAsStream(name);
	}

	/**
	 * ��classpath��ϵͳ��Դ(��lib/pinyin.jar)�еõ�InputStream
	 * @param name ·����: "pinyin/pinyin.txt"
	 * @return
	 */
	public static InputStream inputStream(String name) {

		if (name==null) return null;

		//return ClassLoader.getSystemResourceAsStream(name);
		// jamesqiu 2011-7-27, ������÷���WebfirstCore�п��ܷ���nill�������д����robust
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		
	}

	/**
	 * ȡĳ��·���µ�����������(����·��)��
	 * get all classes in same package with given class, include sub package.
	 * @param cls class with package name like "com.webreport.Node"
	 * @return all classes with full package path (in "com.webrepot.*")
	 */
	@SuppressWarnings("unchecked")
	public static String[] getClasses (Class cls) {

		List<String> list = new ArrayList<String> ();

		// "java.lang.System", "com.webreport.util.ClassUtil"
		File file = new File(getPath(cls));
		String pkgname = cls.getPackage().getName();
		ClassLoader loader = cls.getClassLoader();
		if (loader==null) {
			visitJar (file, pkgname, list);
		} else {
			visitPath (file, pkgname, list);
		}

		return Stringx.list2array(list);
	}

	/**
	 * get class (in classpath) with given className,
	 * @param className like "java.util.Date"
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class getClass (String className) {

		if (isClassNameNull(className)) return null;

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * get class (*NOT* in classpath) with given url.
	 * @param classUrl new classpath like: "./classes/", "."; can *NOT* be ""
	 * @param className like: "c2", "app.App1"
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class getHotClass (String classUrl, String className) {

		File classpath = new File(classUrl);
		if (!classpath.exists() || !classpath.isDirectory()) return null;

		// get new class file path url
		URL url = null;
		try {
			url = classpath.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		URL[] urls = new URL[]{url};
		// load updated class, ����Classx.class.getClassLoader(), ��֤URLClassLoader����ԭ����·��
		ClassLoader cl = new URLClassLoader(urls, Classx.class.getClassLoader());
		try {
			Class cls = cl.loadClass(className);
			return cls;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * get object with name, from classpath.
	 * ͨ�������ֵõ���ʵ����
	 * @param className like "java.util.Date", must in classpath.
	 * @return ����ʵ��
	 */
	public static Object getObject (String className) {

		return getObject(className, null);
	}

	/**
	 * get object with name and constructor params, from classpath
	 * ͨ�������ֵõ���ʵ����
	 * @param className className className string like "java.util.Date"
	 * @param initParams ���췽������, like
	 * 	new Object[]{new Integer(2008), new Integer(12), new Integer(31)}
	 * @return
	 */
	public static Object getObject (String className, Object[] initParams) {

		return getObject (getClass(className), initParams);
	}

	/**
	 * ͨ��Class���õ��޲����Ĺ���Object
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public
	static Object getObject(Class cls) {
		try {
			return cls.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ͨ��Class�͹���������õ�����Object
	 * @param cls
	 * @param initParams
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public
	static Object getObject(Class cls, Object[] initParams) {

		if (initParams==null || initParams.length==0)
			return getObject(cls);

		Class[] types = getInitParamsType(initParams);
		Constructor cons;
		try {
			cons = cls.getConstructor(types);
			return cons.newInstance(initParams);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * get object with url string, from *OUT* of classpath.
	 * Ŀ�ģ�ʵ��������ʱת�������ࣨ������벻��classpath�еģ���
	 * ���췽������������
	 * @param classUrl new classpath like: "./classes/", "."; can *NOT* be ""
	 * @param className like: "c2", "app.App1"
	 * @return
	 */
	public static Object getHotObject (String classUrl, String className) {

		return getHotObject(classUrl, className, null);
	}

	/**
	 * get object with url string and constructor params, from *OUT* of classpath.
	 * Ŀ�ģ�ʵ��������ʱת�������ࣨ������벻��classpath�еģ���
	 * ���췽������������
	 * @see <The Java Developers Almanac 1.4>
	 * 		"e69. Dynamically Reloading a Modified Class"
	 * @param classUrl new classpath like: "./classes/", "."; can *NOT* be ""
	 * @param className like: "c2", "app.App1"
	 * @param initParams params of constructor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object getHotObject (String classUrl, String className, Object[] initParams) {

		Class cls = getHotClass(classUrl, className);
		if (cls==null) return null;

		return getObject(cls, initParams);
	}

	/**
	 * <pre>
	 * �õ�һ�����������public������
	 * </pre>
	 * @param o
	 * @return ������java.lang.Object��9��public����
	 */
	public static Method[] getMethods(Object o) {

		if (o==null) return null;

		return o.getClass().getMethods();
	}

	/**
	 * �õ�java.lang.Object���������public������(9��).
	 * @return
	 */
	private final static Object obj = new java.lang.Object();
	public static String[] objectMethods() {
		Method[] methods = getMethods(obj);
		String[] names = new String[methods.length];
		for (int i = 0; i < methods.length; i++) {
			names[i] = methods[i].getName();
		}
		return names;
	}

	/**
	 * �õ�һ������ָ�����Ƶ�public�������޲�������
	 * @param o
	 * @param name �޲�public������
	 * @return null ���û�����public����
	 */
	public static Method getMethod(Object o, String name) {

		return getMethod(o, name, null);
	}

	/**
	 * �õ�һ������ָ�����Ƶ�public�������޲�������
	 * @param o
	 * @param name �޲�public������
	 * @param paramTypes public�����Ĳ�������
	 * @return null ���û�����public����
	 */
	public static Method getMethod(Object o, String name, Class<?>[] paramTypes) {

		if (o==null || Stringx.nullity(name)) return null;

		if (paramTypes==null) paramTypes = new Class<?>[0];
		try {
			return o.getClass().getMethod(name, paramTypes);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * <pre>
	 * Utility�÷���ִ��һ��ָ�����Ƶ�����޲���������
	 * �Ƽ�ʹ��:
	 * 	{@link #invoke(Object, String)},
	 * 	{@link #invoke(Object, String, Object[])}
	 * usage:
	 *   ClassUtil.invokeMethod("java.util.Date", "toLocaleString");
	 * </pre>
	 * @param className �������ƣ�������classpath��
	 * @param methodName ����ķ�����
	 * @return ����ִ�з���ֵ
	 */
	public static Object invoke (String className, String methodName) {

		return invoke(getObject(className), methodName);
	}

	/**
	 * ִ��һ��������޲���������
	 * usage:
	 *   ClassUtil.invokeMethod(date1, "toString");
	 * @param o ����
	 * @param methodName ����ķ�����
	 * @return ����ִ�з���ֵ
	 */
	public static Object invoke(Object o, String methodName) {
		return invoke (o, methodName, null);
	}

	/**
	 * ִ��һ������ķ�����
	 * usage:
	 *   ClassUtil.invokeMethod(date1, "setYear", new Object[]{100});
	 * @param o ����
	 * @param methodName ����ķ�����
	 * @param args �����Ĳ���
	 * @return ����ִ�з���ֵ
	 */
	@SuppressWarnings("unchecked")
	public static Object invoke (Object o, String methodName, Object[] args) {

		if (o==null && Stringx.nullity(methodName)) return null;

		Object rt = null;
		Class[] types = getInitParamsType(args);
		try {
			Method method = o.getClass().getMethod(methodName, types);
			rt = method.invoke(o, args);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return rt;
	}

	/**
	 * ִ��public��static����
	 * @param cls ��������"java.lang.Math"
	 * @param method ����������"abs"
	 * @param args
	 * @return
	 * @author jamesqiu 2011-1-13
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeStatic(String clsName, String methodName, Object[] args) {

		if(Stringx.nullity(clsName) && Stringx.nullity(methodName)) return null;

		Class cls = getClass(clsName);
		if (cls==null) return null;

		try {
			Method method = cls.getMethod(methodName, getInitParamsType(args));
			return method.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ִ�з�Public�ķ�����������JUnit�в��Է�Public������
	 * @param o
	 * @param methodName
	 * @param args
	 * @return
	 * @see #invoke(Object, String, Object[])
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeUnlimit(Object o, String methodName, Object[] args) {

		if (o==null && Stringx.nullity(methodName)) return null;

		Object rt = null;
		Class[] types = getInitParamsType(args);
		try {
			// �õ����ж����˵�public�ͷ�public����
			Method method = o.getClass().getDeclaredMethod(methodName, types);
			// ����Ϊ���Է���
			method.setAccessible(true);
			rt = method.invoke(o, args);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return rt;
	}

	/**
	 * �ж϶����Ƿ���ָ�����Ƶ�field.
	 * @param obj
	 * @param fieldName
	 * @return true if obj has field with given name
	 */
	public static boolean hasField(Object obj, String fieldName) {

		if (isFieldNull(obj, fieldName)) return false;

		try {
			obj.getClass().getDeclaredField(fieldName);
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchFieldException e) {
			return false;
		}
		return true;
	}

	/**
	 * �õ������fieldֵ��field��public��protected�ġ�
	 * @param obj ����ʵ��
	 * @param fieldName field����
	 * @return �����ֵ����Ҫ����ת������
	 */
	public static Object getField(Object obj, String fieldName) {

		if (isFieldNull(obj, fieldName)) return null;

		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			return field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���ö����fieldֵ��field������private��protected�ġ�
	 * @param obj
	 * @param fieldName
	 * @param fieldValue
	 */
	public static void setField (Object obj, String fieldName, Object fieldValue) {

		if (isFieldNull(obj, fieldName)) return;

		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set (obj, fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isFieldNull(Object obj, String fieldName) {

		if (obj==null || Stringx.nullity(fieldName)) {
			System.err.println("ClassUtil.java get/setField(): obj or fieldName is null");
			return true;
		}
		return false;
	}

	// forName()
	private static boolean isClassNameNull (String className) {

		if (Stringx.nullity(className)) {
			System.err.println("ClassUtil.java forName(): className is " + className);
			return true;
		}
		return false;
	}

	// forName (className, initParams);
	// invokeMethod (o, methodName, params);
	@SuppressWarnings("unchecked")
	private static Class[] getInitParamsType (Object[] initParmas) {

		if(initParmas==null) return null;
		int n = initParmas.length;
		Class[] types = new Class[n];

		for (int i = 0; i < n; i++) {
			Class type = initParmas[i].getClass();
			types[i] = modifyClassType(type);
		}
		return types;
	}

	// getInitParamsType ()
	@SuppressWarnings("unchecked")
	private static Class modifyClassType (Class type) {
		// �ѻ������Ͷ�Ӧ�İ�����תΪ��������
		if (type==Integer.class) 	return int.class;
		if (type==Long.class)		return long.class;
		if (type==Double.class)  	return double.class;
		if (type==Float.class)		return float.class;
		if (type==Boolean.class) 	return boolean.class;

		return type;
	}

	/**
	 * get all classes froma .jar file.
	 * @param file .jar file
	 * @param pkgname package name of
	 * @param list all files in .jar file will be put into this list.
	 */
	private static void visitJar (File file, String pkgname, List<String> list) {

		ZipFile zip = null;
		try {
			zip = new ZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Enumeration<?> entryies = zip.entries();
		while (entryies.hasMoreElements()) {
			String name = ((ZipEntry) entryies.nextElement()).getName();
			if (name.endsWith(".class")) {
				name = name.replace('/', '.');
				name = name.substring(0, name.lastIndexOf('.'));
				if (name.startsWith(pkgname)) {
					list.add(name);
				}
			}
		}

		try {
			zip.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * get all classfile in the dir of given file.
	 * ��������
	 */
	private static void visitPath (File file, String pkgname, List<String> list) {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				if (!files[i].getName().endsWith(".class")) continue;
				String clsname = Stringx.leftback(files[i].getName(), ".class");
				list.add (pkgname + "." + clsname);
			}
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				String dirname = files[i].getName();
				visitPath(files[i], pkgname + "." + dirname, list);
			}
		}
	}

	//----------------- main() -----------------//
	public static void main(String[] args) {

		String[] ss = Classx.getClasses (test.Test1.class);
		String rt = Stringx.join(ss, "\n");
		System.out.println(rt + "\n" + ss.length);
		System.out.println(Classx.getPath(Classx.class));

//		System.out.println(new Date(2008-1900, 10-1, 15));
		Calendar cal = new GregorianCalendar();
		cal.set(2008, 12, 31);
		System.out.println(Datetime.datetime());

		Object o = Classx.getObject("java.util.Date",
			new Object[]{new Integer(2008-1900), new Integer(10-1), new Integer(15)});
		String datetime_s = DateFormat.getDateTimeInstance().format((Date)o);
		System.out.println(datetime_s);
	}

}
