package wr3.html;

public interface Tag {
	
	/**
	 * tag id <tag id=".." ...>...</tag>
	 */
	String id = null;
	/**
	 * ����&lt;tag&gt;��id
	 * @param id
	 * @return
	 */
	public Tag id(String id);
	
	/**
	 * <pre>
	 * �õ�html��ȫ��&lt;tag ...&gt;...&lt;/tag&gt;�ַ���.
	 * </pre>
	 * @return
	 */
	public String html();
	
	
}
