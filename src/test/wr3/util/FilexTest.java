package test.wr3.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Filex;

public class FilexTest {

	@Test
	public void testResourceObjectString() {
		// 测试有空格和中文的情况
		String path = Filex.resource(this, "FilexTest 测试.txt");
		// /F:/dev3/classes/test/wr3/util/FilexTest 测试.txt
		assertTrue(path.endsWith("/FilexTest 测试.txt"));
	}

	@Test
	public void testResourceClassOfQString() {
		// 测试在jar文件中的情况
		String path = Filex.resource(String.class, "String.class");
		// file:/E:/jdk16/lib/rt.jar!/java/lang/String.class
		assertTrue(path.endsWith("rt.jar!/java/lang/String.class"));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(FilexTest.class.getName());
	}

}
