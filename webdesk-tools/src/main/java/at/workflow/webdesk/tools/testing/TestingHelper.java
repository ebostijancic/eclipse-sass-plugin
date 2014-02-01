package at.workflow.webdesk.tools.testing;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.tools.config.StartupPropertyProvider;

public final class TestingHelper {
	
	/** Configures log4j from ./at/workflow/webdesk/tools/testing/log4j.properties. */
	public static void configureLogging() {
		configureLogging("at/workflow/webdesk/tools/testing/log4j.properties");
	}
	
	/** Configures log4j from given log4j.properties or log4j.xml. */
	public static void configureLogging(String logFile) {
    	// init logger
        Resource logResources = new ClassPathResource(logFile);
        
        BasicConfigurator.resetConfiguration();
        
        if (logResources.getFilename().endsWith(".xml")) {
        	try {
				DOMConfigurator.configure(logResources.getURL());
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	Properties logProperties = new Properties();
        	try {
        		logProperties.load(logResources.getInputStream());
        	} catch (IOException e1) {
        		e1.printStackTrace();
        	}
        	PropertyConfigurator.configure(logProperties); 
        }
	}
	
	/** Creates or cleans HSQL-DB working directory in tmp-dir. Writes JDBC driver information to properties and returns them. */
	public static Properties provideTestHsqlDb() {
		String dir = StartupPropertyProvider.getHsqlWorkingDirectory() + System.currentTimeMillis();  // ggruber 02-11-2012: make sure to have unique dir -> temp dir is cleaned on hudson every night!
		dir = StartupPropertyProvider.cleanupHsqlWorkingDirectory(dir);
		return StartupPropertyProvider.getHsqlDbProperties(dir);
	}
	
	/** Clears caches. The same as ((CacheManager) applicationContext.getBean("CacheManager")).clearAll(). */
	public static void clearAllCaches(CacheManager cacheManager) {
		String[] cacheNames = cacheManager.getCacheNames();
		for (int i=0; i<cacheNames.length; i++) {
			Cache myCache = cacheManager.getCache(cacheNames[i]);
			myCache.removeAll();
		}
	}

	
	private TestingHelper()	{}	// do not instantiate

}
