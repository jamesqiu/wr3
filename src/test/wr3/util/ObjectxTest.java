package test.wr3.util;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Objectx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ObjectxTest {

	Person p1 = new Person();
	String name = "anonymous";
	int age = 30;
	String mail = "anonymous@mai.com";
	
	byte[] bytes;
	
	@Before
	public void init() {
		p1.name = name;
		p1.age = age;
		p1.mail = mail;		
		bytes = Objectx.toBytes(p1);
	}
	
	@Test
	public void testToBytes() {
		assertNotNull(bytes);
		assertTrue(bytes.length > 1);
	}

	@Test
	public void testToObject() {
		Person p2 = (Person) Objectx.toObject(bytes);
		assertTrue(p2 instanceof Person);
		assertEquals(name, p2.name);
		assertEquals(age, p2.age);
		assertEquals(mail, p2.mail);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(ObjectxTest.class.getName());
	}

}

class Person implements Serializable {
	
	private static final long serialVersionUID = 1L;

	String name = "james";
	int age = 18;
	String mail = "jamesqiu@msn.com";
	
}