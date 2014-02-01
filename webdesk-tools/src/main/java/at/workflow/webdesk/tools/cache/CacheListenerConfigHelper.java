package at.workflow.webdesk.tools.cache;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;

public class CacheListenerConfigHelper {
	/**
	 * tries to get cacheReplication Configs first from defaults, then 
	 * from webdesk.properties (from keys starting with webdesk.cacheReplication.)
	 * and then from the optional classpath file 'ehCacheListener.properties)
	 * 
	 * @return Properties to use
	 */
	public static Properties getCacheListenerProperties() {
		
		Properties ret = new Properties();
		
		Properties defaultProps = new Properties();
		try {
			defaultProps.load(new ClassPathResource("at/workflow/webdesk/tools/cache/defaultCacheReplicationConfig.properties").getInputStream());
		} catch (IOException e) {
			// ignore
		}
		copyPropertiesWhereKeysStartWith(defaultProps, ret, "webdesk.cacheReplication.listener." , true);
		
		Properties wdProps = new Properties();
		try {
			wdProps.load(new ClassPathResource("webdesk.properties").getInputStream());
		} catch (IOException e) {
			// ignore
		}
		copyPropertiesWhereKeysStartWith(wdProps, ret, "webdesk.cacheReplication.listener.", true );
		
		Properties specialProps = new Properties();
		try {
			specialProps.load(new ClassPathResource("ehCacheListener.properties").getInputStream());
		} catch (IOException e) {
			// ignore
		}
		
		copyPropertiesWhereKeysStartWith(specialProps, ret, null, false);
		
		return ret;
		
	}
	
	private static void copyPropertiesWhereKeysStartWith(Properties sourceProps, Properties destProps, String matchkey, boolean removeMatchKeyInDest) {
		for (Object key : sourceProps.keySet()) {
			if (matchkey==null || key.toString().startsWith(matchkey)) {
				String convertedKey = removeMatchKeyInDest?key.toString().replaceAll(matchkey, ""):key.toString();
				destProps.put(convertedKey, sourceProps.get(key));
			}
		}
	}
	
	public static String getCacheListenerPropertiesAsString() {
		String ret = "";
		Properties props = getCacheListenerProperties();
		for(Object key : props.keySet()) {
			if (!"".equals(ret)) {
				ret+=",";
			}
			ret += (key + "=" + props.get(key));
		}
		return ret;
	}
}
