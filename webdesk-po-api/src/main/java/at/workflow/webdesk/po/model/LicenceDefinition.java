package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Licence Definition file defines various licence defintions.<br>
 * <br>
 * Each licence definition ascertain which actions are allowed <br>
 * if negativeList is false or which actions are not allowed (nl=true).<br>
 * It also assess which modules will be registered. <br>
 * 
 * If checkType is <br>
 * 1 ... the amount of active users accessing actions <br>
 * 2 ... the amount of records per timerange (f.i. year) <br>
 * <br>
 *
 * @author ggruber, hentner
 */
public class LicenceDefinition {
	
	public enum LicenceCheckType { 
		ACTIVE_USERS_ACCESSING_ACTIONS(0), 
		RECORDS_PER_TIMERANGE(1); 
		
	 	private int code; 
	 	private LicenceCheckType(int c) {
	 		code = c; 
	 	} 
		public int getCode() {   
			return code; 
		}
		@Override
		public String toString() {   
			return new Integer(code).toString(); 
		}
		
		public static LicenceCheckType byCode(String code) {
			for (LicenceCheckType regValue : LicenceCheckType.values()) {
				if (code.equals(regValue.toString())) {
					return regValue;
				}
			}
			return null;
		}
	}
    
	/** name of the licence, which is used in licences where we reference this licence definition **/
    private String name;
    
    /** name of the licence which was extended. An extended licence refers to a licence which
     * is a 'subset' of functions of the actual licence. If a client has licence a which extends 
     * licence b and has a=100, b=150, it means, that he can have 100 persons with 'full' rights
     * (licence a), but only 50 users with 'limited' rights (licence b)
     */
    private String extendedLicence;
    
    /** list of spring application contexts which are licenced to be started with
     * this licence definition. */
    private List<String> contexts = new ArrayList<String>();
    
    /** list of actions for further licence processing **/
    private List<String> actions = new ArrayList<String>();
    
    /** defines whether the list actions is a positive or negative list in terms of licencing **/
    private boolean negativeList;
    
    /** sql for volume licence check **/
    private String sql;
    
    /** dialect for executing the volume licence check sql **/
    private String dialect;
    
    /** db where we should execute the volume licence check sql: webdesk or shark **/
    private String whichDb;
    
    /** these are the methodNames which trigger the volume licence checking **/
    private Set<String> triggerMethodsForVolumeLicencing = new HashSet<String>();
    
    
    /** the currently licenced amount **/
    private int currentlyLicencedAmount;

    /** list of modules, which are linked to this licence. meaning all actions of those
     * modules can be used with this licence. As requirement this modules also have to
     * be started with an appropriate spring application context 
     */
	private List<String> modules;
    
	/** currently not used **/
	private LicenceCheckType checkType;
	
	
	// ========== methods ============= 
	
    public int getCurrentlyLicencedAmount() {
        return currentlyLicencedAmount;
    }

    public void setCurrentlyLicencedAmount(int amount) {
        this.currentlyLicencedAmount = amount;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public LicenceCheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(LicenceCheckType checkType) {
        this.checkType = checkType;
    }

    public boolean isNegativeList() {
        return negativeList;
    }

    public void setNegativeList(boolean isNegativeList) {
        this.negativeList = isNegativeList;
    }

    public List<String> getContexts () {
        return contexts;
    }

    public void setContexts(List<String> contexts) {
        this.contexts= contexts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getWhichDb() {
        return whichDb;
    }

    public void setWhichDb(String whichDb) {
        this.whichDb = whichDb;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	public Set<String> getTriggerMethodsForVolumeLicencing() {
		return triggerMethodsForVolumeLicencing;
	}

	public void setTriggerMethodsForVolumeLicencing(Set<String> methodsToCheckWithSql) {
		this.triggerMethodsForVolumeLicencing = methodsToCheckWithSql;
	}

	public String getExtendedLicence() {
		return extendedLicence;
	}

	public void setExtendedLicence(String extendedLicence) {
		this.extendedLicence = extendedLicence;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof LicenceDefinition) {
			LicenceDefinition licDef2 = (LicenceDefinition) obj;
			return this.name.equals(licDef2.getName());
		}
		
		return super.equals(obj);
	}


    
}
