package at.workflow.webdesk.test.integration;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.cocoon.configuration.Settings;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.cfg.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.WebdeskApplicationContextStartupWrapper;
import at.workflow.webdesk.tools.config.StartupListener;
import at.workflow.webdesk.tools.config.StartupPropertyProvider;


/**
 * This class tests the WebdeskApplicationContextStartupWrapper which can be used to
 * get the Webdesk ApplicationContext. This test only works inside the Workflow network,
 * as it relies on a MySQL-DB server running on host webdesk3.
 * 
 * FIXME: change this test or WebdeskApplicationContextStartupWrapper to have no more dependencies
 * on the webdesk3 host!
 * 
 * @author sdzuban
 */
public class WTestAppCtxStartupTest extends TestCase {

	Logger logger = Logger.getLogger(this.getClass());
	
	public void testGetApplicationContext() {
		
    	// init logger
        Resource logResources = new ClassPathResource("at/workflow/webdesk/tools/testing/log4j.properties");
        Properties logProperties = new Properties();
        try {
			logProperties.load(logResources.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        PropertyConfigurator.configure(logProperties); 
		
		logger.debug("Calling StartupWrapper");
		
		System.setProperty("org.apache.cocoon.mode", "test");
		System.setProperty("webdesk.useEmbeddedDatabase", "true");
		System.setProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED, "true");
		
		ApplicationContext ctx = WebdeskApplicationContextStartupWrapper.getApplicationContext();
		assertNotNull("No application context delivered", ctx);
		
		logger.debug("StartupWrapper finished");
		
		Object poService = ctx.getBean("StartupListener");
		assertNotNull("No service provided", poService);
		assertTrue("Wrong service provided", poService instanceof StartupListener);
		
		logger.debug("Checking properties");
		
		Settings settings = (Settings) ctx.getBean("org.apache.cocoon.configuration.Settings");
		assertNotNull("No hibernate dialect", settings.getProperty(Environment.DIALECT));
		
		logger.debug("Application context test successful");
	}
}
