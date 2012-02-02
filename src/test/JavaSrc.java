package test;

import java.io.File;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * ����.javaԴ�ļ����õ���������������
 * @author jamesqiu 2010-6-17
 */
public class JavaSrc {

	static void args() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnits1 = 
			fileManager.getJavaFileObjects(new File("JavaSrc.java"));
		for (JavaFileObject o : compilationUnits1) {
			System.out.println(o);
		}
	}
	
	/**
	 * ͨ��javassist�õ������Ĳ������ƣ���Ҫ��debug���룩
	 */
	static void javassist() {
		String classname = "test.JavaSrc";
		String methodname = "m2";
		try {
			CtClass cls = ClassPool.getDefault().get(classname);
			CtMethod m = cls.getDeclaredMethod(methodname);
			MethodInfo info = m.getMethodInfo();
			CodeAttribute attr = info.getCodeAttribute();
			LocalVariableAttribute tag = (LocalVariableAttribute) attr.getAttribute(LocalVariableAttribute.tag);
			CtClass[] types = m.getParameterTypes();
			int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1; 
			for (int i = 0; i < types.length; i++) {
				String s = tag.variableName(pos + i);
				String type = types[i].getName();
				System.out.println(s + ": " + type);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public double m2(String name, int age) {
		return 0.0d;
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		javassist();
	}
}
