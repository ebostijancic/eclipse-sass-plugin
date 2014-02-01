package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import at.workflow.webdesk.po.Configurable;

/**
 * Represents a Job which is a named batch program which has assigned triggers
 * which describe timeevents, when the job will be started. 
 * the Job can be 'derived' from a parent job, where this job represents the
 * configuration. It can also be itself an abstract parent job, where
 * it has childs.
 * 
 * Created on 13.10.2005
 */
public class PoJob extends PoBase implements Configurable
{
	private static final long serialVersionUID = 1L;
	
	private String uid;
	private String name;
	private String description;
    private Collection<PoJobTrigger> jobTriggers = new HashSet<PoJobTrigger>();
	private String jobClass;
	private boolean configurable;
    private PoModule module;
    private Collection<PoFile> configFiles = new HashSet<PoFile>();
    private Boolean allowUpdateOnVersionChange;
    private Integer type;

    
    // these variables are needed to send
    // emails when an "error" occurs
    private String logLevel;
    private String searchCriteria;
    private boolean linkToLogs;
    private String basicUrl;
    private String emailAdresses;
    private boolean mailForwardingEnabled;
    
   
    
    
    public boolean isMailForwardingEnabled() {
		return mailForwardingEnabled;
	}

	public void setMailForwardingEnabled(boolean mailForwardingEnabled) {
		this.mailForwardingEnabled = mailForwardingEnabled;
	}

    public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	public boolean isLinkToLogs() {
		return linkToLogs;
	}

	public void setLinkToLogs(boolean linkToLogs) {
		this.linkToLogs = linkToLogs;
	}

	public String getBasicUrl() {
		return basicUrl;
	}

	public void setBasicUrl(String basicUrl) {
		this.basicUrl = basicUrl;
	}

	public String getEmailAdresses() {
		return emailAdresses;
	}
	
	public List<String> getEmailAdressesAsList() {
		if (emailAdresses==null)
			return new ArrayList<String>();
		return Arrays.asList(emailAdresses.split(";"));
	}
	
	public void setEmailAdresses(List<String> emailAdresses) {
		Iterator<String> i = emailAdresses.iterator();
		StringBuffer buf = new StringBuffer();
		while (i.hasNext()) {
			String email = i.next();
			buf.append(email);
			if (i.hasNext())
				buf.append(";");
		}
		this.setEmailAdresses(buf.toString());
	}

	public void setEmailAdresses(String emailAdresses) {
		this.emailAdresses = emailAdresses;
	}

	/**
     * returns either PoConstants.JOB or PoConstants.JOB_CONFIG
     * @return Type of Job
     */
    public int getType() {
    	if (this.type==null)
    		return 0;
    	else
    		return type.intValue();
    }

    public void setType(Integer type) {
        this.type = type;
    }



    /**
     * returns all versions of the config files of the job
     * if the job is a configured job (PoConstants.JOB_CONFIG)
     * otherwise an empty set.
     * 
     * @return set of PoFile Objects
     */
    public Collection<PoFile> getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(Collection<PoFile> configFiles) {
        this.configFiles = configFiles;
    }
    
    public void addConfigFile(PoFile file) {
    	this.configFiles.add(file);
    }

    
    private boolean active;
    
    /**
     * specifies if the job is active or not.
     * overrides active flag at jobtrigger in the sense
     * that the job is NOT started even when an assigned
     * and active jobtrigger exists! 
     * 
     * @return if the job is active or not
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * specifies if the job is configureble
     * means that cforms files exist in order to 
     * edit the config xml file
     * 
     * @return true/false
     */
    public boolean isConfigurable() {
        return configurable;
    }

    public void setConfigurable(boolean isConfigurable) {
        this.configurable = isConfigurable;
    }

    /**
     * full name of jobclass to execute (has to extend the class AbstractJob)
     * can be a js file alternatively (then it has to end with .js
     * and will be called by the JavascriptJob)
     * 
     * @return the corresponing java class (alternatively a js file)
     */
    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }


	@Override
	public String getUID() {
		return uid;
	}
	
	/**
	 * @return the name of the job
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	
    /**
     * @return a collection of corresponding PoJobTriggers (you can use the name to find the real trigger)
     */
    public Collection<PoJobTrigger> getJobTriggers() {
        return jobTriggers;
    }

	
	// Set-Methoden:	
	
	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}


	public void setJobTriggers(Collection<PoJobTrigger> jobTriggers) {
		this.jobTriggers= jobTriggers;
	}
    
    @Override
	public String toString() {
        String ret = "PoJob [" +
            " name=" + this.name +
            ", description=" + this.description +
            ", uid=" + this.uid + "]";
        return ret;
    }
    
    /**
     * if is set to true, then an existing job should be overwritten
     * at the time of the job registration by the values
     * inside the job descriptor
     * @return true/false
     */
    public Boolean getAllowUpdateOnVersionChange() {
        return allowUpdateOnVersionChange;
    }

    /**
     * if is set to true, then an existing job should be overwritten
     * at the time of the job registration by the values
     * inside the job descriptor
     */
    public boolean isAllowUpdateOnVersionChange() {
        if (allowUpdateOnVersionChange==null || allowUpdateOnVersionChange.booleanValue()==false)
            return false;
        else
            return true;
    }

    public void setAllowUpdateOnVersionChange(Boolean allowUpdateOnVersionChange) {
        this.allowUpdateOnVersionChange = allowUpdateOnVersionChange;
    }
    
    
    private PoJob parent;

    /**
     * @return parent job, if job is a configured job
     */
    public PoJob getParent() {
        return parent;
    }

    public void setParent(PoJob parent) {
        this.parent = parent;
    }

    private Collection<PoJob> childs = new HashSet<PoJob>();
    
    /**
     * @return collection of configured "child" jobs
     */
    public Collection<PoJob> getChilds() {
        return childs;
    }

    public void setChilds(Collection<PoJob> value) {
        childs = value;
    }

    public boolean addChild(PoJob element) {
        return childs.add(element);
    }

    /**
     * @return corresponding PoModule object 
     */
    public PoModule getModule() {
    	
		if (module==null && parent!=null) {
			return getParent().getModule();
		}

        return module;
    }

    public void setModule(PoModule module) {
        this.module = module;
    }
	
}
