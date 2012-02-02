package wr3.wicket;

import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.protocol.http.ReloadingWicketFilter;

/**
 * 动态重载class的变化
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
