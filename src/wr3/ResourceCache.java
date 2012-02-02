package wr3;

import java.util.HashMap;
import java.util.Map;

import wr3.util.Filex;

/**
 * <pre>
 * 登记保存Resource文件的最后更改时间及对象实例.
 * 目的：
 * 1）运行中更改Resouce不用重启应用（配置更改，controller更改）；
 * 2）不花销不必要的Resource文件解析时间；
 * </pre>
 * @author jamesqiu 2008-11-27
 * @see ResourceInterface
 * @see GroovyConfig#load(String)
 * @see GroovyConfig#parse(String) 
 */
public class ResourceCache {

	// 保存Resource文件最后更新时间
	private static Map<String, Long> timestamps = 
		new HashMap<String, Long>();
	// 保存Resource文件解释装载后的可反复使用的对象
	private static Map<String, ResourceInterface>	cache = 
		new HashMap<String, ResourceInterface>();

	/**
	 * <pre>
	 * 根据传入的Resource对象，取文件时间戳进行判断，
	 * 新时间则装载（不登记，登记在实现的parse()方法中做），
	 * 旧时间则从cache取。
	 * 一般被GroovyConfig、BaseConfig、HotClass的load()方法调用。
	 * </pre>
	 * @param resource 先决条件：不为null且Resource文件无误
	 * @return 装载
	 */
	public static ResourceInterface create(ResourceInterface resource) {
		// 取标准文件名称
		String filename = Filex.fullpath(resource.filename());
		// 已登记且时间未变动，从cache中取
		if (timestamps.containsKey(filename)) {
			long t0 = timestamps.get(filename).longValue();
			long t1 = Filex.timestamp(filename);
			if (t0==t1) {
				return cache.get(filename);
			}
		}
		// 进行解析装载; 请在实现类的parse()中调用set()完成登记
		ResourceInterface o = resource.parse(); 
		return o;			
	}
	
	/**
	 * <pre>
	 * 登记文件更新时间及对象.
	 * 一般由具体实现的parse()方法在解析后调用, 
	 * 如：{@link GroovyConfig#parse(String)}
	 * </pre>
	 * @param filename 资源名称，将转换为标准文件名称进行注册
	 * @param resource
	 */
	public static void regist(ResourceInterface resource) {
		String fileid = Filex.fullpath(resource.filename());
		cache.put(fileid, resource);
		timestamps.put(fileid, Filex.timestamp(fileid));
	}
	
	/**
	 * 监控用，查看ResourceCache中登记的所有Resource
	 */
	public static void list() {
		for (String key : timestamps.keySet()) {
			System.out.printf("%s=%d\n", key, timestamps.get(key));
			System.out.printf("%s=%s\n\n", key, cache.get(key));
		}
	}
	
}
