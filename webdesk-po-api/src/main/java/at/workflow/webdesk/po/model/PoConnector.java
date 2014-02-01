package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashSet;

import at.workflow.webdesk.po.Configurable;

/**
 * <p>
 * The <code>PoConnector</code> model holds all information concerning a
 * registered <code>Connector</code>. <code>PoConnector</code>'s are either
 * configurable or not. A configurable <code>PoConnector</code> has to supply
 * a configuration document, which resides under the same path as the 
 * implementation of the <code>PoConnectorInterface</code>. 
 * </p><p>
 * Every configurable <code>PoConnector</code> can have several configurations. 
 * These configurations are stored as children of the original <code>PoConnector</code>.
 * Additionally, the original <code>PoConnector</code> can be accessed via the
 * <code>getParent()</code> method of this class.
 * </p>
 */
@SuppressWarnings("serial")
public class PoConnector extends PoBase implements Configurable	{
	
	private String uid;
	private String name;
	private String className;
	private boolean configurable;
	private FlowDirection flowDirection;
	@Deprecated
	private boolean writeable;
	private boolean updateOnVersionChange;
	private PoConnector parent;
	private PoModule module;
	private Collection<PoFile> configFiles = new HashSet<PoFile>();
	private Collection<PoConnector> childs = new HashSet<PoConnector>();
	private Collection<PoConnectorLink> srcConnectorLinks = new HashSet<PoConnectorLink>();
	private Collection<PoConnectorLink> destConnectorLinks = new HashSet<PoConnectorLink>();
	
	@Override
	public String getUID() {
		return uid;
	}
	
	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}
	
	/**
     * @return PoFile objects representing all versions of the config files of the job
     * 		if the job is a configured job (PoConstants.JOB_CONFIG) otherwise an empty set.
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
    
    /**
     * @return the <code>PoConnector</code> which is the parent of this <code>PoConnector</code>.
     */
    public PoConnector getParent() {
        return parent;
    }

    /** Sets a new parent (and also sets its text-module to this when parent not null). */
    public void setParent(PoConnector parent) {
        this.parent = parent;
    	if (parent != null)
    		this.module = parent.getModule();
    }

    /**
     * @return a <code>List</code> of <code>PoConnector</code>'s.
     */
    public Collection<PoConnector> getChilds() {
        return childs;
    }

    public void setChilds(Collection<PoConnector> children) {
        childs = children;
    }

    public boolean addChild(at.workflow.webdesk.po.model.PoConnector element) {
        return childs.add(element);
    }
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public boolean isConfigurable() {
		return configurable;
	}
	
	public void setConfigurable(boolean isConfigurable) {
		this.configurable = isConfigurable;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isUpdateOnVersionChange() {
		return updateOnVersionChange;
	}
	public void setUpdateOnVersionChange(boolean updateOnVersionChange) {
		this.updateOnVersionChange = updateOnVersionChange;
	}
	
    public PoModule getModule() {
    	
		if (module==null && parent!=null) {
			return getParent().getModule();
		}
    	
        return module;
    }

    public void setModule(PoModule module) {
        this.module = module;
    }
    
	public Collection<PoConnectorLink> getSrcConnectorLinks() {
		return srcConnectorLinks;
	}

	public void setSrcConnectorLinks(Collection<PoConnectorLink> connectorLinks) {
		this.srcConnectorLinks = connectorLinks;
	}
	
	public Collection<PoConnectorLink> getDestConnectorLinks() {
		return destConnectorLinks;
	}

	public void setDestConnectorLinks(Collection<PoConnectorLink> dstConnectorLinks) {
		this.destConnectorLinks = dstConnectorLinks;
	}
	@Deprecated
	public boolean isWriteable() {
		return writeable;
	}
	@Deprecated
	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}

	public FlowDirection getFlowDirection() {
		return flowDirection;
	}

	public void setFlowDirection(FlowDirection flowDirection) {
		this.flowDirection = flowDirection;
	}

}
