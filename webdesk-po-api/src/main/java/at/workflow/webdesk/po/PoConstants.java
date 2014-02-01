package at.workflow.webdesk.po;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.tools.date.DateTools;

/**
 * TODO: JavaDoc is missing everywhere here! Each public constant needs a description!
 * 
 * Created on 02.06.2005
 * @author hentner
 */
public class PoConstants {

    public static final String DEFAULT_DATE_PATTERN = "dd.MM.yy HH:mm";
    
	/**
	 * Possible PoOrgStructure.orgType value, telling the semantic of e.g. a PoGroup with that OrgStructure.
	 * @deprecated use PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY.
	 */
    public static final int STRUCTURE_TYPE_ORGANISATION_HIERARCHY = 1;
	/**
	 * Possible PoOrgStructure.orgType value, telling the semantic of e.g. a PoGroup with that OrgStructure.
	 * @deprecated use PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS.
	 */
    public static final int STRUCTURE_TYPE_COSTCENTERS = 2;
	/**
	 * Possible PoOrgStructure.orgType value, telling the semantic of e.g. a PoGroup with that OrgStructure.
	 * @deprecated use PoOrgStructure.STRUCTURE_TYPE_PROJECTHIERARCHY.
	 */
    public static final int STRUCTURE_TYPE_PROJECTHIERARCHY = 3;
	/**
	 * Possible PoOrgStructure.orgType value, telling the semantic of e.g. a PoGroup with that OrgStructure.
	 * @deprecated use PoOrgStructure.STRUCTURE_TYPE_CUSTOMGROUPS.
	 */
    public static final int STRUCTURE_TYPE_CUSTOMGROUPS = 4;
    /**
     * Possible PoOrgStructure.orgType value, telling the semantic of e.g. a PoGroup with that OrgStructure.
	 * @deprecated use PoOrgStructure.STRUCTURE_TYPE_LOCATIONS.
     */
    public static final int STRUCTURE_TYPE_LOCATIONS = 5;
    
    public static final int INTEGER = 1;
    public static final int STRING = 2;
    public static final int BOOLEAN = 3;
    public static final int DOUBLE = 4;
    
    public static final String COLUMNTYPE_STRING = "COLUMNTYPE_STRING";
	public static final String COLUMNTYPE_DATE = "COLUMNTYPE_DATE";
	public static final String COLUMNTYPE_BOOLEAN = "COLUMNTYPE_BOOLEAN";
	public static final String COLUMNTYPE_NUMBER = "COLUMNTYPE_NUMBER";
    
    // Discriminator PoRoleCompetenceAll
    public static final String ROLE_HOLDER_BASE_ALL	 = "CA";
    public static final String ROLE_HOLDER_COMPETENCE_PERSON = "CP";
    public static final String ROLE_HOLDER_COMPETENCE_GROUP  = "CG";
    public static final String ROLE_HOLDER_COMPETENCE_CLIENT  = "CC";
    
    // Roletypes - legal/dummy role
    
    /**
     * use PoRole.NORMAL_ROLE instead
     */
    @Deprecated 
    public static final int NORMAL_ROLE = 1;
    
    /**
     * Use PoRole.DUMMY_ROLE instead
     */
    @Deprecated  
    public static final int DUMMY_ROLE = 2;
    
    
    // Discriminator PoAPermission
    public static final String PERMISSION_TYPE_CLIENT ="AC";
    public static final String PERMISSION_TYPE_ROLE ="AR";
    public static final String PERMISSION_TYPE_PERSON ="AP";
    public static final String PERMISSION_TYPE_GROUP ="AG";
    
    
    // Different View Permission types
    public static final int VIEW_PERMISSION_TYPE_NULL = -1; // for negative Permissions
    /**
     *  @deprecated use VIEW_PERMISSION_TYPE_OWN_PERSON instead
     */
    public static final int VIEW_PERMISSION_TYPE_NONE = 0;
    public static final int VIEW_PERMISSION_TYPE_OWN_PERSON = 0;
    public static final int VIEW_PERMISSION_TYPE_OWN_ORG_UNIT = 1;
    public static final int VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS = 2;
    
    /**
     * @deprecated  USE VIEW_PERMISSION_TYPE_ROLE_COMPETENCE
     */
    public static final int VIEW_PERMISSION_TYPE_DUMMY_ROLE = 3;
    public static final int VIEW_PERMISSION_TYPE_ROLE_COMPETENCE = 3;
    
    public static final int VIEW_PERMISSION_TYPE_OWN_CLIENT = 4;
    public static final int VIEW_PERMISSION_TYPE_ALL_CLIENTS = 5;
    
    
    
    // File Types 
    public static final int FLOWSCRIPTFUNCTION = 1;
    public static final int XSLTSTYLESHEET = 2;
    public static final int JXTEMPLATE = 3;
    public static final int CONFIG = 4;
    public static final int FORM_DEFINITION = 5;
    public static final int FORM_TEMPLATE = 6;
    
    
    // different Action Types 
    public static final int ACTION_TYPE_ACTION = 1;
    public static final int ACTION_TYPE_CONFIG = 2;
    public static final int ACTION_TYPE_PROCESS =3;
    public static final int ACTION_TYPE_CUSTOM = 4;
    
    
    public static int getActionTypeFromPostfix(String postfix) {
    	if (postfix.equals(ACTION_POSTFIX_ACTION))
    		return ACTION_TYPE_ACTION;
    	else if(postfix.equals(ACTION_POSTFIX_CONFIG))
    		return ACTION_TYPE_CONFIG;
    	else if(postfix.equals(ACTION_POSTFIX_PROCESS))
    		return ACTION_TYPE_PROCESS;
    	else
    		return ACTION_TYPE_CUSTOM;
    }
    
    // different Action Postfixes
    public static final String ACTION_POSTFIX_ACTION = "act";
    public static final String ACTION_POSTFIX_CONFIG = "cact";
    public static final String ACTION_POSTFIX_PROCESS = "proc";
    
    
    // different File Types 
    public static final int FILE_DEFINITION=1;
    public static final int FILE_TEMPLATE=2;
    public static final int FILE_XSLT=3;
    public static final int FILE_FLOWSCRIPT=4;
    public static final int FILE_XML=5;
    
    // Synchronisation variants
    public static final int DB_FIRST = 1;
    public static final int SYSTEM_FIRST = 2;
    public static final int NEWER_FIRST = 3;
    
    // EHCACHE
    
    public static final String PERMISSIONCACHE="at.workflow.webdesk.po.PERMISSIONCACHE";
    public static final String QUERYCACHE="QUERYCACHE";
    public static final String MENUCACHE="at.workflow.webdesk.po.MENUCACHE";
    public static final String LOCKCACHE = "LOCKCACHE";

    public static final int HOURLY_TRIGGER = 0;
    public static final int DAILY_TRIGGER = 1;
    public static final int WEEKLY_TRIGGER = 2;
    public static final int MONTHLY_TRIGGER = 3;
    public static final int SIMPLE_TRIGGER = 4;
    public static final int CRON_TRIGGER = 5;
    public static final int MINUTELY_TRIGGER = 6;

    public static final int JOB = 1;
    public static final int JOB_CONFIG =2;

    public static final int SYNC_SOURCE_DEST = 1;
    public static final int SYNC_DEST_SOURCE = 2;
    public static final int SYNC_BOTH = 3;
    
    public static final String SELECTIONLIST_TEXTMODULE_PREFIX="selectionlist_";
    
    public static final int SEARCH_DIRECTION_NONE =0;
    
    public static final int SEARCH_DIRECTION_UP =1;

    public static final int SEARCH_DIRECTION_DOWN =2;

    public static final String BEAN_NAME = "BeanName";

    public static final String METHOD_NAME = "MethodName";

    public static final String WHICH_ACTIONS = "WhichActions";

    public static final String SQL_CHECK = "SQLTOCHECK";

    public static final String TRIGGER_SCHEDULED = "Trigger Scheduled";

    public static final String IMMEDIATE_TRIGGER = "Manually Started";
    
    /**
     * The url of the injectionlist.
     * the list is used in order to inject lists in more than one bean.
     */
    public static final String URL_OF_INJECTIONLIST ="at.workflow.webdesk.tools.injectionHelper.InjectionList";

	public static final short GROUP = 0;
	
	public static final short CLIENT = 1;
	public static final short ROLE = 2;
	public static final short ORGSTRUCTURE = 3;
	public static final short PERSON = 4;

	public static final String COMMUNICATIONCACHE = "COMMUNICATIONCACHE";

	public static final String DISTRIBUTED = "isDistributed";
	
    public static List<Integer> getScheduleTypes() {
    	ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(new Integer(HOURLY_TRIGGER));
        al.add(new Integer(DAILY_TRIGGER));
    	al.add(new Integer(WEEKLY_TRIGGER));
    	al.add(new Integer(MONTHLY_TRIGGER));
    	al.add(new Integer(SIMPLE_TRIGGER));
    	al.add(new Integer(CRON_TRIGGER));
    	al.add(new Integer(MINUTELY_TRIGGER));
    	return al;
    }
    
    
	/**
	 * Delivers the default date for validTo when initialized. 
	 * @return a new infinity Date, taken from DateTools.INFINITY_TIMEMILLIS.
	 */
	public static final Date getInfDate() {
		return DateTools.INFINITY;
	}
	

	
    
    /**
     * @return a list of ViewPermissionTypes as Integer Objects 
     */
    public static List<Integer> getViewPermissions() {
    	ArrayList<Integer> al = new ArrayList<Integer>();
    	al.add(new Integer(VIEW_PERMISSION_TYPE_OWN_PERSON));
    	al.add(new Integer(VIEW_PERMISSION_TYPE_OWN_ORG_UNIT));
    	al.add(new Integer(VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS));
    	al.add(new Integer(VIEW_PERMISSION_TYPE_ROLE_COMPETENCE));
    	al.add(new Integer(VIEW_PERMISSION_TYPE_OWN_CLIENT));
    	al.add(new Integer(VIEW_PERMISSION_TYPE_ALL_CLIENTS));
    	return al;
    }
    
    public static List<Integer> getActionTypes() {    	
    	ArrayList<Integer> a = new ArrayList<Integer>();
    	a.add(new Integer(ACTION_TYPE_ACTION));
    	a.add(new Integer(ACTION_TYPE_CONFIG));
    	a.add(new Integer(ACTION_TYPE_PROCESS));
    	return a;
    }
    
    public static List<String> getViewPermissionsAsString() {
   	
    	ArrayList<String> a = new ArrayList<String>();   	    
    	a.add(new Integer(VIEW_PERMISSION_TYPE_OWN_PERSON).toString());
    	a.add(new Integer(VIEW_PERMISSION_TYPE_OWN_ORG_UNIT).toString());
    	a.add(new Integer(VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS).toString());
    	a.add(new Integer(VIEW_PERMISSION_TYPE_ROLE_COMPETENCE).toString());
    	a.add(new Integer(VIEW_PERMISSION_TYPE_OWN_CLIENT).toString());
    	a.add(new Integer(VIEW_PERMISSION_TYPE_ALL_CLIENTS).toString());
    	return a;
    }
    
    public static List<String> getOrgStructureTypesAsString() {
    	
    	ArrayList<String> a = new ArrayList<String>();
    	a.add(new Integer(STRUCTURE_TYPE_ORGANISATION_HIERARCHY).toString());
    	a.add(new Integer(STRUCTURE_TYPE_COSTCENTERS).toString());
    	a.add(new Integer(STRUCTURE_TYPE_PROJECTHIERARCHY).toString());
    	a.add(new Integer(STRUCTURE_TYPE_CUSTOMGROUPS).toString());
    	a.add(new Integer(STRUCTURE_TYPE_LOCATIONS).toString());
    	return a;
    }
            
    public static List<String> getActionTypesAsString() {
    	
    	ArrayList<String> a = new ArrayList<String>();
    	a.add(new Integer(ACTION_TYPE_ACTION).toString());
    	a.add(new Integer(ACTION_TYPE_CONFIG).toString());
    	a.add(new Integer(ACTION_TYPE_PROCESS).toString());
    	return a;
    }
    
    /**
     * @return hardcoded languages de/Deutsch and en/English.
     */
	public static Map<String, String> getStandardLanguagesMap() {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("de", "Deutsch");
		ret.put("en", "English");
		return ret;
	}
}


