package at.workflow.webdesk.tools;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Covers the WebdeskLoggerFactory functionality.
 * 
 * @author fritzberger 26.08.2011
 */
public class WTestWebdeskLoggerFactory extends TestCase {

	public void testLoggerFactoryName()	{
		LoggerFactory factory = new WebdeskLoggerFactory("jobs");
		Logger logger;
		
		logger = factory.makeNewLoggerInstance("at.workflow.webdesk.wf.jobs.jobName.deeper.JobName");
		assertEquals("webdesk.jobs.jobs.JobName", logger.getName());
		
		logger = factory.makeNewLoggerInstance("at.workflow.webdesk.wf.jobs.jobName.JobName");
		assertEquals("webdesk.jobs.wf.JobName", logger.getName());
		
		logger = factory.makeNewLoggerInstance("at.workflow.webdesk.wf.jobs.JobName");
		assertEquals("webdesk.jobs.webdesk.JobName", logger.getName());
		
		logger = factory.makeNewLoggerInstance("at.workflow.webdesk.JobName");
		assertEquals("webdesk.jobs.at.JobName", logger.getName());
		
		logger = factory.makeNewLoggerInstance("JobName");
		assertEquals("webdesk.jobs.JobName", logger.getName());
		
		logger = factory.makeNewLoggerInstance("");
		assertEquals("webdesk.jobs.", logger.getName());		
	}

}
