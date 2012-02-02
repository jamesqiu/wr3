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
	 * 得到类的绝对目录, 或者所在jar文件绝对目录, 不含类文件名
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
		if (res==null) { // jar文件目录
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
	 * 得到和class同目录同名不同后缀的资源类路径.
	 * 如： filepath(getClass(), ".ftl") --> "wr3/util/Classx.ftl"
	 * @param cls 一般用 getClass()
	 * @param postfix 后缀如 ".ftl", ".properties", 注意别漏了"."
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
	 * 此方法屏蔽系统类和非系统类得到ClassLoader的不同.
	 * @param cls
	 * @return
	 */
	public static ClassLoader loader(Class<?> cls) {
		ClassLoader rt = cls.getClassLoader();
		return (rt==null) ? ClassLoader.getSystemClassLoader() : rt;
	}

	/**
	 * <pre>
	 * 得到一个jar包中或者classpath下的资源的url;
	 * 注意：cls为系统类时，name不能是非系统类，选cls越接近用户层越好。
	 *       不在classpath下的资源找不到；
	 * </pre>
	 * @param cls 一般调用getClass()即可,
	 * 		或在static方法中直接填本类名如Filex.class。
	 * @param name 例如："wr3/table/FormTable.ftl", "wr3/Table.class"
	 *             跟路径是class loader "/", 所以不能再写 "/"
	 * @return
	 * @see #inputStream(Class, String)
	 */
	public static URL url(Class<?> cls, String name) {
		// 用loader转载不以"/"开始，用class装载可以"/"开始（绝对路径）
		return loader(cls).getResource(name);
	}

	/**
	 * @see Filex#resource(Class, String)
	 * @see #url(Class, String)
	 * @param cls 一般用getClass(), 或者如 Classx.class
	 * @param name 和 {@link #url(Class, String)} 的name不一样，可以是绝对或者相对路径
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
	 * 得到一个jar包中或者classpath下的资源的InputStream
	 * @param cls 一般调用getClass()即可。
	 * @param name 例如："wr3/table/FormTable.ftl", "wr3/Table.class"
	 * @return
	 * @see Classx#url(Class, String)
	 */
	public static InputStream inputStream(Class<?> cls, String name) {

//		return url(cls, name).openStream();
		return loader(cls).getResourceAsStream(name);
	}

	/**
	 * 从classpath的系统资源(如lib/pinyin.jar)中得到InputStream
	 * @param name 路径如: "pinyin/pinyin.txt"
	 * @return
	 */
	public static InputStream inputStream(String name) {

		if (name==null) return null;

		//return ClassLoader.getSystemResourceAsStream(name);
		// jamesqiu 2011-7-27, 上面的用法在WebfirstCore中可能返回nill，下面的写法更robust
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		
	}

	/**
	 * 取某类路径下的所有类名称(含子路径)。
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
		// load updated class, 增加Classx.class.getClassLoader(), 保证URLClassLoader先搜原来的路径
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
	 * 通过类名字得到类实例。
	 * @param className like "java.util.Date", must in classpath.
	 * @return 对象实例
	 */
	public static Object getObject (String className) {

		return getObject(className, null);
	}

	/**
	 * get object with name and constructor params, from classpath
	 * 通过类名字得到类实例。
	 * @param className className className string like "java.util.Date"
	 * @param initParams 构造方法参数, like
	 * 	new Object[]{new Integer(2008), new Integer(12), new Integer(31)}
	 * @return
	 */
	public static Object getObject (String className, Object[] initParams) {

		return getObject (getClass(className), initParams);
	}

	/**
	 * 通过Class，得到无参数的构造Object
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
	 * 通过Class和构造参数，得到构造Object
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
	 * 目的：实现在运行时转载最新类（该类必须不在classpath中的）。
	 * 构造方法不带参数。
	 * @param classUrl new classpath like: "./classes/", "."; can *NOT* be ""
	 * @param className like: "c2", "app.App1"
	 * @return
	 */
	public static Object getHotObject (String classUrl, String className) {

		return getHotObject(classUrl, className, null);
	}

	/**
	 * get object with url string and constructor params, from *OUT* of classpath.
	 * 目的：实现在运行时转载最新类（该类必须不在classpath中的）。
	 * 构造方法不带参数。
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
	 * 得到一个对象的所有public方法。
	 * </pre>
	 * @param o
	 * @return 至少有java.lang.Object的9个public方法
	 */
	public static Method[] getMethods(Object o) {

		if (o==null) return null;

		return o.getClass().getMethods();
	}

	/**
	 * 得到java.lang.Object对象的所有public方法名(9个).
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
	 * 得到一个对象指定名称的public方法（无参数）。
	 * @param o
	 * @param name 无参public方法名
	 * @return null 如果没有这个public方法
	 */
	public static Method getMethod(Object o, String name) {

		return getMethod(o, name, null);
	}

	/**
	 * 得到一个对象指定名称的public方法（无参数）。
	 * @param o
	 * @param name 无参public方法名
	 * @param paramTypes public方法的参数类型
	 * @return null 如果没有这个public方法
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
	 * Utility用法，执行一个指定名称的类的无参数方法。
	 * 推荐使用:
	 * 	{@link #invoke(Object, String)},
	 * 	{@link #invoke(Object, String, Object[])}
	 * usage:
	 *   ClassUtil.invokeMethod("java.util.Date", "toLocaleString");
	 * </pre>
	 * @param className 对象名称，必须在classpath内
	 * @param methodName 对象的方法名
	 * @return 方法执行返回值
	 */
	public static Object invoke (String className, String methodName) {

		return invoke(getObject(className), methodName);
	}

	/**
	 * 执行一个对象的无参数方法。
	 * usage:
	 *   ClassUtil.invokeMethod(date1, "toString");
	 * @param o 对象
	 * @param methodName 对象的方法名
	 * @return 方法执行返回值
	 */
	public static Object invoke(Object o, String methodName) {
		return invoke (o, methodName, null);
	}

	/**
	 * 执行一个对象的方法。
	 * usage:
	 *   ClassUtil.invokeMethod(date1, "setYear", new Object[]{100});
	 * @param o 对象
	 * @param methodName 对象的方法名
	 * @param args 方法的参数
	 * @return 方法执行返回值
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
	 * 执行public的static方法
	 * @param cls 类名，如"java.lang.Math"
	 * @param method 方法名，如"abs"
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
	 * 执行非Public的方法，可用于JUnit中测试非Public方法。
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
			// 得到所有定义了的public和非public方法
			Method method = o.getClass().getDeclaredMethod(methodName, types);
			// 设置为可以访问
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
	 * 判断对象是否有指定名称的field.
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
	 * 得到对象的field值，field是public和protected的。
	 * @param obj 对象实例
	 * @param fieldName field名称
	 * @return 对象的值，需要自行转换类型
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
	 * 设置对象的field值，field可以是private和protected的。
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
		// 把基本类型对应的包裹类转为基本类型
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
	 * 迭代调用
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
