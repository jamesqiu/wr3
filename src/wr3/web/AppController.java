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
 * ִ��Controller��һ��action,�������Ϊparams
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
	 * @param controller controller������,���Ժ�package
	 * @param action action������
	 * @param params request��������
	 */
	public AppController(String controller, String action,
			Params params, Session session) {
		this.controller = controller;
		this.action = action;
		this.params = params;
		this.session = session;
	}

	/**
	 * ִ��controller��action.
	 * @return ִ�н��
	 */
	public Object run() {

		String classUrl = BaseConfig.appPath();
		String className = BaseConfig.appPackage() + "." + controller;
		Object o = HotClass.create(classUrl, className).object();

		Map<String, String> args = Appx.actionArgs(controller, action);

		if (args==null && Classx.getMethod(o, action)==null)
			return "--[wr3 AppController.run()]: " + this + "û�ж���.";

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
	 * ���ݲ�����������ݼ������ַ�����������ǿն���
	 * �ɴ������֡��ַ��������ڣ���������ֻ�ܷ���ȱʡ���캯��������У���
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
	 * ����һ��ȱʡ��������Ա� {@link #run()} ����
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
	 * App�б�ʾrequest�������ϵ��ֶ�����
	 */
	public static final String PARAMS_NAME = "params";
	/**
	 * App�б�ʾsession���ֶ�����
	 */
	public static final String SESSION_NAME = "session";
}
