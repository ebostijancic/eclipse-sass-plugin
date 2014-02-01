package at.workflow.webdesk.po.impl.util;

import org.apache.cocoon.configuration.Settings;
import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoRuntimeException;

/**
 * Helper Class with gives various infos about the
 * state and configuration details of the cocoon
 * system.
 * 
 * 
 * @author ggruber
 *
 */
public class CocoonHelper {
	
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    
    private Settings settings;
    private final static String CONTINUATION_TIMEOUT="org.apache.cocoon.continuations.time-to-live";

    public String getLocationOfJs(String functionName) {
        if (functionName.length()>3) {            

            StringBuffer sb = new StringBuffer();
            if (functionName.startsWith("custom_")) {
                sb.append("custom://custom/actions/");
            } else {
        		sb.append("resource://at/workflow/webdesk/"); // root folder
            	if (functionName.indexOf("_")>0)
            		sb.append(functionName.substring(0, functionName.indexOf("_"))); // package folder
            	
                sb.append("/actions/");                
            }
            sb.append(functionName.substring(functionName.indexOf("_")+1,functionName.length()));
            sb.append("/");
            sb.append(functionName.substring(functionName.indexOf("_")+1,functionName.length()));
            sb.append(".js");
            
            return sb.toString();
        } else
            throw new PoRuntimeException("Function name is too short");       
    }
    
    
    public int getContTimeToLive() {
        return new Integer(this.settings.getProperty(CONTINUATION_TIMEOUT, "3600000")).intValue();
    }

	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
}
