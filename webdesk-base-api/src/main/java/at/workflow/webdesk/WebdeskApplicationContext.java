package at.workflow.webdesk;

import org.springframework.context.ApplicationContext;

/**
 * Top Level Helper Class
 * 
 * to store and retrieve the Spring Application Context
 * is designed for 3rd Party Applications which are out of scope of
 * the spring IOC functions
 * f.i. custom Log4J Appenders, Shark Tool Agents, Shark Assigenment Manager, etc.
 * 
 * @author ggruber
 *
 */
public final class WebdeskApplicationContext {
	
	/**
	 * Wrapper around an ApplicationContext which exposes only
	 * the getBean(String beanName) method of ApplicationContext 
	 * and is used when passing the applicationContext to scripts.
	 * 
	 * See WDPTM-517.
	 * 
	 * @author ggruber 23.04.2013
	 */
	public static class ApplicationContextGetBeanAccessor  {
		
		public ApplicationContextGetBeanAccessor(ApplicationContext appCtx) {
			this.appCtx = appCtx;
		}
		
		private ApplicationContext appCtx;
		
		public Object getBean(String beanName) {
			return appCtx.getBean(beanName);
		}
	}
	
	
    private static ApplicationContext apctx;
    
    /**
     * gets the current Spring ApplicationContext
     * @return Spring Application Context
     */
    public static ApplicationContext getApplicationContext() {
        return apctx;
    }
    
    /**
     * sets the Spring ApplicationContext in order to be retrieved 
     * later via this static object
     * @param ctx ApplicationContext to set
     */
    public static void setApplicationContext(ApplicationContext ctx) {
        apctx = ctx;
    }

	/**
	 * @param beanName
	 * @return a bean with the the given <code>beanName</code> or null 
	 * if no bean with the given name was found
	 */
	public static Object getBean(String beanName) {
		if (apctx != null) 
			return apctx.getBean(beanName);
		return null; 
	}
	
	private WebdeskApplicationContext()	{}	// beware instantiation

}
