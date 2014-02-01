package at.workflow.webdesk.po.jobs.testJob;

import at.workflow.webdesk.po.jobs.AbstractWebdeskJob;
import at.workflow.webdesk.po.jobs.WebdeskJob;

/**
 * This is test job for testing of job starting and end.
 * 
 * @author sdzuban
 *
 */
public class TestLongLastingJob extends AbstractWebdeskJob implements WebdeskJob {
	
	public static final int WAIT_TIME = 2000;

	@Override
	public void run() {

		try {
			Thread.sleep(WAIT_TIME);
		} catch (InterruptedException e) {
			logger.error(e,e);
		}
	}
}
