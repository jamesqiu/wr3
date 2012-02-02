package test;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * JavaDoc�ɲ��ģ�
 * <a href="file:///F:/lib/vslick_src/google-collect-1.0/javadoc/index.html">
 * file:///F:/lib/vslick_src/google-collect-1.0/javadoc/index.html
 * </a>
 * <br/>
 * ���ӿɲ��ģ�
 * <a href="http://publicobject.com/2007/09/series-recap-coding-in-small-with.html">
 * http://publicobject.com/2007/09/series-recap-coding-in-small-with.html
 * </a>
 */
public class GcollectionTest {

	/**
	 * һ��key�����value
	 */
	void multimap() {
//		Multimap<Long, String> m1 = LinkedHashMultimap.create(); 
		Multimap<Long, String> m1 = ArrayListMultimap.create(); // ͬkey��value���ظ�
		m1.put(1L, "hello");
		m1.put(2L, "cn");
		m1.put(1L, "world");
		m1.put(2L, "����");
		m1.put(2L, "����");
		System.out.println("m1=" + m1);
		System.out.println(m1.get(2L));
	}
	
	/**
	 * �򻯴ӿɱ�List�����ɱ�List��2�δ���, ���� Collections#unmodifiableList
	 */
	void immutableList() {
//		List<Integer> list = Arrays.asList(1,3,5,7,9);
		ImmutableList<Integer> list = ImmutableList.of(1,3,5,7,9);
		int s = 0;
		for(Integer e: list) {
			s += e;
		}
		System.out.println(s);
	}
	
	/**
	 * ���� Exception ���÷�
	 */
	void preconditions() {
		int count = -20;
//		if (count <= 0) {
//			throw new IllegalArgumentException("must be positive: " + count);
//		}
		checkArgument(count > 0, "must be positive: %s", count);
	}
	
	/**
	 * �ж� Iterable �Ƿ����һ��ԭʼ��ȡ������
	 */
	void getOnlyElement() {
		Set<Integer> set = new LinkedHashSet<Integer>();
		set.add(101);
		System.out.println("onlyElement: " + Iterables.getOnlyElement(set));
	}
	
	/**
	 * ���� Iterable �����join (Iterators, Iteratables, arrays, varargs)
	 */
	void join() {
		String s = Joiner.on(" and ").join(Arrays.asList(1,3,5,7));
		System.out.println(s);
	}
	
	/**
	 * Set/List/Map �Ĵ���
	 */
	void factory() {
		
		// List
		List<Character> list = ImmutableList.of('L','i','s','t');
		System.out.println(list);
		
		// Set
		Set<Integer> set = ImmutableSet.of(1,3,5,7,9,7,5,3,1,0);
		System.out.println(set);
		
		// Map
		Map<Integer, String> map = ImmutableMap.of(10, "a", 2, "b", 3, "c", 4, "d", 5, "e");
		System.out.println(map);
	}
	
	/**
	 * �����ܴ�key��value��Ҳ�ܴ�value��key��map
	 */
	void bimap() {
		BiMap<Integer, String> map = ImmutableBiMap.of(1,"a",2,"b",3,"c");
		String s = map.get(2);
		int i = map.inverse().get("b");
		System.out.printf("BiMap: {%d, %s}\n", i, s);
	}
	
	void testAll() {
		
		multimap();
		immutableList();
		getOnlyElement();
		join();
		factory();
		bimap();
		
		preconditions();
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {

		System.out.println("Google Collection Test:");
		new GcollectionTest().testAll();
	}
	
}
