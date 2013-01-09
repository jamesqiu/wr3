package test;

import static java.lang.System.out;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.persistence.Basic;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import wr3.bank.Areacode;
import wr3.util.Numberx;
import wr3.util.Stringx;
import domain.Person;

/**
 * temp use for simple and quick test
 * @author jamesqiu 2008-11-24
 *
 */
public class Test1 {

	/**
	 * 证书的保存
	 * @param key
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeKeyToFile(Key key, String fileName)
			throws IOException {

		byte[] key_byte = key.getEncoded();
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(key_byte);
		fos.close();
	}

	/**
	 * 从字节载入 RSA Private Key
	 * @param private_key_byte
	 * @return
	 * @throws Exception
	 */
	public static RSAPrivateKey loadRSAPrivateKey(byte[] private_key_byte)
		throws Exception {

		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(private_key_byte);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return (RSAPrivateKey) kf.generatePrivate(ks);
	}

	/**
	 * 从字节载入 RSA Public Key
	 * @param public_key_byte
	 * @return
	 * @throws Exception
	 */
	public static RSAPublicKey loadRSAPublicKey(byte[] public_key_byte)
		throws Exception {

		X509EncodedKeySpec ks = new X509EncodedKeySpec(public_key_byte);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) kf.generatePublic(ks);
	}

	//------------------------------------------------------------------//

	static void test1() {
		long t0 = System.currentTimeMillis();
		System.out.println("");
		int n = 10000;
		for (int i = 0; i < n; i++) {
			"/char2".matches("\\w+");
		}

		long t1 = System.currentTimeMillis();
		out.println(t1 - t0);
	}

	static void test2() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("k1", 1);
		map.put("k2", 3.1415);
		map.put("k3", 100L);
		map.put("k4", "hello cn中文");
		System.out.println(map);
		for (Iterator<Entry<String, Object>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Object> type = iterator.next();
			String key = type.getKey();
			Object val = type.getValue();
			if (Stringx.isString(val)) {
				out.println(key + " isString: " + val);
			} else if (Numberx.isNumber(val)) {
				out.println(key + " isNumber: " + val);
			} else {
				out.println("unknow");
			}
		}
	}

	static void test3() throws BackingStoreException {
		Preferences pref = Preferences.userNodeForPackage(Test1.class);
//		pref.put("hello", "为");
//		pref.put("world", "世界");
		pref.put("james", "邱");
		System.out.println(Stringx.join(pref.keys()));
		for (String key : pref.keys()) {
			System.out.println(key + ": " +  pref.get(key, null));
		}
		System.out.println(pref.name());
	}

	static void test4() {
		int[] a = new int[] {1,2,3,4};
		for (int i : a) {
			System.out.println(i);
		}

	}

	@SuppressWarnings({ "unchecked", "serial" })
	static void test5() {

		Map m = new HashMap<Integer, String>() {{
			put(1, "sdlkjf");
			put(2, "aaaaaaaaaa");
			put(300, "lksjdlfjsldf");
		}};
		System.out.println(m);
		for (Object i : m.entrySet()) {
			Entry<Integer, String> e = (Entry<Integer, String>)i;
			System.out.println(e.getKey() + "-->" + e.getValue());
		}

		List<?> l = Stringx.list(10, 30.5, "hello cn中文");
		System.out.println(l);
	}

	static <k,v> String test(Map<k,v> list) {

		return null;
	}

	static void test6() {

		System.out.println(isOdd(5));
		System.out.println(isOdd(-5));
		System.out.println(isOdd(0));
		System.out.println(isOdd(-2));
		System.out.println(isOdd(2));
	}

	// 测试是否奇数
	private static boolean isOdd(int i) {
		System.out.println(i);
		return (i & 1) == 1;
	}

	static void test7() {
		long l0 = 24*60*60*1000*1000L;
		long l1 = 24*60*60*1000;
		System.out.println(l0 + "/" + l1 + "=" + (l0/l1));
		System.out.println(12345+54321);

		char[] i = {'1','3', '5'};
		System.out.println("" + new String(i));

		System.out.println(Byte.MAX_VALUE);
		System.out.println(0 + 0x90);

		int j = 0;
		j = j++;
		System.out.println(j);
	}

	static <T> void print(T a) {
		System.out.println(a);;
	}

	static int sum(int ... ii) {

		int sum = 0;
		for (int i : ii) {
			sum += i;
		}
		return sum;
	}

	/**
	 * use "java -Dnodebug test.Test1" to turn off debug
	 * @param msg
	 */
	static void log(String msg) {

		Logger log = Logger.getLogger(Test1.class.getName());
		if (null!=System.getProperty("nodebug")) log.setLevel(Level.WARNING);
		log.info(msg);
	}

	static void className() {
		System.out.println(Test1.class.getName());
	}

	/**
	 * 输入n，打印1-n的排列组合，如：
	 * 1: 1
	 * 2: 12, 21
	 * 3: 312, 321, 132, 231, 123, 213
	 * @param n
	 * @return
	 */
	static List<String> P(int n) {

		if (n<=1) return Arrays.asList("1");

		List<String> Ln = new ArrayList<String>();
		List<String> Ln_1 = P(n-1);
		for (int i = 0; i < Ln_1.size(); i++) {
			for (int j = 0; j < n; j++) {
				String s0 = Ln_1.get(i);
				String s1 = s0.substring(0, j) + n + s0.substring(j);
				Ln.add(s1);
			}
		}
		return Ln;
	}

	/**
	 * 题目如下：一个六位数，分别用2，3，4，5，6乘它，
	 * 得到的五个新数仍是由原数中的六个数字组成，只是位置不同，则此六位数是多少？
	 */
	static void 小学奥数() {

	L: for (int i = 111111; i < 1000000/6; i++) {
			String si = set(i); 
			List<String> ss =
				Arrays.asList(set(i*2), set(i*3), set(i*4), set(i*5), set(i*6));
			for (String s : ss) {
				if (!s.equals(si)) continue L;
			}
			System.out.printf("i=%d (i*2=%d, i*3=%d, i*4=%d, i*5=%d, i*6=%d) \n",
					i, i*2, i*3, i*4, i*5, i*6);
		}
	}

	/**
	 * used by {@link #小学奥数()}
	 * 数n的所有数字的排序字符串，如：
	 * n = 23432, --> "22334"
	 * @param n
	 * @return
	 */
	private static String set(int n) {

		char[] ca = (""+n).toCharArray();
		Arrays.sort(ca);
		return new String(ca);
	}

	/**
	 * “足球*篮球=踢足球1”，在上面的乘法算式中，不同的汉字代表不同的数字，
	 * 相同的汉字代表相同的数字。其中，乘积是一个个位为1的四位数，
	 * 那么“足球”和“篮球”这两个两位数的和是多少？
	 * 69*39=2691
	 */
	static void 小学奥数2() {

		for (int i = 10; i < 100; i++) {
			for (int j = 10; j < 100; j++) {
				if (i*j<1001) continue;
				char z1 = (""+i).charAt(0);
				char q1 = (""+i).charAt(1);
				char l2 = (""+j).charAt(0);
				char q2 = (""+j).charAt(1);
				char t3 = (""+i*j).charAt(0);
				char z3 = (""+i*j).charAt(1);
				char q3 = (""+i*j).charAt(2);
				char c3 = (""+i*j).charAt(3);
				if (c3!='1') continue;
				if (z1==q1 || l2==q2 || t3==l2 || t3==z3 || t3==q3) continue;
				if ((z1==z3) && (q1==q2) && (q1==q3)) {
					System.out.printf("%d*%d=%d\n", i, j, i*j);;
				}
			}
		}
	}

	static void java_properties() {
		System.getProperties().list(out);
	}

	static void sys_env() {
		for (Map.Entry<String, String> e : System.getenv().entrySet()) {
			out.println(e.getKey() + ": " + e.getValue());
		}
	}

	/**
	 * 使用 generics 对 List 的元素逐个进行处理
	 * @param <T>
	 * @param L0
	 * @param filter
	 */
	@SuppressWarnings("unchecked")
	static <T> List<T> foreach(List<T> L0, Filter filter) {

		List<T> rt = new ArrayList<T>();
		for (int i = 0, n = L0.size(); i < n; i++) {
			rt.add((T) (filter.process(L0.get(i))));
		}
		return rt;
	}
	static interface Filter<T> {
		T process(T s);
	}

	// 测试在for中删除元素
	static void foreachList() {

		List<String> list = new ArrayList<String>(Arrays.asList("aa","bb","Aa","dd"));

		for (String s : new ArrayList<String>(list)) {
			if (s.equalsIgnoreCase("aa")) list.remove(s);
		}
		System.out.println("list=" + list);
	}

	/**
	 * 获取domain/下类的annotation定义
	 */
	static void annotationRead() {

		for (Field f : Person.class.getDeclaredFields()) {

			System.out.println(f.getName() + ":");
			// 得到所有annotation
			for (Annotation a : f.getAnnotations()) {
				System.out.printf("\tannotation: [%s]\n", a.annotationType().getName());
			}
			// 是否有特定的Annotation
			if (f.isAnnotationPresent(Basic.class)) {
				System.out.println("\t“Basic” annotation: " + f.getName());
			}
		}

	}

	static void temp() throws Exception {
		System.out.println(Areacode.areas2("11"));
		
	}
	
	static int indexOfAny(String str, char[] searchChars) {
		if (Stringx.nullity(str) || searchChars == null
				|| searchChars.length == 0) {
			return -1;
		}
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			for (int j = 0; j < searchChars.length; j++) {
				if (searchChars[j] == ch) {
					return i;
				}
			}
		}
		return -1;
	}
	
	static void img4j() throws IOException, InterruptedException, IM4JavaException {
		ConvertCmd cmd = new ConvertCmd(true);
		IMOperation op = new IMOperation();	
		op.addImage();
		op.resize(100, 40);
		op.addImage();
		cmd.setSearchPath("e:\\GraphicsMagick");
		cmd.run(op, "f:\\logo.jpg", "f:\\logo4.jpg");
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) throws Exception {

//		log("begin-->");
//		System.out.println(sum(1,3,5,6));
//		log("<--end");
		className();
		System.out.println(1<<3);

		// 打印1-n，n个数的排列组合
		int n = 4;
		List<String> rt = P(n);
		System.out.println(rt);
		System.out.println(rt.size());

		小学奥数();
		小学奥数2();

		// Python: [x.upper() for x in ["a","b","c"]]
		// Groovy: ["a","b","c"].collect { x-> x.toUpperCase() }
		List<String> L0 = Arrays.asList("a", "b", "c");
		List<String> L1 = foreach(L0,
				new Filter<String>() {
					public String process(String s) { return s.toUpperCase(); }
				});
		System.out.printf("%s-->%s\n", L0, L1);
		// Python: [x*x*x for x in [1,2,3,4,5]]
		// Groovy: [1,2,3,4,5].collect { x-> x*x*x }
		List<Integer> ii0 = Arrays.asList(1,2,3,4,5);
		List<Integer> ii1 = foreach(ii0,
				new Filter<Integer>() {
					public Integer process(Integer s) { return s*s*s; }
				});
		System.out.printf("%s-->%s\n", ii0, ii1);

		foreachList();

		annotationRead();

//		final int i1 = Integer.MAX_VALUE-1000;
//		final int i2 = Integer.MAX_VALUE;
		final int i1 = 0;
		final int i2 = 100;
		for (int i = i1; i < i2; i++) {
			if (Numberx.isPrime(i)) System.out.print(i + " ");
		}

		temp();
		System.out.println(indexOfAny("hello world", new char[]{'z','m','n'}));
		System.out.println("------- end --------c");
		
//		img4j();
	}

}
