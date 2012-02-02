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
 * App util ������:
 * usage:
 *   Template t = Appx.view(this); // �õ���ǰController��action��Ӧ��view
 *   filepath(this, ".groovy");    // �õ���ǰController��Ӧ���ļ�·����App1.groovy
 * </pre>
 * @author jamesqiu 2009-12-2
 */
public class Appx {

	/**
	 * <pre>
	 * �õ���ǰcontroller��action��Ӧ��template.
	 * ��: Cube#codes -->  Cube.codes.html
	 * </pre>
	 * @param clazz һ����getClass()
	 * @param action action�����֣�nullʱȡindex
	 * @return Ԥ����action��controller��webapp������Template
	 */
	public static Template view(Class<?> clazz, String action) {

		if (clazz==null) return null;

		if (action==null) action = "index";


		String filepath = filepath(clazz, "." + action + ".html");
		if (filepath==null) return null;

		// �磺"/f:/dev3/classes/app/Cube.codes.html"
		Template t = Template.create(filepath);
		// Ԥ���${action}, ${controller}������ֵ
		String controller = controller(clazz.getName());
		t.set("webapp", BaseConfig.webapp());
		t.set("controller", controller).set("action", action);

		return t;
	}

	/**
	 * @see #view(Class, String)
	 * @param clazz
	 * @param params �����ṩ��ǰ���õ�action
	 * @return
	 */
	public static Template view(Class<?> clazz, Params params) {

		if (params==null) return view(clazz, "index");

		return view(clazz, params.get("action"));
	}

	/**
	 * @see #view(Class, Params)
	 * @param o
	 * @param params �����ṩ��ǰ���õ�action
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
	 * ��Controller����ȡ��ǰ���õ�action,����ȱʡview Template
	 * @param o Controller����
	 * @return null ��û��"params"������ֵ
	 */
	public static Template view(Object o) {

		final String field = AppController.PARAMS_NAME; // "params";
		if (o!=null && Classx.hasField(o, field)) {
			return view(o, (Params)Classx.getField(o, field));
		}
		return null;
	}

	/**
	 * ��Controller����ȡaction,����ȱʡ.groovy�����ļ�
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
	 * �õ���controllerͬĿ¼ͬ����ͬ��׺���ļ���ȫ·��.
	 * �磺 ��Cube.java��action�е��� filepath(getClass(), ".groovy") -->
	 * 		"/f:/dev3/classes/app/Cube.groovy"
	 * @param clazz һ����getClass()
	 * @param postfix �ļ���׺
	 * @return �����ļ��������򷵻�null
	 */
	public static String filepath(Class<?> clazz, String postfix) {

		String filename = filename(clazz, postfix);
		return Filex.resource(clazz, filename); // // f:/dev3/classes/app/Cube.groovy
	}

	/**
	 * ����ΪController�е���filename(getClass(), ".groovy")
	 * @param clazz
	 * @param postfix
	 * @return �ļ�������Ҳ�����ļ���
	 */
	private static String filename(Class<?> clazz, String postfix) {

		if (clazz==null) return null;

		String filename = controller(clazz.getName()); // "app.Cube" -> "Cube"
		if (postfix!=null) filename = filename + postfix; // "Cube.groovy"
		return filename;
	}

	/**
	 * @see #filepath(Class, String)
	 * @param o һ����this
	 * @param postfix �磺".html"
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
	 * ͨ��javassist�õ�action�����Ĳ������Ƽ����ͣ���Ҫ��debug���룩
	 * @param controller
	 * @param action
	 * @return {name->type}, �޲�����action����{}
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
			CtMethod m = cls.getDeclaredMethod(methodname); // �����������һ��
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
			cls.detach(); // ����ж�£����ĺ�����Ȳ�����Ч
		} catch (NotFoundException e) {
			System.err.println(e);
			return null;
		}
		return map;
	}

}
