package at.workflow.webdesk.po.jobs.testJob;

import java.util.Date;

import at.workflow.webdesk.po.jobs.AbstractWebdeskInterruptableJob;

public class TestInterruptableJob extends AbstractWebdeskInterruptableJob {
	
	public static String message = "idle";
	public static Date messageWritten;

	@Override
	public void run() {

		message = "running";
		messageWritten = new Date(); 

		for (int timems = 0; timems <= 5000; timems += 50) {
			if (wasInterrupted()) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error(e,e);
			}
		}

		message = "finished";
		messageWritten = new Date(); 
	}

	@Override
	public void interrupt() {
		
		super.interrupt();

		message = "interrupted";
		messageWritten = new Date();
	}
}
