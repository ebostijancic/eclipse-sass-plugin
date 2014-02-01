package at.workflow.webdesk.po.jobs.testJob;

import java.util.Date;

/**
 * TestJob for testing PoJobService in UnitTest
 */
import at.workflow.webdesk.po.jobs.AbstractWebdeskJob;

public class TestJob extends AbstractWebdeskJob {
	
	public static String message;
	public static Date messageWritten;

	@Override
	public void run() {
		
		if(message == null)
			logger.info("called the very first time...");
		
		Date now = new Date();
		message = "This Job was just started at " + now;
		messageWritten = now; 
		
		logger.info("first line");
		logger.info("second line");
	}

}
