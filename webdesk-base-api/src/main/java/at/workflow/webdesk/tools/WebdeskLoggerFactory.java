package at.workflow.webdesk.tools;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Used to create loggers with a name made from a webdesk-job class name,
 * together with a 'category' tag. See <code>makeNewLoggerInstance()</code>.
 * 
 * @see at.workflow.webdesk.tools.WTestWebdeskLoggerFactory
 */
public class WebdeskLoggerFactory implements LoggerFactory {

	private String category;

	public WebdeskLoggerFactory(String category) {
		this.category = category;
	}

	/**
	 * Normally jobs reside in packages of the form
	 * <code>at.workflow.webdesk.[moduleName].jobs.jobName.[JobName]</code>.
	 * So for
	 * <pre>at.workflow.webdesk.wf.jobs.checkDeadlines.WfCheckDeadlines</pre>
	 * the result must be
	 * <pre>webdesk.jobs.wf.WfCheckDeadlines</pre>
	 * (Gabriel 2011-08-25).
	 * 
	 * TODO: think about finding the module from front on index 3.
	 * TODO: think about appending the "." to category only when moduleName is not empty (avoid training dot).
	 */
	@Override
	public Logger makeNewLoggerInstance(String dottedName) {
		String [] dottedNames = dottedName.split("\\.");
		
		String jobName = dottedNames[dottedNames.length - 1];	// the last part
		
		int moduleIndex = (dottedNames.length > 3) ? dottedNames.length - 4 : -1;
		// int moduleIndex = 3;
		String moduleName = (moduleIndex >= 0) ? dottedNames[moduleIndex]+"." : "";	// last - 3 part
		
		String loggerName = "webdesk." + category + "." + moduleName + jobName;
		return Logger.getLogger(loggerName);
	}

}
