package wr3.web;

import static wr3.util.Stringx.rightback;

import java.util.LinkedHashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import wr3.BaseConfig;
import wr3.text.Template;
import wr3.util.Classx;
import wr3.util.Filex;
import wr3.util.Stringx;

/**
 * <pre>
 * App util 辅助类:
 * usage:
 *   Template t = Appx.view(this); // 得到当前Controller和action对应的view
 *   filepath(this, ".groovy");    // 得到当前Controller对应的文件路径如App1.groovy
 * </pre>
 * @author jamesqiu 2009-12-2
 */
public class Appx {

	/**
	 * <pre>
	 * 得到当前controller和action对应的template.
	 * 如: Cube#codes -->  Cube.codes.html
	 * </pre>
	 * @param clazz 一般用getClass()
	 * @param action action的名字，null时取index
	 * @return 预设了action、controller和webapp变量的Template
	 */
	public static Template view(Class<?> clazz, String action) {

		if (clazz==null) return null;

		if (action==null) action = "index";


		String filepath = filepath(clazz, "." + action + ".html");
		if (filepath==null) return null;

		// 如："/f:/dev3/classes/app/Cube.codes.html"
		Template t = Template.create(filepath);
		// 预设好${action}, ${controller}变量的值
		String controller = controller(clazz.getName());
		t.set("webapp", BaseConfig.webapp());
		t.set("controller", controller).set("action", action);

		return t;
	}

	/**
	 * @see #view(Class, String)
	 * @param clazz
	 * @param params 用于提供当前调用的action
	 * @return
	 */
	public static Template view(Class<?> clazz, Params params) {

		if (params==null) return view(clazz, "index");

		return view(clazz, params.get("action"));
	}

	/**
	 * @see #view(Class, Params)
	 * @param o
	 * @param params 用于提供当前调用的action
	 * @return
	 */
	public static Template view(Object o, Params params) {
		return view(o.getClass(), params);
	}

	/**
	 * @see #view(Class, String)
	 * @param o
	 * @param action
	 * @return
	 */
	public static Template view(Object o, String action) {

		if (o==null) return null;
		return view(o.getClass(), action);
	}

	/**
	 * 从Controller对象取当前调用的action,返回缺省view Template
	 * @param o Controller对象
	 * @return null 若没有"params"对象域值
	 */
	public static Template view(Object o) {

		final String field = AppController.PARAMS_NAME; // "params";
		if (o!=null && Classx.hasField(o, field)) {
			return view(o, (Params)Classx.getField(o, field));
		}
		return null;
	}

	/**
	 * 从Controller对象取action,返回缺省.groovy配置文件
	 * @param o
	 */
	public static void config(Object o) {

//		final String field = "params";
//		if (o!=null && Classx.hasField(o, field)) {
//			return
//		}

		// TODO
	}

	/**
	 * 得到和controller同目录同名不同后缀的文件名全路径.
	 * 如： 在Cube.java的action中调用 filepath(getClass(), ".groovy") -->
	 * 		"/f:/dev3/classes/app/Cube.groovy"
	 * @param clazz 一般用getClass()
	 * @param postfix 文件后缀
	 * @return 对于文件不存在则返回null
	 */
	public static String filepath(Class<?> clazz, String postfix) {

		String filename = filename(clazz, postfix);
		return Filex.resource(clazz, filename); // // f:/dev3/classes/app/Cube.groovy
	}

	/**
	 * 在名为Controller中调用filename(getClass(), ".groovy")
	 * @param clazz
	 * @param postfix
	 * @return 文件不存在也返回文件名
	 */
	private static String filename(Class<?> clazz, String postfix) {

		if (clazz==null) return null;

		String filename = controller(clazz.getName()); // "app.Cube" -> "Cube"
		if (postfix!=null) filename = filename + postfix; // "Cube.groovy"
		return filename;
	}

	/**
	 * @see #filepath(Class, String)
	 * @param o 一般用this
	 * @param postfix 如：".html"
	 * @return
	 */
	public static String filepath(Object o, String postfix) {

		if (o==null) return null;

		return filepath(o.getClass(), postfix);
	}

	// app.Cube --> Cube
	private static String controller(String filename) {

		if (filename.indexOf('.')>0)
			filename = rightback(filename, "."); // "Cube"

		return filename;
	}

	/**
	 * 通过javassist得到action方法的参数名称及类型（需要带debug编译）
	 * @param controller
	 * @param action
	 * @return {name->type}, 无参数的action返回{}
	 */
	public static Map<String, String> actionArgs(String controller, String action) {

		Map<String, String> map = new LinkedHashMap<String, String>();

		String classname = BaseConfig.appPackage() + "." + controller; //"app.Actions";
		String methodname = action; //"m2";

		try {
			ClassPool pool = ClassPool.getDefault();
			String classpath = BaseConfig.appPath(); // 'f:/dev3/classes/'
			if (classpath.endsWith("/"))
				classpath = Stringx.sub(classpath, 0, -1); // 'f:/dev3/classes'
			pool.appendClassPath(classpath);

			CtClass cls = pool.get(classname);
			CtMethod m = cls.getDeclaredMethod(methodname); // 若多个，返回一个
			CodeAttribute attr = m.getMethodInfo().getCodeAttribute();
			LocalVariableAttribute tag = (LocalVariableAttribute) attr.getAttribute(LocalVariableAttribute.tag);
			CtClass[] types = m.getParameterTypes();
			int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;
			for (int i = 0; i < types.length; i++) {
				String s = tag.variableName(pos + i);
				String type = types[i].getName();
				map.put(s, type);
//				System.out.println(s + ": " + type);
			}
			cls.detach(); // 必须卸下，更改后才能热部署生效
		} catch (NotFoundException e) {
			System.err.println(e);
			return null;
		}
		return map;
	}

}
