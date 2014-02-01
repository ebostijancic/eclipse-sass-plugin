package at.workflow.webdesk.tools.config;

import java.util.Iterator;
import java.util.Map;

public class StartupSystemPropertiesWriter {
	
	public StartupSystemPropertiesWriter(Map props) {
		
		Iterator itr = props.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String value = (String) props.get(key);
			System.setProperty(key, value);
		}
	}
}
