package at.workflow.webdesk.tools;

import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

import at.workflow.webdesk.tools.errors.ErrorMessage;
import at.workflow.webdesk.tools.errors.ErrorLog;

/**
 * Simplest Velocity template application.
 * 
 * @author sdzuban 25.01.2013
 */
public class VelocityUtil {

	private Logger velocityLog;	// will be found by Velocity using log4j magic
	private Map<String, Object> initialContext = null;
	
	private ErrorLog errorLog;
	
	/**
	 * This constructor enables hand over of errorLog object
	 * that can be used for display of encountered errors to the user.
	 * @param loggerName name of the logger to be used
	 * @param errorLog logsErrors for display to the user
	 */
	public VelocityUtil(String loggerName, ErrorLog errorLog) {
		this(loggerName);
		this.errorLog = errorLog;
	}
	
	/**
	 * This constructor enables injection of initial context
	 * @param loggerName name of the logger to be used
	 * @param initialContext for the evaluation
	 */
	public VelocityUtil(String loggerName, Map<String, Object> initialContext) {
		this(loggerName);
		this.initialContext = initialContext;
	}

	/**
	 * @param loggerName name of the logger to be used
	 */
	public VelocityUtil(String loggerName) {

		if (StringUtils.isNotBlank(loggerName))
			velocityLog = Logger.getLogger(loggerName);

		try {
			// initialize Velocity logging to use our logger
			// see http://velocity.apache.org/engine/devel/developer-guide.html#Configuring_Logging
			Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );
			Velocity.setProperty("runtime.log.logsystem.log4j.logger", loggerName);
			
			Velocity.setProperty("velocimacro.permissions.allow.inline.to.replace.global", "false");
			Velocity.setProperty("velocimacro.permissions.allow.inline", "true");
			// fri_2011-09-19
			// According to http://people.apache.org/~henning/velocity/html/ch07s02.html
			// Default for "velocimacro.permissions.allow.inline" == "true"
			// Default for "velocimacro.permissions.allow.inline.to.replace.global" == "false"
			// so no need to set this here.
			Velocity.setProperty("velocimacro.permissions.allow.inline.local.scope", "true");
			// fri_2011-09-19 TODO please comment why this has been set to true here (default is false)
			
			Velocity.init();
		}
		catch (Exception e) {
			throw new RuntimeException("Initialization failed: " + e, e);
		}
	}
	
	public String velocitySubstitution(String template, Map<String, Object> placeholderValues) {
		
		Context context = new VelocityContext();
		if (initialContext != null)
			for (Map.Entry<String,Object> e : initialContext.entrySet())
				context.put(e.getKey(), e.getValue());
		
		for (Map.Entry<String,Object> e : placeholderValues.entrySet())
			context.put(e.getKey(), e.getValue());

		StringWriter stringWriter = new StringWriter();
		try {
			Velocity.evaluate(context, stringWriter, "SimpleVelocityUtil", template);
			stringWriter.close();
			return stringWriter.toString();
		}
		catch (Exception e) {
			if (velocityLog != null)
				velocityLog.warn("Velocity-problem evaluating >"+template+"< : " + e, e);
			if (errorLog != null)
				errorLog.addErrorMessage(new ErrorMessage("Velocity-problem evaluating >"+template+"<", e, context));
		}
		return null;
	}

}
