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
 * �ӷ�classpathĿ¼װ�����¸Ķ���class����ִ�С�
 * ����ʵ�֣�{@link GroovyConfig}
 * 
 * usage1: // ��ִ��һ��������
 * 	HotClass.create("classes/", "app.App1").invoke("m1");
 * 
 * usage2: // �õ������ִ�ж�����������߶�η���
 * 	o = HotClass.create("test/", "c2").object();
 * 	Classx.invokeMethod("setName", new String[]{"jamesqiu"});
 * 	name = Classx.invokeMethod("getName");
 * 
 * </pre>
 * @author jamesqiu 2008-12-2
 */
public class HotClass implements ResourceInterface {

	/**
	 * ͨ��classUrl��className�õ���
	 *		like: "c:/dev3/classes/app/App1.class", "./test/c2.class"
	 */
	private String filename;	
	private String classUrl;	// like: "c:/dev3/classes, "./test/"
	private String className;	// like: "app.A11p", "c2"
	
	
	private Class<?> hotclass; // ����װ�صĽ��

	private HotClass() {
		
	}
	
	/**
	 * contructor util.
	 * @return this, ��������ִ��load()���ſ���ȡ�ö����ִ�з�����
	 */
	public static HotClass create() {
		return new HotClass();
	}
	
	/**
	 * contructor util.
	 * @param filename �Զ����ʶ�������������ļ�������·����������
	 *		like: "c:/dev3/classes/app.App1", "./classes/app.App1"
	 * @return this���Ѿ�װ�غ�hotclass, ����ȡ�ö����ִ�з�����
	 */
	public static HotClass create(String classUrl, String className) {
		return create().load(classUrl, className);
	}
	
	/**
	 * �õ�hotclass��һ���޲�instance
	 * @return
	 */
	public Object object() {
		
		if (hotclass==null) return null;
		
		return Classx.getObject(hotclass);		
	}
	
	/**
	 * ����һ��objcetʵ����ִ��һ�β��������ķ���
	 * @param methodName
	 * @return ��������ֵ
	 */
	public Object invoke(String methodName) {
		
		return invoke(methodName, null);
	}
	
	/**
	 * ����һ��objectʵ����ִ��һ�δ������ķ���
	 * @param methodName
	 * @param args
	 * @return ��������ֵ
	 */
	public Object invoke(String methodName, Object[] args) {
		
		if (hotclass==null) return null;
		
		Object o = Classx.getObject(hotclass);
		return Classx.invoke(o, methodName, args);
	}
	
	/**
	 * ���ô˷��������Զ�װ�أ��仯�����½�����������cache��ȡ��
	 */
	public HotClass load(String classUrl, String className) {
		
		this.classUrl = classUrl;
		this.className = className;
		this.filename = filepath(classUrl, className);	// ʹResourceCache.create()�ܹ���ȡ�ļ���
		return (HotClass) (ResourceCache.create(this));
	}
	
	// ʵ��2���ӿ�
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

		ResourceCache.regist(this);	// ��ConfigCache���Ǽ�
		return this;
	}
	
	/**
	 * ͨ��classUrl��className�õ�filename
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
