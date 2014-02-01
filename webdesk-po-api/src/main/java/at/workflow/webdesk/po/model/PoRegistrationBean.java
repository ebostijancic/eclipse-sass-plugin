package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.workflow.webdesk.tools.hibernate.HibernateAnnotationsRegistration;
import at.workflow.webdesk.tools.hibernate.HibernateHbmRegistration;

/**
 * Define a singleton instance via spring per module to define all paths
 * where artifacts like actions, jobs, connectors, textmodules and so on
 * are located in order for them to get registered for later use. 
 * 
 * @author ggruber, hentner
 */
public class PoRegistrationBean implements HibernateHbmRegistration, HibernateAnnotationsRegistration {
    
    private List<String> registerActions = new ArrayList<String>();
    
    private List<String> registerJobs = new ArrayList<String>(); 
    
    private List<String> registerActionFlowScripts = new ArrayList<String>();
    
    private List<String> registerModuleFlowScripts = new ArrayList<String>();
    
    private List<String> registerConfigs = new ArrayList<String>();
    
    private List<String> registerJobConfigs = new ArrayList<String>();
    
    private List<String> registerConnectors = new ArrayList<String>();
    
    private List<String> syncTextModules = new ArrayList<String>();
    
    private List<String> registerKeyValueTypes = new ArrayList<String>();
    
    private List<String> registerMenuTree = new ArrayList<String>();
    
    private List<String> registerHbmFiles = new ArrayList<String>();
    
    private int versionNumber;
    
    private String folderOfPackage; 

    private String implFolder;
    
    private List<String> registerUpdateScripts;

	private List<String> registerHelpMessages = new ArrayList<String>();
	
	private List<String> beanPropertyNegativeList = new ArrayList<String>();
	
	private List<String> beanPropertyPasswordList = new ArrayList<String>();

	private List<String> annotatedClasses = new ArrayList<String>();
	
	private List<String> annotatedPackages = new ArrayList<String>();

	private Set<String> beanPropertyClassesNegativeList = new HashSet<String>();
        
	
    public void setBeanPropertyClassesNegativeList(Set<String> beanPropertyClassesNegativeList) {
		this.beanPropertyClassesNegativeList = beanPropertyClassesNegativeList;
	}

	public void setAnnotatedClasses(List<String> annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	public List<String> getBeanPropertyPasswordList() {
		return beanPropertyPasswordList;
	}

	public void setBeanPropertyPasswordList(List<String> beanPropertyPasswordList) {
		this.beanPropertyPasswordList = beanPropertyPasswordList;
	}

	public void setFolderOfPackage(String folderOfPackage) {
        this.folderOfPackage = folderOfPackage;
    }

    public void setRegisterActions(List<String> registerActions) {
        this.registerActions = registerActions;
    }

    public void setRegisterConfigs(List<String> registerConfigs) {
        this.registerConfigs = registerConfigs;
    }

    public void setRegisterActionFlowScripts(List<String> registerFlowScripts) {
        this.registerActionFlowScripts = registerFlowScripts;
    }

    public void setSyncTextModules(List<String> syncTextModules) {
        this.syncTextModules = syncTextModules;
    }

    public void setRegisterJobs(List<String> registerJobs) {
        this.registerJobs = registerJobs;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setRegisterUpdateScripts(List<String> registerUpdateScripts) {
        this.registerUpdateScripts = registerUpdateScripts;
    }

    public void setRegisterModuleFlowScripts(List<String> registerModuleFlowScripts) {
        this.registerModuleFlowScripts = registerModuleFlowScripts;
    }

    public void setRegisterJobConfigs(List<String> registerJobConfigs) {
        this.registerJobConfigs = registerJobConfigs;
    }

    public void setRegisterConnectors(List<String> registerConnectors) {
        this.registerConnectors = registerConnectors;
    }

    public String getFolderOfPackage() {
        return folderOfPackage;
    }

    public List<String> getRegisterActionFlowScripts() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/*/*.js" };
		return enforceDefaultValues(registerActionFlowScripts, defaults); 
    }

    public List<String> getRegisterActions() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/*/act-descr.xml" };
		return enforceDefaultValues(registerActions, defaults); 
    }

    public List<String> getRegisterConfigs() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/configs/**/*.xml" };
		return enforceDefaultValues(registerConfigs, defaults); 
    }

    public List<String> getRegisterConnectors() {
    	String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/connector/*/connector-descr.xml" };
		return enforceDefaultValues(registerConnectors, defaults);
    }

    public List<String> getRegisterJobConfigs() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/jobs/configs/**/*.xml" };
		return enforceDefaultValues(registerJobConfigs, defaults); 
    }

    public List<String> getRegisterJobs() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/jobs/*/job-descr.xml" };
		return enforceDefaultValues(registerJobs, defaults); 
    }

    public List<String> getRegisterModuleFlowScripts() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/*.js" };
		return enforceDefaultValues(registerModuleFlowScripts, defaults); 
    }

    /**
     * @return a list of classpath entries, eg.: classpath*:/at/workflow/webdesk/gw/impl/update/*\/*.xml
     */
    public List<String> getRegisterUpdateScripts() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/" + implFolder + "/update/*/*.xml" };
		return enforceDefaultValues(registerUpdateScripts, defaults); 
    }

    public List<String> getSyncTextModules() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/**/i18n.xml",
				"classpath*:/at/workflow/webdesk/" + folderOfPackage + "/**/i18n*.xml",};
		return enforceDefaultValues(syncTextModules, defaults); 		
    }

    public String getImplFolder() {
    	if (implFolder == null)
    		return "impl";	// default value
    	
        return implFolder;
    }

    public void setImplFolder(String implFolder) {
        this.implFolder = implFolder;
    }

	public List<String> getRegisterKeyValueTypes() {
		String[] defaults = {
			"classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/*/keyvaluetypes.xml" , 
			"classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/keyvaluetypes.xml"
		};
		return enforceDefaultValues(registerKeyValueTypes, defaults); 
	}
	
	/**
	 * enforces some default values!
	 * checkes whether the passed Arrays of strings is contained in the list
	 * if not adds them.
	 * 
	 * @param values List of Patterns (Strings)
	 * @param defaultValues: Array of Patterns (Strings)
	 */
	private List<String> enforceDefaultValues(List<String> values, String[] defaultValues) {
		List<String> defaultValuesList = Arrays.asList(defaultValues);
		List<String> ret = new ArrayList<String>();
		ret.addAll(values);
		ret.removeAll(defaultValuesList);
		ret.addAll(defaultValuesList);
		return ret;
	}

	public void setRegisterKeyValueTypes(List<String> registerKeyValueTypes) {
		this.registerKeyValueTypes = registerKeyValueTypes;
	}

	public List<String> getRegisterHelpMessages() {
		return registerHelpMessages;
	}

	public void setRegisterHelpMessages(List<String> registerHelpMessages) {
		this.registerHelpMessages = registerHelpMessages;
	}

	public List<String> getBeanPropertyNegativeList() {
		return beanPropertyNegativeList;
	}

	public void setBeanPropertyNegativeList(List<String> beanPropertyNegativeList) {
		this.beanPropertyNegativeList = beanPropertyNegativeList;
	}

	public List<String> getRegisterMenuTree() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/actions/*.admin.xml" };
		return enforceDefaultValues(registerMenuTree, defaults); 
	}

	public void setRegisterMenuTree(List<String> registerMenuTree) {
		this.registerMenuTree = registerMenuTree;
	}

	public List<String> getRegisterHbmFiles() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/model/*.hbm.xml" };
		return enforceDefaultValues(registerHbmFiles, defaults); 
	}

	public void setRegisterHbmFiles(List<String> registerHbmFiles) {
		this.registerHbmFiles = registerHbmFiles;
	}

	@Override
	public List<String> getHbmFileLocations() {
		return getRegisterHbmFiles();
	}

	@Override
	public List<String> getAnnotatedClasses() {
		String[] defaults = { "classpath*:/at/workflow/webdesk/" + folderOfPackage + "/model/*.class" };
		return enforceDefaultValues(annotatedClasses, defaults); 
	}

	@Override
	public List<String> getAnnotatedPackages() {
		return this.annotatedPackages;
	}

	public void setAnnotatedPackages(List<String> annotatedPackages) {
		this.annotatedPackages = annotatedPackages;
	}

	public Set<String> getBeanPropertyClassesNegativeList() {
		return beanPropertyClassesNegativeList;
	}

	@Override
	public String toString() {
		return getFolderOfPackage()+" v"+getVersionNumber();
	}

}
