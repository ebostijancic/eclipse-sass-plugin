package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLicenceActionService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.model.PoAPermissionAdapter;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAPermissionUniversal;
import at.workflow.webdesk.po.model.PoAPermissionVisitor;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.IfDate;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.api.SysAdminUserInfo;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * This class provides services related to action permissions.
 * 
 * @author sdzuban, ggruber
 */
public class PoActionPermissionServiceImpl implements PoActionPermissionService {

	private static final String ARG_PERSON_UIDS = "argpersonuids";
	private static final String ARG_GROUP_UIDS = "arggroupuids";
	private static final String ARG_CLIENT_UIDS = "argclientuids";

	private class ActionPermissionComparator implements Comparator<PoAPermissionBase> {
		@Override
		public int compare(PoAPermissionBase o1, PoAPermissionBase o2) {
			return o1.getAction().getName().toLowerCase().compareTo(o2.getAction().getName().toLowerCase());
		}
	}

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/*----------------------------------- 
	 DAO's 
	 -----------------------------------*/
	private PoActionDAO actionDAO;
	private PoGroupDAO groupDAO;

	/*----------------------------------- 
	 Services 
	 -----------------------------------*/
	private PoActionService actionService;
	private PoOrganisationService orgService;
	private PoRoleService roleService;
	private PoLicenceActionService licenceActionService;

	/*----------------------------------- 
	 Util's 
	 -----------------------------------*/
	private SysAdminUserInfo sysAdminUserInfo;
	private CacheManager cacheManager;

	private Cache personActionPermissionCache;
	
	private PoActionPermissionService instanceWithAopAdvices;	// caching calls to findViewPermission() even internally
	
	
	@Override
	public PoAPermissionBase getAPermission(String uid) {
		return this.actionDAO.getAPermission(uid);
	}

	@Override
	public PoAPermissionRole getAPermissionRole(String uid) {
		return this.actionDAO.getAPermissionRole(uid);
	}

	@Override
	public String getViewPermissionAsString(PoAPermissionBase apb, PoGroup group, Date date) {

		if (apb.isNegative())
			return "";

		switch (apb.getViewPermissionType()) {

		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			return ""; // ev. an i18n key for all
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			return "";
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			return "";
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			PoAPermissionRole apr = (PoAPermissionRole) apb;
			List<CompetenceTarget> compTargets = this.findActionCompetenceTargets(group, apb, date);
			Iterator<CompetenceTarget> compTargetIt = compTargets.iterator();
			String targets = "";
			while (compTargetIt.hasNext()) {
				CompetenceTarget compTarget = compTargetIt.next();
				if (compTarget instanceof PoGroup) {
					if (apr.isViewInheritToChilds())
						targets += ((PoGroup) compTarget).getShortName() + "+";
					else
						targets += ((PoGroup) compTarget).getShortName();
				
				} else if (compTarget instanceof PoPerson) {

					targets += ((PoPerson) compTarget).getFullName();

				} else if (compTarget instanceof PoClient) {
					
					targets += ((PoClient) compTarget).getName();
				}
				if (compTargetIt.hasNext())
					targets += ", ";
			}
			return targets;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT:
			return group.getClient().getName();
		case PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS:
			List<PoClient> clients = this.orgService.loadAllClients();
			String res = "";
			for (PoClient client : clients)
				res += client.getName() + " ";
			return res;
		default:
			return "not defined";
		}
	}

	@Override
	public String getViewPermissionAsString(PoAPermissionBase apb, PoClient client, Date date) {

		if (apb.isNegative())
			return "";

		switch (apb.getViewPermissionType()) {

		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			return ""; // ev. an i18n key for all
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			return "";
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			return "";
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			return "";
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT:
			return client.getName();
		case PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS:
			List<PoClient> clients = this.orgService.loadAllClients();
			String res = "";
			for (PoClient clnt : clients)
				res += clnt.getName() + " ";
			return res;
		default:
			return "not defined";
		}
	}

	@Override
	public String getViewPermissionAsString(PoAPermissionBase apb, PoPerson person, Date date) {

		if (apb.isNegative())
			return "";

		switch (apb.getViewPermissionType()) {

		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			return person.getFullName(); // rights for the own person
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			return orgService.getPersonsHierarchicalGroup(person, date).getShortName();
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			return orgService.getPersonsHierarchicalGroup(person, date).getShortName() + " + ";
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			PoAPermissionRole apr = (PoAPermissionRole) apb;
			List<CompetenceTarget> compTargets = this.findActionCompetenceTargets(person, apb, date);
			Iterator<CompetenceTarget> compTargetIt = compTargets.iterator();
			String targets = "";
			while (compTargetIt.hasNext()) {
				CompetenceTarget compTarget = compTargetIt.next();
				if (compTarget instanceof PoGroup) {
					if (apr.isViewInheritToChilds())
						targets += ((PoGroup) compTarget).getShortName() + "+";
					else
						targets += ((PoGroup) compTarget).getShortName();
					
				} else if (compTarget instanceof PoPerson) {

					targets += ((PoPerson) compTarget).getFullName();
					
				} else if (compTarget instanceof PoClient) {
						
						targets += ((PoClient) compTarget).getName();
				}
				if (compTargetIt.hasNext())
					targets += ", ";
			}

			return targets;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT:
			return person.getClient().getName();
		case PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS:
			List<PoClient> clients = this.orgService.loadAllClients();
			String res = "";
			for (PoClient client : clients)
				res += client.getName() + " ";
			return res;
		default:
			return "not defined";
		}
	}

	@Override
	public List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date) {
		return this.licenceActionService.findAllPermissionsForActionF(action, date);
	}

	@Override
	public List<PoAPermissionGroup> findActionPermissionsOfGroup(PoGroup group, Date date) {

		List<PoAPermissionGroup> res = new ArrayList<PoAPermissionGroup>();
		res.addAll(actionDAO.findDirectlyLinkedActionPermissions(group, date));

		PoParentGroup parentGroup = orgService.getParentGroup(group, date);
		while (parentGroup != null) {
			group = parentGroup.getParentGroup();
			List<PoAPermissionGroup> actionPermissions = actionDAO
					.findDirectlyLinkedActionPermissions(group, date);
			for (PoAPermissionGroup apg : actionPermissions) {
				if (apg.isInheritToChilds())
					res.add(apg);
			}
			parentGroup = orgService.getParentGroup(group, date);
		}
		return res;
	}

	@Override
	public List<PoAPermissionGroup> findActionPermissionsOfGroupF(PoGroup group, Date date) {

		boolean itc = true; // inherit to child always true
		List<PoAPermissionGroup> res = new ArrayList<PoAPermissionGroup>();
		res.addAll(this.findDirectlyLinkedActionPermissionsF(group, date));
		PoParentGroup parentGroup = orgService.getParentGroup(group, date);
		while (parentGroup != null && itc) {
			group = parentGroup.getParentGroup();
			List<PoAPermissionGroup> actionPermissions = this
					.findDirectlyLinkedActionPermissionsF(group, date);

			for (PoAPermissionGroup apg : actionPermissions) {
				if (apg.isInheritToChilds())
					res.add(apg);
			}
			parentGroup = orgService.getParentGroup(group, date);
		}
		// sorting
		Comparator<PoAPermissionBase> myAC = new ActionPermissionComparator();
		Collections.sort(res, myAC);

		return res;
	}

	@Override
	public void deleteAPermission(PoAPermissionBase actionPermission) {
		actionPermission.setValidto(new Date());
		this.actionDAO.saveAPermission(actionPermission);
	}

	@Override
	public void deletePermissionCacheForAction(String actionName) {

		Cache cache = this.cacheManager.getCache(PoConstants.QUERYCACHE);
		PoAction action = licenceActionService.findActionByName(actionName);
		if (action != null)
			cache.remove(action.getUID());
	}

	@Override
	public void removeActionFromObject(PoAPermissionBase object) {

		object.historicize();
		PoAPermissionVisitor apV = new PoAPermissionAdapter();
		object.accept(apV);

		if (apV.getWhichClassVisited().equals("AR")) {
			PoAPermissionRole apr = (PoAPermissionRole) apV.getVisitedObject();
			if (apr.getRole().getRoleType() == PoRole.DUMMY_ROLE) {
				PoRole role = apr.getRole();
				role.setValidto(new Date());
				roleService.saveRole(role);
			}
		}
		actionDAO.saveAPermission(object);
	}

	/**
	 * resolves ViewPermissions for a particular User and action
	 * to a List of PoPersons
	 * 
	 * @param person
	 * @param action
	 * @return List of PoPerson Objects
	 */
	@Override
	public List<PoPerson> resolveViewPermissionsToListOfPersons(PoPerson person, PoAction action) {
		
		return resolveViewPermissionsToListOfPersons(person, action, null, null);
	}

	@Override
	public List<PoPerson> resolveViewPermissionsToListOfPersons(PoPerson person, PoAction action, Date from, Date to) {
		
		List<PoPerson> persons = new ArrayList<PoPerson>();

		if (isUserSysAdmin(person)) {
			List<PoClient> clients = this.orgService.loadAllClients();
			for (PoClient client : clients) {
				persons.addAll(this.orgService.findPersonsOfClient(client, HistorizationHelper.getNowWithHourPrecision()));
			}
			Collections.sort(persons);
		}
		else {
			Map<String, List<String>> permissions = getInstanceWithAopAdvices().findViewPermissions(person, action, HistorizationHelper.getNowWithHourPrecision());

			if (permissions.containsKey(PERSONS)) {
				// fetch persons
				for (String personUID : permissions.get(PERSONS)) {
					PoPerson pers = orgService.getPerson(personUID);
					Date myFrom = from == null ? DateTools.now() : from;
					Date myTo = to == null ? DateTools.now() : to;
					if (!pers.getValidfrom().after(myFrom) && pers.getValidto().after(myTo))
						persons.add(pers);
				}
			}

			// fetch persons of group
			if (permissions.containsKey(GROUPS)) {
				for (String groupUID : permissions.get(GROUPS)) {
					PoGroup myGroup = this.orgService.getGroup(groupUID);
					List<PoPerson> myPersons;
					if (from == null && to == null) {
						myPersons = this.orgService.findPersonsOfGroup(myGroup, HistorizationHelper.getNowWithHourPrecision());
					} else {
						myPersons = this.orgService.findPersonsOfGroup(myGroup, from, to);
					}
					for (PoPerson myPerson : myPersons) {
						if (!persons.contains(myPerson))
							persons.add(myPerson);
					}
				}
			}

			// fetch persons of client
			if (permissions.containsKey(CLIENTS)) {
				List<String> clientUIDs = permissions.get(CLIENTS);
				for (String clientUID : clientUIDs) {
					PoClient myClient = this.orgService.getClient(clientUID);
					List<PoPerson> myPersons;
					if (from == null && to == null) {
						myPersons = this.orgService.findPersonsOfClientF(myClient, HistorizationHelper.getNowWithHourPrecision());
					} else {
						myPersons = this.orgService.findPersonsOfClient(myClient, from, to);
					}
					for (PoPerson myPerson : myPersons) {
						if (!persons.contains(myPerson))
							persons.add(myPerson);
					}
				}
			}
			Collections.sort(persons);
		}
		return (persons);
	}

	/**
	 * resolves ViewPermissions for a particular User and action
	 * to a List of PoGroups
	 * TODO why the list of CLIENTS is not processed and expanded?
	 * @param person
	 * @param action
	 * @return List of PoGroup Objects
	 */
	@Override
	public List<PoGroup> resolveViewPermissionsToListOfGroups(PoPerson origPerson, PoAction action) {

		Date nowWithHourPrecision = HistorizationHelper.getNowWithHourPrecision();
		Map<String, List<String>> permissions = getInstanceWithAopAdvices().findViewPermissions(origPerson, action, nowWithHourPrecision);
		List<PoGroup> groups = groupDAO.resolveViewPermissionsToGroups(permissions, nowWithHourPrecision);
		Collections.sort(groups);
		return groups;
	}

	@Override
	public Map<PoOrgStructure, List<PoGroup>> resolveViewPermissionsToGroupsMap(PoPerson person, PoAction action) {

		List<PoGroup> l = resolveViewPermissionsToListOfGroups(person, action);

		@SuppressWarnings("unchecked")
		Map<PoOrgStructure, List<PoGroup>> osMap = new ListOrderedMap();

		Iterator<PoGroup> i = l.iterator();
		while (i.hasNext()) {
			PoGroup group = i.next();

			// is the orgstructure of the group already contained
			if (osMap.get(group.getOrgStructure()) != null) {
				// the osMap M(x,y), where x is the groupUid and y is a list of groupUids
				List<PoGroup> groupList = osMap.get(group.getOrgStructure());
				// is the group already contained in the group map
				if (!groupList.contains(group)) {
					// the group is already contained
					groupList.add(group);
					osMap.put(group.getOrgStructure(), groupList);
				}
			}
			else {
				// the osMap does not contain the given orgstructure
				List<PoGroup> groupList = new ArrayList<PoGroup>();
				groupList.add(group);
				osMap.put(group.getOrgStructure(), groupList);
			}
		}
		return osMap;
	}

	@Override
	public List<PoClient> resolveViewPermissionsToListOfClients(PoPerson origPerson, PoAction action) {
		
		List<PoClient> clients = new ArrayList<PoClient>();
		
		if (isUserSysAdmin(origPerson)) {
			
			clients = orgService.loadAllClients();
			
		} else {
			
			Map<String, List<String>> permissions = getInstanceWithAopAdvices().findViewPermissions(origPerson, action, HistorizationHelper.getNowWithHourPrecision());
			
			// fetch clients
			if (permissions.containsKey(CLIENTS)) {
				List<String> clientUIDs = permissions.get(CLIENTS);
				for (String clientUID : clientUIDs) {
					PoClient myClient = this.orgService.getClient(clientUID);
					if (!clients.contains(myClient))
						clients.add(myClient);
				}
			}
		}
		Collections.sort(clients);
		return clients;
	}
	
	@Override
	public boolean hasViewPermissionForAllClients(PoPerson person, PoAction action) {
		
		List<PoClient> existingClients = orgService.loadAllClients();
		List<PoClient> permittedClients = resolveViewPermissionsToListOfClients(person, action);
		if (existingClients.size() != permittedClients.size())
			return false;
		for (PoClient existing : existingClients)
			if (!permittedClients.contains(existing))
				return false;
		return true;
	}
	
	@Override
	public List<PoAPermissionBase> findAllActionPermissionsOfPerson(PoPerson person, Date date) {

		List<PoAPermissionBase> res = new ArrayList<PoAPermissionBase>();
		// get all actions from Client
		res.addAll(this.findActionPermissionsOfClient(person.getClient(), date));
		// get all actions from Person
		res.addAll(this.findActionPermissionsOfPerson(person, date));
		// get all actions from Role of person
		List<PoRole> roles = roleService.findRoles(person, date);

		if (roles != null) {
			for (PoRole role : roles) {
				res.addAll(this.findActionPermissionsOfRole(role, date));
			}
		}

		// get all actions from group (directly linked, as well as parentgroups, if inheritToChilds is set)
		List<PoGroup> groupList = orgService.findPersonsLinkedGroups(person, date);
		if (groupList != null) {
			for (PoGroup group : groupList) {
				List<PoAPermissionGroup> afg = this.findActionPermissionsOfGroup(group, date);
				if (afg != null)
					res.addAll(afg);

				for (PoRole role : roleService.findRoles(group, date)) {
					res.addAll(this.findActionPermissionsOfRole(role, date));
				}

				res.addAll(this.findInheritedActionPermissionsOfGroup(group, date));
			}
		}

		return res;
	}

	@Override
	public List<PoAPermissionBase> findAllActionPermissionsOfPersonF(PoPerson person, Date date) {

		List<PoAPermissionBase> res = new ArrayList<PoAPermissionBase>();

		// get all actions from Client
		res.addAll(this.findActionPermissionsOfClientF(person.getClient(), date));

		// get all actions from Person
		res.addAll(this.findActionPermissionsOfPersonF(person, date));

		// get all actions from Role
		List<PoRole> roles = roleService.findRoles(person, date);
		if (roles != null) {
			for (PoRole role : roles) {
				res.addAll(this.findActionPermissionsOfRoleF(role, date));
			}
		}

		// get all actions from group
		List<PoGroup> groups = orgService.findPersonsLinkedGroups(person, date);
		if (groups != null) {
			for (PoGroup group : groups) {
				List<PoAPermissionGroup> afg = this.findActionPermissionsOfGroupF(group, date);
				if (afg != null)
					res.addAll(afg);

				for (PoRole role : roleService.findRoles(group, date)) {
					res.addAll(this.findActionPermissionsOfRoleF(role, date));
				}

				res.addAll(this.findInheritedActionPermissionsOfGroupF(group, date));
			}
		}

		return res;
	}

	@Override
	public List<PoAPermissionBase> findAllActionPermissionsOfGroupF(PoGroup group, Date date) {

		List<PoAPermissionBase> res = new ArrayList<PoAPermissionBase>();

		// get all actions from Client
		res.addAll(this.findActionPermissionsOfClientF(group.getClient(), date));

		// get all actions from Group
		res.addAll(this.findActionPermissionsOfGroupF(group, date));

		// get all actions from Role
		List<PoRole> roles = roleService.findRolesF(group, date);

		if (roles != null) {
			for (PoRole role : roles) {
				res.addAll(this.findActionPermissionsOfRoleF(role, date));
			}
		}

		// add inherited Action Permissions
		res.addAll(findInheritedActionPermissionsOfGroupF(group, date));

		// sorting
		Comparator<PoAPermissionBase> myAC = new ActionPermissionComparator();
		Collections.sort(res, myAC);

		return res;
	}

	public List<PoAPermissionBase> findAllActionPermissionsOfGroup(PoGroup group, Date date) {

		List<PoAPermissionBase> res = new ArrayList<PoAPermissionBase>();

		// get all actions from Client
		res.addAll(this.findActionPermissionsOfClient(group.getClient(), date));

		// get all actions from Group
		res.addAll(this.findActionPermissionsOfGroup(group, date));

		// get all actions from Role
		List<PoRole> roles = roleService.findRoles(group, date);

		if (roles != null) {
			for (PoRole role : roles) {
				res.addAll(this.findActionPermissionsOfRole(role, date));
			}
		}

		// add inherited Action Permissions
		res.addAll(findInheritedActionPermissionsOfGroup(group, date));

		// sorting
		Comparator<PoAPermissionBase> myAC = new ActionPermissionComparator();
		Collections.sort(res, myAC);

		return res;
	}

	private List<PoAPermissionRole> findInheritedActionPermissionsOfGroup(PoGroup group, Date date) {
		List<PoAPermissionRole> res = new ArrayList<PoAPermissionRole>();
		PoParentGroup parentGroup = orgService.getParentGroup(group, date);
		while (parentGroup != null) {
			group = parentGroup.getParentGroup();
			// add all actions from Dummy-Roles
			// 'special' permissions assigned to parent groups
			// which have flag inheritToChilds set
			for (PoRole role : roleService.findRoles(group, date)) {
				if (role.getRoleType() == PoRole.DUMMY_ROLE) {
					for (PoAPermissionRole rolePerm : this.findActionPermissionsOfRole(role, date)) {
						if (rolePerm.isInheritToChilds()) {
							res.add(rolePerm);
						}
					}
				}
			}
			parentGroup = orgService.getParentGroup(group, date);
		}
		return res;
	}

	private List<PoAPermissionRole> findInheritedActionPermissionsOfGroupF(PoGroup group, Date date) {
		List<PoAPermissionRole> res = new ArrayList<PoAPermissionRole>();
		PoParentGroup parentGroup = orgService.getParentGroup(group, date);
		while (parentGroup != null) {
			group = parentGroup.getParentGroup();
			// add all actions from Dummy-Roles
			// 'special' permissions assigned to parent groups
			// which have flag inheritToChilds set
			for (PoRole role : roleService.findRolesF(group, date)) {
				if (role.getRoleType() == PoRole.DUMMY_ROLE) {
					for (PoAPermissionRole rolePerm : this.findActionPermissionsOfRoleF(role, date)) {
						if (rolePerm.isInheritToChilds()) {
							res.add(rolePerm);
						}
					}
				}
			}
			parentGroup = orgService.getParentGroup(group, date);
		}
		return res;
	}

	@Override
	public List<PoAPermissionUniversal> findUniversallyAllowedActions() {

		List<PoAction> actions = this.actionDAO.findActionsUniversallyAllowed();
		List<PoAPermissionUniversal> res = new ArrayList<PoAPermissionUniversal>();
		for (PoAction action : actions) {
			PoAPermissionUniversal apu = new PoAPermissionUniversal();
			apu.setAction(action);
			apu.setValidfrom(action.getValidfrom());
			apu.setValidto(action.getValidto());
			res.add(apu);
		}
		return res;
	}

	@Override
	public List<PoAction> findAllActionsOfPerson(PoPerson person, Date date) {

		List<PoAction> actions = findAllAllowedActionsOfPerson(person, date);
		List<PoAction> result = new ArrayList<PoAction>();

		result.addAll(actions);

		// add all parents
		for (PoAction action : actions) {
			if (action != null && action.getParent() != null && isPersonRefusedForAction(person, date, action.getParent())==false) {
				result.add(action.getParent());
			}
		}

		return result;
	}

	/** checks whether the person is explicitly blocked to use the passed action by an
	 * negative permission */
	private boolean isPersonRefusedForAction(PoPerson person, Date date, PoAction action) {
		
		List<Object> negativePerms = actionDAO.findByQueryAndNamedParameters("from PoAPermissionBase perm " +
					" where perm.negative=:neg and action=:action and " +
					" validfrom<:date and validto>:date", 
					new String[] { "neg", "action", "date" },
					new Object[] { true, action, date });
		
		class NegativePermissionChecker extends PoAPermissionAdapter {
			public NegativePermissionChecker(PoPerson person) {
				super();
				this.person = person;
			}

			private PoPerson person;
			
			private boolean negativePermissionApplies = false;
			
			@Override
			public void visit(PoAPermissionClient apc) {
				negativePermissionApplies = person.getClient().equals(apc.getClient());
			}
			
			@Override
			public void visit(PoAPermissionPerson app) {
				negativePermissionApplies = person.equals(app.getPerson());
			}
			
			@Override
			public void visit(PoAPermissionGroup apg) {
				List<PoGroup> groups = orgService.findPersonsLinkedGroups(person);
				
				Set<PoGroup> permissionGroups = new HashSet<PoGroup>();
				permissionGroups.add(apg.getGroup());
				
				// expand also all child groups
				if (apg.isInheritToChilds()) {
					permissionGroups.addAll( orgService.findAllChildGroupsFlat(apg.getGroup()) );
				}
				
				// check whether the groups have elements in common
				// if yes -> the negative permission applies!
				negativePermissionApplies = (Collections.disjoint(permissionGroups, groups)==false);
				
			}
			
			@Override
			public void visit(PoAPermissionRole apr) {
				List<PoPerson> roleHolders = roleService.findRoleHolders(apr.getRole());
				negativePermissionApplies = roleHolders.contains(person);
			}
			
			public boolean isNegativePermissionApplies() {
				return negativePermissionApplies;
			}
			
		}
		
		NegativePermissionChecker checker = new NegativePermissionChecker(person);
		for (Object permObj : negativePerms) {
			
			PoAPermissionBase perm = (PoAPermissionBase) permObj;
			perm.accept(checker);
			
			if (checker.isNegativePermissionApplies())
				return true;
			
		}
		
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	private List<PoAction> findAllAllowedActionsOfPerson(PoPerson person, Date queryDate) {

		List<PoAction> result = new ArrayList<PoAction>();
		List<String> resultUids = new ArrayList<String>();
		String cacheKey = person.getUserName();

		if (this.personActionPermissionCache.isKeyInCache(cacheKey)) {
			try {
				resultUids = (List<String>) personActionPermissionCache.get(cacheKey).getValue();

				for (String uid : resultUids) {
					result.add(this.actionDAO.get(uid));
				}
				return result;

			}
			catch (Exception e) {
				// something went wrong -> get it from DB!
			}
		}

		List<PoAPermissionBase> actionPermList = findAllActionPermissionsOfPerson(person, queryDate);

		Set<PoAction> prohibitedActions = new HashSet<PoAction>();
		for (PoAPermissionBase perm : actionPermList) {
			if (perm.isNegative()) {
				prohibitedActions.add(perm.getAction());
			}
		}

		// add universal actions
		Set<PoAction> actions = new HashSet<PoAction>();
		for (PoAction action : actionDAO.findAllUniversallyAllowedActions(queryDate)) {
			if (!prohibitedActions.contains(action)) {
				actions.add(action);
			}
		}
		
		// add actions from permission list
		// also take into account special property 'allowsAction' which can 
		// inherit permission from one action to another.
		for (PoAPermissionBase ap : actionPermList) {
			if (!prohibitedActions.contains(ap.getAction())) {
				actions.add(ap.getAction());
			}
			
			PoAction directlyAllowedAction = ap.getAction().getAllowsAction(); 
			if (directlyAllowedAction !=null && !prohibitedActions.contains(directlyAllowedAction)) {
				actions.add(directlyAllowedAction);
			}
			
			if (ap.getAction().getParent()!=null) {
				PoAction parentAllowedAction = ap.getAction().getParent().getAllowsAction(); 
				if (parentAllowedAction !=null && !prohibitedActions.contains(parentAllowedAction)) {
					actions.add(parentAllowedAction);
				}
			}
		}
		result.addAll(actions);

		for (PoAction action : actions) {
			resultUids.add(action.getUID());
		}

		// put into cache
		personActionPermissionCache.put(new Element(cacheKey, resultUids));

		return result;
	}
	
	

	@Override
	public List<PoAPermissionRole> findAllActionPermissionsOfRoleF(PoRole role, Date date) {

		return this.findActionPermissionsOfRoleF(role, date);
	}

	/*
	 * (non-Javadoc)
	 * @see at.workflow.webdesk.po.PoActionService#deleteAndFlushAPermission(at.workflow.webdesk.po.model.PoAPermissionBase)
	 */
	@Override
	public void deleteAndFlushAPermission(PoAPermissionBase aPermissionObject, Date date) {

		if (aPermissionObject.getValidfrom().after(date))
			actionDAO.deleteAPermission(aPermissionObject);
		else
			throw new PoRuntimeException(
						PoRuntimeException.ERROR_LINK_CANNOT_BE_DELETED_LINK_ALREADY_ACTIVE);
	}

	@Override
	public void changeValidityAPermission(PoAPermissionBase aPermissionObject, Date validFrom,
			Date validTo, boolean inheritToChild, boolean viewInheritToChild) {

		if (validFrom == null || validFrom.before(new Date()))
			validFrom = new Date();
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		PoAPermissionVisitor apV = new PoAPermissionAdapter();
		aPermissionObject.accept(apV);

		if (apV.getWhichClassVisited().equals(PoConstants.PERMISSION_TYPE_ROLE)) {
			PoAPermissionRole apr = (PoAPermissionRole) apV.getVisitedObject();

			List<PoAPermissionRole> rolePerms = actionDAO.findActionPermissionRoleWithActionAndRole(apr, validFrom, validTo);
			if (rolePerms.size() == 1) {
				PoAPermissionRole hApr = rolePerms.get(0);
				hApr.setValidfrom(validFrom);
				hApr.setValidto(validTo);
				hApr.setViewPermissionType(apr.getViewPermissionType());
				hApr.setViewInheritToChilds(viewInheritToChild);
				hApr.setInheritToChilds(inheritToChild);
				actionDAO.saveAPermission(hApr);
			}
			if (rolePerms.size() > 1) {
				Iterator<PoAPermissionRole> i = rolePerms.iterator();
				boolean firstRun = true;
				while (i.hasNext()) {
					PoAPermissionRole hApr = i.next();
					if (firstRun) {
						if (hApr.getValidfrom().before(validFrom))
							validFrom = hApr.getValidfrom();
						firstRun = false;
					}
					if (!i.hasNext()) { // last element
						if (hApr.getValidto().after(validTo))
							validTo = hApr.getValidto(); // validTo of the
						// last element is used as our new validTo
					}
					hApr.getAction().getPermissions().remove(hApr);
					hApr.getRole().getPermissions().remove(hApr);
					this.deleteAPermission(hApr);
				}
				PoAPermissionRole newRole = new PoAPermissionRole();
				newRole.setAction(aPermissionObject.getAction());
				newRole.setViewInheritToChilds(viewInheritToChild);
				newRole.setRole(apr.getRole());
				newRole.setType(apV.getWhichClassVisited());
				newRole.setValidfrom(validFrom);
				newRole.setValidto(validTo);
				newRole.setViewPermissionType(apr.getViewPermissionType());
				actionDAO.saveAPermission(newRole);
			}
		}
		if (apV.getWhichClassVisited()
				.equals(PoConstants.PERMISSION_TYPE_GROUP)) {
			PoAPermissionGroup apg = (PoAPermissionGroup) apV
					.getVisitedObject();
			apg.setInheritToChilds(inheritToChild);
			List<PoAPermissionGroup> groupPerms = actionDAO.findActionPermissionGroupWithActionAndGroup(apg, validFrom);

			if (groupPerms.size() == 1) {
				PoAPermissionGroup hApg = groupPerms.get(0);
				hApg.setValidfrom(validFrom);
				hApg.setValidto(validTo);
				hApg.setInheritToChilds(inheritToChild);
				hApg.setViewPermissionType(apg.getViewPermissionType());
				actionDAO.saveAPermission(hApg);
			}
			if (groupPerms.size() > 1) {
				Iterator<PoAPermissionGroup> i = groupPerms.iterator();
				boolean firstRun = true;
				while (i.hasNext()) {
					PoAPermissionGroup hApg = i.next();
					if (firstRun) {
						if (hApg.getValidfrom().before(validFrom))
							validFrom = hApg.getValidfrom();
						firstRun = false;
					}
					if (!i.hasNext()) { // last element
						if (hApg.getValidto().after(validTo))
							validTo = hApg.getValidto(); // validTo of the
						// last
						// element is used as
						// our new validTo
					}
					hApg.getAction().getPermissions().remove(hApg);
					hApg.getGroup().getPermissions().remove(hApg);
					this.deleteAPermission(hApg);
				}
				PoAPermissionGroup newGroup = new PoAPermissionGroup();
				newGroup.setAction(aPermissionObject.getAction());
				newGroup.setGroup(apg.getGroup());
				newGroup.setType(apV.getWhichClassVisited());
				newGroup.setValidfrom(validFrom);
				newGroup.setValidto(validTo);
				newGroup.setInheritToChilds(inheritToChild);
				newGroup.setViewPermissionType(apg.getViewPermissionType());
				actionDAO.saveAPermission(newGroup);
			}
		}
		if (apV.getWhichClassVisited().equals(
				PoConstants.PERMISSION_TYPE_CLIENT)) {
			PoAPermissionClient apc = (PoAPermissionClient) apV
					.getVisitedObject();

			apc.setValidfrom(validFrom);
			apc.setValidto(validTo);
			actionDAO.saveAPermission(apc);

			/*
			 * TODO vergleiche diese Variante mit den anderen! :)
			 * Verhalten mit gabriel abkl�ren (�berschneidungen)
			 */
			List<PoAPermissionClient> clientPerms = actionDAO.findActionPermissionClientWithActionAndClientF(apc.getAction(), apc.getClient(), new Date());
			if (clientPerms.size() == 1) {
				PoAPermissionClient hApc = clientPerms.get(0);
				hApc.setValidfrom(validFrom);
				hApc.setValidto(validTo);
				hApc.setViewPermissionType(apc.getViewPermissionType());
				actionDAO.saveAPermission(hApc);
			}
		}

		if (apV.getWhichClassVisited().equals(
				PoConstants.PERMISSION_TYPE_PERSON)) {
			PoAPermissionPerson app = (PoAPermissionPerson) apV.getVisitedObject();
			List<PoAPermissionPerson> personPerms = actionDAO.findActionPermissionPersonWithActionAndPersonF(app.getAction(), app.getPerson(), new Date());
			if (personPerms.size() == 1) {
				PoAPermissionPerson hApp = personPerms.get(0);
				hApp.setValidfrom(validFrom);
				hApp.setValidto(validTo);
				hApp.setViewPermissionType(app.getViewPermissionType());
				actionDAO.saveAPermission(hApp);
			}
			if (personPerms.size() > 1) {
				Iterator<PoAPermissionPerson> i = personPerms.iterator();
				boolean firstRun = true;
				while (i.hasNext()) {
					PoAPermissionPerson hApp = i.next();
					if (firstRun) {
						if (hApp.getValidfrom().before(validFrom))
							validFrom = hApp.getValidfrom();
						firstRun = false;
					}
					if (!i.hasNext()) { // last element
						if (hApp.getValidto().after(validTo))
							validTo = hApp.getValidto(); // validTo of the
						// last
						// element is used as
						// our new validTo
					}
					hApp.getAction().getPermissions().remove(hApp);
					hApp.getPerson().getPermissions().remove(hApp);
					actionDAO.deleteAPermission(hApp);
				}
				PoAPermissionPerson newPerson = new PoAPermissionPerson();
				newPerson.setAction(aPermissionObject.getAction());
				newPerson.setPerson(app.getPerson());
				newPerson.setType(apV.getWhichClassVisited());
				newPerson.setValidfrom(validFrom);
				newPerson.setValidto(validTo);
				newPerson.setViewPermissionType(app.getViewPermissionType());
				actionDAO.saveAPermission(newPerson);
			}
		}
	}

	/**
	 * @deprecated
	 */
	@Override
	public void addClientToAction(PoAction action, PoClient client,
			Date validFrom, Date validTo, int viewPermissionType) {

		this.assignPermission(action, client, validFrom, validTo, viewPermissionType);
	}

	@Override
	public void assignPermission(PoAction action, PoClient client,
			Date validFrom, Date validTo, int viewPermissionType) {

		assignPermission(action, client, validFrom, validTo, viewPermissionType, false);
	}

	private void assignPermission(PoAction action, PoClient client,
			Date validFrom, Date validTo, int viewPermissionType, boolean negative) {

		PoAPermissionClient apc = new PoAPermissionClient();
		apc.setAction(action);
		apc.setClient(client);
		apc.setType(PoConstants.PERMISSION_TYPE_CLIENT);
		apc.setNegative(negative);
		apc.setViewPermissionType(viewPermissionType);
		if (validFrom != null)
			apc.setValidfrom(validFrom);
		else
			apc.setValidfrom(new Date());
		if (validTo != null)
			apc.setValidto(validTo);
		else
			apc.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		action.addPermission(apc);
		actionDAO.saveAPermission(apc);
	}

	/**
	 * @deprecated
	 */
	@Override
	public void addGroupToAction(PoAction action, PoGroup group,
			Date validFrom, Date validTo, boolean inheritToChilds,
			int viewPermissionType) {

		this.assignPermission(action, group, validFrom, validTo, inheritToChilds, viewPermissionType, false);
	}

	@Override
	public void assignPermission(PoAction action, PoGroup group, Date validFrom, Date validTo,
			boolean inheritToChilds, int viewPermissionType) {

		this.assignPermission(action, group, validFrom, validTo, inheritToChilds, viewPermissionType, false);
	}

	private void assignPermission(PoAction action, PoGroup group, Date validFrom, Date validTo,
			boolean inheritToChilds, int viewPermissionType, boolean negative) {

		PoAPermissionGroup apg = new PoAPermissionGroup();
		apg.setAction(action);
		apg.setGroup(group);
		apg.setInheritToChilds(inheritToChilds);
		apg.setType(PoConstants.PERMISSION_TYPE_GROUP);
		apg.setNegative(negative);
		apg.setViewPermissionType(viewPermissionType);
		if (validFrom != null)
			apg.setValidfrom(validFrom);
		else
			apg.setValidfrom(new Date());
		if (validTo != null)
			apg.setValidto(validTo);
		else
			apg.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		action.addPermission(apg);
		actionDAO.saveAPermission(apg);
	}

	/**
	 * @deprecated
	 */
	@Override
	public void addPersonToAction(PoAction action, PoPerson person,
			Date validFrom, Date validTo, int viewPermissionType) {

		this.assignPermission(action, person, validFrom, validTo, viewPermissionType);
	}

	@Override
	public void assignPermission(PoAction action, PoPerson person,
			Date validFrom, Date validTo, int viewPermissionType) {

		this.assignPermission(action, person, validFrom, validTo, viewPermissionType, false);
	}

	private void assignPermission(PoAction action, PoPerson person, Date validFrom, Date validTo,
			int viewPermissionType, boolean negative) {

		PoAPermissionPerson app = new PoAPermissionPerson();
		app.setAction(action);
		app.setPerson(person);
		app.setType(PoConstants.PERMISSION_TYPE_PERSON);
		app.setNegative(negative);
		app.setViewPermissionType(viewPermissionType);
		if (validFrom != null)
			app.setValidfrom(validFrom);
		else
			app.setValidfrom(new Date());
		if (validTo != null)
			app.setValidto(validTo);
		else
			app.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		action.addPermission(app);
		// link on both sides
		person.getPermissions().add(app);

		actionDAO.saveAPermission(app);
	}

	/**
	 * @deprecated
	 */
	@Override
	public void addRoleToAction(PoAction action, PoRole role, Date validFrom,
			Date validTo, int viewPermissionType, boolean inheritToChilds, boolean viewInheritToChilds) {

		this.assignPermission(action, role, validFrom, validTo, viewPermissionType, inheritToChilds, viewInheritToChilds);
	}

	@Override
	public void assignPermission(PoAction action, PoRole role, Date validFrom, Date validTo,
			int viewPermissionType, boolean inheritToChilds, boolean viewInheritToChilds) {

		assignPermission(action, role, validFrom, validTo, viewPermissionType, inheritToChilds, viewInheritToChilds, false);
	}

	private void assignPermission(PoAction action, PoRole role, Date validFrom, Date validTo,
			int viewPermissionType, boolean inheritToChilds, boolean viewInheritToChilds, boolean negative) {

		PoAPermissionRole apr = new PoAPermissionRole();
		apr.setAction(action);
		apr.setRole(role);
		apr.setType(PoConstants.PERMISSION_TYPE_ROLE);
		apr.setNegative(negative);
		apr.setViewPermissionType(viewPermissionType);
		apr.setInheritToChilds(inheritToChilds);
		apr.setViewInheritToChilds(viewInheritToChilds);
		if (validFrom != null)
			apr.setValidfrom(validFrom);
		else
			apr.setValidfrom(new Date());
		if (validTo != null)
			apr.setValidto(validTo);
		else
			apr.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));

		action.addPermission(apr);
		actionDAO.saveAPermission(apr);

	}

	/**
	 * @deprecated
	 */
	@Override
	public void addPersonToActionWithCompetenceList(PoAction action,
			PoPerson person, Date validFrom, Date validTo,
			List<CompetenceTarget> competenceList, int ranking, boolean viewInheritToChilds) {

		this.assignPermissionWithCompetenceTargets(action, person, validFrom, validTo,
				competenceList, ranking, viewInheritToChilds);
	}

	@Override
	public void assignPermissionWithCompetenceTargets(PoAction action,
			PoPerson person, Date validFrom, Date validTo,
			List<CompetenceTarget> competenceList, int ranking, boolean viewInheritToChilds) {

		if (validFrom == null)
			validFrom = new Date();
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		if (competenceList != null) {

			PoRole dummyRole = this.roleService.findRoleByName(action.getName() + "_" + person.getFirstName()
					+ "_" + person.getLastName() + "_" + validFrom.getTime(), person.getClient(), validFrom);
			if (dummyRole == null) {
				dummyRole = new PoRole();
				dummyRole.setClient(person.getClient());
				dummyRole.setDescription("This is a private role of "
						+ person.getEmployeeId());
				dummyRole.setName(action.getName() + "_" + person.getFirstName()
						+ "_" + person.getLastName() + "_" + validFrom.getTime());
				dummyRole.setOrgType(orgService.getPersonsHierarchicalGroup(person, validFrom).getOrgStructure().getOrgType());
				dummyRole.setRoleType(PoRole.DUMMY_ROLE);
				dummyRole.setValidfrom(validFrom);
				dummyRole.setValidto(validTo);
				dummyRole.setParticipantId(dummyRole.getName());
				roleService.saveRole(dummyRole);
			}
			for (CompetenceTarget compTarget : competenceList) {
				try {
					if (compTarget instanceof PoPerson) {
						roleService.assignRoleWithPersonCompetence(dummyRole,
								person, (PoPerson) compTarget, validFrom, validTo,
								ranking);

					}
					else if (compTarget instanceof PoGroup) {
						PoGroup hgroup = (PoGroup) compTarget;
						roleService.assignRoleWithGroupCompetence(
									dummyRole, person, hgroup, validFrom,
									validTo, ranking);
					}
					else if (compTarget instanceof PoClient) {
						PoClient hClient = (PoClient) compTarget;
						roleService.assignRoleWithClientCompetence(
								dummyRole, person, hClient, validFrom,
								validTo, ranking);
					}
				}
				catch (Exception ne) {
					ne.printStackTrace();
				}
			}

			// this should return exactly one or none, if one is returned, the
			// step below is not needed, as this action is already assigned.
			if (this.getInstanceWithAopAdvices().findActionPermissionsOfRole(dummyRole, validFrom).size() == 0)
				if (competenceList.size() != 0)
					this.assignPermission(action, dummyRole, validFrom, validTo,
							PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE, false, viewInheritToChilds);

		}
	}

	/**
	 * @deprecated
	 */
	@Override
	public void addGroupToActionWithCompetenceList(PoAction action, PoGroup group, Date validFrom, Date validTo,
			List<CompetenceTarget> competenceList, int ranking, boolean inheritToChilds, boolean viewInheritToChilds) {

		this.assignPermissionWithCompetenceTargets(action, group, validFrom, validTo, competenceList, ranking, inheritToChilds, viewInheritToChilds);
	}

	@Override
	public void assignPermissionWithCompetenceTargets(PoAction action, PoGroup group, Date validFrom, Date validTo,
			List<CompetenceTarget> competenceList, int ranking, boolean inheritToChilds, boolean viewInheritToChilds) {

		if (validFrom == null)
			validFrom = new Date();
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		if (competenceList != null) {
			PoRole dummyRole = new PoRole();
			dummyRole.setClient(group.getClient());
			dummyRole.setDescription("This is a private role of "
					+ group.getName());

			dummyRole.setName(action.getName() + "_" + group.getName() + "_"
					+ System.currentTimeMillis());
			dummyRole.setParticipantId(dummyRole.getName());
			dummyRole.setValidfrom(validFrom);
			dummyRole.setValidto(validTo);
			dummyRole.setRoleType(PoRole.DUMMY_ROLE);
			roleService.saveRole(dummyRole);
			for (CompetenceTarget compTarget : competenceList) {
				if (compTarget instanceof PoPerson)
					roleService.assignRoleWithPersonCompetence(dummyRole,
							group, (PoPerson) compTarget, validFrom, validTo, ranking);
				else if (compTarget instanceof PoGroup)
					roleService.assignRoleWithGroupCompetence(dummyRole,
							group, (PoGroup) compTarget, validFrom, validTo, ranking);
				else if (compTarget instanceof PoClient)
					roleService.assignRoleWithClientCompetence(dummyRole,
							group, (PoClient) compTarget, validFrom, validTo, ranking);
			}
			if (competenceList.size() != 0)
				this.assignPermission(action, dummyRole, validFrom, validTo,
						PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,
						inheritToChilds, viewInheritToChilds);
		}
	}

	// negative permissions

	@Override
	public void assignNegativePermission(PoAction action, PoClient client,
			Date validFrom, Date validTo) {

		assignPermission(action, client, validFrom, validTo, PoConstants.VIEW_PERMISSION_TYPE_NULL, true);

	}

	@Override
	public void assignNegativePermission(PoAction action, PoGroup group,
			Date validFrom, Date validTo, boolean inheritToChild) {

		this.assignPermission(action, group, validFrom, validTo, inheritToChild, PoConstants.VIEW_PERMISSION_TYPE_NULL, true);

	}

	@Override
	public void assignNegativePermission(PoAction action, PoPerson person,
			Date validFrom, Date validTo) {

		this.assignPermission(action, person, validFrom, validTo, PoConstants.VIEW_PERMISSION_TYPE_NULL, true);
	}

	@Override
	public void assignNegativePermission(PoAction action, PoRole role,
			Date validFrom, Date validTo, boolean inheritToChild) {

		assignPermission(action, role, validFrom, validTo, PoConstants.VIEW_PERMISSION_TYPE_NULL, inheritToChild, inheritToChild, true);
	}

	@Override
	public List<PoAPermissionClient> findActionPermissionsOfClient(PoClient client, Date date) {
		return actionDAO.findActionPermissionsOfClient(client, date);
	}

	@Override
	public List<PoAPermissionClient> findActionPermissionsOfClientF(PoClient client, Date date) {
		List<PoAPermissionClient> res = actionDAO.findActionPermissionsOfClientF(client, date);
		return res;
	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionsOfPerson(PoPerson person, Date date) {
		return actionDAO.findActionPermissionsOfPerson(person, date);
	}

	@Override
	public List<PoAPermissionPerson> findActionPermissionsOfPersonF(PoPerson person, Date date) {
		return actionDAO.findActionPermissionsOfPersonF(person, date);

	}

	@Override
	public List<PoAPermissionRole> findActionPermissionsOfRole(PoRole role, Date date) {
		return actionDAO.findActionPermissionsOfRole(role, date);
	}

	@Override
	public List<PoAPermissionRole> findActionPermissionsOfRoleF(PoRole role, Date date) {
		return actionDAO.findActionPermissionsOfRoleF(role, date);
	}

	@Override
	public void saveAPermission(PoAPermissionBase permission) {
		this.actionDAO.saveAPermission(permission);
	}

	@Override
	public List<PoAPermissionGroup> findDirectlyLinkedActionPermissionsF(PoGroup group, Date date) {
		return this.actionDAO.findDirectlyLinkedActionPermissionsF(group, date);
	}

	@Override
	public List<CompetenceTarget> findActionCompetenceTargets(PoPerson person, PoAPermissionBase aPermissionBase, Date date) {
		PoAPermissionVisitor actionPermissionVisitor = new PoAPermissionAdapter();
		aPermissionBase.accept(actionPermissionVisitor);

		if (date == null)
			date = HistorizationHelper.getNowWithHourPrecision();

		List<CompetenceTarget> res = new ArrayList<CompetenceTarget>();
		switch (aPermissionBase.getViewPermissionType()) {
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			res.add(person);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			this.fillUpSubgroups(orgService.getPersonsHierarchicalGroup(person, date), date, res);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			PoGroup group = orgService.findPersonGroups(person, date, PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY).get(0).getGroup();
			if (group == null)
				throw new PoRuntimeException(PoRuntimeException.ERROR_NO_HIERARCHICAL_GROUP_EXISTS);
			res.add(group);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			if (actionPermissionVisitor.getWhichClassVisited().equals("AR")) {
				PoRole role = ((PoAPermissionRole) aPermissionBase).getRole();
				res.addAll(roleService.findCompetenceClientsOfPerson(person, role, date));
				res.addAll(roleService.findCompetenceGroupsOfPerson(person, role, date));
				res.addAll(roleService.findCompetencePersonsOfPerson(person, role, date));
			}
			else
				throw new PoRuntimeException(PoRuntimeException.ERROR_VIEW_PERMISSION_TYPE_NOT_VALID);
			break;
		}
		return res;
	}

	private List<CompetenceTarget> fillUpSubgroups(PoGroup group, Date date, List<CompetenceTarget> groups) {
		List<String> groupUIDs = getUIDsOfSubGroups(group, date);
		for (String groupUID : groupUIDs) {
			groups.add(orgService.getGroup(groupUID));
		}
		return groups;
	}

	/**
	 * The function traverses recursively through all child-groups of the given group.
	 * The result is a list which contains the UID of passed group, and all UIDs of child groups.
	 * The result will be collected into the List parameter, and additionally returned by method return.
	 * @param group the group to search sub-groups for.
	 * @param date the date to check validity of sub-groups.
	 * @param groupUidsList an empty list that will be filled with all UID's, must not be null.
	 * @return the groupUidsList parameter.
	 */
	private List<String> getUIDsOfSubGroups(PoGroup group, Date date) {
		List<String> groupUidsList = new ArrayList<String>();
		if (group.getValidfrom().compareTo(date) <= 0 && group.getValidto().after(date)) {
			getUIDsOfSubGroups(group.getUID(), date, groupUidsList);
		}
		return groupUidsList;
	}

	// fri_23-02-2011: performance tuning: only UIDs are needed here
	private List<String> getUIDsOfSubGroups(String groupUid, Date date, List<String> groupUidsList) {
	    if (logger.isDebugEnabled())
			logger.debug("getUIDsOfSubGroups at " + date);
		groupUidsList.add(groupUid);
		List<String> childGroupUids = orgService.findChildGroupUids(groupUid, date);
		if (childGroupUids != null)	{
			for (String childGroupUid : childGroupUids) {
				if (childGroupUid != null) {
					getUIDsOfSubGroups(childGroupUid, date, groupUidsList);
				}
			}
		}
		return groupUidsList;
	}

	private boolean isInSameGroupOrSubGroup(PoGroup executerG, PoGroup controlledG, PoRoleHolderDynamic rhd) {
		if (controlledG != null && executerG != null) {
			if (rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_OWN_HIERARCHY) {
				if (controlledG.equals(executerG)) {
					return true;
				}
			}
			else if (rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_OWN_HIERARCHYPLUS) {
				if (this.orgService.isGroupChildGroup(executerG, controlledG) != null)
					return true;
			}
		}
		return false;
	}

	@Override
	public List<CompetenceTarget> findActionCompetenceTargets(PoGroup group, PoAPermissionBase aPermissionBase, Date date) {
		PoAPermissionVisitor apV = new PoAPermissionAdapter();
		aPermissionBase.accept(apV);

		if (date == null)
			date = HistorizationHelper.getNowWithHourPrecision();
		
		List<CompetenceTarget> res = new ArrayList<CompetenceTarget>();
		switch (aPermissionBase.getViewPermissionType()) {
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			res.add(group);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			res.add(group);
			fillUpSubgroups(group, date, res);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			res.add(group);
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			if (apV.getWhichClassVisited().equals("AR")) {
				PoRole role = ((PoAPermissionRole) aPermissionBase).getRole();
				res.addAll(roleService.findCompetenceClientsOfGroup(group,
						role, date));
				res.addAll(roleService.findCompetenceGroupsOfGroup(group,
						role, date));
				res.addAll(roleService.findCompetencePersonsOfGroup(group,
						role, date));
			}
			else
				throw new PoRuntimeException(
						PoRuntimeException.ERROR_VIEW_PERMISSION_TYPE_NOT_VALID);
			break;
		}
		return res;
	}

	private boolean containsGroup(List<? extends PoRoleHolderLink> c, PoGroup g) {
		boolean result = false;
		for (PoRoleHolderLink o : c) {
			if (o instanceof PoRoleHolderGroup) {
				PoRoleHolderGroup rhg = (PoRoleHolderGroup) o;
				if (g.equals(rhg.getGroup()))
					result = true;
			}
		}
		return result;
	}

	private boolean containsPerson(List<? extends PoRoleHolderLink> c, PoPerson p) {
		boolean result = false;
		for (Object o : c) {
			/*
			 * Seperate String for person and rhp uid are needed.
			 * Otherwise after the second walkthrough the condition is false, even if it is true.
			 * hentner, 29.5.06
			 */
			if (o instanceof PoRoleHolderPerson) {
				PoRoleHolderPerson rhp = (PoRoleHolderPerson) o;
				String p_uid = p.getUID();
				String rhp_uid = rhp.getPerson().getUID();
				if (p_uid.equals(rhp_uid))
					result = true;
			}
		}
		return result;
	}

	/**
	 * This function returns a HashMap filled with the UID's of the instances the given person<br>
	 * has rights.
	 * 
	 * 
	 * @param hm the hashmap that is filled
	 * @param person the person for which the viewPermission is determined
	 * @param vp the viewPermission (see PoConstants for detailed information)
	 * @param date
	 * @param g if this field is not null, it is used instead of the person.<br>
	 * e.g.: if an action is assigned to a person with inheritToChild set to true, <br>
	 * then the given person may not be correct.
	 * @return
	 */
	private Map<String, List<String>> resolveViewPermission(Map<String, List<String>> hm, PoPerson person, int vp, Date date) {
		switch (vp) {
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON:
			addToMap(hm, PERSONS, person.getUID());
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT:
			addToMap(hm, GROUPS, orgService.getPersonsHierarchicalGroup(person, date).getUID());
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS:
			addToMap(hm, GROUPS, getUIDsOfSubGroups(orgService.getPersonsHierarchicalGroup(person, date), date));
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE:
			// this is solved directly
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT:
			addToMap(hm, CLIENTS, person.getClient().getUID());
			break;
		case PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS:
			Iterator<PoClient> clients = orgService.loadAllClients().iterator();
			while (clients.hasNext())
				addToMap(hm, CLIENTS, clients.next().getUID());
			break;
		}
		return hm;
	}

	/**
	 * @param l a list of PoAPermission objects
	 * @param person
	 * @param hm a empty or already filled hashmap
	 * @param date
	 * @param group
	 * @return a hashMap, filled with the correct Id's of clients,groups or persons<br>
	 * depending on the viewPermissionType of the actionPermissions
	 */
	private Map<String, List<String>> fillHashMap(List<? extends PoAPermissionBase> list, PoPerson person, Map<String, List<String>> hm, Date date) {
		for (PoAPermissionBase apb : list) {
			resolveViewPermission(hm, person, apb.getViewPermissionType(), date);
		}
		return hm;
	}

	private Map<String, List<String>> addToMap(Map<String, List<String>> hm, String key, String item) {
		List<String> list4Key = listFromMap(hm, key);
		putToMap(list4Key, item);
		return hm;
	}

	private Map<String, List<String>> addToMap(Map<String, List<String>> hm, String key, List<String> items) {
		if (items.size() <= 0)
			return hm;
		List<String> list4Key = listFromMap(hm, key);
		for (String item : items)
			putToMap(list4Key, item);
		return hm;
	}

	private List<String> listFromMap(Map<String, List<String>> hm, String key) {
		List<String> list4Key = hm.get(key);
		if (list4Key == null)
			hm.put(key, list4Key = new ArrayList<String>());
		return list4Key;
	}

	private void putToMap(List<String> list4Key, String item) {
		if (false == list4Key.contains(item))	// TODO this is sequential access, could tune this
			list4Key.add(item);
	}
	
	/*
	 * BEGINNING OF VIEW PERMISSION PROCESSING
	 */
	
	@Override
	public Map<String, List<String>> findViewPermissions(PoPerson person, PoAction action, Date date) {
		return findViewPermissions(person, action, date, null);
	}

	@Override
	public Map<String, List<String>> findViewPermissions(PoPerson person, PoAction action, Date date, PoAction actionToInheritViewPermissionsFrom) {
		if (logger.isDebugEnabled())
			logger.debug("findViewPermissions with date: "+date);
		
		
		if (person.getUserName().equals(this.sysAdminUserInfo.getSysAdminUser())) {
			// The system administrator has view permissions for everything
			// replace $CLIENTUIDS$ with the uid's of every client
			Map<String, List<String>> adminResult = new HashMap<String,List<String>>();
			List<String> l = orgService.findUidsOfClients();
			adminResult.put(CLIENTS, l);
			return adminResult;
		}
		
		
		long time = System.currentTimeMillis();
		
		Map<String, List<String>> result = findViewPermissionsPrivate(person, action, date);
		
		// see if there are actions that allow this action
		List<PoAction> actionsThatAllowActions = actionDAO.findActionsThatAllowAction(action, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date));
		
		// add all configs of found configureable actions
		Set<PoAction> configsToAdd = new HashSet<PoAction>();
		for (PoAction actAction : actionsThatAllowActions) {
			if (actAction.isConfigurable() && actAction.getChilds().size() > 0) {
				configsToAdd.addAll(actAction.getChilds());
			}
		}
		actionsThatAllowActions.addAll(configsToAdd);
		
		if (actionToInheritViewPermissionsFrom!=null && !action.equals(actionToInheritViewPermissionsFrom)) {
			if (actionsThatAllowActions.contains(actionToInheritViewPermissionsFrom)) {
				actionsThatAllowActions.clear();
				actionsThatAllowActions.add(actionToInheritViewPermissionsFrom);
			} else {
				// as actionToInheritViewPermissionsFrom is only a priority hint, we are
				// skipping the info, if it is not contained in the List actionsThatAllowActions!
				actionsThatAllowActions.clear();
			}
		}
		

		for (PoAction prevAction : actionsThatAllowActions) {
			mergeMaps(result, findViewPermissionsPrivate(person, prevAction, date));
		}
		
		if (logger.isDebugEnabled())
			logger.debug("findViewPermissions needed millis: "+(System.currentTimeMillis() - time));
		
		return result;
	}

	private Map<String, List<String>> findViewPermissionsPrivate(PoPerson person, PoAction action, Date date) {
		if (date == null)
			date = HistorizationHelper.getNowWithHourPrecision();

		PoGroup hierarchicalGroup = orgService.getPersonsHierarchicalGroup(person, date);

		Map<String, List<String>> hm = new HashMap<String, List<String>>();
		
		// direct assignment via client, hierarchicalGroup or person
		checkDirectAssignment(person, action, hierarchicalGroup, date, hm);

		checkAssignmentViaLooseGroup(person, action, date, hm);

		// the role is assigned to a person
		checkAssignmentViaNormalRole(person, action, hierarchicalGroup, date, hm);

		// loop through all assigned groups and check if
		// one of those is roleholder of a PoAPermissionRole object with the given action
		// Especially loose groups are considered here!
		checkIndirectAssignmentViaNormalRole(person, action, hierarchicalGroup, date, hm);

		checkAssignmentViaDummyRole(person, action, hierarchicalGroup, date, hm);

		return hm;
	}

	private void mergeMaps(Map<String, List<String>> map1, Map<String, List<String>> map2) {
		List<String> elements2 = map2.get(PERSONS);
		if (elements2 != null)
			addToMap(map1, PERSONS, elements2);

		elements2 = map2.get(GROUPS);
		if (elements2 != null)
			addToMap(map1, GROUPS, elements2);

		elements2 = map2.get(CLIENTS);
		if (elements2 != null)
			addToMap(map1, CLIENTS, elements2);
	}

	private void checkDirectAssignment(PoPerson person, PoAction action, PoGroup hierarchicalGroup, Date date, Map<String, List<String>> hm) {
		// Step 1 - > check if the action is assigned via a client
		// **********************************************************
		List<? extends PoAPermissionBase> permissions = actionDAO.findActionPermissionClientWithActionAndClient(action, person.getClient(), date);
		if (permissions.size() != 0) {
			logger.debug(action.getName() + " is assigned via a client");
			hm = fillHashMap(permissions, person, hm, date);
		}
		// Step 2 - > check if the action is assigned via a person
		// **********************************************************
		permissions = actionDAO.findActionPermissionPersonWithActionAndPerson(action, person, date);
		if (permissions.size() != 0) {
			logger.debug(action.getName() + " is assigned via a person");
			hm = fillHashMap(permissions, person, hm, date);
		}
		// Step 3 - > check if the action is assigned via a Group
		// **********************************************************
		permissions = actionDAO.findActionPermissionGroupWithActionAndGroup(action, hierarchicalGroup, date);
		if (permissions.size() != 0) {
			logger.debug(action.getName() + " is assigned via a group");
			hm = fillHashMap(permissions, person, hm, date);
		}

		// if the inheritToChild flag is set, an action can also be inherited from parent entries.<br>
		// here only assignments to groups are checked
		PoGroup group = hierarchicalGroup;
		PoParentGroup pg = orgService.getParentGroup(group);
		if (pg != null)
			group = pg.getParentGroup();
		else {
			group = null;
		}

		while (group != null) {
			permissions = actionDAO.findActionPermissionGroupWithActionAndGroupAndInheritToChilds(action, group, date, true);
			hm = fillHashMap(permissions, person, hm, date);

			PoParentGroup pg1 = orgService.getParentGroup(group);
			if (pg1 != null)
				group = pg1.getParentGroup();
			else
				group = null;
		}
	}

	private void checkAssignmentViaLooseGroup(PoPerson person, PoAction action, Date date, Map<String, List<String>> hm) {
		List<PoAPermissionRole> rolePermissionsOfLooseGroups = new ArrayList<PoAPermissionRole>();

		List<PoGroup> looseGroups = this.orgService.findNotHierarchicalGroupsOfPerson(person, date);
		
		for (PoGroup looseGroup : looseGroups) {

			// find dummy roles where the perfomer the looseGroup
			rolePermissionsOfLooseGroups.addAll(actionDAO.findActionPermissionRoleWithActionAndGroupAndRoleType(action, looseGroup, date, PoRole.DUMMY_ROLE));
			
			// iterate through the PoAPermissionRole objects assigned to the loose group
			for (PoAPermissionRole actionPermissionRole : rolePermissionsOfLooseGroups) {
				// fill the hashMap accordingly
				fillHmWithApRoleAndGroup(actionPermissionRole, looseGroup, date, hm);
			}

			List<PoAPermissionGroup> permissions = actionDAO.findActionPermissionGroupWithActionAndGroup(action, looseGroup, date);

			if (permissions.size() > 0)
				fillHashMap(permissions, person, hm, date);
		}
	}

	private void checkAssignmentViaNormalRole(PoPerson person, PoAction action, PoGroup hierarchicalGroup, Date date, Map<String, List<String>> hm) {
		List<PoAPermissionRole> roles = actionDAO.findAPRoleWithActionAndPerson(person, date, action);

		// get roleHolderPersons with given action assigned
		if (roles.size() != 0) {
			// TODO this part should be refactored. Better performance
			for (PoAPermissionRole apr : roles) {
				if (apr.getViewPermissionType() == PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE) {
					// check if person was assigned to the role
					// with competence for all
					// if yes resolve those dynamic
					List<PoRoleHolderPerson> list = new ArrayList<PoRoleHolderPerson>();
					List<PoRoleCompetenceAll> rVp4all = roleService.findRoleCompetenceAll(apr.getRole(), HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date));
					for (PoRoleCompetenceAll rh : rVp4all) {
						List<PoRoleHolderPerson> rhp_allI = rh.getRoleHolderPersons();
						// add actual roleholder to list
						// if it equals the actual current person
						for (PoRoleHolderPerson rhp : rhp_allI) {
							if (rhp.getValidto().after(date) && new IfDate(rhp.getValidfrom()).beforeEqualsSoft(new IfDate(date)) && rhp.getPerson().equals(person))
								list.add(rhp);
						}

						// resolve view permissions for the dynamic roleholders
						List<PoRoleHolderDynamic> rhDynamics = rh.getRoleHolderDynamics();
						for (PoRoleHolderDynamic rhd : rhDynamics) {
							// check if valid
							if (rhd.getValidfrom().before(new Date()) && rhd.getValidto().after(new Date())) {
								// add all clients, as the role competence is for all
								resolveViewPermission(hm, person, PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS, date);
								break;
							}
						}
					}
					
					// ggruber 2012-03-01
					// fix for support-problem:
					// Notes://Miraculix/C1256B300058B5FC/9DCBD34A250DFDF7C12576F60048AD6D/317B71F8D094072AC12579B4005CCDDC

					List<PoClient> clients = roleService.findCompetenceClientsOfPerson(person, apr.getRole(), date);
					List<PoGroup> groups = roleService.findCompetenceGroupsOfPerson(person, apr.getRole(), date);
					List<PoPerson> persons = roleService.findCompetencePersonsOfPerson(person, apr.getRole(), date);

					// resolveViewPermission for all controlled persons
					for (PoPerson p : persons) {
						if (logger.isDebugEnabled())
							logger.debug(action.getName() + " is assigned via a role - add Person: " + p.getUserName());
						
						addToMap(hm, PERSONS, p.getUID());
					}
					
					for (PoGroup g : groups) {
						if (logger.isDebugEnabled())
							logger.debug(action.getName() + " is assigned via a role - add Group: " + g.getName());
						
						addToMap(hm, GROUPS, g.getUID());

						if (apr.isViewInheritToChilds()) {
							addToMap(hm, GROUPS, getUIDsOfSubGroups(g, date));
						}
					}

					for (PoClient c : clients) {
						if (logger.isDebugEnabled())
							logger.debug(action.getName() + " is assigned via a role - add Client: " + c.getName());
						
						addToMap(hm, CLIENTS, c.getUID());
					}
					
					// now see if there are dynamic role holders
					List<PoRoleHolderDynamic> dynamics = roleService.findRoleHolderDynamicF(apr.getRole(), date);
					for (PoRoleHolderDynamic rhd : dynamics) {
						if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetencePerson) {
							PoRoleCompetencePerson rcp = (PoRoleCompetencePerson) rhd.getRoleCompetenceBase();
							// dynamic role holder --> with competence --> person
							if (rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_ALL_CLIENTS ||
									(rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_OWN_CLIENT &&
											rcp.getCompetence4Person().getClient().equals(person.getClient())) ||
									isInSameGroupOrSubGroup(hierarchicalGroup, orgService.getPersonsHierarchicalGroup(rcp.getCompetence4Person()),
											rhd))
								addToMap(hm, PERSONS, rcp.getCompetence4Person().getUID());

						}
						else if (rhd.getRoleCompetenceBase() instanceof PoRoleCompetenceGroup) {
							PoRoleCompetenceGroup rcg = (PoRoleCompetenceGroup) rhd.getRoleCompetenceBase();
							// dynamic role holder --> with competence --> group
							if (rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_ALL_CLIENTS ||
									(rhd.getRoleHolderType() == PoRoleService.DYNAMIC_TYPE_OWN_CLIENT &&
											rcg.getCompetence4Group().getClient().equals(person.getClient())) ||
									isInSameGroupOrSubGroup(hierarchicalGroup,
											rcg.getCompetence4Group(), rhd))
								addToMap(hm, GROUPS, rcg.getCompetence4Group().getUID());

						} // no dynamic roles for client competence
					}

					boolean addAll = false;
					if (this.containsPerson(list, person))
						addAll = true;

					if (addAll) {
						clients = orgService.loadAllClients();
						for (PoClient c : clients) {
							// if the role has no dedicated client -> add all clients otherwise
							// only add the dedicated client
							if (apr.getRole().getClient() == null || apr.getRole().getClient().equals(c))
								this.addToMap(hm, CLIENTS, c.getUID());
						}
					}
				}
				else { // Action was not assigned with viewPermission == competenceTarget
					resolveViewPermission(hm, person, apr.getViewPermissionType(), date);
				}
			}
		}
	}

	private void checkIndirectAssignmentViaNormalRole(PoPerson person, PoAction action, PoGroup hierarchicalGroup, Date date, Map<String, List<String>> hm) {
		List<PoGroup> looseGroups = orgService.findNotHierarchicalGroupsOfPerson(person, date);
		looseGroups.add(hierarchicalGroup);

		for (PoGroup looseOrHGroup : looseGroups) {
			List<PoAPermissionRole> roles = actionDAO.findAPRoleWithActionAndGroup(looseOrHGroup, date, action);
			// if at least one PoAPermissionRole object with given group and action was found
			if (roles.size() != 0) {
				List<PoRoleHolderGroup> roleCompetenceAllList = new ArrayList<PoRoleHolderGroup>();
				// loop through PoAPermission role objects
				// group has permission for All
				for (PoAPermissionRole apr : roles) {

					if (apr.getViewPermissionType() == PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE) {

						// add PoRoleCompetenceAll objects
						List<PoRoleCompetenceAll> rVp4all = roleService.findRoleCompetenceAll(apr.getRole(), HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date));
						for (PoRoleCompetenceAll rh : rVp4all) {
							roleCompetenceAllList.addAll(rh.getRoleHolderGroups());
						}
						
						// ggruber 2012-03-01
						// fix for support-problem:
						// Notes://Miraculix/C1256B300058B5FC/9DCBD34A250DFDF7C12576F60048AD6D/317B71F8D094072AC12579B4005CCDDC
						

						List<PoClient> clients = roleService.findCompetenceClientsOfGroup(looseOrHGroup, apr.getRole(), date);
						List<PoGroup> groups = roleService.findCompetenceGroupsOfGroup(looseOrHGroup, apr.getRole(), date);
						List<PoPerson> persons = roleService.findCompetencePersonsOfGroup(looseOrHGroup, apr.getRole(), date);

						// resolveViewPermission for all controlled persons
						List<String> uids = new ArrayList<String>();
						for (PoPerson p : persons)
							uids.add(p.getUID());
						addToMap(hm, PERSONS, uids);
						
						for (PoGroup g : groups) {
							addToMap(hm, GROUPS, g.getUID());
							if (apr.isViewInheritToChilds())
								addToMap(hm, GROUPS, getUIDsOfSubGroups(g, date));
						}
						
						for (PoClient c : clients)
							addToMap(hm, CLIENTS, c.getUID());
					}
					else {
						resolveViewPermission(hm, person, apr.getViewPermissionType(), date);
					}
				}

				/*********
				 * AddAll is circumstantially solved, but if collections are nested,
				 * it results in ConcurrentModificationExceptions, even they are only read.
				 **/
				boolean addAll = false;
				if (this.containsGroup(roleCompetenceAllList, looseOrHGroup) || this.containsPerson(roleCompetenceAllList, person))
					addAll = true;

				if (addAll) {
					List<PoClient> clients = orgService.loadAllClients();
					for (PoClient c : clients) {
						this.addToMap(hm, CLIENTS, c.getUID());
					}
				}
			}
		}
	}

	private void checkAssignmentViaDummyRole(PoPerson person, PoAction action, PoGroup hierarchicalGroup, Date date, Map<String, List<String>> hm) {
		// The first query checks for directly linked roles via the person object
		List<PoAPermissionRole> persons = actionDAO.findActionPermissionRoleWithActionAndPersonAndRoleType(action, person, date, PoRole.DUMMY_ROLE);

		// The second query checks for directly linked roles via the hierarchical group object
		List<PoAPermissionRole> groups = actionDAO.findActionPermissionRoleWithActionAndGroupAndRoleType(action, hierarchicalGroup, date, PoRole.DUMMY_ROLE);

		// **********************************************
		// DummyRoles are assigned via the person object
		// **********************************************
		if (persons.size() != 0) {
			for (PoAPermissionRole actionPermissionRole : persons) {
				List<PoPerson> competencePersons = roleService.findCompetencePersonsOfPerson(person, actionPermissionRole.getRole(), date);
				List<PoGroup> competenceGroups = roleService.findCompetenceGroupsOfPerson(person, actionPermissionRole.getRole(), date);
				List<PoClient> competenceClients = roleService.findCompetenceClientsOfPerson(person, actionPermissionRole.getRole(), date);
				
				List<String> uids = new ArrayList<String>();
				for (PoPerson p : competencePersons)
					uids.add(p.getUID());
				addToMap(hm, PERSONS, uids);
				
				for (PoGroup g : competenceGroups) {
					addToMap(hm, GROUPS, g.getUID());
					if (actionPermissionRole.isViewInheritToChilds())
						addToMap(hm, GROUPS, getUIDsOfSubGroups(g, date));
				}
				for (PoClient c : competenceClients) {
					addToMap(hm, CLIENTS, c.getUID());
				}
			}
		}
		
		// ***********************DummyRoles are assigned via the hierarchical group object**********************************************//
		if (groups.size() != 0) {
			for (PoAPermissionRole apr : groups) {
				fillHmWithApRoleAndGroup(apr, hierarchicalGroup, date, hm);
			}
		}

		// ****************************************************
		// Check if DummyRoles are assigned via a
		// higher level group object
		// ***************************************************
		PoGroup group = hierarchicalGroup;
		// go up one level
		PoParentGroup pg1 = orgService.getParentGroup(group);
		if (pg1 != null)
			group = pg1.getParentGroup();
		else
			group = null;

		while (group != null) {
			// find all APermissionRole objects with the given action and group (owner)
			// there could exist more than one dummy role for given action and group
			for (PoAPermissionRole permissionRole : actionDAO.findActionPermissionRoleWithActionAndGroup(action, group, date)) {
				if (permissionRole.isInheritToChilds()) {
					PoGroup permissionGroup = group;

					// process permissions from dummy roles
					List<PoPerson> competencePersons = roleService.findCompetencePersonsOfGroup(permissionGroup, permissionRole.getRole(), date);
					// process the extracted permissions and add them to the resulting view permission map
					for (PoPerson p : competencePersons) {
						addToMap(hm, PERSONS, p.getUID());
					}
					
					List<PoGroup> competenceGroups = roleService.findCompetenceGroupsOfGroup(permissionGroup, permissionRole.getRole(), date);
					for (PoGroup competenceGroup : competenceGroups) {
						addToMap(hm, GROUPS, competenceGroup.getUID());
						if (permissionRole.isViewInheritToChilds()) {
							List<String> uids = getUIDsOfSubGroups(competenceGroup, date);
							addToMap(hm, GROUPS, uids);
						}
					}

					// process permissions from dummy roles
					List<PoClient> competenceClients = roleService.findCompetenceClientsOfGroup(permissionGroup, permissionRole.getRole(), date);
					// process the extracted permissions and add them to the resulting view permission map
					for (PoClient p : competenceClients) {
						addToMap(hm, CLIENTS, p.getUID());
					}
					
				}
			}

			// go up to parent if it exists
			pg1 = orgService.getParentGroup(group);
			if (pg1 != null)
				group = pg1.getParentGroup();
			else
				group = null;
		}

		if (logger.isDebugEnabled())
			logger.debug("findViewPermissions for action '" + action.getName() + actionService.getActionPostfix(action) + "' for person=" + person.getFullName() + " results in " + hm);

		if (action.isUniversallyAllowed()) {
			// add the default view permission if the action is universally allowed.
			resolveViewPermission(hm, person, action.getDefaultViewPermissionType(), date);
		}
	}

	/*
	 * END OF VIEW PERMISSION PROCESSING
	 */

	/**
	 * This function can handle dummy roles... no time..
	 * 
	 * @param apr
	 * @param group
	 * @param date
	 * @param hm
	 */
	private void fillHmWithApRoleAndGroup(PoAPermissionRole apr, PoGroup group, Date date, Map<String, List<String>> hm) {
		List<PoPerson> competencePersons = roleService.findCompetencePersonsOfGroup(group, apr.getRole(), date);
		List<PoGroup> competenceGroups = roleService.findCompetenceGroupsOfGroup(group, apr.getRole(), date);
		List<PoClient> competenceClients = roleService.findCompetenceClientsOfGroup(group, apr.getRole(), date);
		
		List<String> uids = new ArrayList<String>();
		for (PoPerson p : competencePersons)
			uids.add(p.getUID());
		addToMap(hm, PERSONS, uids);
		
		for (PoGroup g : competenceGroups) {
			addToMap(hm, GROUPS, g.getUID());
			if (apr.isViewInheritToChilds())
				addToMap(hm, GROUPS, getUIDsOfSubGroups(g, date));
		}
		
		for (PoClient c : competenceClients) {
			addToMap(hm, CLIENTS, c.getUID());
		}
	}
	
	private boolean containsViewPermissionPlaceHolders(String query) {
		return (query.contains("$CLIENTUIDS$") || query.contains("$GROUPUIDS$") || query.contains("$PERSONUIDS$"));
	}
	
	private String replaceViewPermissionPlaceHoldersWithNamedParameters(String query) {
		query = query.replaceAll("\\$CLIENTUIDS\\$", " :" + ARG_CLIENT_UIDS+ " ");
		query = query.replaceAll("\\$GROUPUIDS\\$", " :" + ARG_GROUP_UIDS + " ");
		query = query.replaceAll("\\$PERSONUIDS\\$", " :" + ARG_PERSON_UIDS + " ");
		return query;
	}

	@Override
	public String replacePlaceHoldersInQuery(String query, Map<String, List<String>> hm) {
		// replace $CLIENTUIDS$ with the uids of the found clients
		List<String> clients = hm.get(CLIENTS);
		if (clients != null && clients.size() != 0) {
			String res = actionDAO.generateCommaList(clients, true);
			query = query.replaceAll("\\$CLIENTUIDS\\$", res);
		}
		else
			query = query.replaceAll("\\$CLIENTUIDS\\$", "''");

		// replace $GROUPUIDS$ with the uids of the found groups
		List<String> groups = hm.get(GROUPS);
		if (groups != null && groups.size() != 0) {
			String res = actionDAO.generateCommaList(groups, true);
			query = query.replaceAll("\\$GROUPUIDS\\$", res);
		}
		else
			query = query.replaceAll("\\$GROUPUIDS\\$", "''");
		// replace $PERSONUIDS$ with the uids of the found persons
		List<String> persons = hm.get(PERSONS);
		if (persons != null && persons.size() != 0) {
			String res = actionDAO.generateCommaList(persons, true);
			query = query.replaceAll("\\$PERSONUIDS\\$", res);
		}
		else
			query = query.replaceAll("\\$PERSONUIDS\\$", "''");
		return query;
	}

	@Override
	public void clearPermissionCaches() {
		Cache cache;
		try {
			cache = cacheManager.getCache(PoConstants.QUERYCACHE);
			cache.removeAll();
			cache = cacheManager.getCache(PoConstants.PERMISSIONCACHE);
			cache.removeAll();
			cache = cacheManager.getCache("at.workflow.webdesk.po.CONFIGFILECACHE");
			cache.removeAll();
			this.personActionPermissionCache.removeAll();

		}
		catch (Exception e) {
			throw new PoRuntimeException(e);
		}

	}

	@Override
	public NamedQuery getViewPermissionQuery(PoAction action, String user, Date date) {
		return getViewPermissionQuery(action, user, new NamedQuery(action.getSqlQuery(), null, null), date);
	}
	
	@Override
	public NamedQuery getViewPermissionQuery(PoAction action, String user, NamedQuery query, Date date) {
		return getViewPermissionQuery(action, user, query, date, null);
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public NamedQuery getViewPermissionQuery(PoAction action, String user, NamedQuery query, Date date, PoAction actionToInheritVPfrom) {
		
		Cache cache = this.cacheManager.getCache(PoConstants.QUERYCACHE);
		net.sf.ehcache.Element e = null;
		try {
			e = cache.get(action.getUID());
		}
		catch (IllegalStateException e1) {
			logger.warn("Problems accessing the cache: " + PoConstants.QUERYCACHE, e1);
		}
		catch (CacheException e1) {
			logger.warn("Problems accessing the cache: " + PoConstants.QUERYCACHE, e1);
		}
		
		HashMap<String, Map<String, List<String>>> hm = null;
		// when a referring action is specified
		// do not build a cache as it would not cover all cases
		if (e != null && actionToInheritVPfrom == null) {
			hm = (HashMap<String, Map<String, List<String>>>) e.getValue();
			if (hm.containsKey(user)) {
				logger.debug("- getViewPermissionQuery: the result was already cached. Using the cached Value.");
				
				Map<String, List<String>> m = hm.get(user);
				NamedQuery newQuery = recreateQuery(query, m); 
				return newQuery;
			}
		}
		Map<String, List<String>> hm2 = new HashMap<String, List<String>>();
		if (hm == null)
			hm = new HashMap<String, Map<String, List<String>>>();

		// the view permissions for the persons have to be found
		PoPerson person = orgService.findPersonByUserName(user);
		hm2 = getInstanceWithAopAdvices().findViewPermissions(person, action, date, actionToInheritVPfrom);

		
		NamedQuery newQuery = recreateQuery(query, hm2);
		
		hm.put(user, hm2);
		if (actionToInheritVPfrom == null) {
			// when a referring action is specified
			// do not build a cache as it would not cover all cases
			e = new net.sf.ehcache.Element(action.getUID(), hm);
			cache.put(e);
		}
		return newQuery;
	}

	private NamedQuery recreateQuery(NamedQuery query, 	Map<String, List<String>> hm) {
		
		String queryText = replaceViewPermissionPlaceHoldersWithNamedParameters(query.getQueryText());
		String[] paramNames = (String[]) ArrayUtils.addAll(query.getParamNames(), createStdVpParamNames( queryText ) );
		
		List<Object> paramValues = new ArrayList<Object>();
		
		if (query.getParamValues()!=null)
			paramValues.addAll( Arrays.asList(query.getParamValues()) );
		
		paramValues.addAll( 
				Arrays.asList( createStdVpParamValues(queryText, 
						getListOfUidsFromVpMap(hm,CLIENTS), 
						getListOfUidsFromVpMap(hm,GROUPS), 
						getListOfUidsFromVpMap(hm,PERSONS) )
				)
		);
		
		NamedQuery newQuery = new NamedQuery( queryText, paramNames, paramValues.toArray() );
		return newQuery;
	}
	
	private List<String> getListOfUidsFromVpMap(Map<String,List<String>> hm, String key) {
		if (hm.containsKey(key)==false) {
			return Arrays.asList( new String[] { "" } );
//			return Collections.EMPTY_LIST;
		}
		
		return hm.get(key);
	}

	/**
	 * checks if the person has the right to access the object with specified UID by using the specified action
	 * implicitly extends the List allowedIds by all objectIds for which the person has the right to access
	 * with the specified action
	 * 
	 * @param person: current User
	 * @param action is the action to check against
	 * @param date is the date for which the check should run
	 * @param uid of the Object to edit
	 * @param allowedIds List of ObjectIds, to be extended!
	 * @return true or false (depending on outcome!)
	 */
	private boolean checkIfAllowed(PoPerson person, PoAction action, Date date, String uid) {
		
		if (logger.isDebugEnabled())
			logger.debug("call checkIfAllowed with person=" + person.getUserName() + ",action=" + action.getName() + ",date=" + date);
		
		Map<String, List<String>> hm = getInstanceWithAopAdvices().findViewPermissions(person, action, date);
		
		// fillup returned list
		List<String> clients = hm.get(CLIENTS);
		List<String> persons = hm.get(PERSONS);
		List<String> groups = hm.get(GROUPS);
		
		String query = action.getSqlQuery();
		
		if ( StringUtils.isBlank(query)== false && containsViewPermissionPlaceHolders(query) ) {
			query = replaceViewPermissionPlaceHoldersWithNamedParameters(query);
			
			List<Object> l = actionDAO.findByQueryAndNamedParameters(query, 
					createStdVpParamNames(query), 
					createStdVpParamValues(query, clients, groups, persons) );

			if (l != null && l.contains(uid))
				return true;

			return false;

		}

		logger.debug("Person mit id: " + uid);


		// changed code ggruber 14.5.2007
		// if the uid is part of one of the 3 arrays -> true is returned (without the check of the database)
		// if the uid is no group, person or client, it is assumed that it is no manageable
		// viewpermission -> thus return true
		// otherwise return false

		if (clients != null && clients.contains(uid))
			return true;

		if (persons != null && persons.contains(uid))
			return true;

		if (groups != null && persons != null && groups.contains(uid))
			return true;

		if (orgService.getPerson(uid) == null && orgService.getGroup(uid) == null && orgService.getClient(uid) == null)
			return true;

		// the uid to open is a person, but the viewpermission is only given through a group (groups does not contain the person - uid)
		if (hm.get(CLIENTS) != null) {
			if (orgService.findPersonIdsOfClients(hm.get(CLIENTS), date).contains(uid)) {
				return true;
			}

			if (orgService.findGroupIdsOfClients(hm.get(CLIENTS), date).contains(uid)) {
				return true;
			}
		}

		if (hm.get(GROUPS) != null) {
			if (orgService.findPersonUidsOfGroups(hm.get(GROUPS), date).contains(uid))
				return true;
		}

		return false;
	}

	private String[] createStdVpParamNames(String queryText) {
		List<String> paramNames = new ArrayList<String>();
		if (queryText.contains(":"+ARG_CLIENT_UIDS))
			paramNames.add(ARG_CLIENT_UIDS);
		if (queryText.contains(":"+ARG_GROUP_UIDS))
			paramNames.add(ARG_GROUP_UIDS);
		if (queryText.contains(":"+ARG_PERSON_UIDS))
			paramNames.add(ARG_PERSON_UIDS);
		return paramNames.toArray(new String[] {});
	}
	
	private Object[] createStdVpParamValues(String queryText, List<String> clientUids, List<String> groupUids, List<String> personUids) {
		
		List<Object> paramValues = new ArrayList<Object>();
		
		if (queryText.contains(ARG_CLIENT_UIDS))
			paramValues.add( clientUids );
		
		if (queryText.contains(ARG_GROUP_UIDS))
			paramValues.add( groupUids );
		
		if (queryText.contains(ARG_PERSON_UIDS))
			paramValues.add( personUids );
		
		return paramValues.toArray();
	}

	@Override
	public boolean hasPersonPermissionToEditObjectWithId(String uid, String userName, PoAction action) {
		return hasPersonPermissionToEditObjectWithId(uid, userName, action, null);
	}

	@Override
	public boolean hasPersonPermissionToEditObjectWithId(String uid, String userName, PoAction action, Date date) {

		// FIXME: when this function is called inside the template, it has no session
		// getOrgJournal()
		action = actionDAO.get(action.getUID());

		PoPerson person = orgService.findPersonByUserName(userName);

		if (date == null)
			date = DateTools.nowWithHourPrecision();
		
		// check viewpermissions (by evaluating hql queries if present)
		if (checkIfAllowed(person, action, date, uid))
			return true;

		// check inherited permissions from actions which inherit their
		// permissions (general and view) to another action
		Collection<PoAction> actions = action.getAllowedByActions();
		for (PoAction otherAction : actions) {
			if (hasPersonPermissionForAction(userName, otherAction) && checkIfAllowed(person, otherAction, date, uid))
				return true;
		}

		return false;
	}

	@Override
	public boolean hasPersonPermissionToEditWithIdConfig(String uid, String userName, PoAction action) {

		if (hasPersonPermissionToEditObjectWithId(uid, userName, action, null))
			return true;

		// if this is called within the template the action is not assigned with an action
		action = actionDAO.get(action.getUID());

		// get the best config for the call and the target person
		PoAction config = actionService.getConfigFromActionWithoutTargetPermCheck(this.orgService.findPersonByUserName(userName), action, uid);

		if (config == null)
			return false;

		return true;

	}

	@Override
	public boolean hasPersonPermissionForAction(PoPerson person, PoAction action) {
		return hasPersonPermissionForAction(person.getUserName(), action);
	}

	@Override
	public boolean hasPersonPermissionForAction(String user, String switchedUser, PoAction action) {

		String switchUser = (switchedUser == null || "".equals(switchedUser) || "undefined".equals(switchedUser)) ? null : switchedUser;
		
		if (switchUser != null && !user.equals(switchUser) && action.isProhibitedForUsageBySwitchedUsers()) {
			return false;
		}

		return hasPersonPermissionForAction(user, action);
	}

	@Override
	public boolean hasPersonPermissionForAction(String user, PoAction action) {

		if (personActionPermissionCache.isKeyInCache(user)) {
			try {
				@SuppressWarnings("unchecked")
				List<String> actionUidList = (List<String>) personActionPermissionCache.get(user).getValue();
				return actionUidList.contains(action.getUID());
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieving Actionlist from Cache failed, load from db...");
				}
			}
		}

		boolean found = false;
		try {

			// universal action?
			if (action.isUniversallyAllowed())
				return true;

			PoPerson person = orgService.findPersonByUserName(user);
			List<PoAPermissionBase> permissions = getInstanceWithAopAdvices().findAllActionPermissionsOfPerson(person, DateTools.nowWithHourPrecision());

			// iterate over all found actionpermissions
			// return true if
			// a) current permission's action equals requested action
			// b) current permission's action passes on permission to another action (allowsAction) which equals the requested action

			for (PoAPermissionBase ap : permissions) {
				if (((ap.getAction().equals(action))
						|| (ap.getAction().getAllowsAction() != null && ap.getAction().getAllowsAction().equals(action))
						|| (ap.getAction().getParent() != null && ap.getAction().getParent().getAllowsAction() != null
							&& ap.getAction().getParent().getAllowsAction().equals(action)))
						&& !ap.isNegative()) {
					found = true;
				}
				else if (ap.getAction().equals(action) && ap.isNegative()) {
					found = false;
					break;
				}
			}

		}
		catch (Exception e) {
			logger.error("problems evaluating permission for an action", e);
			throw new PoRuntimeException(e);
		}
		return found;
	}

	@Override
	public List<String> findAssignedPersonsOfAction(PoAPermissionBase apb) {

		List<String> list = new ArrayList<String>();
		PoAPermissionVisitor apV = new PoAPermissionAdapter();
		apb.accept(apV);
		if (apV.getWhichClassVisited().equals("AP")) {
			PoAPermissionPerson app = (PoAPermissionPerson) apV.getVisitedObject();
			list.add(app.getPerson().getUID());
		}
		if (apV.getWhichClassVisited().equals("AR")) {
			// find groups & persons which have the given role assigned
			PoAPermissionRole apr = (PoAPermissionRole) apV.getVisitedObject();
			// only normal roles
			// TODO also load dummy - roles
			List<PoRoleHolderPerson> rhPersons = roleService.findRoleHolderPersonsWithRoleF(apr.getRole(), apb.getValidfrom());
			for (PoRoleHolderPerson rhp : rhPersons) {
				list.add(rhp.getPerson().getUID());
			}
			List<PoRoleHolderGroup> rhGroups = roleService.findRoleHolderGroupF(apr.getRole(), apb.getValidfrom());
			for (PoRoleHolderGroup rhg : rhGroups) {
				list = addPersonsOfGroupToList(list, rhg.getGroup());
			}
		}

		if (apV.getWhichClassVisited().equals("AG")) {
			PoAPermissionGroup apg = (PoAPermissionGroup) apV.getVisitedObject();
			list = addPersonsOfGroupToList(list, apg.getGroup());
			if (apg.isInheritToChilds()) {
				List<PoGroup> childs = orgService.findAllChildGroupsFlat(apg.getGroup());
				for (PoGroup group : childs) {
					addPersonsOfGroupToList(list, group);
				}
			}
		}

		if (apV.getWhichClassVisited().equals("AC")) {
			PoAPermissionClient apc = (PoAPermissionClient) apV.getVisitedObject();
			List<PoPerson> persons = orgService.findPersonsOfClientF(apc.getClient(), HistorizationHelper.getNowWithHourPrecision());
			for (PoPerson person : persons) {
				list.add(person.getUID());
			}
		}
		return list;
	}

	@Override
	public List<CompetenceTarget> findCompetenceTargetForAction(PoGroup group, PoAction action, Date date) {

		List<PoAPermissionBase> perms = this.findAllActionPermissionsOfGroup(group, date);
		List<PoAPermissionBase> permsForAction = new ArrayList<PoAPermissionBase>();

		for (PoAPermissionBase perm : perms) {
			if (perm.getAction().equals(action)) {
				permsForAction.add(perm);
			}
		}

		Set<CompetenceTarget> compTargets = new HashSet<CompetenceTarget>();

		for (PoAPermissionBase perm : permsForAction) {
			if (perm.getViewPermissionType() == PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE) {
				// resolve role competence
				PoAPermissionRole permRole = (PoAPermissionRole) perm;
				for (CompetenceTarget ct : this.findActionCompetenceTargets(group, perm, date)) {
					compTargets.add(ct);
					if (ct instanceof PoGroup && permRole.isViewInheritToChilds()) {
						compTargets.addAll(this.orgService.findAllChildGroupsFlat((PoGroup) ct, date));
					}
				}
			}
			else {
				// just add the returned list
				compTargets.addAll(this.findActionCompetenceTargets(group, perm, date));
			}
		}

		return new ArrayList<CompetenceTarget>(compTargets);
	}

	@Override
	public List<CompetenceTarget> findCompetenceTargetForAction(PoPerson person, PoAction action, Date date) {

		List<PoAPermissionBase> perms = this.findAllActionPermissionsOfPerson(person, date);
		List<PoAPermissionBase> permsForAction = new ArrayList<PoAPermissionBase>();

		for (PoAPermissionBase perm : perms) {
			if (perm.getAction().equals(action)) {
				permsForAction.add(perm);
			}
		}

		Set<CompetenceTarget> compTargets = new HashSet<CompetenceTarget>();

		for (PoAPermissionBase perm : permsForAction) {
			if (perm.getViewPermissionType() == PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE) {
				// resolve role competence
				PoAPermissionRole permRole = (PoAPermissionRole) perm;
				for (CompetenceTarget ct : this.findActionCompetenceTargets(person, perm, date)) {
					compTargets.add(ct);
					if (ct instanceof PoGroup && permRole.isViewInheritToChilds()) {
						compTargets.addAll(this.orgService.findAllChildGroupsFlat((PoGroup) ct, date));
					}
				}
			}
			else {
				// just add the returned list
				compTargets.addAll(findActionCompetenceTargets(person, perm, date));
			}
		}

		return new ArrayList<CompetenceTarget>(compTargets);
	}

	private List<String> addPersonsOfGroupToList(List<String> l, PoGroup g) {

		List<PoPerson> persons = orgService.findPersonsOfGroup(g, HistorizationHelper.getNowWithHourPrecision());
		for (PoPerson p : persons) {
			l.add(p.getUID());
		}
		return l;
	}

	public void setActionDAO(PoActionDAO actionDAO) {
		this.actionDAO = actionDAO;
	}

	public void setGroupDAO(PoGroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}
	
	@Override
	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}

	public void setRoleService(PoRoleService roleService) {
		this.roleService = roleService;
	}

	public void setLicenceActionService(PoLicenceActionService licenceActionService) {
		this.licenceActionService = licenceActionService;
	}

	public void setSysAdminUserInfo(SysAdminUserInfo sysAdminUserInfo) {
		this.sysAdminUserInfo = sysAdminUserInfo;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setPersonActionPermissionCache(Cache personActionPermissionCache) {
		this.personActionPermissionCache = personActionPermissionCache;
	}

	
	private PoActionPermissionService getInstanceWithAopAdvices()	{
		if (instanceWithAopAdvices == null)
			instanceWithAopAdvices = (PoActionPermissionService) WebdeskApplicationContext.getBean("PoActionPermissionService");
		return instanceWithAopAdvices;
	}

	private boolean isUserSysAdmin(PoPerson person) {
		return this.sysAdminUserInfo.getSysAdminUser().equals(person.getUserName());
	}

}
