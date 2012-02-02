package wr3.text;

/**
 * <pre>
 * 行文本过滤器接口，配合TextFile文本文件过滤器和Regex.replaceByFilter()使用。
 * usage1:
 *   LineFilter lf = new LineSearch ("001");
 *   TextFile.create(lf).process("a.txt");
 * 
 * usage2: (使用匿名类)
 *   LineFilter lf = new LineSearch () {
 *       public void process (String line) {...}
 *   };	
 *   TextFile.create(lf).process ("a.txt");
 *   
 * usage3:
 *   Text
 * 		
 * </pre>
 * 
 * @author jamesqiu 2007-7-29
 * @see wr3.text.TextFile	(处理文本文件)
 * @see wr3.db.RowFilter		(处理关系型数据库的每一条记录)
 * @see com.webreport.util.StdInputFilter	(处理标准输入)
 * @see TextLineSearch	(查找关键字的一种实现)
 * 
 */
public interface LineFilter {

	/**
	 * 处理每一行, 返回处理结果(在{@link wr3.util.Regex#replaceByFilter(CharSequence, java.util.regex.Pattern, LineFilter)}中使用到) 
	 */
	String process (String line);
	
	/**
	 * 可以增加自己的构造方法和其他方法进行调用。
	 */	
}
