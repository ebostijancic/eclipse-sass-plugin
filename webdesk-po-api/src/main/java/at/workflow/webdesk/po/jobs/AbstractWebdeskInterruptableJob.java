package at.workflow.webdesk.po.jobs;

/**
 * Extending this class makes sense only for a job that runs longer than 30 minutes.
 * 
 * This class enables Quartz interrupt to be forwarded to the job logic.
 * 
 * Implements WebdeskInterruptableJob by holding a private interrupted flag.
 * 
 * @author sdzuban 12.08.2011
 */
public abstract class AbstractWebdeskInterruptableJob extends AbstractWebdeskJob implements WebdeskInterruptableJob {

	private boolean interrupted = false;

	@Override
	public void onBeforeRun() {
		interrupted = false;
	}
	
	/**
	 * Sets the private flag to true.
	 * {@inheritDoc}
	 */
	@Override
	public void interrupt() {
		interrupted = true;
		// getJobLogger().info("Job " + getClass().getName() + " interrupted.");
		// TODO does not work TODO find out why
	}

	/**
	 * Returns the value of the private flag. 
	 * {@inheritDoc}
	 */
	@Override
	public final boolean wasInterrupted() {
		if (interrupted)
			getJobLogger().info("Job " + getClass().getName() + " interrupted.");
		return interrupted;
	}

}
