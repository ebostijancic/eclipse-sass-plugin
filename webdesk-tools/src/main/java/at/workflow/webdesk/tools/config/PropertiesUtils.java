package at.workflow.webdesk.tools.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Convenience Class to get Access to the Properties within the Webdesk System outside of Spring
 * 
 * @author ggruber
 */
public class PropertiesUtils {
	
	private static final Logger logger = Logger.getLogger(PropertiesUtils.class);
	
	private static final String WEBDESK_PROPERTIES = "webdesk.properties";
	
	public static void saveWebdeskProperty(String key, String value) {
		Resource resource = new ClassPathResource("webdesk.properties");
		if (resource.exists()) {
			try {
				PropertiesConfiguration config = new PropertiesConfiguration(resource.getURL());
				config.setProperty(key, value);
				config.save();
			}
			catch (ConfigurationException e) {
				logger.warn("Problems saving a webdesk property (key=" + key + ",value=" + value + ")",e);
			}
			catch (IOException e) {
				logger.warn("Problems saving a webdesk property (key=" + key + ",value=" + value + ")",e);
			}
		}
	}

	
	private Properties properties = new Properties();
	private Properties webdeskProperties = new Properties();
	private Resource webdeskPropertiesResource;
	
	public PropertiesUtils() {
    	PropertiesLoader propertiesLoader = new PropertiesLoader();
    	webdeskPropertiesResource = new ClassPathResource(WEBDESK_PROPERTIES);
    	propertiesLoader.setLocations(new Resource [] { webdeskPropertiesResource });
    	propertiesLoader.setIgnoreResourceNotFound(true);
    	
    	try {
    		properties = propertiesLoader.mergeProperties();
    		webdeskProperties.load(webdeskPropertiesResource.getInputStream());
    		
    		final String logMessage = "Loaded "+WEBDESK_PROPERTIES+" by "+PropertiesUtils.class.getSimpleName()+" from "+webdeskPropertiesResource.getURL();
    		System.err.println(logMessage);
    		logger.info(logMessage);
    	}
    	catch (Exception e) {
    		logger.warn("Couldn't load "+WEBDESK_PROPERTIES+": "+e);
    	}
	}

	/** @return the merged Properties */
	public Properties getProperties() {
		return properties;
	}
	
	/** @return the Properties of the file ./WEB-INF/classes/webdesk.properties */
	public Properties getWebdeskProperties() {
		return webdeskProperties;
	}

	/** @returns Webdesk Properties Resource */
	public Resource getWebdeskPropertiesResource() {
		return webdeskPropertiesResource;
	}
	
}
