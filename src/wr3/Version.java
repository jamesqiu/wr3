package wr3;

import wr3.util.Stringx;

/**
 * <pre>
 * ��ѯ��ǰ�汾��ʹ��
 * java wr3.Version
 * </pre>
 * @author jamesqiu 2010-10-12
 */
public class Version {

	final static String version = "2010-10-12";
	final static String[] changelog = {
		"2010-10-12:\n����wr3.Version, ����scala��Stringx��װ.",
		""
	};

	public static String version() {

		return "------ WebReport 3 (wr3) " + version + " ------";
	}

	public static String changelog() {

		return Stringx.join(changelog, "\n");
	}

	public static void main(String[] args) {

		int argn = args.length;

		if (argn==0) {
			System.out.println(version());
			return;
		}

		if (argn==1 && Stringx.in(args[0].toLowerCase(), new String[]{"change", "changelog"})) {
			System.out.println(version());
			System.out.println(changelog());
			return;
		}
	}
}
