package wr3.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import wr3.util.Charsetx;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * <pre>
 * 包含其他绝对url结果的自定义tag, 可用在web或者非web环境。
 * 限制: 
 *   - 不支持输出非text结果(如image等2进制格式); 
 *   - 不支持相对url; 
 *   - 只支持1级结果.
 * usage:
 *  <@include url="${baseurl}/Test3/cross" />
 *  <@include url="http://www.google.cn" enc="UTF-8" />
 *  
 * </pre>
 * @see <@include_page path="../test.jsp"/>
 * @author jamesqiu 2009-11-26
 */
public class TemplateInclude implements TemplateDirectiveModel {

	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

		if (loopVars.length != 0) {
			throw new TemplateModelException(
					"This directive doesn't allow loop variables.");
		}
		
		String url = get(params, env, "url", false); // 绝对地址
		String enc = get(params, env, "enc", true);	 // 编码
		
		Writer out = env.getOut();
		out.write(text(url, enc));
	}
	
	@SuppressWarnings("unchecked")
	private String get(Map params, Environment env, String param_name, boolean optional) 
		throws TemplateException {
		
        // Determine the url
        final TemplateModel param0 = (TemplateModel)params.get(param_name);
        if (param0==null && optional) return null;
        if (param0==null) throw new TemplateException(
        		"Missing required parameter "+param_name, env);
       
        if (!(param0 instanceof TemplateScalarModel)) {
            throw new TemplateException("Expect scalar model, but is " + 
            		param0.getClass().getName(), env);
        }
        final String param1 = ((TemplateScalarModel)param0).getAsString();
        if (param1 == null) throw new TemplateException(
        		param_name + " parameter is null", env);
        
        return param1;
	}
	
	private String text(String url, String enc) throws IOException {
		
		URL url2 = new URL(url);
		InputStream is = url2.openStream();
		Filter1 filter = new Filter1();
		if (enc==null) enc = Charsetx.UTF;
		TextFile.create(filter).process(is, enc);
		String text = filter.toString();
		return text;
	}
	
	class Filter1 implements LineFilter {

		StringBuilder sb = new StringBuilder();
		public String process(String line) {
			sb.append(line).append('\n');
			return null;
		}
		
		@Override
		public String toString() {
			return sb.toString();
		}
	}
	
}
