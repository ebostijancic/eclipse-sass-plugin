package at.workflow.webdesk.tools.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderSupport;

public class PropertiesLoader extends PropertiesLoaderSupport {
	
	private Properties[] localProperties;
	private boolean localOverride;

	@Override
	public void setLocalOverride(boolean localOverride) {
		super.setLocalOverride(this.localOverride = localOverride);
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.localProperties = new Properties [] { properties };
		super.setProperties(properties);
	}

	@Override
	public void setPropertiesArray(Properties[] propertiesArray) {
		super.setPropertiesArray(this.localProperties = propertiesArray);
	}
	
	/** Overridden to do another kind of merge. TODO explain what and why this is done! */
	@Override
	public Properties mergeProperties() throws IOException {
		Properties result = new Properties();

		if (localOverride) {
			// Load properties from file upfront, to let local properties override.
			loadProperties(result);
		}

		if (localProperties != null) {
			for (int i = 0; i < localProperties.length; i++) {
				Properties props = localProperties[i];
				// Use propertyNames enumeration to also catch default properties.
				for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements();) {
					String key = (String) en.nextElement();
					result.setProperty(key, props.getProperty(key));
				}
			}
		}

		if (localOverride == false) {
			// Load properties from file afterwards, to let those properties override.
			loadProperties(result);
		}

		return result;
	}

}
