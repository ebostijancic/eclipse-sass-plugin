package at.workflow.webdesk.po.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.workflow.webdesk.Detachable;
import at.workflow.webdesk.po.Configurable;
import at.workflow.webdesk.po.PoConstants;

/**
 * Represents a clickable action within the webdesk application which can be
 * assigned to one or more objects of the following: PoPerson, PoGroup, PoRole 
 * or PoClient. A set of Actions make up the menu
 * a User gets when he starts the webdesk application. <br/><br/>
 * 
 * a action can be divided into the following categories: <br/>
 * a) "real" action <br/>
 * b) configuration of another action<br/>
 * c) process <br/>
 * 
 * Created on 16.06.2005
 * @author Gabriel Gruber, Harald Entner
 */
@SuppressWarnings("serial")
public class PoAction extends PoHistorization implements Detachable, Configurable {

	private String uid;
	
    /**
     * name of action, equals flowscriptfunction name AND
     * flowscript-folder name MUST be unique accross system
     */
    private String name;
    
    /** the webdesk module, this action belongs to. */
    private PoModule module;
    
    /** collection of all historic (xml) configuration files including the currently active one */
    private Collection<PoFile> files = new HashSet<PoFile>();
    
    /** collection of all help messages (one per language) */ 
    private Collection<PoHelpMessage> helpMessages = new HashSet<PoHelpMessage>();
    
    /** ranking allows you to define an order for all configs of the same parent action. That
     * will influence which config is choosen, if more than one config of a parent action is a candidate
     * to be used, when a user calls a action directly (without having permissions to it). */
    private int ranking;
    
    /**
     * Description is an extra text describing the Action
     * should be used in the menu as alt-text or hint
     */
    private String description;
    
    /**
     * All permissions linked to this action. Permissions can be linked to Persons, Roles, Groups or Clients.
     */
    private Collection<PoAPermissionBase> permissions = new HashSet<PoAPermissionBase>();
    
    /** Defines whether certain changes in this action metadata contained in the webdesk deployment code (act-descr.xml) should
     * be automatically synchronised into the production upon run of registration. If set to yes, these informations in production
     * will be overwritten by deployment code: caption, description, image, imageSet alongside with all the technical metadata 
     * like: controller, controllerPattern, attributes, sqlQuery, ...
     */
    private boolean allowUpdateOnVersionChange;
    
    /** Defines the default view permission Type which is taken into account, if the action is 
     * permitted generally by every webdesk user via use of the field @{@link #universallyAllowed}  */
    private int defaultViewPermissionType;
    
    
    /** Holds a set of directly linked textmodules which are linked to this action. The linking of Textmodules to PoActions
     * is just for convenience to easier find the used textmodules of an action, but it has no consequences for production. */
    private Collection<PoTextModule> textModules = new HashSet<PoTextModule>();

    /** allows to define a action reference which inherits all the permissions (including view permissions) the current action holds */
    private PoAction allowsAction;
    
    /** inverse collection, where all actions are referenced whose permissions are inherited by the current action (multiple inheritence!) */
    private Set<PoAction> allowedByActions = new HashSet<PoAction>();
    
    /** defines the parent action. Should be null in case of a PoAction of type 1 (ACTION) or 3 (PROCESS_REFERENCE)*/
    private PoAction parent;
    
    /** list of configurations, derived from the current PoAction (which has to be of type 1 (ACTION) */
    private Collection<PoAction> childs = new HashSet<PoAction>();
    
    private String actionFolder;
    
    /** Name of the image being used. */
	private String image;
	
	/** Name of the imageset being used. Is used together with image to construct an image-URL for the current Action */
	private String imageSet;
	
	/**
	 * Layout template to be used for the action
	 */
	private String layoutTemplateToUse = null;
	
	/**
	 * Shall the action be opened always in new window?
	 */
	private boolean alwaysOpenInNewWindow;
	
	private String controller;
	private String controllerPattern;
	
    /**
     * is interpreted by the cocoon continuations manager to disable 
     * continuation invalidation based on the max-number-of-continuations-per-user
     * setting (set in cocoon.xconf)
     */
    private boolean disableContinuationInvalidate;
    
    private Map<String, String> attributes = new HashMap<String, String>();
    
    /**
     * A HQL query for further processing. 
     * In case of a "show" Action using the
     * HQLSimpleDataListActionHandler this query is typically used to fetch the objects
     * to be displayed from the database.
     * 
     * In case of a "edit" Action where the ID of the object to be viewed or edited is
     * passed by a request parameter named 'uid', the query is used to check, if the 
     * calling user has the correct viewpermissions to open this particular object.
     * 
     * In both cases the query may contain the placeholders $PERSONUIDS$, $GROUPUIDS$ and/or $CLIENTUIDS$
     * which are replaced before execution with the corresponding String-Lists of Person-UIDs, Group-UIDs
     * or Client-Uids. This Uids are created out of the Viewpermissions of the current user 
     * for this particular action.
     */
    private String sqlQuery;
    
    private boolean universallyAllowed;
    
    
    private Boolean detached;
    private String processDefId;
    
    /**
     * caption is the displayed text in the menu
     * Comment for <code>caption</code>
     */
    private String caption;
    
    /** if set a Switched user will not be able to access an action marked with this flag 
     * no matter if he is able to call the action with personal rights **/
    private Boolean prohibitUsageBySwitchedUsers;
    
    /** if set to true, coming back by historyBack.do to the current action
     * will result in the complete processing (postProcessInLoop() and preProcessInLoop()) of the Controller/Actionhandler
     * with the last known form content.  
     */
    private Boolean refreshOnComingBack;
    
    /** defines whether the current action is configurable. Can only be true, if the Action is of type 1 (ACTION) */
    private boolean configurable;
    
    /** defines the action */
    private int actionType;
    

	private Collection<PoActionParameter> parameters = new HashSet<PoActionParameter>();
    
	private Collection<PoActionUrlPattern> urlPatterns = new HashSet<PoActionUrlPattern>();
	
	private Collection<PoContextParameter> contextParameters = new HashSet<PoContextParameter>();
	
	
	/***** Getters and Setter *******/
	
	
	public boolean isProhibitedForUsageBySwitchedUsers() {
		return prohibitUsageBySwitchedUsers !=null && prohibitUsageBySwitchedUsers;
	}
	
	/** if true, on coming back to this action, a form submit cycle will be processed (calling
	 * postProcessInLoop() and preProcessInLoop() methods in the ActionHandler/Controller.
	 * @return true if the action needs a refresh, when coming back with back-button.
	 */
	public boolean shouldRefreshOnComingBack() {
		return refreshOnComingBack !=null && refreshOnComingBack;
	}
	
	public Boolean getProhibitUsageBySwitchedUsers() {
		return prohibitUsageBySwitchedUsers;
	}
	
	public void setProhibitUsageBySwitchedUsers(Boolean prohibitUsageBySwitchedUsers) {
		this.prohibitUsageBySwitchedUsers = prohibitUsageBySwitchedUsers;
	}
    
    @Override
	public String getUID() {
        return uid;
    }

    @Override
	public void setUID(String uid) {
        this.uid = uid;
    }


    @Override
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    
    /**
     * @return Returns the caption.
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * @param caption The caption to set.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }


    public Collection<PoFile> getFiles() {
        return this.files;
    }

    public void setFiles(Collection<PoFile> files) {
        this.files = files;
    }

    public boolean addFile(PoFile element) {
        return files.add(element);
    }
    
    public Collection<PoHelpMessage> getHelpMessages() {
		return helpMessages;
	}

	public void setHelpMessages(Collection<PoHelpMessage> helpMessages) {
		this.helpMessages = helpMessages;
	}

    public Collection<PoAPermissionBase> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Collection<PoAPermissionBase> permissions) {
        this.permissions = permissions;
    }

    public boolean addPermission(PoAPermissionBase element) {
        return permissions.add(element);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAllowUpdateOnVersionChange() {
        return allowUpdateOnVersionChange;
    }

    public void setAllowUpdateOnVersionChange(boolean allowUpdateOnVersionChange) {
        this.allowUpdateOnVersionChange = allowUpdateOnVersionChange;
    }

    public Collection<PoTextModule> getTextModules() {
        return textModules;
    }

    public void setTextModules(Collection<PoTextModule> textmodules) {
        this.textModules = textmodules;
    }

    public boolean addTextModule(PoTextModule element) {
        return textModules.add(element);
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getDefaultViewPermissionType() {
        return defaultViewPermissionType;
    }

    public void setDefaultViewPermissionType(int defaultViewPermissionType) {
        this.defaultViewPermissionType = defaultViewPermissionType;
    }
    
    public PoAction getAllowsAction() {
        return allowsAction;
    }

    public void setAllowsAction(PoAction allowsAction) {
        this.allowsAction = allowsAction;
    }
    
    public Set<PoAction> getAllowedByActions() {
        return allowedByActions;
    }

    public void setAllowedByActions(Set<PoAction> allowedByActions) {
        this.allowedByActions = allowedByActions;
    }

    public PoAction getParent() {
        return parent;
    }
    
    public void setParent(PoAction parent) {
        this.parent = parent;
    }
    
    public Collection<PoAction> getChilds() {
        return childs;
    }

    public void setChilds(Collection<PoAction> childs) {
        this.childs = childs;
    }

    public boolean addChild(PoAction element) {
    	element.setParent(this);
        return childs.add(element);
    }
    
    
   /**
    * get the Folder (Filesystem directory and logical Group) where action belongs to
    * can be f.i. "po", "ta", "admin", "wf" or also "custom"
    * actions which are created by a customer are saved inside the "custom" folder
    * 
    */
	public String getActionFolder() {
		return actionFolder;
	}
	/**
    */
	public void setActionFolder(String actionFolder) {
		this.actionFolder = actionFolder;
	}
	
	
    /**
     * returns filepath beginning from the imageSet and Sizes directory
     * f.i. apps/kdmconfig.png
     * 
     * @return Returns the image.
     */
    public String getImage() {
        return image;
    }
    /**
     * @param image The image to set.
     */
    public void setImage(String image) {
        this.image = image;
    }
    /**
     * returns imageSet of the Action. 
     * f.i. nuovola
     * 
     * @return Returns the imageSet.
     */
    public String getImageSet() {
        return imageSet;
    }
    /**
     * @param imageSet The imageSet to set.
     */
    public void setImageSet(String imageSet) {
        this.imageSet = imageSet;
    }
    
    /**
     * type of Action. can be either a Action, a Configuration or
     * a Process. Lookup PoConstants for static finals
     * 
     * @return Returns the type.
     */
    public int getActionType() {
        return actionType;
    }
    /**
     * @param type The type to set.
     */
    public void setActionType(int myType) {
        this.actionType = myType;
    }
    
    /**
     * returns processdefinitionID being referenced from the workflow system
     * has to be set, when type of Action is Process.
     * 
     * @return Returns the processId.
     */
    public String getProcessDefId() {
        return processDefId;
    }
    /**
     * @param processId The processId to set.
     */
    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }
    
    /**
     * in case PoAction is a REAL action, determines if Action
     * can be configured via a Config-xml
     * if it is true, then cforms files exist which 
     * can be used to edit the config files
     * 
     * @return Returns the isConfigurable.
     */
    public boolean isConfigurable() {
        return configurable;
    }
    /**
     * @param isConfigurable The isConfigurable to set.
     */
    public void setConfigurable(boolean isConfigurable) {
        this.configurable = isConfigurable;
    }
    

   
    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    
    public boolean isUniversallyAllowed() {
            return this.universallyAllowed;
    }

    public void setUniversallyAllowed(boolean universallyAllowed) {
        this.universallyAllowed = universallyAllowed;
    }
    
	public boolean isDisableContinuationInvalidate() {
		return disableContinuationInvalidate;
	}

	public void setDisableContinuationInvalidate(boolean disableContinuationInvalidate) {
		this.disableContinuationInvalidate = disableContinuationInvalidate;
	}
	
	/**
	 * defines a set of attributes, which can additionally configure the controller.
	 * this gives the possibility to feed standard controllers with the differences
	 * (f.i. textmodule and service names for a standard Cform-action-handler)
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
    
    /**
     * returns the name of the special controller / action-handler to use.
     * overrides any controller defined by convention (f.i. controller-code within the
     * actions folder)
     * 
     * @return the name of the controller/action-handler to use.
     */
	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	/**
	 * returns the controller-pattern to use. this could be a framework specific
	 * string, which tells the front-end dispatcher, which sitemap to use and
	 * how the controller should be looked up and executed.
	 * 
	 * valid values:
	 * - cocoon-flow
	 * - cocoon-flow-java
	 * - cocoon-apples ? (TODO)
	 * - wicket ? (TODO)
	 * - jsf ? (TODO)
	 * 
	 * if not defined, first looks for a flowscript controller, afterwards for a
	 * java actionhandler called by flowscript.
	 * 
	 */
	public String getControllerPattern() {
		return controllerPattern;
	}

	public void setControllerPattern(String controllerPattern) {
		this.controllerPattern = controllerPattern;
	}

	public boolean isAlwaysOpenInNewWindow() {
		return alwaysOpenInNewWindow;
	}

	public void setAlwaysOpenInNewWindow(boolean alwaysOpenInNewWindow) {
		this.alwaysOpenInNewWindow = alwaysOpenInNewWindow;
	}

	public String getLayoutTemplateToUse() {
		return layoutTemplateToUse;
	}

	public void setLayoutTemplateToUse(String layoutTemplateToUse) {
		this.layoutTemplateToUse = layoutTemplateToUse;
	}
	
	
    @Override
	public String toString() {
        String ret = "PoAction[" +
            "name=" + this.name + 
            ", actionType=" + this.actionType + 
            ", actionFolder=" + this.actionFolder +
            ", detached=" + this.detached +
            ", module=" + this.module +
            ", uid=" + this.uid + "]";
        return ret;
    }

	@Override
	public PoModule getModule() {
		
		if (module==null && parent!=null) {
			return getParent().getModule();
		}
		
		return module;
	}

	public void setModule(PoModule module) {
		this.module = module;
	}

	@Override
	public boolean isDetached() {
		
		if(this.parent!=null) {
			return this.parent.isDetached();
		}
		
		if (this.detached==null) {
			if (this.module==null) {
				return false;
			}
			return this.module.isDetached();
		}
		
		if (this.module==null && this.actionType != PoConstants.ACTION_TYPE_PROCESS) {
			if ("custom".equals(this.actionFolder))
				return false;
			else
				return true;
		}
		
		if (this.actionType == PoConstants.ACTION_TYPE_PROCESS) {
			return detached;
		}
		
		return (this.detached.booleanValue() || this.module.isDetached());
	}
	
	public void setDetached(Boolean detached) {
		this.detached = detached;
	}

	@Override
	public void reactivate() {
		setDetached(false);
	}

	public Collection<PoActionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<PoActionParameter> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(PoActionParameter parameter) {
		this.parameters.add(parameter);
		parameter.setAction(this);
	}
	
	public Collection<PoActionUrlPattern> getUrlPatterns() {
		return urlPatterns;
	}
	
	public void setUrlPatterns(Collection<PoActionUrlPattern> urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
	
	public void addUrlPattern(PoActionUrlPattern urlPattern) {
		this.urlPatterns.add(urlPattern);
		urlPattern.setAction(this);
	}
	
	public Collection<PoContextParameter> getContextParameters() {
		return contextParameters;
	}
	
	public void setContextParameters(Collection<PoContextParameter> contextParameters) {
		this.contextParameters = contextParameters;
	}
	
	public void addContextParameter(PoContextParameter contextParameter) {
		this.contextParameters.add(contextParameter);
		contextParameter.setAction(this);
	}

	@Override
	public Collection<PoFile> getConfigFiles() {
		return getFiles();
	}

	@Override
	public void addConfigFile(PoFile file) {
		addFile(file);
	}

	/** @see PoAction#refreshOnComingBack */
	public Boolean getRefreshOnComingBack() {
		return refreshOnComingBack;
	}

	/** @see PoAction#refreshOnComingBack */
	public void setRefreshOnComingBack(Boolean refreshOnComingBack) {
		this.refreshOnComingBack = refreshOnComingBack;
	}
	
}
