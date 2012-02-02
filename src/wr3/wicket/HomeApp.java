package wr3.wicket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application class for hello world example.
 * 
 * @author Jonathan Locke
 */
public class HomeApp extends WebApplication
{
	static {
		Logger logger = Logger.getLogger("org.apache.wicket");
		logger.setLevel(Level.WARNING);
	};
	
	/**
	 * Constructor.
	 */
	public HomeApp() { 
		mountBookmarkablePage("/user", UserEditPage.class);
		mountBookmarkablePage("/stateless", StatelessPage.class);
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<HomePage> getHomePage() {
		return HomePage.class;
	}
}
