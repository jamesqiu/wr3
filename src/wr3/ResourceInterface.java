package wr3;

/**
 * <pre>
 * ���Ը�����Դ�ļ����������ڽ���ת�ص�Resource�ӿ�.
 * 
 * Resourceһ��������2�࣬
 * 1) Config���ã���ϵͳ�����н��и��Ķ����������ģ�������Ϊ�����ã�
 *    �� BaseConfig.properties, config.groovy��
 * 2) Service����ָ�ɵ���ִ�еķ��������.class, .groovy, .bsh
 * 
 * ʵ�ָýӿڵ����У�
 * {@link BaseConfig} {@link GroovyConfig} {@link HotClass}, {@link HotGroovy}
 * </pre>
 * @author jamesqiu 2008-11-27
 */
public interface ResourceInterface {

	/**
	 * �õ�Resource�ļ����ļ�·��
	 * @return
	 */
	public String filename();
	
	/**
	 * <pre>
	 * ����װ��Resource�ļ�����ʵ���������ܿ����ϴ󣩣����磺
	 * load .properteis�ļ�������groovy configslurper����url����Class
	 * 
	 * Implements usage:
	 * 	1) ��ӡ��Ϣ
	 *	   System.out.printf("--[wr3 xxx.parse(): %s\n", filename);
	 * 	2) ���н���
	 * 	3) ConfigCache.set(filename, this); // ��ConfigCache���Ǽ�
	 * </pre>
	 * @return
	 */
	public ResourceInterface parse();
}
