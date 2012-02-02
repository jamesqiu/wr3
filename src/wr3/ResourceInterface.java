package wr3;

/**
 * <pre>
 * 可以根据资源文件最后更新日期进行转载的Resource接口.
 * 
 * Resource一般有如下2类，
 * 1) Config配置，在系统运行中进行更改而不需重启的，都可认为是配置，
 *    如 BaseConfig.properties, config.groovy；
 * 2) Service程序，指可调用执行的服务程序如.class, .groovy, .bsh
 * 
 * 实现该接口的类有：
 * {@link BaseConfig} {@link GroovyConfig} {@link HotClass}, {@link HotGroovy}
 * </pre>
 * @author jamesqiu 2008-11-27
 */
public interface ResourceInterface {

	/**
	 * 得到Resource文件的文件路径
	 * @return
	 */
	public String filename();
	
	/**
	 * <pre>
	 * 解析装载Resource文件的真实操作（可能开销较大），例如：
	 * load .properteis文件，解析groovy configslurper，从url创建Class
	 * 
	 * Implements usage:
	 * 	1) 打印信息
	 *	   System.out.printf("--[wr3 xxx.parse(): %s\n", filename);
	 * 	2) 进行解析
	 * 	3) ConfigCache.set(filename, this); // 在ConfigCache做登记
	 * </pre>
	 * @return
	 */
	public ResourceInterface parse();
}
