package at.workflow.webdesk.po.model;

import java.util.Date;

/**
 * Non persistent Bean describing the state of the a PoJob in the Job-Engine.
 * Will only get returned for scheduled jobs in the Job-Engine, one job
 * which is planned several times with different triggers, will be returned
 * for every trigger.
 * 
 * @author sdzuban
 */
public class PoJobStateInfo implements Comparable<PoJobStateInfo> {

	private String jobName;
	private String triggerName;
	private String triggerUID;
	private boolean executing;
	private Date nextFireTime;
	private boolean active;
	
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}
	public String getTriggerUID() {
		return triggerUID;
	}
	public void setTriggerUID(String triggerUID) {
		this.triggerUID = triggerUID;
	}
	public boolean isExecuting() {
		return executing;
	}
	public void setExecuting(boolean executing) {
		this.executing = executing;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if ( o == null || !(o instanceof PoJobStateInfo))
			return false;
		
		PoJobStateInfo other = (PoJobStateInfo) o;
		String jobName2 = other.getJobName();
		String triggerUID2 = other.getTriggerUID();
		
		if ((jobName == null && jobName2 == null
				|| jobName != null && jobName2 != null && jobName.equals(jobName2))
			&& (triggerUID == null && triggerUID2 == null
					|| triggerUID != null && triggerUID2 != null && triggerUID.equals(triggerUID2))
			)
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		
		int hash1 = jobName != null ? jobName.hashCode() : 0;
		int hash2 = triggerUID != null ? triggerUID.hashCode() : 0;
		return hash1*17 + hash2*37; 
	}
	public int compareTo(PoJobStateInfo o) {
		if ( o == null)
			return 1;
		
		PoJobStateInfo other = o;
		String jobName2 = other.getJobName();
		if (jobName != null && jobName2 != null)
			return this.jobName.compareToIgnoreCase(jobName2);
		else if (jobName == null)
			return -1;
		else if (jobName2 == null)
			return 1;
		else
			return 0;
	}
}
