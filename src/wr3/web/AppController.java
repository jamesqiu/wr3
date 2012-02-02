package wr3.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wr3.BaseConfig;
import wr3.HotClass;
import wr3.util.Classx;
import wr3.util.Datetime;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * 执行Controller的一个action,传入参数为params
 * @author jamesqiu 2008-12-28
 *
 */
public class AppController {

	private String controller;
	private String action;
	private Params params;
	private Session session;

	@SuppressWarnings("unused")
	private AppController() {
	}

	/**
	 * @param controller controller的名字,可以含package
	 * @param action action的名字
	 * @param params request参数集合
	 */
	public AppController(String controller, String action,
			Params params, Session session) {
		this.controller = controller;
		this.action = action;
		this.params = params;
		this.session = session;
	}

	/**
	 * 执行controller的action.
	 * @return 执行结果
	 */
	public Object run() {

		String classUrl = BaseConfig.appPath();
		String className = BaseConfig.appPackage() + "." + controller;
		Object o = HotClass.create(classUrl, className).object();

		Map<String, String> args = Appx.actionArgs(controller, action);

		if (args==null && Classx.getMethod(o, action)==null)
			return "--[wr3 AppController.run()]: " + this + "没有定义.";

		if (Classx.hasField(o, PARAMS_NAME)) {
			Classx.setField(o, PARAMS_NAME, params);
		}
		if (Classx.hasField(o, SESSION_NAME)) {
			Classx.setField(o, SESSION_NAME, session);
		}
		Object rt;
		if (args.size()==0) {
			rt = Classx.invoke(o, action);
		} else {
			Object[] argObjs = run_args(args);
			rt = Classx.invoke(o, action, argObjs);
		}

		return rt;
	}

	private Object[] run_args(Map<String, String> args) {

		List<Object> objs = new ArrayList<Object>();
		for ( Entry<String, String> arg : args.entrySet()) {
			String name = arg.getKey();
			String type = arg.getValue();
			String param = params.get(name);
			if (param != null) {
				Object o = run_args_wrap(param, type);
				objs.add(o);
			}
		}
		return objs.toArray();
	}

	/**
	 * <pre>
	 * 根据参数对象的内容及类型字符串，构造出非空对象。
	 * 可处理数字、字符串、日期，其他对象只能返回缺省构造函数（如果有）。
	 * </pre>
	 * @param param
	 * @param type
	 * @return
	 */
	private Object run_args_wrap(String param, String type) {
		if ("int".equals(type)) {
			return Numberx.toInt(param, 0);
		} else if ("long".equals(type)) {
			return Numberx.toLong(param, 0L);
		} if ("double".equals(type)) {
			return Numberx.toDouble(param, 0d);
		} else if ("java.lang.String".equals(type)) {
			return Stringx.s(param, "");
		} else if ("java.util.Calendar".equals(type)) {
			return run_args_wrap_date(param);
		} else if ("java.util.Date".equals(type)) {
			return Datetime.asDate(run_args_wrap_date(param));
		} else {
			return run_args_wrap_unhandle(param, type);
		}
	}

	private Calendar run_args_wrap_date(String param) {

		Calendar c0 = Datetime.calendar(1900, 1, 1);
		if (Stringx.nullity(param)) return c0;

		Calendar c = Datetime.parse(param);
		if (c==null) {
			System.out.println("--[wr3 AppController.run_args_wrap_date()]: " +
					"date format is *NOT* yyyy-MM-dd in " + this);
			return c0;
		}
		return c;
	}

	/**
	 * 返回一个缺省构造对象，以便 {@link #run()} 处理。
	 * @param param
	 * @param type
	 * @return
	 */
	private Object run_args_wrap_unhandle(String param, String type) {

		try {
			System.err.println("--[wr3 AppController.run_args_wrap_unhandle()]: '" +
					type + "' in " + this);
			return Classx.getClass(type).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return controller + "." + action + "()";
	}

	/**
	 * App中表示request参数集合的字段名称
	 */
	public static final String PARAMS_NAME = "params";
	/**
	 * App中表示session的字段名称
	 */
	public static final String SESSION_NAME = "session";
}
