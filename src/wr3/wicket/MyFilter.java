package wr3.wicket;

import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.protocol.http.ReloadingWicketFilter;

/**
 * ��̬����class�ı仯
 * @author jamesqiu 2009-7-1
 *
 */
public class MyFilter extends ReloadingWicketFilter {

	static {
		ReloadingClassLoader.includePattern("wr3.wicket.*");
		ReloadingClassLoader.includePattern("com.company.*");
		ReloadingClassLoader.excludePattern("com.company.spring.beans.*");
	}
}
