package test.wr3.util;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Classx;
import wr3.util.Datetime;
import wr3.util.Stringx;

public class ClassxTest {

	@Test
	public void getPath() {
		String path1 = Classx.getPath(String.class); // /E:/jdk16/lib/rt.jar
		String path2 = Classx.getPath(Classx.class); // /F:/dev3/classes/wr3/util
		
		assertTrue(path1.endsWith("rt.jar"));
		assertTrue(path2.endsWith("/classes/wr3/util"));
	}

	@Test
	public void getClasses() {
		String[] a1 = Classx.getClasses(String.class);
		String[] a2 = Classx.getClasses(Classx.class);
		assertTrue(a1.length>200);
		assertTrue(Stringx.in("java.lang.String", a1));
		assertTrue(a2.length>1);
		assertTrue(Stringx.in(Classx.class.getName(), a2));
	}

	@Test
	public void getObject_String() {
		Object o = Classx.getObject("java.util.Date");
		assertTrue(o.toString().endsWith(""+Datetime.year()));
	}
	
	@Test
	public void getObject_StringObjectArray() {
		Object o = Classx.getObject("java.util.Date", new Integer[]{1975-1900, 10-1, 15});
		assertTrue(o.toString().endsWith("1975"));
		assertTrue(o.toString().indexOf("Oct 15")>0);
		
	}

	@Test
	public void getHotObject_String() {
		Object o = Classx.getHotObject("./test", "c2");
		assertEquals("JamesQiu's age: 18", o.toString());
	}

	@Test
	public void getHotObject_StringObjectArray() {
		Object o = Classx.getHotObject("./test/", "c2", new Integer[]{25});
		assertEquals("JamesQiu's age: 25", o.toString());
		
	}

	@Test
	public void getMethods() {
		Object o = Classx.getHotObject("./test", "c2");
		
		Method[] ms = Classx.getMethods(o);
		assertTrue(ms.length>9);
		
		Method m1 = Classx.getMethod(o, "getName");
		assertEquals("getName", m1.getName());
		
		m1 = Classx.getMethod(o, "setName");
		assertNull(m1);
		
		// 方法参数为int
		m1 = Classx.getMethod(o, "setAge", new Class[]{int.class});
		assertNotNull(m1);
		assertEquals("setAge", m1.getName());
		// 判断返回类型为void
		assertEquals(void.class, m1.getReturnType());
	}
	
	@Test
	public void invokeMethod() {
		Object o = Classx.getHotObject("./test", "c2");
		Object rt1 = null;
		Object rt2 = null;
		// String getName(); 
		assertEquals("JamesQiu" , Classx.invoke(o, "getName"));

		// void setName(String), void setAge(int)
		rt1 = Classx.invoke(o, "setName", new String[]{"name1"});
		rt2 = Classx.invoke(o, "setAge", new Integer[]{5});
		assertNull(rt1);
		assertNull(rt2);
		assertEquals("name1", Classx.invoke(o, "getName"));
		assertEquals(5, Classx.invoke(o, "getAge"));		

		// void setAll(String, int)
		rt1 = Classx.invoke(o, "setAll", new Object[]{"all1", 100});
		assertNull(rt1);
		assertEquals("all1", Classx.invoke(o, "getName"));
		assertEquals(100, Classx.invoke(o, "getAge"));
		
		// void setDefault()
		rt1 = Classx.invoke(o, "setDefault");
		assertNull(rt1);
		assertEquals("defaultName", Classx.invoke(o, "getName"));
		assertEquals(0, Classx.invoke(o, "getAge"));		
	}
	
	final static int n = 1000;

//	@Test
	// 大概耗时：1700ms
	public void invokeSpeed1() {
		long t0 = System.currentTimeMillis();
		
		for (int i = 0; i < n; i++) {
			Object o = Classx.getHotObject("./test", "c2");
			Classx.invoke(o, "setAll", new Object[]{"name"+i, i});
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("speed1: " + (t1 - t0));
	}
	
	@SuppressWarnings("unchecked")
//	@Test
	// 大概耗时：30ms
	public void invokeSpeed2() {
		long t0 = System.currentTimeMillis();
		
		Class cls = Classx.getHotClass("./test", "c2");
		for (int i = 0; i < n; i++) {
			Object o = Classx.getObject(cls);
			Classx.invoke(o, "setAll", new Object[]{"name"+i, i});
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("speed2: " + (t1 - t0));
	}
	
//	@Test
	// 大概耗时：16ms
	public void invokeSpeed3() {
		long t0 = System.currentTimeMillis();
		
		Object o = Classx.getHotObject("./test", "c2");
		for (int i = 0; i < n; i++) {
			Classx.invoke(o, "setAll", new Object[]{"name"+i, i});
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("speed3: " + (t1 - t0));
	}
	
	@Test
	public void invokeMethod_StringString() {
		Object o = Classx.invoke("java.util.Date", "toLocaleString");
		// 极限条件下有可能不相等
		assertEquals(Datetime.datetime(), o.toString());
	}

	@Test
	public void invokeMethod_ObjectString() {
		Object o = Classx.invoke(new java.util.Date(), "toLocaleString");
		// 极限条件下有可能不相等
		assertEquals(Datetime.datetime(), o.toString());		
	}

	@Test
	public void invokeMethod_ObjectStringObjectArray() {
		Date date = new java.util.Date();
		Classx.invoke(date, "setYear", new Integer[]{1982-1900});
		assertTrue(date.toString().endsWith("1982"));		
		
	}

	@Test
	public void getField() {
		Object o = Classx.getHotObject("test", "c2");
		String s = Classx.getField(o, "name").toString();
		assertEquals("JamesQiu", s);
	}

	@Test
	public void setField() {
		Object o = Classx.getHotObject("test", "c2");
		Classx.setField(o, "age", new Integer(30));
		assertEquals("JamesQiu's age: 30", o.toString());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(ClassxTest.class.getName());
	}
}
