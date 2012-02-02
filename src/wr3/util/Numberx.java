package wr3.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import test.wr3.util.NumberxTest;
/**
 * Number (int, Double) handle utilities.
 * <pre>
 * usage:

	// 常用
	Numberx.random(10);								// int[10]随机数列(0~100)
	Numberx.getSafeIndex (4, 3);					// return 1
	Numberx.getSafeRange (10, -2, 3); 				// return 3
	Numberx.getNumberString (new Double(10.343)); 	// "10.343"
	Numberx.getInt ("-234d", -100);					// -100

	// 方便计算
	Numberx.power (2, 4); 							// 16
	Numberx.sum ({0.1, 0.3, 0.6});					// 1.0
	Numberx.radio ({0.2, 0.3});						// {0.4, 0.6}
	Numberx.hasZero ({0.1, 4.0, 0, 1.2});			// true
	Numberx.zeros ({2, 5, 0, 4, 0});				// {1,1,0,1,0}
	Numberx.toDouble ({2, 5, 0, 4});				// {2.0, 5.0, 0.0, 4.0}
	Numberx.toInt ({2.3, 5.8, 0.0, 4.0});			// {2, 5, 0, 4}
	Numberx.multiply ({5.0, 5.0}, {0.1, 0.9})		// {0.5, 4.5}
	Numberx.adjustSum ({2,3,5}, {0,4,5})			// {0, (40+20*3/8), (50+20*5/8)}
	Numberx.adjustRound ({3.3, 3.3, 3.4}, 0);		// {4, 3, 3}
	Numberx.isDigit ("234324");						// true

	// Excel 行列号
	Numberx.Excel2Ints ("AB30");					// {27, 29}
	Numberx.AZ2Int ("AB");  						// 27
	Numberx.Int2AZ (27);							// "AB"

	// 判断类型
	Numberx.isNumber(object);

	// 整数各位数字
	Numberx.digits(2008);							// [2,0,0,8]
	Numberx.digitSet(-2008);						// [0,2,8]

 *  </pre>
 * @see NumberxTest
 * @see Stringx, Datetime
 */
public class Numberx {

	/**
	 * 得到一个[0, 100]范围的随机整数
	 * @return
	 */
	public static int random() {
		return random(100);
	}

	/**
	 * 得到一个[0, max]范围的随机整数
	 * @param max
	 * @return
	 */
	public static int random(int max) {
		return new Random().nextInt(max + 1);
	}

	/**
	 * 得到个数为n的一系列[0, 100]范围的随机整数
	 * @param n 随机数列的长度
	 * @return 随机数列, 如: random(5) = [20, 0, 75, 3, 100]
	 */
	public static int[] randoms(int n) {

		return randoms(n, 100);
	}

	/**
	 * 得到个数为n的一系列[0, max]范围的随机整数
	 * @param n
	 * @param max
	 * @return
	 */
	public static int[] randoms(int n, int max) {

		if (n < 0) n = 0;
		if (max <= 0) max = 1;

		int[] rt = new int[n];
		Random random = new Random();
		for (int i = 0; i < n; i++) {
			rt[i] =random.nextInt(max + 1);
		}
		return rt;
	}

	/**
	 * <pre>
	 * 把数组index限定在安全范围内[0, array.length-1]
	 * return index in range [0, length)
	 * condition: length > 0;
	 * 例如：
	 * -1 --> 最后一个(array.length-1),
	 * -2 --> 倒数第2个(array.length-2)
	 * </pre>
	 * @param index 任意int值
	 * @param length 数组的大小（array.length）, 必须大于0
	 * @param [0, length-1]范围内的数字
	 */
	public static int safeIndex (int index, int length) {

		if (length<1) return 0;

		index %= length;	// 限定到-(length-1),-1,0...(length-1)
		if (index<0) index += length;
		return index;
//		return (index % length) + (index < 0 ? length : 0);
	}

	/**
	 * 把给定值n限定在指定区间[min, max]
	 * return int value in range [min, max]
	 */
	public static int safeRange (int n, int min, int max) {

		if (n < min) return min;
		if (n > max) return max;
		return n;
	}

	/**
	 * 得到一个连续的整数序列[from, to]
	 * @see {@link #range(int, int, int)}
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<Integer> range(int from, int to) {
		return range(from, to, 1);
	}

	/**
	 * 得到一个连续的整数序列[from, to]
	 * @param from 开始
	 * @param to 结束
	 * @param step 可正可负
	 * @return
	 */
	public static List<Integer> range(int from , int to, int step) {

		List<Integer> rt = new ArrayList<Integer>();

		if ( (step==0) || ((from>to) && (step>0)) || ((from<to) && (step<0)) )
			return rt;

		if (from < to) {
			for (int i = from; i <= to; i+=step) {
				rt.add(i);
			}
		} else {
			for (int i = from; i >= to; i+=step) {
				rt.add(i);
			}
		}
		return rt;
	}

	/**
	 * return formated string of a double.
	 * -10.0 --> -10
	 * -10.53 --> -10.53
	 */
	public static String toString (Double number) {

		if (number == null) return "";

        return toString(number.doubleValue());
	}

	/**
	 * return formated string of a double.
	 * -10.0 --> -10
	 * -10.53 --> -10.53
	 */
	public static String toString (double number) {

		return toString(number, "#.########");
	}

	/**
	 * return formated string of a double.
	 * @param number
	 * @param formatPattern 形如: "#.##"
	 * @return
	 */
	public static String toString(double number, String formatPattern) {

		if (Double.isNaN(number)) return "NaN";

		NumberFormat formatter = new DecimalFormat (formatPattern);
		String string = formatter.format (number);

		return string;
	}

	/**
	 * safe version of Integer.parseInt ();
	 * return transfered int value of given valid string, default int value for invalid string.
	 */
	public static int toInt (String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	public static long toLong(String s, long defaultValue) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	/**
	 * get double from string.
	 * @param s double string to parse.
	 * @param defaultValue default value if exception.
	 * @return double
	 */
	public static double toDouble (String s, double defaultValue) {
		try {
			return Double.parseDouble(s);
		} catch (Exception e) { // NumberFormatException, NullPointerException, ..
			return defaultValue;
		}
	}

	/**
	 * 把double转换为占比, 如: 1.03 -> 103%, 3.14159 -> 314.16%, 0.0350 -> 3.5%
	 * @param d
	 * @return
	 */
	public static String toPercent (double d) {
		return toString(d, "#.##%");
	}
	/**
	 * alias of {@link #toPercent(double)}
	 * @param d
	 * @return
	 */
	public static String percent(double d) { return toPercent(d); }

	/**
	 * return n**m,
	 * this is to compute integer, Math.pow() is to compute double.
	 */
	public static int power (int n, int m) {
		int rt = 1;
		for (int i = 0; i < m; i++) {
			rt *= n;
		}
		return rt;
	}

	/**
	 * @return sum of int[] array
	 * @param values 待计算的int[]
	 */
	public static double sum (double[] values) {
		if (values == null) return 0;
		double rt = 0.0;
		for (int i = 0; i < values.length; i++) {
			rt += values[i];
		}
		return rt;
	}

	/**
	 * 计算int[]中每个元素的占比
	 */
	public static double[] radio (double[] values) {
		if (values == null) return null;

		double sum = sum (values);
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = (sum==0) ? Double.NaN : (values[i]/sum);
		}
		return rt;
	}

	/**
	 * 判断是否有值为0的元素
	 */
	public static boolean hasZero (double[] values) {
		if (values==null) return false;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0.0) return true;
		}
		return false;
	}

	/**
	 * 把所有非零元素设为1
	 * @param values like {2, 5, 0, 4, 0}
	 * @return {1, 1, 0, 1, 0}
	 */
	public static double[] zeros (double[] values) {
		if (values==null) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = values[i]==0 ? 0.0 : 1.0;
		}
		return rt;
	}

	/**
	 * 把int[]强制转换为double[]
	 */
	public static double[] toDouble (int[] values) {
		if (values==null) return null;
		double[] rt = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			rt[i] = (double) values[i];
		}
		return rt;
	}

	/**
	 * 把double[]强制转换为int[]
	 */
	public static int[] toInt (double[] values) {
		if (values==null) return null;
		int[] rt = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			rt[i] = (int) values[i];
		}
		return rt;
	}

	/**
	 * 值（double[]）和系数（double[]）相乘
	 */
	public static double[] multiply (double[] values, double[] radios) {
		if (values==null || radios==null) return null;
		if (values.length != radios.length) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = values[i] * radios[i];
		}
		return rt;
	}

	/**
	 * 总分值调平，调平结果的sum值 == 基准值的sum值，例如：
	 * bases：20%，30%，50%
	 * values: 0%, 40%, 50%
	 * -->
	 * return: 0%, (40+20*3/8)%, (50+20*5/8)%
	 *
	 * @param v0 基准值
	 * @param v1 实际值
	 * @return 调整值
	 */
	public static double[] adjustSum (double[] v0, double[] v1) {

		if (v0==null || v1==null) return null;
		if ((v0.length != v1.length) || !hasZero(v1))
			return v1;

		int n = v0.length;
//		double sum = sum (v1);
		double sum1 = sum(v0) - sum(v1);
		double[] zeros = zeros (v1);
		double[] v2 = multiply(v0, zeros);
		double[] radio = radio (v2);
		double[] rt = new double[n];
		for (int i = 0; i < n; i++) {
			rt[i] = v1[i] + sum1 * radio[i];
		}
		return rt;
	}

	/**
	 * 四舍五入调平。double[]每个值四舍五入后余数调平到某个指定值上，例如：
	 * {3.3, 3.3, 3.4} -->
	 * {3, 3, 4}
	 * @param index 将被调平的那个值的index
	 * 验证：Math.round (未调平之和) = 调平之和
	 */
	public static double[] adjustRound (double[] values, int index) {
		if (values==null) return null;
		int n = values.length;
		double[] rt = new double[n];
		double sum0 = Math.round(sum (values));
		for (int i = 0; i < n; i++) {
			rt[i] = Math.round(values[i]);
		}
		double sum1 = sum (rt);
		if (sum0 != sum1) {
			double delta = sum0 - sum1;
			int safeIndex = safeIndex(index, values.length);
			rt[safeIndex] += Math.round (delta);
		}

		return rt;
	}

	/**
	 * 判断是否表示整数的数字字符串，不含小数点和"-"符号
	 */
	public static boolean isDigit (String value) {
		if (value==null) return false;
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) return false;
		}
		return true;
	}

	/**
	 * 判断Object是否为Number类型
	 * @param o 待判断类型的对象
	 * @return
	 */
	public static boolean isNumber(Object o) {
		if (o==null) return false;
		return o.getClass().getSuperclass()==Number.class;
	}



	/**
	 * 把Excel的单元格位置标识转换为int(col, row)。
	 * 规则：第一个必须是字母，第二个是字母或者数字，之后的全是数字
	 * A1->(0,0), IV3->(255,2)
	 */
	public static int[] excel2Ints (String code) {

		int[] rt = new int[]{-1,-1};
		if (code==null || code.length()<2) {
			System.err.println("Numberx.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}
		char[] chars = code.toCharArray();
		if (!Character.isLetter(chars[0])) {
			System.err.println("Numberx.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}
		int colLen = Character.isLetter(chars[1]) ? 2 : 1;
		String col = code.substring(0, colLen);
		String row = code.substring(colLen);

		if (!isDigit(row)) {
			System.err.println("Numberx.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}

		int col_i = AZ2Int(col);
		int row_i = toInt(row, 0);
		if (row_i==0) {
			System.err.println("Numberx.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}

		return new int[]{col_i, row_i-1};
	}

	/**
	 * 把Excel的列号(A-ZZ)转换为数值。
	 * A->0, B->1, Z->25, AA->26, IV->255
	 */
	private static int RANGE = 'Z'-'A'+1;	 // 26
	public static int AZ2Int (String AZ) {

		if (AZ == null || (AZ.length() != 1 && AZ.length() != 2)) {
			System.err.println("Numberx.AZtoInt() 列代号不符合要求：" + AZ);
			return -1;
		}
		AZ = AZ.toUpperCase();
		if (AZ.length() == 1) {
			return (AZ.charAt(0)-'A');
		}
		char c0 = AZ.charAt(0);
		char c1 = AZ.charAt(1);
		return ((c0-'A'+1)*RANGE + (c1-'A'));
	}

	/**
	 * 把数值(0-255)转换为Excel的列号(A-ZZ)。
	 * 0->A, 1->B, 25->Z, 26->AA, 255->IV
	 */
	public static String int2AZ (int i) {

		if (i < 0 || i > 255) {
			System.err.println("列标号出界[0-255]：" + i);
			return null;
		}
		if (i < RANGE) {
			return "" + (char)('A' + i);
		} else {
			return ("" + (char)('A' + i/RANGE - 1) + (char)('A' + i%RANGE));
		}
	}

	/**
	 * 得到一个整数的各位数字, 2008 -> [2,0,0,8]
	 * @param argv
	 */
	public static int[] digits(int n) {
		n = Math.abs(n);
		char[] chars = ("" + n).toCharArray();
		int[] digits = new int[chars.length];
		for (int i = 0; i < chars.length; i++) {
			digits[i] = chars[i] - '0';
		}
		return digits;
	}

	/**
	 * 得到一个整数的各位数字的有序Set, 2008 -> [0,2,8]
	 * @param n
	 * @return
	 */
	public static Set<Integer> digitSet(int n) {

		int[] digits = digits(n);
		Set<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < digits.length; i++) {
			set.add(digits[i]);
		}
		return set;
	}

	/**
	 * 得到整数的二进制表示字符串，负整数带符号。
	 * @param n
	 * @return 如：9 -> "1001"
	 */
	public static String binary(int n) {

		return bin(n);
	}

	/**
	 * 判断是否质数（素数）
	 * @param i
	 */
	public static boolean isPrime(int n) {

		if (n<=1) return false;
		if (n==2 || n==3 || n==5 || n==7) return true;
		if (n%2==0 || n%3==0 || n%5==0 || n%7==0) return false; // 把大部分非质数先筛掉
		for (int i = 7; i*i>0 && i*i<=n; i+=2) { // 比使用sqrt速度快
			if (n%i==0) return false;
		}
		return true;
	}

	/**
	 * 把一个10进制整数表示为16进制小写
	 * @param n
	 * @return
	 */
	public static String hex(int n) {
		return Integer.toString(n, 16);
	}

	/**
	 * 把一个10进制整数表示为2进制
	 * @param n
	 * @return
	 */
	public static String bin(int n) {
		return Integer.toString(n, 2);
	}

	/**
	 * 把16进制表示（不以0x开头）转换为10进制整数
	 * @param hex
	 * @return
	 */
	public static int hex2i(String hex) {
		try {
			return Integer.parseInt(hex, 16);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * 把2进制表示（不以0x开头）转换为10进制整数
	 * @param bin
	 * @return
	 */
	public static int bin2i(String bin) {
		try {
			return Integer.parseInt(bin, 2);
		} catch (Exception e) {
			return -1;
		}
	}

	//----------------- main() -----------------//
	public static void main(String argv[]) {

		String[] ss = {
			//"A1", "B3", "AA1", "IV52342", "zZ1024"
			" A1", "112", "A-3", "Azz3", "AZ-2.3"
		};
		for (int i = 0; i < ss.length; i++) {
			System.out.println(Arrays.asList(excel2Ints(ss[i])));
		}

		System.out.println(toString(new Double(-10.3343)));
		System.out.println(toString(new Double(-10.0)));
	} // main ()

} // class
