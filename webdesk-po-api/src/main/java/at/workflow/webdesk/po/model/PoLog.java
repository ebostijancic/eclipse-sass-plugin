package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 *  <p>
 *  The <code>PoLog</code> represents a collection of 
 *  log entries (<code>PoLogDetails</code>). These entries
 *  can correspond to an action <code>actionName</code> 
 *  or a job (<code>referenceUID</code>). In the latter case 
 *  the <code>actionName</code> contains the name 
 *  of the job, but it should be preferred to get the 
 *  <code>job</code> via the <code>referenceUID</code>. 
 *  <p>
 *  The <code>referenceId</code> represents the <code>TaId</code> of
 *  the user. But will contain a lot of times a <code>null</code>
 *  value.
 *  <p>
 *  
 *  @see PoLogDetail
 *  @see PoLogDetailThrowable
 *  @see PoLogRequestParameter
 *  
 *  @author ggruber
 */
public class PoLog extends PoBase{
	private static final long serialVersionUID = 1L;
	
	private String UID;
    private String userName;
    private String switchedUserName;
    private Date beginTime;
    private Date endTime;
    private String actionName;
    private String referenceUID;
    private boolean ok;
    private String ip;
    private String browserVersion;
    private String requestParams;
    private Date referenceDate;
    private String referenceId;
    private String continuationId;
    private String sessionId;
    private Double duration;
    private Boolean ajax; 
    private String clusterNode;
    private String url;
    private String httpMethod;
    
    private Collection<PoLogRequestParameter> requestParameters;
    
    /**
     * @return Returns the name of a 
     * <code>PoAction</code> or a <code>PoJob</code>. 
     * If the latter case is true, <code>.job</code> is attached 
     * to the name. (this seems true after looking into the logs.) 
     *
     * FIXME check the words said above. why do we attach .job to the name
     * The advantage is, that it is possible to distinguisch between a job 
     * and an action. (better solution would be to avoid haven the jobs name
     * inside here! what happens if it changes. -> the name inside here will
     * remain the same!  
     */
    public String getActionName() {
        return actionName;
    }
    
    /**
     * @param actionName The actionName to set.
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    /**
     * @return Returns the beginTime.
     */
    public Date getBeginTime() {
        return beginTime;
    }
    /**
     * @param beginTime The beginTime to set.
     */
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }
    /**
     * @return Returns the browserVersion.
     */
    public String getBrowserVersion() {
        return browserVersion;
    }
    /**
     * @param browserVersion The browserVersion to set.
     */
    public void setBrowserVersion(String browserVersion) {
    	
    	if (browserVersion!=null && browserVersion.length()>38) {
    		this.browserVersion = browserVersion.substring(0, 37) + "..";
     	} else {
     		this.browserVersion = browserVersion;
     	}
    }
    /**
     * @return Returns the ip.
     */
    public String getIp() {
        return ip;
    }
    /**
     * @param ip The ip to set.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * @return Returns the requestParams.
     */
    public String getRequestParams() {
        return requestParams;
    }
    /**
     * @param requestParams The requestParams to set.
     */
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    /**
     * @return Returns the switchedUserName.
     */
    public String getSwitchedUserName() {
        return switchedUserName;
    }
    /**
     * @param switchedUserName The switchedUserName to set.
     */
    public void setSwitchedUserName(String switchedUserName) {
        this.switchedUserName = switchedUserName;
    }
    /**
     * @return Returns the uID.
     */
    @Override
	public String getUID() {
        return UID;
    }
    /**
     * @param uid The uID to set.
     */
    @Override
	public void setUID(String uid) {
        UID = uid;
    }
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }
    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    /**
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return endTime;
    }
    /**
     * @param endTime The endTime to set.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    /**
     * @return Returns the ok.
     */
    public boolean isOk() {
        return ok;
    }
    /**
     * @param ok The ok to set.
     */
    public void setOk(boolean ok) {
        this.ok = ok;
    }
    /**
     * @return Returns the id of the 
     * <code>PoJob</code>. If a <code>PoAction</code> was 
     * logged, this field is emtpy. Rather use the 
     * <code>actionName</code> field, in order to find the action-
     * but consider: the <code>actionName</code> is filled with the 
     * name of the <code>PoJob</code> when a job was the subject
     * of the observation.
     */
    public String getReferenceUID() {
        return referenceUID;
    }
    /**
     * @param referenceUID The referenceUID to set.
     * 
     * 
     */
    public void setReferenceUID(String referenceUID) {
        this.referenceUID = referenceUID;
    }
    public Date getReferenceDate() {
        return referenceDate;
    }
    public void setReferenceDate(Date referenceDate) {
        this.referenceDate = referenceDate;
    }
    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Double getDuration() {
		return duration;
	}
	/**
	 * @param duration
	 */
	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public String getContinuationId() {
		return continuationId;
	}

	public void setContinuationId(String continuationId) {
		this.continuationId = continuationId;
	}

	public Boolean getAjax() {
		return ajax;
	}

	public void setAjax(Boolean ajax) {
		this.ajax = ajax;
	}

	public String getClusterNode() {
		return clusterNode;
	}

	public void setClusterNode(String clusterNode) {
		this.clusterNode = clusterNode;
	}
	
	public void addRequestParameter(PoLogRequestParameter requestParameter) {
		if (this.requestParameters==null) {
			requestParameters = new LinkedHashSet<PoLogRequestParameter>();
		}
		
		requestParameters.add(requestParameter);
		requestParameter.setLog(this);
	}

	public Collection<PoLogRequestParameter> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Collection<PoLogRequestParameter> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
    
    
}
