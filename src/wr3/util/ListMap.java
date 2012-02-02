package wr3.util;

import java.util.ArrayList;
import java.util.List;

import test.wr3.util.ListMapTest;

/**
 * <pre>
 * ����һ��id��List,�����������ʹ�õ�LinkedHashMap���ص㣺
 *  - ��ͨ��indexֱ�Ӷ�/дkey��val
 *  - ��ɾ��Ԫ��
 *  - ����key��val����
 *  - key, val ����Ϊnull
 *  
 * usage:
 *   nl = NamedList.create()
 *     .put("id", map)
 *     .put(101, map2)
 *     .key(0, "ID")
 *     .val(0, map3);
 *   nl.size(); // 2
 *   nl.key(0); // "ID"
 *   nl.val("ID"); // map 
 * </pre>
 * @author jamesqiu 2009-8-31
 * 
 * @see ListMapTest
 *
 */
public class ListMap {

	private List<Object> keys = new ArrayList<Object>();
	
	private List<Object> vals = new ArrayList<Object>();
	
	public ListMap() {}
	
	/**
	 * �õ�instance
	 * @return
	 */
	public static ListMap create() {
		return new ListMap();
	}
	
	/**
	 * ������{key, val}�ԣ����߸����Ѵ��ڵ�key��Ӧ��val��
	 * @param key
	 * @param val
	 * @return
	 */
	public ListMap put(Object key, Object val) {
		
		if (keys.contains(key)) {
			int i = keys.indexOf(key);
			vals.set(i, val);
		} else {
			keys.add(key);
			vals.add(val);			
		}
		
		return this;
	}
	
	/**
	 * �õ�ָ��λ�õ�key
	 * @param index
	 * @return
	 */
	public Object key(int index) {
		
		index = Numberx.safeIndex(index, size());
		return keys.get(index);
	}
	
	/**
	 * ���޸�ָ��index��key����Ӱ���Ӧ��val
	 * @param index
	 * @param newKey
	 * @return
	 */
	public Object key(int index, Object newKey) {
		
		index = Numberx.safeIndex(index, size());
		keys.set(index, newKey);
		return this;
	}
	
	/**
	 * �õ�ָ��λ�õ�val
	 * @param index
	 * @return
	 */
	public Object val(int index) {
		
		index = Numberx.safeIndex(index, size());
		return vals.get(index);
	}
	
	/**
	 * �õ�ָ��key��val
	 * @param key
	 * @return
	 */
	public Object val(Object key) {
		
		if (keys.contains(key)) {
			int i = keys.indexOf(key);
			return vals.get(i);
		} else {
			return null;
		}
	}
	
	/**
	 * ���޸�ָ��index��val
	 * @param index
	 * @param newVal
	 * @return
	 */
	public Object val(int index, Object newVal) {
		
		index = Numberx.safeIndex(index, size());
		vals.set(index, newVal);
		return this;
	}
	
	public int size() {
		
		return keys.size();
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder("{\n");
		for (int i = 0, n = size(); i < n; i++) {
			sb.append('<').append(keys.get(i)).append("> : <")
				.append(vals.get(i)).append('>').append("\n");
		}		
		sb.append("}");
		return sb.toString();
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		ListMap nl = ListMap.create()
			.put("id", null)
			.put(1, "hello")
			.put(101, 20)
			.put("id", "�滻")
			;
		
		for (int i = 0, n = nl.size(); i < n; i++) {
			
			nl.key(i, (""+nl.key(i)).toUpperCase());
		}
		
		System.out.println(nl);
	}
}
