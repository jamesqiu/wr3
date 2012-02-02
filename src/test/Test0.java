package test;

import wr3.util.Numberx;

/**
 * 做临时快速测试
 * @author jamesqiu 2010-11-9
 */
public class Test0 {

	public static void test1() {

		Object[] objects= new String[1];
		System.out.println(objects.length);
		System.out.println(objects[0]);
		objects[0]  = new Integer(5);
	}

	public static void main(String[] args) {

		String usage = "usage: Test 1|2|3|...";
		if (args.length==0) {
			System.out.println(usage);
			return;
		}
		int n = Numberx.toInt(args[0], -1);
		switch (n) {
		case 1:
			test1();
			break;
		default:
			System.out.println(usage);
			break;
		}
	}
}
