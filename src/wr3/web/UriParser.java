package wr3.web;

import wr3.util.Regex;
import wr3.util.Stringx;

/**
 * <pre>
 * 按照Grails的缺省惯例解析url:
 * -----------------------------------------------------------------------------
 *                                  controller       id(optional)
 *                                 ------------      ---
 * http://localhost:8080/racetrack/registration/show/007
 *                       ---------              ----
 *            application context root          action(optional) default='index'
 * -----------------------------------------------------------------------------
 * 需要解析的uri如：
 * 	/registration
 * 	/registration/
 * 	/registration/list
 * 	/registration/list/
 * 	/registration/show/007
 *  /com.nasoft.wr3.Test1/show/001
 * 
 * usage:
 * 	urlparser = new UrlParser("/registration/show/007");
 * 	if (!urlparser.ok()) return;
 * 	urlparser.controller();	// registration
 * 	urlparser.action();		// show
 * 	urlparser.id();			// 007
 * </pre>
 * @author jamesqiu 2008-11-30
 *
 */
public class UriParser {

	private String[] result = new String[0]; // 解析结果
	
	@SuppressWarnings("unused")
	private UriParser() {}
	
	/**
	 * 生成一个uri解析器
	 * @param uri  
	 */
	public UriParser(String uri) {
		parse(uri);
	}
	
	/**
	 * 是否符合格式
	 * @return
	 */
	public boolean ok() {
		if (result.length==0 || result.length>3) return false;		
		return controllerOk() && actionOk() && idOk();
	}
	
	// TODO 考虑以 admin.user.Test 的形式提供子包
	private boolean controllerOk() {
		if (result.length>=1) {
//			return result[0].matches("\\w+");
			return Regex.isPackageName(result[0]);
		}
		return false;
	}
	
	private boolean actionOk() {
		if (result.length>=2) {
//			return result[1].matches("\\w+");
			return Regex.isVarName(result[1]);
		} else {
			return true;
		}
	}
	
	private boolean idOk() {
		if (result.length>=3) {
			return result[2].matches("\\w+");
		} else { 
			return true;
		}
	}
	
	/**
	 * get controller name
	 * @return
	 */
	public String controller() {
		if (result.length>=1) {
			return result[0];
		} else {
			return null;
		}
	}
	
	/**
	 * get action name
	 * @return
	 */
	public String action() {
		if (result.length>=2) {
			return result[1];
		} else {
			return "index";
		}
	}
	
	/**
	 * check if has id
	 * @return
	 */
	public boolean hasId() {
		return result.length==3;
	}
	
	/**
	 * get id string
	 * @return
	 */
	public String id() {
		if (hasId()) {
			return result[2];
		} else {
			return null;
		}
	}
	
	private void parse(String uri) {
		if (!Stringx.nullity(uri)) {
			if (uri.charAt(0)=='/') uri = uri.substring(1);
			result = uri.split("/");  // 注意："a/b/c", "a/b/c/" 的结果都是3（但"/a/b/c", "a//b/c"的结果是4）
		}
	}
	
}
