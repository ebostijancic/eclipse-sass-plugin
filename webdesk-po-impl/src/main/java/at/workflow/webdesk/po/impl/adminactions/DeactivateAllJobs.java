package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.model.PoJob;

/**
 * An admin action that sets all jobs to inactive
 * 
 * @author ggruber 15092011
 */
public class DeactivateAllJobs extends AbstractAdminAction {
	
	private PoJobService jobService;

	@Override
	public void run() {
		
		for (PoJob job : jobService.loadAllJobs(false))	{
			deactivateJob(job);
		}
		jobService.scheduleAll();
	}
	
	private void deactivateJob(PoJob job) {
		job.setActive(false);
		jobService.saveOrUpdateJob(job); 
	}

	/** Spring accessor. */
	public void setJobService(PoJobService jobService) {
		this.jobService = jobService;
	}

}
