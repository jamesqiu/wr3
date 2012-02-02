package wr3;

import java.util.HashMap;
import java.util.Map;

import wr3.util.Filex;

/**
 * <pre>
 * �ǼǱ���Resource�ļ���������ʱ�估����ʵ��.
 * Ŀ�ģ�
 * 1�������и���Resouce��������Ӧ�ã����ø��ģ�controller���ģ���
 * 2������������Ҫ��Resource�ļ�����ʱ�䣻
 * </pre>
 * @author jamesqiu 2008-11-27
 * @see ResourceInterface
 * @see GroovyConfig#load(String)
 * @see GroovyConfig#parse(String) 
 */
public class ResourceCache {

	// ����Resource�ļ�������ʱ��
	private static Map<String, Long> timestamps = 
		new HashMap<String, Long>();
	// ����Resource�ļ�����װ�غ�Ŀɷ���ʹ�õĶ���
	private static Map<String, ResourceInterface>	cache = 
		new HashMap<String, ResourceInterface>();

	/**
	 * <pre>
	 * ���ݴ����Resource����ȡ�ļ�ʱ��������жϣ�
	 * ��ʱ����װ�أ����Ǽǣ��Ǽ���ʵ�ֵ�parse()������������
	 * ��ʱ�����cacheȡ��
	 * һ�㱻GroovyConfig��BaseConfig��HotClass��load()�������á�
	 * </pre>
	 * @param resource �Ⱦ���������Ϊnull��Resource�ļ�����
	 * @return װ��
	 */
	public static ResourceInterface create(ResourceInterface resource) {
		// ȡ��׼�ļ�����
		String filename = Filex.fullpath(resource.filename());
		// �ѵǼ���ʱ��δ�䶯����cache��ȡ
		if (timestamps.containsKey(filename)) {
			long t0 = timestamps.get(filename).longValue();
			long t1 = Filex.timestamp(filename);
			if (t0==t1) {
				return cache.get(filename);
			}
		}
		// ���н���װ��; ����ʵ�����parse()�е���set()��ɵǼ�
		ResourceInterface o = resource.parse(); 
		return o;			
	}
	
	/**
	 * <pre>
	 * �Ǽ��ļ�����ʱ�估����.
	 * һ���ɾ���ʵ�ֵ�parse()�����ڽ��������, 
	 * �磺{@link GroovyConfig#parse(String)}
	 * </pre>
	 * @param filename ��Դ���ƣ���ת��Ϊ��׼�ļ����ƽ���ע��
	 * @param resource
	 */
	public static void regist(ResourceInterface resource) {
		String fileid = Filex.fullpath(resource.filename());
		cache.put(fileid, resource);
		timestamps.put(fileid, Filex.timestamp(fileid));
	}
	
	/**
	 * ����ã��鿴ResourceCache�еǼǵ�����Resource
	 */
	public static void list() {
		for (String key : timestamps.keySet()) {
			System.out.printf("%s=%d\n", key, timestamps.get(key));
			System.out.printf("%s=%s\n\n", key, cache.get(key));
		}
	}
	
}
