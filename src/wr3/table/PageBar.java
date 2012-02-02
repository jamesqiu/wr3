package wr3.table;

import java.util.ResourceBundle;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * ��ҳ����
 * <usage>
 *  
 *  pagebar = PageBar.create()
 *    .total(100)  // ��100��
 *    .page(4)     // ��3ҳ����91-100����
 *    .max(30)     // ÿҳ��ʾ30��
 *    ;
 *  pagebar.html();
 *  i = pagebar.beginIndex();  // ��ʼ��index
 *  j = pagebar.endIndex();    // ����index
 *  
 * </usage>
 * @author jamesqiu 2009-9-7
 *
 */
public class PageBar {

	private int total;		// ���м�¼����
	private int page = 1;	// ��ǰҳ��
	private int max = 10; 	// ÿҳ�����ʾ����
	private String url;		// url�����ӣ������õ�ǰ���url
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle(PageBar.class.getName());
	
	private PageBar() {}
	
	public static PageBar create() {
		return new PageBar();
	}
	
	/**
	 * ��¼������
	 * @param total
	 * @return
	 */
	public PageBar total(int total) {
		
		if (total < 0) total = 0;
		
		this.total = total;
		return this;
	}
	
	/**
	 * ��ǰҳ�룬��1��ʼ
	 * @param page, С��1���Բ�����λ����ԭ��ҳ�档
	 * @return
	 */
	public PageBar page(int page) {

		if (page >= 1) {
			this.page = page;
		}
		return this;
	}

	/**
	 * ÿҳ��ʾ����
	 * @param max С��1���Բ�����ʹ��ԭ����
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
	 * �õ���ǰҳ��Ŀ�ʼ��¼��index����1��index=1
	 * @return ���ҳ��Խ�磬�õ����һ��
	 */
	public int beginIndex() {
	
		int i = (page-1)*max + 1;
		if (i > total) i = total;
		return i;
	}
	
	/**
	 * �õ���ǰҳ��Ľ�����¼��index����1��index=1
	 * @return ���ҳ��Խ�磬�õ����һ��
	 */
	public int endIndex() {
		
		int j = page*max;
		if (j > total) j = total;
		return j;
	}
	
	/**
	 * �����ܹ��ж���ҳ
	 * @return
	 */
	public int pages() {
		
		int p1 = total/max;
		int p2 = (total%max==0) ? 0 : 1;
		return p1 + p2;
	}
	
	/**
	 * �õ�pagebar��html
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
		
		if (page==1 || pages()==1) return ""; // ��1ҳ��ֻ��1ҳ��
		int prev_page = page-1;
		// ��Խ��ҳ����ǰ����ʱ����ֱ�ӵ����ҳ��
		if (prev_page > pages()) prev_page = pages();		
//		return "<a class=\"pg-prev\" href=\"?page=" + (page-1) + "\"><��һҳ</a> ";
		return rs("prev", prev_page);
	}
	
	// ��ʾ���10��ҳ����
	private String links() {
		
		StringBuilder sb = new StringBuilder();
		int page0 = Numberx.safeRange(page - 5, 1, page);
		int page1 = Numberx.safeRange(page0 + 9, 1, pages());
		for (int i = page0; i <= page1; i++) {
			if (i == page) { // ��ǰҳ
//				sb.append("<strong>"+page+"</strong> ");
				sb.append(rs("links_current", page));
			} else { // ����ҳ
//				sb.append("<a href=\"?page="+ i +"\">"+i+"</a> ");
				sb.append(rs("links_other", i, i));
			}
		}
		return sb.toString();
	}
	
	private String next() {
		
		if (page==pages()) return ""; // ��ǰҳ�����һҳ��
//		return "<a class=\"pg-next\" href=\"?page=" + (page+1) + "\">��һҳ></a> ";
		return rs("next", (page+1));
	}
	
	private String info() {
//		return Stringx.printf(
//			"<span class=\"pg-info\">��%d��..��¼,��%dҳ(%d��/ҳ)</span>", 
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
