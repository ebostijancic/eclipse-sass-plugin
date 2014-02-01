package at.workflow.webdesk.po.impl;

import org.quartz.InterruptableJob;
import org.quartz.UnableToInterruptJobException;

import at.workflow.webdesk.po.jobs.WebdeskInterruptableJob;
import at.workflow.webdesk.po.jobs.WebdeskJob;

/**
 * This class adds interrupt method for forwarding 
 * interrupt to the WebdeskInterrutableJob for processing.
 * 
 * @author sdzuban
 */
public class PoGenericInterruptableQuartzJob extends PoGenericQuartzJob implements InterruptableJob {

	/** {@inheritDoc} */
	@Override
	public void interrupt() throws UnableToInterruptJobException {
		WebdeskJob job = getRunningJob();
		if (job instanceof WebdeskInterruptableJob) {
			job.getJobLogger().info("Trying to interrupt Job " + job.getClass().getName());
			((WebdeskInterruptableJob) job).interrupt();
		} else {
			job.getJobLogger().info("Tried to interrupt noninterruptable job with " + job.getClass());
		}

	}

}
