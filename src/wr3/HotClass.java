/**
 * 
 */
package wr3;

import java.io.FileNotFoundException;

import wr3.util.Classx;
import wr3.util.Filex;
import wr3.util.Stringx;

/**
 * <pre>
 * 从非classpath目录装载最新改动的class进行执行。
 * 类似实现：{@link GroovyConfig}
 * 
 * usage1: // 仅执行一个方法；
 * 	HotClass.create("classes/", "app.App1").invoke("m1");
 * 
 * usage2: // 得到对象后执行多个方法，或者多次方法
 * 	o = HotClass.create("test/", "c2").object();
 * 	Classx.invokeMethod("setName", new String[]{"jamesqiu"});
 * 	name = Classx.invokeMethod("getName");
 * 
 * </pre>
 * @author jamesqiu 2008-12-2
 */
public class HotClass implements ResourceInterface {

	/**
	 * 通过classUrl和className得到，
	 *		like: "c:/dev3/classes/app/App1.class", "./test/c2.class"
	 */
	private String filename;	
	private String classUrl;	// like: "c:/dev3/classes, "./test/"
	private String className;	// like: "app.A11p", "c2"
	
	
	private Class<?> hotclass; // 解析装载的结果

	private HotClass() {
		
	}
	
	/**
	 * contructor util.
	 * @return this, 接下来需执行load()，才可以取得对象或执行方法。
	 */
	public static HotClass create() {
		return new HotClass();
	}
	
	/**
	 * contructor util.
	 * @param filename 自定义标识，不是真正的文件名，类路径加类名，
	 *		like: "c:/dev3/classes/app.App1", "./classes/app.App1"
	 * @return this，已经装载好hotclass, 可以取得对象或执行方法。
	 */
	public static HotClass create(String classUrl, String className) {
		return create().load(classUrl, className);
	}
	
	/**
	 * 得到hotclass的一个无参instance
	 * @return
	 */
	public Object object() {
		
		if (hotclass==null) return null;
		
		return Classx.getObject(hotclass);		
	}
	
	/**
	 * 创建一个objcet实例并执行一次不带参数的方法
	 * @param methodName
	 * @return 方法返回值
	 */
	public Object invoke(String methodName) {
		
		return invoke(methodName, null);
	}
	
	/**
	 * 创建一个object实例并执行一次带参数的方法
	 * @param methodName
	 * @param args
	 * @return 方法返回值
	 */
	public Object invoke(String methodName, Object[] args) {
		
		if (hotclass==null) return null;
		
		Object o = Classx.getObject(hotclass);
		return Classx.invoke(o, methodName, args);
	}
	
	/**
	 * 调用此方法进行自动装载（变化则重新解析，不变则cache中取）
	 */
	public HotClass load(String classUrl, String className) {
		
		this.classUrl = classUrl;
		this.className = className;
		this.filename = filepath(classUrl, className);	// 使ResourceCache.create()能够获取文件名
		return (HotClass) (ResourceCache.create(this));
	}
	
	// 实现2个接口
	/**
	 * @see wr3.ResourceInterface#filename()
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * @see wr3.ResourceInterface#parse(java.lang.String)
	 *		like: "c:/dev3/classes/app.App1", "./classes/app.App1"
	 */
	public ResourceInterface parse() {
		
		if (!Filex.has(filename)) {
			new FileNotFoundException(Filex.fullpath(filename)).printStackTrace();
			return this;
		}

		System.out.printf("--[wr3 HotClass.parse()]: %s\n", Filex.fullpath(filename));
		
		hotclass = Classx.getHotClass(classUrl, className);

		ResourceCache.regist(this);	// 在ConfigCache做登记
		return this;
	}
	
	/**
	 * 通过classUrl和className得到filename
	 */
	public static String filepath(String classUrl, String className) {
		
		if (Stringx.nullity(classUrl) || Stringx.nullity(className)) 
			return null;

		return (classUrl+"/"+className.replace('.', '/')+".class");
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

	}

}
