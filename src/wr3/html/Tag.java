package wr3.html;

public interface Tag {
	
	/**
	 * tag id <tag id=".." ...>...</tag>
	 */
	String id = null;
	/**
	 * 设置&lt;tag&gt;的id
	 * @param id
	 * @return
	 */
	public Tag id(String id);
	
	/**
	 * <pre>
	 * 得到html安全的&lt;tag ...&gt;...&lt;/tag&gt;字符串.
	 * </pre>
	 * @return
	 */
	public String html();
	
	
}
