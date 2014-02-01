package at.workflow.webdesk.po.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.tools.JSTools;
import at.workflow.tools.SimpleObjectFactory;
import at.workflow.webdesk.tools.VelocityUtil;
import at.workflow.webdesk.tools.api.JSExecutionContext;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.WebdeskApplicationContext.ApplicationContextGetBeanAccessor;

public class PoScriptingServiceImpl implements PoScriptingService, ApplicationContextAware {

	private ApplicationContext applicationContext;

	// Java-Script

	/** {@inheritDoc} */
	@Override
	public JSExecutionContext createContext(Map<String, ?> variableValues) {
		return JSTools.createContext(variableValues);
	}
	
	/** {@inheritDoc} */
	@Override
	public JSExecutionContext createContext(Object variableValuesHoldingBean) {
		return JSTools.createContext(variableValuesHoldingBean);
	}

	/** {@inheritDoc} */
	@Override
	public Object executeJS(String javaScript, JSExecutionContext variableValues) {
		return JSTools.executeJS(javaScript, variableValues);
	}
	
	/** {@inheritDoc} */
	@Override
	public Object executeJS(String javaScript, Map<String, Object> variableValues) {
		if (variableValues == null)
			variableValues = new HashMap<String, Object>();
		
		variableValues.put("appCtx", new ApplicationContextGetBeanAccessor(applicationContext));
		return JSTools.executeJS(javaScript, variableValues);
	}
	
	
	// Velocity

	private VelocityUtil velocityUtil = null;
	private final String LOGGER_NAME = PoScriptingServiceImpl.class.getName();

	@Override
	public synchronized String velocitySubstitution(String template, Map<String, Object> placeholderValues) {
		if (velocityUtil == null) {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("appCtx", new ApplicationContextGetBeanAccessor(applicationContext));
			context.put("DateTools", DateTools.class);
			context.put("DateUtils", DateUtils.class);
			context.put("StringUtils", StringUtils.class);			
			context.put("simpleObjectFactory", new SimpleObjectFactory());		
			
			velocityUtil = new VelocityUtil(LOGGER_NAME, context);
		}
		
		return velocityUtil.velocitySubstitution(template, placeholderValues);
	}


	/** Spring XML noise, do not use. */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
