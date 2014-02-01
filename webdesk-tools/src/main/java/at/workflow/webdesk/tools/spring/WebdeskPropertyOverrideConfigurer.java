package at.workflow.webdesk.tools.spring;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.Resource;

/**
 * See servicesConfig.xml, a PropertyOverrideConfigurer bean defined there
 * loads all webdesk.properties in all CLASSPATH roots.
 * This class is to log ANY webdesk.properties read, to be able to keep
 * control over what is configured (properties could overwrite each other). 
 * 
 * @author fritzberger 19.07.2013
 */
public class WebdeskPropertyOverrideConfigurer extends PropertyOverrideConfigurer
{
	private static final Logger logger = Logger.getLogger(WebdeskPropertyOverrideConfigurer.class);
	
	/** Overridden to log resource paths and then call super. */
	@Override
	public void setLocations(Resource[] locations) {
		for (Resource location : locations)
			logWebdeskProperties(location);
		
		super.setLocations(locations);
	}

	/** Overridden to log resource path and then call super. */
	@Override
	public void setLocation(Resource location) {
		logWebdeskProperties(location);
		
		super.setLocation(location);
	}
	
	private void logWebdeskProperties(Resource location) {
		try {
			final String logMessage = "Loaded "+location.getFile().getName()+" by "+WebdeskPropertyOverrideConfigurer.class.getSimpleName()+" (see servicesConfig.xml) from "+location.getURL();
			System.err.println(logMessage);
    		logger.info(logMessage);
		}
		catch (IOException e) {	// as this is called by Spring on application boot, we just report to stderr
			System.err.println("Could not report reading of webdesk.properties due to: "+e);
		}
	}
	
}
