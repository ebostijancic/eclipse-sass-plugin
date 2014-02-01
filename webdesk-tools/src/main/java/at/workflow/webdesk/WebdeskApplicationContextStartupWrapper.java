package at.workflow.webdesk;


import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * purpose of this class is to be single entry for script based
 * reports to access webdesk domain objects, no matter if the report
 * engine is run inside webdesk servlet container or as a standalone
 * eclipse product. (with a minimal webdesk project in the classpath)
 * 
 * @author sdzuban
 *
 */
public class WebdeskApplicationContextStartupWrapper {
	
	private static Logger logger = Logger.getLogger(WebdeskApplicationContextStartupWrapper.class);

	private static ApplicationContext apctx;
	
	public static ApplicationContext getApplicationContext() {

		if (apctx==null) {
		
			if (WebdeskApplicationContext.getApplicationContext()!=null // not null
					&& WebdeskApplicationContext.getApplicationContext().containsBean("StartupListener"))  // initialisiert
				apctx = WebdeskApplicationContext.getApplicationContext();
			else {
				
	        	// init logger
	            Resource logResources = new ClassPathResource("at/workflow/webdesk/tools/failSafeLog4j.properties");
	            Properties logProperties = new Properties();
	            try {
		            logProperties.load(logResources.getInputStream());
		            PropertyConfigurator.configure(logProperties);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
				
	            if ("test".equals(System.getProperty("org.apache.cocoon.mode"))) {
	            	readProperties("at/workflow/webdesk/tools/failSafeWebdesk.properties");
	            } else {
	            	readProperties("webdesk.properties");
	            }
				
				logger.info("Starting application context");
		    	// setup Spring Beans
			    String[] xmlfile = {"classpath*:/at/workflow/webdesk/tools/failSafeApplicationContext.xml"};
				apctx = new ClassPathXmlApplicationContext(xmlfile, false);
				
		        ((ClassPathXmlApplicationContext) apctx).refresh();
		        
		        WebdeskApplicationContext.setApplicationContext(apctx);
		        
		        logger.info("Webdesk application context set up");
			}
		}
		return apctx;
	}
	
	public static Object getWebdeskBean(String beanName) {
		
		Object result = null;
		try {
			result = apctx.getBean(beanName);
			logger.debug("Instantiated " + beanName);
		} catch (Exception e) {
			logger.warn("Could not instantiate " + beanName, e);
		}
		return result;
	}
	
	private static void readProperties(String propertiesFileName) {
		
		Properties props = new Properties();
		try {
			InputStream in = new ClassPathResource(propertiesFileName).getInputStream();
			props.load(in);
		} catch (IOException e) {
			logger.error("Could not read " + propertiesFileName, e);
		}
		Enumeration<?> keys = props.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			System.setProperty(key, props.getProperty(key));
		}
	}
}
