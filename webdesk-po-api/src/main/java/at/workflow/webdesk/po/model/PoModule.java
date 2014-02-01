package at.workflow.webdesk.po.model;

import java.util.Date;
import java.util.Collection;

import at.workflow.webdesk.Detachable;

/**
 * Represents a "Software Component", i.e. a functional unit of Webdesk application.
 * The base module is module "po", all other modules depend on it.
 * For dependency order see <code>PoModuleUpdateService.sortedRegistrationBeans()</code>.
 * There are core modules and product modules.
 * <p/>
 * A module can be detached (TODO: explain what this means).
 * <p/>
 * Various configuration artifacts are linked to a module, e.g.
 * textmodules (here the word "module" has another meaning!), actions, connectors,
 * jobs, beanproperties.
 */
@SuppressWarnings("serial")
public class PoModule extends PoBase implements Detachable, Comparable<PoModule> {
   
    private String uid;
    private String name;
    private int versionNumber;
    private Date createdAt; 
    private Collection<PoTextModule> textModules;
    private Collection<PoJob> jobs;
    private Collection<PoConnector> connectors;
    private Collection<PoBeanProperty> beanProperties;
    private Collection<PoFile> updateScripts;
    private Collection<PoAction> actions;
    private Boolean detached = false;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<PoTextModule> getTextModules() {
        return textModules;
    }


    public void setTextModules(Collection<PoTextModule> textModules) {
        this.textModules = textModules;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }

    /** @deprecated not used anymore, was intended for down-grade. */
    public Collection<PoFile> getUpdateScripts() {
        return updateScripts;
    }

    /** @deprecated not used anymore, was intended for down-grade. */
    public void setUpdateScripts(Collection<PoFile> updateScripts) {
        this.updateScripts = updateScripts;
    }

    public Collection<PoJob> getJobs() {
        return jobs;
    }

    public void setJobs(Collection<PoJob> jobs) {
        this.jobs = jobs;
    }
    
    
    public Collection<PoConnector> getConnectors() {
        return connectors;
    }

    public void setConnectors(Collection<PoConnector> connectors) {
        this.connectors= connectors;
    }

    public Collection<PoBeanProperty> getBeanProperties() {
		return beanProperties;
	}

	public void setBeanProperties(Collection<PoBeanProperty> beanProperties) {
		this.beanProperties = beanProperties;
	}


	@Override
	public boolean isDetached() {
		return (this.detached==null) || this.detached.booleanValue();
	}


	public void setDetached(Boolean detached) {
		this.detached = detached;
	}


	@Override
	public void reactivate() {
		setDetached(false);
	}

	public Collection<PoAction> getActions() {
		return actions;
	}

	public void setActions(Collection<PoAction> actions) {
		this.actions = actions;
	}
	
	/** {@inheritDoc} */
	@Override
	public int compareTo(PoModule m) {
		if (m == null)
			return 1;
		if (name == null && m.getName() == null)
			return 0;
		if (name == null)
			return -1;
		if (m.getName() == null)
			return 1;
		return name.compareTo(m.getName());
	}
	
	@Override
	public String toString() {
        String ret = "PoModule[" +
	        "name=" + name + 
	        ", versionNumber=" + versionNumber + 
	        ", detached=" + detached + 
	        ", uid=" + uid + "]";
        return ret;
	}

}
