package test.wr3.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Filex;

public class FilexTest {

	@Test
	public void testResourceObjectString() {
		// �����пո�����ĵ����
		String path = Filex.resource(this, "FilexTest ����.txt");
		// /F:/dev3/classes/test/wr3/util/FilexTest ����.txt
		assertTrue(path.endsWith("/FilexTest ����.txt"));
	}

	@Test
	public void testResourceClassOfQString() {
		// ������jar�ļ��е����
		String path = Filex.resource(String.class, "String.class");
		// file:/E:/jdk16/lib/rt.jar!/java/lang/String.class
		assertTrue(path.endsWith("rt.jar!/java/lang/String.class"));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(FilexTest.class.getName());
	}

}
