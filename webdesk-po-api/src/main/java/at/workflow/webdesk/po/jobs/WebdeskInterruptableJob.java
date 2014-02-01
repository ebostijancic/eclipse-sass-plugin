package at.workflow.webdesk.po.jobs;
/**
 * Interruptable means interrupting a currently working job
 * (working <code>run</code> Method is interrupted).
 * It does not influence any scheduling configurations.
 * Implementing this interface makes sense only for a job that runs longer than 30 minutes.
 * 
 * sdzuban 22.06.2011
 */
public interface WebdeskInterruptableJob extends WebdeskJob {
	
	/**
	 * This method implements interrupt logic
	 * like setting a flag to stop main loop
	 * or interrupting a thread.
	 */
	public void interrupt();
	
	/** 
	 * @return true when this job was interrupted at least once.
	 * 		After having been interrupted once, this method will always return true.
	 */
	public boolean wasInterrupted();

}
