package at.workflow.webdesk.po.model;

import java.util.Date;

/**
 * Model Class of a job trigger. It can hold various job, that all start at the same<br>
 * time. <br>
 * <br> 
 * The scheduleType determines the period of the JobTrigger. See PoConstants for<br> 
 * possible values (e.g. daily, weekly, monthly, yearly, ...). <br>
 * The startDate determines when the process starts <br>
 * (and a result the exact date of the repetition). <br>
 *
 * @author hentner, ggruber
 */
@SuppressWarnings("serial")
public class PoJobTrigger extends PoBase
{
	private String uid;
	private String name;
	private String cronExpression;
	private int scheduleType;		
	private int intervalMinutes;	
	private Date startDate;		
	private Date endDate;		
	private int  repeatCount;
    private boolean active;		 
    private PoJob job;
    private String runInCluster;
	

	/**
	 * @return the cluster node where the job should be executed on.
	 * only used when webdesk is running in a cluster.
	 * if NULL is returned when webdesk is running in a cluster, this job
	 * trigger will never be used!
	 */
	public String getRunInCluster() {
		return runInCluster;
	}

	public void setRunInCluster(String runInCluster) {
		this.runInCluster = runInCluster;
	}

	@Override
	public String getUID() {
		return uid;
	}

    /**
     * @return the name of the trigger (required)
     */
    public String getName() {
        return name;
    }
    
    public PoJob getJob() {
        return job;
    }
    
	/**
	 * @return the cron expression (see scheduleType for more information)
	 */
	public String getCronExpression() {
		return cronExpression;
	}
	
	/**
	 * @return the schedule type determines what kind of trigger is used <br>
     * and which attributes are needed.<br>
     * <br>See PoConstants for a list of possible Attributes.<br>
     * <br>Set the needed attributes of the <b>startDate</b> object in order to get your trigger running correctly<br>
     * <br>
     * <ul>
     * <li>DAILY_TRIGGER</li> a trigger that fires every day - set the time (hour + minute) 
     * <li>WEEKLY_TRIGGER</li> a trigger that fires every week - set the time (h+m) and the day <br>
     *                    of the week (complete date, only the day of the week is considered)
     * <li>MONTHLY_TRIGGER</li> a trigger that fires every month - set the time (h+m) and the day<br>
     *                    of the month (complete date, only the day of the month is considered).<br>
     *                     if the day of month does not exist (e.g. 31 -> February), the trigger <br>
     *                     will not fire
     * <li>SIMPLE_TRIGGER</li> a trigger that fires the first time at the given date (<b>startDate</b>)<br>
     *                     and fires <b>repeatCount</b> times or until the <b>endDate</b> is reached<br>
     *                     The interval between them is determined by the <b>intervalMinutes</b> value.<br>
     *                     
     * <li>CRON_TRIGGER</li> a calendar expression format - set the <b>cronExpression</b><br>
     * visit http://www.opensymphony.com/quartz/api/org/quartz/CronExpression.html and<br> 
     * http://www.opensymphony.com/quartz/api/org/quartz/CronTrigger.html for detailed information<br> 
     * <li>MINUTELY_TRIGGER</li>
     * </ul>
     * <br>
	 */
	public int getScheduleType() {
		return scheduleType;
	}

	public int getIntervalMinutes() {
		return intervalMinutes;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public boolean isActive() {
		return active;
	}
	
	// Set-Methoden:

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public void setScheduleType(int scheduleType) {
		this.scheduleType = scheduleType;
	}
	
	public void setIntervalMinutes(int intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}
	
	public void setStartTime(Date startTime) {
		this.startDate = startTime;
	}
	
	public void setEndTime(Date endDate) {
		this.endDate = endDate;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
    
    @Override
	public String toString() {
        String ret = "PoJobTrigger [" + 
            " startDate=" + this.startDate +
            ", endDate=" + this.endDate +
            ", scheduleType=" + this.scheduleType +
            ", cronExpression=" + this.cronExpression +
            ", intervalMinutes=" + this.intervalMinutes +
            ", active=" + this.active +
            ", uid=" + this.uid + "]";
        return ret;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setJob(PoJob job) {
        this.job = job;
    }

}
