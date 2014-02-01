package at.workflow.webdesk.po.impl.licence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.ActionPermission;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoLicenceActionService;
import at.workflow.webdesk.po.licence.Licence;
import at.workflow.webdesk.po.licence.LicenceHelper;
import at.workflow.webdesk.po.licence.LicenceReader;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.po.model.LicenceDefinition.LicenceCheckType;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionUniversal;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * Helper Class which contains outsourced method used in PoLicenceInterceptor
 * and PoShowLicences.
 * 
 * @author ggruber
 */
public class LicenceHelperImpl implements LicenceHelper {
	
	private Logger logger = Logger.getLogger(LicenceHelperImpl.class);

	private List<String> allPersonUidsCached = null;
	private Date lastAccessTimeStamp = null;
	
	private LicenceDefinition findLicenceDefinition(PoAction action) {
		
		LicenceReader licenceReader = getLicenceReader();
		
		for (LicenceDefinition licDef : licenceReader.getLicenceDefinitions()) {
			Licence licence = licenceReader.getLicenceMap().get(licDef.getName());
			
			// only process valid licences...
			if ("employee".equals(licDef.getName()) || licDef.getCurrentlyLicencedAmount()>0 && licence!=null ) {
				
				if (getAllActionNamesOfReferencedModules(licDef).contains(action.getName()))
						return licDef;
				
			}
		}
		return null;
			
	}
	
	private PoLicenceActionService getLicenceActionService() {
		return (PoLicenceActionService) WebdeskApplicationContext.getApplicationContext().getBean("PoLicenceActionService");
	}
	
	private LicenceReader getLicenceReader() {
		return (LicenceReader) WebdeskApplicationContext.getApplicationContext().getBean("LicenceReader");
	}
	
	private PoGeneralDbService getGeneralDbService() {
		return (PoGeneralDbService) WebdeskApplicationContext.getApplicationContext().getBean("PoGeneralDbService");
	}
	
	private DaoJdbcUtil getDaoJdbcUtil() {
		return (DaoJdbcUtil) WebdeskApplicationContext.getApplicationContext().getBean("DaoJdbcUtil");
	}
	
	@Override
	public Map<String, Integer> getAllowedActions(LicenceDefinition licDef,Map<String, Integer> allowedActions) {
        if (allowedActions==null)
        	allowedActions = Collections.synchronizedMap( new HashMap<String, Integer>() );
        
		// negativeList: given actions (in the licdef) are not allowed
        if (licDef.isNegativeList()) {
        	
        	List<String> actionNames = new ArrayList<String>();
        	
        	// get all modules of the licence definition
        	// and fetch its actions
    		actionNames.addAll( getAllActionNamesOfReferencedModules(licDef) );
    		
    		List<String> negativeActionNames = resolveActionNames(licDef);
        	
        	// insert all actions of  into a global map of allowedactions as key
        	// if its name is not contained in the negative list of the licencedefinition
        	// sum up amounts (value of MapEntry) per licencedefinition...
            for (String actionName : actionNames) {
                if (!negativeActionNames.contains(actionName))
					insertIntoGlobalMapOfAllowedActions(licDef, allowedActions,actionName);
            }
        } else {
            for (String actionName : resolveActionNames(licDef)) {
        		insertIntoGlobalMapOfAllowedActions(licDef, allowedActions,	actionName);                    
            }
        }
        return allowedActions;
	}
	
	/**
	 * resolve ActionNames of LicenceDefinition which can end with the '*' pattern,
	 * to the real actionnames.
	 * 
	 * @param licDef - LicenceDefinition to process
	 * @return List of Strings containing real actionnames (f.i. po_editAction)
	 */
	private List<String> resolveActionNames(LicenceDefinition licDef) {
		List<String> resolvedActionNames = new ArrayList<String>();
		for (String actionName : licDef.getActions()) {
			if (actionName.endsWith("*")) {
        		// allow asterix at end
        		String searchActionStr = actionName.substring(0, actionName.lastIndexOf("*"));
        		for (String resolvedActionName : searchForActionsStartingWith(searchActionStr, licDef)) {
        			resolvedActionNames.add(resolvedActionName);
        		}
        	} else {
        		resolvedActionNames.add(actionName);
        	}
		}
		return resolvedActionNames;
	}
	
	/**
	 * Query through all referenced Modules of the passed LicenceDefinition
	 * and return its Actions as strings within a List
	 */
	private List<String> getAllActionNamesOfReferencedModules(LicenceDefinition licDef) {
		List<String> actionNames = new ArrayList<String>();
      	for (String module : licDef.getModules()) {
    		actionNames.addAll(getLicenceActionService().findActionNamesOfModule(module));
    	}
      	return actionNames;
	}
	
	private List<String> searchForActionsStartingWith(String searchString, LicenceDefinition licDef) {
		List<String> ret = new ArrayList<String>();
		for (String actionName : getAllActionNamesOfReferencedModules(licDef)) {
			if (actionName.startsWith(searchString)) {
				ret.add(actionName);
			}
		}
		return ret;
	}

	private void insertIntoGlobalMapOfAllowedActions(LicenceDefinition licDef,
			Map<String, Integer> allowedActions, String actionName) {
		if (allowedActions.containsKey(actionName)) {
			if (licDef.getCurrentlyLicencedAmount()>0) {
		        Integer oldAmount = allowedActions.get(actionName);
		        allowedActions.put(actionName,new Integer(oldAmount.intValue()+licDef.getCurrentlyLicencedAmount()));
			}
		} else {
		    allowedActions.put(actionName,new Integer(licDef.getCurrentlyLicencedAmount()));
		}
	}

	@Override
	public List<LicenceInfo> getLicenceInfo() {
		return getLicenceInfo(false);
	}
	
	@Override
	public List<LicenceInfo> getLicenceInfo(boolean retrieveInfosForAllLicenceDefinitions) {
		
		PoLicenceActionService licenceActionService = getLicenceActionService();
		LicenceReader licenceReader = getLicenceReader();
		
		Map<String, Integer> mergedMapOfAllAllowedActions = new HashMap<String, Integer>();
		List<LicenceInfo> licenceInfos = new ArrayList<LicenceInfo>();
		
		for (LicenceDefinition licDef : licenceReader.getLicenceDefinitions()) {
			Licence licence = licenceReader.getLicenceMap().get(licDef.getName());
			
			// only process valid licences...
			if (retrieveInfosForAllLicenceDefinitions || "employee".equals(licDef.getName()) || licDef.getCurrentlyLicencedAmount()>0 && licence!=null ) {
				
				String name = licDef.getName();
				if (licence!=null && licence.getExpires()!=null) {
					name = licDef.getName() + " [ " + 
					DateTools.toDateFormat(licence.getExpires());
					if (licence.getExpires().before(new Date()))
						name+= " - expired]";
					else
						name+= " ]";
				}
				
				String description = "po_licence_" + licDef.getName() + "_description";
				int amount = licDef.getCurrentlyLicencedAmount();
				
				String[] modules = new String[licDef.getContexts().size()];
				
				int i=0;
				for (Iterator<String> moduleI = licDef.getContexts().iterator(); moduleI.hasNext();) {
					modules[ i++ ] = moduleI.next();
				}
				
				Integer maxInUse = 0;
				
				if (licDef.getSql() != null && !"".equals(licDef.getSql())) {
					maxInUse = new Long(getCurrentlyUsedVolumeAmount(licDef)).intValue();
				}
				
				Licence lic = licenceReader.getLicenceMap().get(licDef.getName());
				Date expires = null;
				if (lic!=null)
					expires = lic.getExpires();
				
				LicenceInfo licenceInfo = new LicenceInfo(name, description, modules, maxInUse, amount, expires);
				
				
				if (licDef.getCheckType() == null || licDef.getCheckType().equals(LicenceCheckType.ACTIVE_USERS_ACCESSING_ACTIONS) ) {
					
					Map<String, Integer> actions = getAllowedActions(licDef, null);
					for (Iterator<String> actionI = actions.keySet().iterator(); actionI.hasNext();) {
						String action = actionI.next();
						PoAction poAction = licenceActionService.findActionByName(action);
						
						Integer oldAmount = null;
						Integer newAmount = null;
						Integer sum = 0; 
						if (poAction!=null && poAction.getModule()!=null && !poAction.getModule().isDetached()) {
							int noOfUsersOfAction = getNoOfUsersWithPermissionForActionAndChildConfigs(poAction);
							if (noOfUsersOfAction > maxInUse) {
								maxInUse = noOfUsersOfAction;
							}
						
							if (mergedMapOfAllAllowedActions.containsKey(action)) {
								oldAmount = mergedMapOfAllAllowedActions.get(action);
								newAmount = actions.get(action);
								sum = oldAmount + newAmount;
							} else {
								sum = actions.get(action);
							}
							
							LicenceInfoActionDetail licenceInfoActionDetail = new LicenceInfoActionDetail(poAction, noOfUsersOfAction, oldAmount, newAmount, sum);
							licenceInfo.addLicenceInfoActionDetail(licenceInfoActionDetail);
						}
					}
					mergedMapOfAllAllowedActions = getAllowedActions(licDef, mergedMapOfAllAllowedActions);
				}
				
				// only add licenceinfos, where we have licenced something
				// or we have used some artifacts which are licence protected
				if ( licenceInfo.getMaxInUse() > 0 || licenceInfo.getAmount() > 0 )
					licenceInfos.add(licenceInfo);
			}
		}
		
		return licenceInfos;
	}
	
	public int getNoOfUsersWithPermissionForAction(PoAction poAction) {
		return getNumberOfPersonsAllowedToExecuteAction(getLicenceActionService(), poAction, false);
	}
	
	@Override
	public int getNoOfUsersWithPermissionForActionAndChildConfigs(	PoAction poAction) {
		return getNumberOfPersonsAllowedToExecuteAction(getLicenceActionService(), poAction, true);
	}
	

	private int getNumberOfPersonsAllowedToExecuteAction(PoLicenceActionService licenceActionService, PoAction poAction, boolean checkChilds) {
		
		assert (poAction!=null && licenceActionService!=null);
		
		if (checkChilds==true && (poAction.getActionType()==PoConstants.ACTION_TYPE_CONFIG
				|| poAction.getActionType()==PoConstants.ACTION_TYPE_PROCESS)) {
			throw new IllegalArgumentException("You have to pass a PoAction object of type 'action'!");
		}
		
		final String actionName = poAction.getName();
		
		logger.trace("    getNumberOfPersonsAllowedToExecuteAction(): " + actionName + "->findAllPersmissionsForAction");
		
		List<PoAPermissionBase> actionAssignments = licenceActionService.findAllPermissionsForActionF(poAction,new Date());
		
		if (checkChilds) {
			logger.trace("    getNumberOfPersonsAllowedToExecuteAction(): " + actionName + "->findAllPersmissionsForAction for childs");
			
			Iterator<PoAction> poActionI = poAction.getChilds().iterator();
			while (poActionI.hasNext()) {
				actionAssignments.addAll(licenceActionService.findAllPermissionsForActionF(poActionI.next(),new Date()));
			}
		}
		
		/** struct to store Lists of allowed/disallowed PersonUids per action */
		class ActionPermissions {
			Set<String> allowedPersonUids = new HashSet<String>();
			Set<String> disallowedPersonUids = new HashSet<String>();
		}
		
		// build map per action
		Map<PoAction, ActionPermissions> permissionsPerAction = new HashMap<PoAction, ActionPermissions>();
		Set<String> negativeResult = new HashSet<String>();
		
		// bugfix for:
		// http://intranet/intern/ifwd_mgm.nsf/0/86ED4A7A04849777C12579790036DDE5?OpenDocument 
		// notes://Miraculix/intern/ifwd_mgm.nsf/0/86ED4A7A04849777C12579790036DDE5?EditDocument
		for (PoAPermissionBase apb : actionAssignments ) {
			ActionPermissions perms = permissionsPerAction.get(apb.getAction());
			if (perms==null) {
				perms = new ActionPermissions();
				permissionsPerAction.put(apb.getAction(), perms);
			}
			
			Set<String> l = licenceActionService.findAssignedPersonsOfAction(apb);
			
			if (apb.isNegative())
				perms.disallowedPersonUids.addAll(l);
			else 
				perms.allowedPersonUids.addAll(l);
			
		}
		
		List<PoAction> actionAndChilds = new ArrayList<PoAction>();
		actionAndChilds.add(poAction);
		if (checkChilds) actionAndChilds.addAll(poAction.getChilds());
		
		List<String> allPersonUidsCached = null;
		
		for (PoAction act : actionAndChilds ) {
			ActionPermissions perms = permissionsPerAction.get(act);
			if (perms==null) {
				perms = new ActionPermissions();
				permissionsPerAction.put(act, perms);
			}
			if ( act.isUniversallyAllowed() ) {
				if (allPersonUidsCached==null) {
					allPersonUidsCached = getUidsOfAllActivePersons();
				}
				perms.allowedPersonUids.addAll( allPersonUidsCached );
			}
			
		}
		
		Set<String> completeAllowedUids = new HashSet<String>();
		
		for (PoAction act : permissionsPerAction.keySet() ) {
			ActionPermissions perms = permissionsPerAction.get(act);
			Set<String> allowedUids = new HashSet<String>();
			allowedUids.addAll(perms.allowedPersonUids);
			allowedUids.removeAll(perms.disallowedPersonUids);
			
			completeAllowedUids.addAll( allowedUids );
		}
		
		return completeAllowedUids.size();
	}
	
	/** Get UIDs of all active persons and cache the result internally for 5 minutes */
	private List<String> getUidsOfAllActivePersons() {
		
		if (allPersonUidsCached==null || lastAccessTimeStamp==null || lastAccessTimeStamp.before( DateUtils.addMinutes( DateTools.now(), -5))) {
			
			long startTime = System.currentTimeMillis();
			Date refTimeStamp = DateTools.nowWithMinutePrecision();
			allPersonUidsCached = getGeneralDbService().getElementsAsList("select UID from PoPerson where activeUser=true and validfrom<? and validto>?", new Object[] { refTimeStamp, refTimeStamp });
			if (logger.isTraceEnabled())
				logger.trace("Getting UIDs of all persons from datbase needed " + (System.currentTimeMillis()-startTime) + "ms.");
			
			lastAccessTimeStamp = DateTools.now();
		}
		
		return allPersonUidsCached;
	}
	
	public long getCurrentlyUsedVolumeAmount(LicenceDefinition ld) {

		long res = 0;
		
		try {
	        if (ld.getDialect().equals("hql")) {
	        	res = executeCountHql(ld.getSql());
	        } else {
	            res = getDaoJdbcUtil().queryForInt(ld.getSql(),ld.getWhichDb());
	        }
		} catch ( Exception e) {
			this.logger.warn("Problems evaluating licence volume query: " + ld.getSql() );
		}
        return res;
	}
	
    private long executeCountHql(String hql) {
    	if (this.logger.isDebugEnabled())
    		this.logger.debug("execute query for licencecheck: " + hql);
    	
		@SuppressWarnings("unchecked")
		List<Long> rows = getGeneralDbService().getElementsAsList(hql, null);
    	Long res = rows.get(0);

    	if (this.logger.isDebugEnabled())
    		this.logger.debug("query returned: " + res.longValue());
    	
    	return res.longValue();
    }

	@Override
	public int getMaxNoOfUsersLicencedForAction(PoAction poAction) {
		LicenceDefinition licDef = findLicenceDefinition( poAction );
		if (licDef==null) {
			Licence basicLicence = getLicenceReader().getLicenceMap().get("basic");
			if (basicLicence!=null)
				return basicLicence.getAmount();
			else 
				return -1;
		}
					
		return licDef.getCurrentlyLicencedAmount();
	}

	@Override
	public int getNoOfUsersResultingOfPermission( ActionPermission actionPermission ) {
		if (actionPermission instanceof PoAPermissionUniversal)
			return getUidsOfAllActivePersons().size();
		else {
			PoAPermissionBase apb = (PoAPermissionBase) actionPermission;
			int noOfUsers = getLicenceActionService().findAssignedPersonsOfAction(apb).size();
			if (apb.isNegative())
				return -noOfUsers;
			else
				return noOfUsers;
					
		}
	}



}
