package wr3.table;

import java.util.ResourceBundle;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * 分页条。
 * <usage>
 *  
 *  pagebar = PageBar.create()
 *    .total(100)  // 供100条
 *    .page(4)     // 第3页（从91-100条）
 *    .max(30)     // 每页显示30条
 *    ;
 *  pagebar.html();
 *  i = pagebar.beginIndex();  // 开始条index
 *  j = pagebar.endIndex();    // 结束index
 *  
 * </usage>
 * @author jamesqiu 2009-9-7
 *
 */
public class PageBar {

	private int total;		// 所有记录条数
	private int page = 1;	// 当前页码
	private int max = 10; 	// 每页最多显示条数
	private String url;		// url的样子，否则用当前相对url
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle(PageBar.class.getName());
	
	private PageBar() {}
	
	public static PageBar create() {
		return new PageBar();
	}
	
	/**
	 * 记录总条数
	 * @param total
	 * @return
	 */
	public PageBar total(int total) {
		
		if (total < 0) total = 0;
		
		this.total = total;
		return this;
	}
	
	/**
	 * 当前页码，从1开始
	 * @param page, 小于1忽略不处理，位置在原来页面。
	 * @return
	 */
	public PageBar page(int page) {

		if (page >= 1) {
			this.page = page;
		}
		return this;
	}

	/**
	 * 每页显示条数
	 * @param max 小于1忽略不处理，使用原设置
	 * @return
	 */
	public PageBar max(int max) {
		
		if (max >= 1) {
			this.max = max;
		}
		return this;
	}
	
	/**
	 * url
	 * @param url
	 * @return
	 */
	public PageBar url(String url) {
		
		if (Stringx.nullity(url)) return this;
		
		this.url = url;
		return this;
	}
	
	/**
	 * 得到当前页面的开始记录的index，第1条index=1
	 * @return 如果页数越界，得到最后一条
	 */
	public int beginIndex() {
	
		int i = (page-1)*max + 1;
		if (i > total) i = total;
		return i;
	}
	
	/**
	 * 得到当前页面的结束记录的index，第1条index=1
	 * @return 如果页数越界，得到最后一条
	 */
	public int endIndex() {
		
		int j = page*max;
		if (j > total) j = total;
		return j;
	}
	
	/**
	 * 计算总共有多少页
	 * @return
	 */
	public int pages() {
		
		int p1 = total/max;
		int p2 = (total%max==0) ? 0 : 1;
		return p1 + p2;
	}
	
	/**
	 * 得到pagebar的html
	 * @return
	 */
	public String html() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(rs("div_start"));
		sb.append(prev());
		sb.append(links());
		sb.append(next());
		sb.append(info());
		sb.append(rs("div_end"));
		return sb.toString();
	}
	
	private String prev() {
		
		if (page==1 || pages()==1) return ""; // 第1页或只有1页的
		int prev_page = page-1;
		// 从越界页数往前翻的时候能直接到最后页。
		if (prev_page > pages()) prev_page = pages();		
//		return "<a class=\"pg-prev\" href=\"?page=" + (page-1) + "\"><上一页</a> ";
		return rs("prev", prev_page);
	}
	
	// 显示最多10个页链接
	private String links() {
		
		StringBuilder sb = new StringBuilder();
		int page0 = Numberx.safeRange(page - 5, 1, page);
		int page1 = Numberx.safeRange(page0 + 9, 1, pages());
		for (int i = page0; i <= page1; i++) {
			if (i == page) { // 当前页
//				sb.append("<strong>"+page+"</strong> ");
				sb.append(rs("links_current", page));
			} else { // 其他页
//				sb.append("<a href=\"?page="+ i +"\">"+i+"</a> ");
				sb.append(rs("links_other", i, i));
			}
		}
		return sb.toString();
	}
	
	private String next() {
		
		if (page==pages()) return ""; // 当前页是最后一页的
//		return "<a class=\"pg-next\" href=\"?page=" + (page+1) + "\">下一页></a> ";
		return rs("next", (page+1));
	}
	
	private String info() {
//		return Stringx.printf(
//			"<span class=\"pg-info\">共%d条..记录,分%d页(%d条/页)</span>", 
//			total, pages(), max);
		return rs("info", total, pages(), max);
	}
	
	private String rs(String key) {
		
		return resource.getString(key);
	}
	
	private String rs(String key, Object ... args) {
		
		return Stringx.printf(rs(key), args);
	}
	
	public String foo() {
		// TODO
		return url;
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		PageBar pagebar = PageBar.create()
					.page(2)
					.max(15)
					.total(32);
		System.out.println("pages=" + pagebar.pages());
		System.out.println("i0=" + pagebar.beginIndex());
		System.out.println("i1=" + pagebar.endIndex());
		System.out.println(pagebar.html());
		
		System.out.println(pagebar.resource.getString("test"));
	}
}
