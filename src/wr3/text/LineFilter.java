package wr3.text;

/**
 * <pre>
 * ���ı��������ӿڣ����TextFile�ı��ļ���������Regex.replaceByFilter()ʹ�á�
 * usage1:
 *   LineFilter lf = new LineSearch ("001");
 *   TextFile.create(lf).process("a.txt");
 * 
 * usage2: (ʹ��������)
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
 * @see wr3.text.TextFile	(�����ı��ļ�)
 * @see wr3.db.RowFilter		(�����ϵ�����ݿ��ÿһ����¼)
 * @see com.webreport.util.StdInputFilter	(�����׼����)
 * @see TextLineSearch	(���ҹؼ��ֵ�һ��ʵ��)
 * 
 */
public interface LineFilter {

	/**
	 * ����ÿһ��, ���ش�����(��{@link wr3.util.Regex#replaceByFilter(CharSequence, java.util.regex.Pattern, LineFilter)}��ʹ�õ�) 
	 */
	String process (String line);
	
	/**
	 * ���������Լ��Ĺ��췽���������������е��á�
	 */	
}
