package test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Id;

import wr3.model.Annotationx;
import domain.Person;

@AnnotationDefTest(value=0, name="Annotation测试")
public class AnnotationUseTest {

	@Id int id;
	
	@AnnotationDefTest(value=10)
	void m1() { }
	
	@AnnotationDefTest(name="qh")
	void m2() {}
	
	@AnnotationDefTest
	void m3() {}
	
	@AnnotationDefTest(value=108, name="jamesqiu") 
	int f1;
	
	/**
	 * 自定义annotation
	 */
	static void test1() {

		// 得到类的 annotation
		if (AnnotationUseTest.class.isAnnotationPresent(AnnotationDefTest.class)) {
			AnnotationDefTest a = AnnotationUseTest.class.getAnnotation(AnnotationDefTest.class);
			System.out.printf("%s: %d,%s\n", AnnotationUseTest.class.getName(), a.value(), a.name());			
		}
		
		// 得到方法的 annotation
		for (Method m : AnnotationUseTest.class.getDeclaredMethods()) {
			if (m.isAnnotationPresent(AnnotationDefTest.class)) {
				AnnotationDefTest a = m.getAnnotation(AnnotationDefTest.class);
				System.out.printf("%s: %d,%s\n", m.getName(), a.value(), a.name());
			}
		}
		
		// 得到field的 annotation
		for (Field f : AnnotationUseTest.class.getDeclaredFields()) {
			if (f.isAnnotationPresent(AnnotationDefTest.class)) {
				AnnotationDefTest a = f.getAnnotation(AnnotationDefTest.class);
				System.out.printf("%s: %d,%s\n", f.getName(), a.value(), a.name());
			}
		}
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		test1();
		
		Annotationx.info(Person.class);
		System.out.println("\n");
		Annotationx.info(Greeting.class);
	}
	
}
