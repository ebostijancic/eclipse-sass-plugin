package at.workflow.webdesk.po.impl;

import java.beans.Expression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.CollectionUtils;
import org.springmodules.cache.util.Reflections;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.HistorizationServiceAdapter;
import at.workflow.webdesk.po.HistorizationTimelineHelper;
import at.workflow.webdesk.po.HistorizationTimelineRepairer;
import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.daos.PoClientDAO;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.daos.PoOrgStructureDAO;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.daos.PoRoleCompetenceDAO;
import at.workflow.webdesk.po.daos.PoRoleDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderGroupDAO;
import at.workflow.webdesk.po.daos.PoRoleHolderPersonDAO;
import at.workflow.webdesk.po.impl.daos.PoPersonBankAccountDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoPersonGroupDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoPersonImagesDAOImpl;
import at.workflow.webdesk.po.model.OrgTree;
import at.workflow.webdesk.po.model.OrgTreeNode;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonBankAccount;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.po.timeline.PoPersonGroupLinkHandler;
import at.workflow.webdesk.po.timeline.TimelineProcessor;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.I18nRuntimeException;
import at.workflow.webdesk.tools.api.SysAdminUserInfo;
import at.workflow.webdesk.tools.api.User;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.HistorizationUtils;

/**
 * IMPLEMENTATION OF PoOrganisationServiceImpl,
 * documentation can be found in the PoOrganisationService interface.
 * Have a look at po.applicationContext.xml in order to see how the 
 * dependency injection works.
 * Also the audit log interceptor is defined there.
 *
 * Created on 01.07.2005
 * @author hentner (Harald Entner)
 * @author ggruber
 */
public class PoOrganisationServiceImpl implements PoOrganisationService {

	private static final Logger logger = Logger.getLogger(PoOrganisationServiceImpl.class);

	// DAO'S //
	private PoClientDAO clientDAO;
	private PoGroupDAO groupDAO;
	private PoOrgStructureDAO orgStructureDAO;
	private PoPersonDAO personDAO;
	private PoPersonImagesDAOImpl personImagesDAO;
	private PoPersonGroupDAOImpl personGroupDAO;

	private PoRoleDAO roleDAO;
	private PoRoleCompetenceDAO roleCompetenceDAO;
	private PoRoleHolderDAO roleHolderDAO;
	private PoRoleHolderPersonDAO roleHolderPersonDAO;
	private PoRoleHolderGroupDAO roleHolderGroupDAO;
	private PoPersonBankAccountDAOImpl personBankAccountDAO;

	private PoActionDAO actionDAO;
	private HistorizationTimelineHelper historizationTimelineHelper;
	private HistorizationTimelineRepairer timelineRepairer;

	private PoGeneralDAO generalDAO;

	private boolean allowOnlySingleCostCenterAssignment = true;

	private PoPasswordService passwordService;

	private String headerText;

	private Cache groupHierarchyCache;

	private SysAdminUserInfo sysAdminUserInfo;

	private PoSearchService poSearchIndexService;

	public void setPersonBankAccountDAO(PoPersonBankAccountDAOImpl personBankAccountDao) {
		this.personBankAccountDAO = personBankAccountDao;
	}

	public void setTimelineRepairer(HistorizationTimelineRepairer timelineRepairer) {
		this.timelineRepairer = timelineRepairer;
	}

	/** Spring accessor */
	public void setPoSearchIndexService(PoSearchService poSearchIndexService) {
		this.poSearchIndexService = poSearchIndexService;
	}

	/** Spring accessor */
	public void setGeneralDAO(PoGeneralDAO generalDAO) {
		this.generalDAO = generalDAO;
	}

	/** Spring accessor */
	public void setPasswordService(PoPasswordService passwordService) {
		this.passwordService = passwordService;
	}

	/** Implemented by the new generic "find" method. */
	@Override
	public PoPerson findPersonByEmail(String email) {
		return personDAO.findPersonByEmail(email);
	}

	public void setHistorizationTimelineHelper(HistorizationTimelineHelper historizationTimelineHelper) {
		this.historizationTimelineHelper = historizationTimelineHelper;
	}

	/** Spring dependency injection setter. */
	public void setClientDAO(PoClientDAO clientDAO) {
		this.clientDAO = clientDAO;
	}

	/** Spring dependency injection setter. */
	public void setGroupDAO(PoGroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}

	/** Spring dependency injection setter. */
	public void setOrgStructureDAO(PoOrgStructureDAO orgStructureDAO) {
		this.orgStructureDAO = orgStructureDAO;
	}

	/** Spring dependency injection setter. */
	public void setPersonDAO(PoPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	/** Spring dependency injection setter. */
	public void setPersonImagesDAO(PoPersonImagesDAOImpl personImagesDAO) {
		this.personImagesDAO = personImagesDAO;
	}

	/** Spring dependency injection setter. */
	public void setActionDAO(PoActionDAO actionDAO) {
		this.actionDAO = actionDAO;
	}

	/** Spring dependency injection setter. */
	public void setRoleDAO(PoRoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	/** Spring dependency injection setter. */
	public void setRoleCompetenceDAO(PoRoleCompetenceDAO roleCompetenceDAO) {
		this.roleCompetenceDAO = roleCompetenceDAO;
	}

	/** Spring dependency injection setter. */
	public void setRoleHolderDAO(PoRoleHolderDAO roleHolderDAO) {
		this.roleHolderDAO = roleHolderDAO;
	}

	/** Spring dependency injection setter. */
	public void setRoleHolderPersonDAO(PoRoleHolderPersonDAO roleHolderPersonDAO) {
		this.roleHolderPersonDAO = roleHolderPersonDAO;
	}

	/** Spring dependency injection setter. */
	public void setRoleHolderGroupDAO(PoRoleHolderGroupDAO roleHolderGroupDAO) {
		this.roleHolderGroupDAO = roleHolderGroupDAO;
	}

	/* CLIENT METHOD'S ********************************************************/

	@Override
	public void saveClient(PoClient client) {
		clientDAO.save(client);
	}

	@Override
	public PoClient getClient(String uid) {
		return clientDAO.get(uid);
	}

	@Override
	public List<PoClient> loadAllClients() {
		return clientDAO.loadAll();

	}

	@Override
	public PoClient findClientByName(String name) {
		return clientDAO.findClientByName(name);
	}

	@Override
	public void deleteClient(PoClient client) {
		throw new PoRuntimeException("Not implemented so far. Version 3.1");
	}

	@Override
	public boolean isClientExistent(String name) {
		return clientDAO.isClientExistent(name);
	}

	@Override
	public boolean isOrgStructureExistent(String name, String client_uid) {
		return orgStructureDAO.isOrgStructureExistent(name, client_uid);
	}

	/* GROUP METHOD'S ********************************************************/

	/** save, get (Parent) , find ,setParent, delete methods **/
	@Override
	public void saveGroup(PoGroup group) {
		if (group.getValidfrom() == null)
			group.setValidfrom(DateTools.today());
		if (group.getValidto() == null)
			group.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));

		if (group.getShortName() == null || group.getShortName().equals(""))
			throw new PoRuntimeException(PoRuntimeException.ERROR_NO_SHORTNAME_SPECIFIED);

		if (group.getUID() == null)
			logger.debug("Save group " + group.getShortName());
		else
			logger.debug("Update group " + group.getShortName());

		groupDAO.save(group);
	}

	@Override
	public void saveParentGroup(PoParentGroup pg) {
		groupDAO.saveParentGroup(pg);
	}

	@Override
	public PoGroup getGroup(String uid) {
		return groupDAO.get(uid);
	}

	@Override
	public PoParentGroup getParentGroup(String uid) {
		return groupDAO.getParentGroup(uid);
	}

	@Override
	public PoPersonGroup getPersonGroup(String uid) {
		return personDAO.getPersonGroup(uid);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroupLink(at.workflow.webdesk.po.model.PoGroup, at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public PoParentGroup getParentGroupLink(PoGroup childGroup, PoGroup parentGroup, Date validAt) {
		return groupDAO.getParentGroup(childGroup, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(validAt));
	}

	@Override
	public void deletePGWithoutConstraints(PoParentGroup pg) {
		groupDAO.deleteParentGroup(pg);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#removeGroupFromParentGroup(at.workflow.webdesk.po.model.PoParentGroup)
	 */
	@Override
	public void removeGroupFromParentGroup(PoParentGroup parentGroup) {
		parentGroup.historicize();
		if (parentGroup.getValidity().isPositive())
			groupDAO.saveParentGroup(parentGroup);
		else
			groupDAO.deleteParentGroup(parentGroup);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#changeValidityParentGroupLink(at.workflow.webdesk.po.model.PoParentGroup, java.util.Date, java.util.Date)
	 */
	@Override
	public void changeValidityParentGroupLink(PoParentGroup parentGroup, Date validFrom, Date validTo) {
		if (validFrom == null || validFrom.before(DateTools.today()))
			validFrom = DateTools.today(); // changed to day exact assignment
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		parentGroup.setValidfrom(validFrom);
		parentGroup.setValidto(validTo);
		
		if ( ! parentGroup.getValidity().isPositive())
			groupDAO.deleteParentGroup(parentGroup);
		// copied from dao -> no save method!
	}

	@Override
	public List<PoGroup> findGroupByName(String key) {
		return findGroupByName(key, DateTools.today());
	}

	@Override
	public PoGroup findGroupByName(String key, PoClient client) {
		return findGroupByName(key, client, DateTools.today());
	}

	@Override
	public List<PoGroup> findGroupByName(String key, Date referenceDate) {
		return groupDAO.findGroupByName(key, HistorizationHelper.generateUsefulValidFromDay(referenceDate));
	}

	@Override
	public PoGroup findGroupByName(String key, PoClient client, Date referenceDate) {
		return groupDAO.findGroupByName(key, client, HistorizationHelper.generateUsefulValidFromDay(referenceDate));
	}

	@Override
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date date) {
		return groupDAO.findGroupsWithFilter(uids, names, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date from, Date to) {
		return groupDAO.findGroupsWithFilter(uids, names, HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findChildGroups(at.workflow.webdesk.po.model.PoGroup)
	 */
	@Override
	public List<PoParentGroup> findChildGroups(PoGroup group) {
		return groupDAO.findChildGroups(group);
	}

	@Override
	public List<PoParentGroup> findChildGroups(PoGroup group, Date effectiveDate) {
		return groupDAO.findChildGroups(group, HistorizationHelper.generateUsefulValidFromDay(effectiveDate));
	}

	@Override
	public List<PoPerson> findPersonsOfClientF(PoClient client, Date date) {
		return personDAO.findPersonsOfClientF(client, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoGroup> findGroupsFromClientF(PoClient client, Date date) {
		return groupDAO.findGroupsFromClientF(client, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public PoParentGroup getParentGroup(PoGroup group) {
		return getParentGroup(group, null);
	}

	@Override
	public PoParentGroup getParentGroup(PoGroup group, Date referenceDate) {
		return groupDAO.getParentGroup(group, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(referenceDate));
	}

	@Override
	public List<PoGroup> loadAllGroups() {
		return groupDAO.loadAll();
	}

	@Override
	public List<PoParentGroup> loadAllParentGroups() {
		return groupDAO.loadAllParentGroups();
	}

	@Override
	public void setParentGroup(PoGroup group, PoGroup parent) {
		setParentGroup(group, parent, new Date(), null, false);
	}

	@Override
	public void setParentGroup(PoGroup group, PoGroup parent, Date validFrom, Date validTo) {
		setParentGroup(group, parent, validFrom, validTo, false);
	}

	public void setParentGroup(PoGroup child, PoGroup parent, Date validFrom, Date validTo, boolean deleteFuturePlans) {
		
		PoGroup hParent = parent;
		PoParentGroup hParentGroup = new PoParentGroup();

		if (parent.equals(child))
			throw new PoRuntimeException(PoRuntimeException.ERROR_TRY_TO_ASSIGN_ITSELF_AS_PARENT);

		if (getTopLevelGroupsOfOrgStructure(child.getOrgStructure(), true).contains(child))
			throw new PoRuntimeException(PoRuntimeException.ERROR_TRY_TO_ASSIGN_A_TOP_LEVEL_GROUP);

		hParentGroup = getParentGroup(hParent);

		// avoid circles in PoParentGroup objects 
		if (hParentGroup != null) {
			hParent = hParentGroup.getParentGroup();

			while (hParent != null) {
				if (hParent == child)
					throw new PoRuntimeException(PoRuntimeException.ERROR_TRY_TO_ASSIGN_CHILD_AS_PARENT);

				hParentGroup = getParentGroup(hParent);
				if (hParentGroup != null)
					hParent = hParentGroup.getParentGroup();
				else
					hParent = null;
			}
		}

		// use historizationhelper to generate useful validfrom/validto days
		validFrom = HistorizationHelper.generateUsefulValidFromDay(validFrom);
		validTo = HistorizationHelper.generateUsefulValidToDay(validTo);

		// parent has to be valid before or concurrent to the new relation
		if (parent.getValidfrom().compareTo(validFrom) <= 0) {
			// parent has to be longer valid than the effective Date
			if (parent.getValidto().compareTo(validTo) >= 0) {
				// child has to be valid before or concurrently to the start date.
				if (child.getValidfrom().compareTo(validFrom) <= 0)
				{ // child has to be valid at least until the  validFrom 
					if (child.getValidto().compareTo(validTo) >= 0) {

						// check for a time overlap of the actual child 
						// search for all relationships 

						List<PoParentGroup> existingHistObjects = groupDAO.findParentGroupsWithTimeOverlap(
								child, validFrom, child.getValidto());
						HistorizationServiceAdapter adapter = new PoParentGroupHistorizationServiceAdapter(
								parent, child);
						historizationTimelineHelper.generateHistorizationObject(existingHistObjects, adapter,
								validFrom, validTo);

					} else
						throw new PoRuntimeException(PoRuntimeException.ERROR_SET_CHILD_NOT_SO_LONG_VALID);
				} else
					throw new PoRuntimeException(PoRuntimeException.ERROR_SET_CHILD_NOT_YET_VALID);
			} else
				throw new PoRuntimeException(PoRuntimeException.ERROR_SET_PARENT_NOT_SO_LONG_VALID);
		} else
			throw new PoRuntimeException(PoRuntimeException.ERROR_SET_PARENT_NOT_YET_VALID);
	}

	@Override
	public List<PoParentGroup> findParentGroupsAll(PoGroup group) {
		return groupDAO.findParentGroupsAll(group);
	}

	@Override
	public void deleteGroup(PoGroup group) {
		
		Date yesterdayLastMoment = DateTools.lastMomentOfDay(DateTools.yesterday());

		// group expired! let all relations between groups and persons expire
		if (groupDAO.findPersonsOfGroupF(group, yesterdayLastMoment).size() > 0)
			throw new PoRuntimeException(PoRuntimeException.ERROR_DELETE_NOT_ALLOWED + ": dedicated persons exist");

		if (findChildGroupsF(group, yesterdayLastMoment).size() > 0)
			throw new PoRuntimeException(PoRuntimeException.ERROR_DELETE_NOT_ALLOWED + ": dedicated childgroups exist");

		PoRoleHolderGroup myRoleHolderGroup;

		// let all relations between groups and roleHolders expire
		Iterator<PoRoleHolderGroup> roleHolderIterator = group.getReferencedAsRoleHolder().iterator();
		while (roleHolderIterator.hasNext()) {
			myRoleHolderGroup = roleHolderIterator.next();
			;
			if (HistorizationHelper.isValid(myRoleHolderGroup, yesterdayLastMoment)) {
				myRoleHolderGroup.setValidto(yesterdayLastMoment);
				roleHolderGroupDAO.save(myRoleHolderGroup);
			}
		}

		// let all relations between groups and competence targets of role holders expire
		Iterator<PoRoleCompetenceGroup> roleHolderCompetenceTarget = group.getReferencedAsCompetenceTarget().iterator();
		List<PoRoleCompetenceBase> toRemove = new ArrayList<PoRoleCompetenceBase>();
		while (roleHolderCompetenceTarget.hasNext()) {
			PoRoleCompetenceBase o = roleHolderCompetenceTarget.next();
			if (o instanceof PoRoleCompetenceGroup && HistorizationHelper.isValid(o, yesterdayLastMoment)) {
				PoRoleCompetenceGroup myRoleHolderCompetenceGroup = (PoRoleCompetenceGroup) o;
				myRoleHolderCompetenceGroup.setValidto(yesterdayLastMoment);
				roleCompetenceDAO.save(myRoleHolderCompetenceGroup);
				toRemove.add(o);
			}
			if (o instanceof PoRoleCompetencePerson && HistorizationHelper.isValid(o, yesterdayLastMoment)) {
				PoRoleCompetencePerson myRoleHolderCompetencePerson = (PoRoleCompetencePerson) o;
				myRoleHolderCompetencePerson.setValidto(yesterdayLastMoment);
				roleCompetenceDAO.save(myRoleHolderCompetencePerson);
				toRemove.add(o);

			}
		}

		group.getReferencedAsCompetenceTarget().removeAll(toRemove);
		if (HistorizationHelper.isValid(group, yesterdayLastMoment)) {
			group.historicize();
			groupDAO.save(group);
		} else
			groupDAO.delete(group);
	}

	private void walkDownRecursive(PoGroup group, List<PoGroup> parentGroups) {
		Iterator<String> i = findChildGroupUids(group.getUID(), null).iterator();
		while (i.hasNext()) {
			String childGroupUid = i.next();
			PoGroup childGroup = groupDAO.get(childGroupUid);
			parentGroups.remove(childGroup);
			walkDownRecursive(childGroup, parentGroups);
		}
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findAvailableParentGroups(at.workflow.webdesk.po.model.PoGroup)
	 */
	@Override
	public List<PoGroup> findAvailableParentGroups(PoGroup group) {
		List<PoGroup> parentGroups = findGroupsOfOrgStructureF(group.getOrgStructure(), DateTools.today());

		if (StringUtils.isBlank(group.getUID()))
			return parentGroups;
		
		if (parentGroups.contains(group)) // remove group itself
			parentGroups.remove(group);

		walkDownRecursive(group, parentGroups); // remove all child groups

		PoParentGroup parentGroup = getParentGroup(group);
		if (parentGroup != null) // remove current parentgroup
			parentGroups.remove(parentGroup.getParentGroup());

		return parentGroups;
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findAvailableChildGroups(at.workflow.webdesk.po.model.PoGroup)
	 */
	@Override
	public List<PoGroup> findAvailableChildGroups(PoGroup group) {
		Date today = DateTools.today();

		List<PoGroup> groupsOfOrgStructure = findGroupsOfOrgStructureF(group.getOrgStructure(), today);
		if (groupsOfOrgStructure.contains(group))
			groupsOfOrgStructure.remove(group);

		groupsOfOrgStructure.removeAll(findAllChildGroupsFlat(group, today));

		return groupsOfOrgStructure;
	}

	/**
	 * <p>Returns an <code>OrgTree</code> object which represents 
	 * the hierarchical structure of the underlying groups. This function
	 * only makes sense for <code>PoOrgStructure</code> objects with 
	 * property hierarchy set to true. 
	 * </p><p>
	 * This function will be cached. Consider that 
	 * the OrgTree has a <code>validTo</code> property, that 
	 * states until when tree is valid. As the 
	 * caching is declarative, we manually have to check this.
	 * </p><p>
	 * The current date is used to query the database. If 
	 * you need the orgModel in past or in future use 
	 * getOrgModel(2) 
	 * </p>
	 * @return an <code>OrgTree</code>
	 */
	@Override
	public OrgTree getOrgModelCached(PoOrgStructure orgS) {
		return getOrgModel(orgS, null);
	}

	/**
	 * Returns an <code>OrgTree</code> object which represents 
	 * the hierarchical structure of the underlying groups. This function
	 * only makes sense for <code>PoOrgStructure</code> objects with 
	 * property hierarchy set to true. 
	 */
	@Override
	public OrgTree getOrgModel(PoOrgStructure organisationStructure, Date date) {
		long time = System.currentTimeMillis();

		OrgTree organisationTree = new OrgTree();
		Iterator<PoGroup> topLevelGroupsIterator = getTopLevelGroupsOfOrgStructure(organisationStructure, true).iterator();

		while (topLevelGroupsIterator.hasNext()) {
			PoGroup topLevelGroup = topLevelGroupsIterator.next();
			if (logger.isDebugEnabled())
				logger.debug("building orgmodel for toplevelgroup " + topLevelGroup.getShortName());

			organisationTree.addRootNode(getOrgModelRecursive(topLevelGroup, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date)));
		}

		if (logger.isDebugEnabled())
			logger.debug("getOrgModel needed millis: " + (System.currentTimeMillis() - time));

		return organisationTree;
	}

	/**
	 * Helper class that holds the childGroupUid with the relation UID (PoParentGroup),
	 * as sometimes the relation records of the hierarchy are needed.
	 */
	@SuppressWarnings("serial")
	private static class GroupParentChildRelation implements Serializable
	{
		final String relationUid;
		final String childGroupUid;

		public GroupParentChildRelation(String relationUid, String childGroupUid) {
			this.relationUid = relationUid;
			this.childGroupUid = childGroupUid;
		}
	}

	/**
	 * Helper with key = PoGroup UID and value List of child PoGroup UIDs
	 * that represents the parent/child hierarchy of PoGroup via relation PoParentGroup.
	 */
	@SuppressWarnings("serial")
	private static class GroupHierarchy extends Hashtable<String, List<GroupParentChildRelation>>
	{
		public void putRelation(String relationUid, String parentGroupUid, String childGroupUid) {
			List<GroupParentChildRelation> children = get(parentGroupUid);
			if (children == null) {
				children = new ArrayList<GroupParentChildRelation>();
				put(parentGroupUid, children);
			}
			children.add(new GroupParentChildRelation(relationUid, childGroupUid));
		}
	}

	private GroupHierarchy buildGroupHierarchy(Date date) {
		final Date queryDate = HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date);
		final int cacheKey = Reflections.reflectionHashCode(queryDate);

		if (groupHierarchyCache.isKeyInCache(cacheKey)) {
			Element cacheObject = groupHierarchyCache.get(cacheKey);
			if (cacheObject != null) { // see https://extranet.workflow.at/jira/browse/WDPTM-297
				Object o = cacheObject.getValue();
				if (o instanceof GroupHierarchy) {
					return (GroupHierarchy) o;
				}
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("Call buildGroupHierarchy with date: " + queryDate);

		@SuppressWarnings({ "cast", "unchecked" })
		final List<PoParentGroup> groupRelations = (List<PoParentGroup>) generalDAO.getElementsAsList(
				"from PoParentGroup pg where pg.validfrom <= ? and pg.validto > ? order by pg.ranking, pg.childGroup.shortName asc",
				new Object[] { queryDate, queryDate, });

		final GroupHierarchy hierarchy = new GroupHierarchy();
		for (PoParentGroup groupRelation : groupRelations) {
			PoGroup parentGroup = groupRelation.getParentGroup();
			PoGroup childGroup = groupRelation.getChildGroup();
			hierarchy.putRelation(groupRelation.getUID(), parentGroup.getUID(), childGroup.getUID());
		}

		groupHierarchyCache.put(new Element(cacheKey, hierarchy));

		return hierarchy;
	}

	private OrgTreeNode getOrgModelRecursive(PoGroup group, Date date) {
		assert group != null;

		OrgTreeNode organisationTreeNode = new OrgTreeNode(group);
		GroupHierarchy hierarchy = buildGroupHierarchy(date);
		List<GroupParentChildRelation> parentChildRelations = hierarchy.get(group.getUID());
		if (parentChildRelations != null) { // groups that have no child groups are legal
			Iterator<GroupParentChildRelation> relationsIterator = parentChildRelations.iterator();
			while (relationsIterator.hasNext()) {
				// refresh the cached Hibernate object from hibernate cache
				PoGroup childGroup = groupDAO.get(relationsIterator.next().childGroupUid);

				// this check for validity fixes: http://intranet/intern/ifwd_mgm.nsf/0/1824D93E56E8D019C125798F0057798F?OpenDocument 
				// notes://Miraculix/intern/ifwd_mgm.nsf/0/1824D93E56E8D019C125798F0057798F?EditDocument
				if (HistorizationHelper.isValid(childGroup, date) == true)
					organisationTreeNode.addChild(getOrgModelRecursive(childGroup, date));
			}
		}
		return organisationTreeNode;
	}

	@Override
	public List<String> findChildGroupUids(String groupUid, Date date) {
		GroupHierarchy hierarchy = buildGroupHierarchy(HistorizationHelper.generateUsefulValidFromDay(date));
		List<String> childGroupUids = new ArrayList<String>();
		if (groupUid == null) // non-persistent group
			return childGroupUids;
		List<GroupParentChildRelation> relations = hierarchy.get(groupUid);
		if (relations != null) {
			for (GroupParentChildRelation relation : relations) {
				childGroupUids.add(relation.childGroupUid);
			}
		}
		return childGroupUids;
	}

	@Override
	public void deleteAndFlushGroup(PoGroup group) {
		// delete parentgroup links 
		Iterator<PoParentGroup> parentGroupIterator = groupDAO.findAllParentGroups(group).iterator();
		while (parentGroupIterator.hasNext()) {
			groupDAO.deleteParentGroup(parentGroupIterator.next());
		}

		// delete child group links
		GroupHierarchy groupHierarchy = buildGroupHierarchy(HistorizationHelper.getNowWithHourPrecision());
		Iterator<PoParentGroup> childGroupIterator = findAllChildGroupRelationsFlatRecursive(groupHierarchy, group, HistorizationHelper.getNowWithHourPrecision(), new ArrayList<PoParentGroup>())
				.iterator();
		while (childGroupIterator.hasNext()) {
			groupDAO.deleteParentGroup(childGroupIterator.next());
		}

		group.getOrgStructure().getGroups().remove(group);
		group.getClient().getGroups().remove(group);
		groupDAO.delete(group);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getDepthOfGroup(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public int getDepthOfGroup(PoGroup group, Date validAt) {
		Date queryDate = HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(validAt);
		int depth = 0;
		PoGroup tempGroup = group;
		while (tempGroup != null) {
			PoParentGroup pg = getParentGroup(tempGroup, queryDate);
			if (pg != null) {
				tempGroup = pg.getParentGroup();
				depth++;
			} else
				tempGroup = null;
		}
		return depth;
	}

	/**
	 * @see at.workflow.webdesk.po.PoGroupDAO#getAllChildGroupsFlat(at.workflow.webdesk.po.model.PoGroup)
	 */
	@Override
	public List<PoGroup> findAllChildGroupsFlat(PoGroup group) {
		return findAllChildGroupsFlat(group, null);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findAllChildGroupsFlat(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public List<PoGroup> findAllChildGroupsFlat(PoGroup group, Date effectiveDate) {
		return findAllChildGroupsFlatRecursive(group, HistorizationHelper.generateUsefulValidFromDay(effectiveDate), new ArrayList<PoGroup>());
	}

	private List<PoGroup> findAllChildGroupsFlatRecursive(PoGroup group, Date effectiveDate, List<PoGroup> childGroupList) {
		if (logger.isDebugEnabled())
			logger.debug("findAllChildGroupsFlatRecursive at " + effectiveDate);
		List<String> gpChildren = findChildGroupUids(group.getUID(), effectiveDate);
		for (String childGroupUid : gpChildren) {
			PoGroup childGroup = groupDAO.get(childGroupUid);
			childGroupList.add(childGroup);
			findAllChildGroupsFlatRecursive(childGroup, effectiveDate, childGroupList);
		}
		return childGroupList;
	}

	private List<PoParentGroup> findAllChildGroupRelationsFlatRecursive(GroupHierarchy groupHierarchy, PoGroup group, Date effectiveDate, List<PoParentGroup> childRelationList) {

		List<GroupParentChildRelation> relations = groupHierarchy.get(group.getUID());
		if (relations != null) {
			for (GroupParentChildRelation relation : relations) {
				PoGroup childGroup = groupDAO.get(relation.childGroupUid);
				childRelationList.add(groupDAO.getParentGroup(relation.relationUid));
				findAllChildGroupRelationsFlatRecursive(groupHierarchy, childGroup, effectiveDate, childRelationList);
			}
		}

		return childRelationList;
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroupByIndex(at.workflow.webdesk.po.model.PoGroup, int, java.util.Date)
	 */
	@Override
	public PoGroup getParentGroupByIndex(PoGroup group, int index, Date date) {
		Date queryDate = HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date);
		while (getDepthOfGroup(group, queryDate) >= index) {
			PoParentGroup pg = getParentGroup(group, queryDate);
			if (pg != null)
				group = pg.getParentGroup();
			else
				break;
		}
		return group;
	}

	@Override
	public List<PoGroup> findNotHierarchicalGroupsOfPerson(PoPerson person, Date date) {
		Date referenceDate = HistorizationHelper.generateUsefulValidFromDay(date);
		List<PoGroup> groups = new ArrayList<PoGroup>();
		for (PoOrgStructure struct : loadAllOrgStructures())
			if (false == struct.isHierarchy()) {
				groups.addAll(groupDAO.findGroupWithOrgStructureAndPerson(person, struct, referenceDate));
			}
		return groups;
	}

	/* ORGSTRUCTURE METHODS ********************************************************/

	@Override
	public PoOrgStructure getOrgStructure(String uid) {
		return orgStructureDAO.get(uid);
	}

	/** TODO fri_24-02-2011: this method could be private. */
	@Override
	public List<PoGroup> getTopLevelGroupsOfOrgStructure(PoOrgStructure os, boolean includeFlag) {
		return orgStructureDAO.getTopLevelGroupsOfOrgStructure(os, new Date(), includeFlag);
	}

	@Override
	public List<PoGroup> getTopLevelGroupsOfOrgStructureWithDate(PoOrgStructure os, Date date, boolean includeFlag) {
		return orgStructureDAO.getTopLevelGroupsOfOrgStructure(os, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date), includeFlag);
	}

	@Override
	public void saveOrgStructure(PoOrgStructure orgStructure) {
		PoClient client = orgStructure.getClient();
		if (client != null) {
			// to include current structure in the check
			List<PoOrgStructure> orgStructures = new ArrayList<PoOrgStructure>(client.getOrgStructures());
			if (orgStructures.contains(orgStructure) == false)
				orgStructures.add(orgStructure);

			checkClientsOrgStructures(client, orgStructures);
		}
		orgStructureDAO.save(orgStructure);
	}

	@Override
	public List<PoOrgStructure> loadAllOrgStructures(PoClient client) {
		return orgStructureDAO.findOrgStructuresOfClient(client);
	}

	@Override
	public List<PoOrgStructure> loadAllOrgStructures() {
		return orgStructureDAO.loadAll();
	}

	@Override
	public PoOrgStructure findOrgStructureByName(PoClient client, String name) {
		List<PoOrgStructure> orgStructures = orgStructureDAO.findOrgStructureByName(client, name);
		if (orgStructures.size() == 1)
			return orgStructures.get(0);
		else if (orgStructures.size() > 1)
			throw new PoRuntimeException("More than one OrgStructures with name " + name + " and client " + client.getName() + " exists: " + orgStructures.size());
		else
			return null;
	}

	@Override
	public PoOrgStructure getOrgHierarchy(PoClient client) {
		return orgStructureDAO.getOrgHierarchy(client);
	}

	@Override
	public PoOrgStructure getOrgLocations(PoClient client) {
		return orgStructureDAO.getOrgLocations(client);
	}

	@Override
	public PoOrgStructure getOrgCostCenters(PoClient client) {
		return orgStructureDAO.getOrgCostCenters(client);
	}

	@Override
	public void deleteOrgStructure(PoOrgStructure orgStructure) {
		throw new PoRuntimeException("not implemented -> version 3.1");
	}

	@Override
	public void deleteAndFlushOrgStructure(PoOrgStructure orgStructure) {
		orgStructureDAO.delete(orgStructure);
	}

	private void checkClientsOrgStructures(PoClient client, List<PoOrgStructure> orgStructures) {

		if (CollectionUtils.isEmpty(orgStructures))
			throw new I18nRuntimeException("The client must have at least one org structure");

		boolean hasHierarchical = false, hasCostCenters = false, hasLocations = false;
		for (PoOrgStructure orgStructure : orgStructures) {

			if (orgStructure.getOrgType() == PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
				if (hasHierarchical)
					throw new I18nRuntimeException("The client has at least two hierarchical structures");
				else if (!orgStructure.isHierarchy() || !orgStructure.isAllowOnlySingleGroupMembership())
					throw new I18nRuntimeException("The client has inconsistent hierarchical structure");
				else
					hasHierarchical = true;
			} else if (orgStructure.getOrgType() == PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS) {
				if (hasCostCenters)
					throw new I18nRuntimeException("The client has at least two cost center structures");
				else if (!orgStructure.isAllowOnlySingleGroupMembership())
					throw new I18nRuntimeException("The client has inconsistent cost center structure");
				else
					hasCostCenters = true;
			} else if (orgStructure.getOrgType() == PoOrgStructure.STRUCTURE_TYPE_LOCATIONS) {
				if (hasLocations)
					throw new I18nRuntimeException("The client has at least two locations structures");
				else if (orgStructure.isHierarchy() || !orgStructure.isAllowOnlySingleGroupMembership())
					throw new I18nRuntimeException("The client has inconsistent location structure");
				else
					hasCostCenters = true;
			}
		}
	}

	/* PERSON METHOD'S ********************************************************/

	/** Save, find, link to group, remove from group and delete **/
	@Override
	public void savePerson(PoPerson person, PoGroup group) {
		if (person.getValidfrom() == null)
			person.setValidfrom(DateTools.today());
		if (person.getValidto() == null)
			person.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));

		// THINK THINK
		// there might be better places to do this...
		// in the javabean? or in the DAO?
		if (person.getUserName() == null || person.getUserName().equals("")) {
			// no username is not allowed -> throw an error
			throw new PoRuntimeException(PoRuntimeException.ERROR_NO_USERNAME_SPECIFIED);
		}
		if (person.getLastName() == null || person.getLastName().equals("")) {
			// no username is not allowed -> throw an error
			throw new PoRuntimeException(PoRuntimeException.ERROR_NO_LASTNAME_SPECIFIED);
		}

		if (group.getOrgStructure().isHierarchy()) {

// TODO Wd-39			
//			HistorizationTimelineUtils.checkValidity(person, group, person.getValidity());
			
			PoPersonGroup pg = new PoPersonGroup();
			pg.setPerson(person);
			pg.setGroup(group);
			pg.setValidfrom(person.getValidfrom());
			pg.setValidto(person.getValidto());
			group.addPersonGroup(pg);
			person.addMemberOfGroup(pg);

			personDAO.save(person, group);
			personDAO.savePersonGroup(pg);
			saveBankAccounts(person);
		} else {
			throw new PoRuntimeException(PoRuntimeException.ERROR_NO_HIERARCHICAL_GROUP_EXISTS);
		}
	}

	@Override
	public void updatePerson(PoPerson person) {
		personDAO.update(person);
		savePersonGroups(person);
		saveBankAccounts(person);
	}

	private void savePersonGroups(PoPerson person) {

		Collection<PoPersonGroup> links = person.getMemberOfGroups();
		List<PoPersonGroup> fromDB = personDAO.findPersonGroupsAll(person);
		
// TODO: WD-39		
//		for (PoPersonGroup link : links)
//			HistorizationTimelineUtils.checkValidity(person, getGroup(link.getGroup().getUID()), link.getValidity());
		
		TimelineProcessor<PoPersonGroup> processor =
				new TimelineProcessor<PoPersonGroup>(personGroupDAO, new PoPersonGroupLinkHandler());
		processor.processAndSaveLinks(fromDB, links);
	}

	@Override
	public void saveBankAccounts(PoPerson person) {

		Collection<PoPersonBankAccount> links = person.getBankAccounts();
		List<PoPersonBankAccount> fromDB = personBankAccountDAO.findBankAccountsForPerson(person);

		// collect uniquely all usage codes
		Set<String> usageCodes = new HashSet<String>();
		for (PoPersonBankAccount bankAccount : links)
			usageCodes.add(bankAccount.getUsageCode());
		for (PoPersonBankAccount bankAccount : fromDB)
			usageCodes.add(bankAccount.getUsageCode());

		TimelineProcessor<PoPersonBankAccount> processor =
				new TimelineProcessor<PoPersonBankAccount>(personBankAccountDAO, new PoPersonBankAccountLinkRemover());

		// historicize bank accounts per usage code
		for (String usageCode : usageCodes) {

			List<PoPersonBankAccount> linksWithUsageCodes = new ArrayList<PoPersonBankAccount>();
			for (PoPersonBankAccount account : links)
				if (StringUtils.equals(account.getUsageCode(), usageCode))
					linksWithUsageCodes.add(account);

			List<PoPersonBankAccount> fromDBWithUsageCodes = new ArrayList<PoPersonBankAccount>();
			for (PoPersonBankAccount account : fromDB)
				if (StringUtils.equals(account.getUsageCode(), usageCode))
					fromDBWithUsageCodes.add(account);

			timelineRepairer.repairTimeline(fromDBWithUsageCodes, linksWithUsageCodes, person.getValidity());

			processor.processAndSaveLinks(fromDBWithUsageCodes, linksWithUsageCodes);
		}
	}

	@Override
	public void historicizeBankAccounts(PoPerson person) {

		for (PoPersonBankAccount bankAccount : person.getBankAccounts())
			if (bankAccount.getValidto().after(DateTools.now()))
				personBankAccountDAO.historicize(bankAccount);
	}

	@Override
	public PoPerson getPerson(String uid) {
		return personDAO.get(uid);
	}

	@Override
	public List<PoPerson> loadAllPersons() {
		return personDAO.loadAll();
	}

	@Override
	public boolean isPersonActualMemberOfGroup(PoPerson person, PoGroup group) {
		return personDAO.isPersonActualMemberOfGroup(person, group);
	}

	@Override
	public List<PoPerson> findAllPersons(Date date) {
		return personDAO.findAllPersons(HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoPerson> findAllCurrentPersons() {
		return personDAO.findAllPersons(DateTools.today());
	}

	@Override
	public List<PoPerson> findAllPersons(Date date, boolean activeUser) {
		return personDAO.findAllPersons(HistorizationHelper.generateUsefulValidFromDay(date), activeUser);
	}

	@Override
	public List<PoPerson> findAllCurrentActivePersons() {
		return personDAO.findAllPersons(DateTools.today(), true);
	}

	@Override
	public void refresh(Object obj) {
		personDAO.refresh(obj);
	}

	@Override
	public void linkPerson2Group(PoPerson person, PoGroup group) {
		linkPerson2Group(person, group, null, null);
	}

	/**
	 * FIXME: Needs some refactoring
	 * - bug: it is possible to link a person 2 times to a non hierarchical group!
	 */
	@Override
	public void linkPerson2Group(PoPerson person, PoGroup group, Date validFrom, Date validTo) {
		// sdzuban 11.12.2013 change to day-exact dates
    	validFrom = HistorizationHelper.generateUsefulValidFromDay(validFrom);
    	validTo = HistorizationHelper.generateUsefulValidToDay(validTo);

    	validFrom = checkValidity(person, group, validFrom, validTo);

        boolean allowOnlyOneAssignmentAtATime =
        	(group.getOrgStructure().getOrgType() == PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY ||
        		(group.getOrgStructure().getOrgType() == PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS && allowOnlySingleCostCenterAssignment) ||
        		group.getOrgStructure().isAllowOnlySingleGroupMembership());
        
        // dates of group, person and date arguments are valid
        // is it an organisation hierarchy?
        if (allowOnlyOneAssignmentAtATime) {
            // get already assigned group, ordered by validTo (orgType = hierarchical)
            List<PoPersonGroup> currentPGroups = getPersonGroups(person, group.getOrgStructure(), validFrom);
            
            if (currentPGroups.size() == 0) {
            	handleZeroPGroups(person, group, validFrom, validTo);
				return;
			}

			if (currentPGroups.size() == 1) { // mandatory, at one moment, only one group is allowed
				HistorizationServiceAdapter adapter = new PoPersonGroupHistorizationServiceAdapter(person, group);

				// for change also existing future PoPersonGroups must be considered
				List<PoPersonGroup> currentAndFuturePGroups = findPersonGroupsF(person, validFrom, group.getOrgStructure());
				historizationTimelineHelper.generateHistorizationObject(currentAndFuturePGroups, adapter, validFrom, validTo);

			} else {
				// this is an error normally, but we could try to fix it
				// unfortunately the fix is not shown to the user (except for a logging message.
				handleMultipleHierarchyGroups(person, currentPGroups);
			}
			// clear GroupMembership Entry
		} else {
			// no organisation hierarchy,
			// the only constraint is that the same group is not allowed to be assigned twice, when already assigned.
			handleNonHierarchicGroups(person, group, validFrom, validTo);
		}
	}

	private Date checkValidity(PoPerson person, PoGroup group, Date validFrom, Date validTo) {

		boolean b = validTo.after(new Date());
        boolean c = validFrom.before(validTo);
        boolean d = person.getValidfrom().compareTo(validFrom) <= 0;
//        sdzuban 11.12.2013 because of use of PoDayHistorization as base for PoPerson this is no more necessary
//      // if person was created today his validFrom has time part which we need to add to validFrom
//        if (!d && DateTools.dateOnly(person.getValidfrom()).compareTo(validFrom) == 0) {
//        	validFrom = person.getValidfrom();
//        	d = true;
//        }
        boolean e = group.getValidfrom().compareTo(validFrom) <= 0;
        boolean f = person.getValidto().compareTo(validTo) >= 0;
        boolean g = group.getValidto().compareTo(validTo) >= 0;
        boolean all = b && c && d && e && f && g;
        if (!all)
            throw new PoRuntimeException(
                    PoRuntimeException.ERROR_DATES_CANNOT_BE_APPLIED);
		return validFrom;
	}

	private void handleNonHierarchicGroups(PoPerson person, PoGroup group, Date validFrom, Date validTo) {

		if (validFrom == null)
			validFrom = DateTools.today();
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);
// TODO WD-39		
//		HistorizationTimelineUtils.checkValidity(person, group, validFrom, validTo);

		List<? extends Historization> futureGroupLinks = personDAO.findPersonGroupsF(person, group, validFrom);
		
		// here the overlaps of assignments to the group are checked and handled
		List<Historization> overLappingEntries = HistorizationUtils.getOverlappingEntries(futureGroupLinks, validFrom, validTo);

		if (overLappingEntries.size() == 0) {
			PoPersonGroup pglink = new PoPersonGroup();
			pglink.setGroup(group);
			pglink.setPerson(person);
			pglink.setValidfrom(validFrom);
			pglink.setValidto(validTo);
			person.addMemberOfGroup(pglink);
			group.addPersonGroup(pglink);
			personDAO.savePersonGroup(pglink);
		} else {
			Iterator<Historization> overLappingEntriesI = overLappingEntries.iterator();
			while (overLappingEntriesI.hasNext()) {
				PoPersonGroup pg = (PoPersonGroup) overLappingEntriesI.next();
				if (pg.getValidfrom().before(validFrom))
					validFrom = pg.getValidfrom();
				if (pg.getValidto().after(validTo))
					validTo = pg.getValidto();

				if (overLappingEntriesI.hasNext()) {
					if (pg.getValidfrom().after(new Date()))
						personDAO.deletePersonGroupLink(pg);
					else {
						pg.setValidto(new Date());
						personDAO.savePersonGroup(pg);
					}

				} else {
					pg.setValidfrom(validFrom);
					pg.setValidto(validTo);
					personDAO.savePersonGroup(pg);
				}
			}
		}
	}

	private void handleMultipleHierarchyGroups(PoPerson person, List<PoPersonGroup> currentPGroups) {
		boolean mergePossible = true;
		Date earliestDate = new Date();
		Date latestDate = new Date();
		PoGroup tempGroup = null;
		for (PoPersonGroup actGroup : currentPGroups) {
			if (tempGroup == null || actGroup.getGroup().equals(tempGroup)) {
				tempGroup = actGroup.getGroup();
				if (earliestDate.after(actGroup.getValidfrom()))
					earliestDate = actGroup.getValidfrom();
				if (latestDate.before(actGroup.getValidto()))
					latestDate = actGroup.getValidto();
			} else {
				mergePossible = false;
			}
		}

		if (false == mergePossible) {
			throw new PoRuntimeException("More than one relations between person and hierarchical group exist for the same time!");
		}

		String logMessage = "There have been multiple assignments of group " +
				tempGroup.getName() + " to person " + person.getUserName() +
				" which is a result of inconsistent data. Consistency was reestablished!" +
				"Try your assignment again.";
		logger.warn(logMessage);

		// delete all but one assignment
		Iterator<PoPersonGroup> personGroupIterator = currentPGroups.iterator();
		while (personGroupIterator.hasNext()) {
			PoPersonGroup pg = personGroupIterator.next();
			if (personGroupIterator.hasNext()) {
				personDAO.deletePersonGroupLink(pg);
			}
			else {
				pg.setValidfrom(earliestDate);
				pg.setValidto(latestDate);
			}
		}
	}

	private void handleZeroPGroups(PoPerson person, PoGroup group, Date validFrom, Date validTo) {
		if (group.getOrgStructure().isHierarchy())
			// there is a point in time (validFrom) where no hierarchical
			// group is assigned, or the given group has a hierarchical 
			// orgstructure, in this case it was just removed before
			throw new PoRuntimeException(PoRuntimeException.ERROR_NO_HIERARCHICAL_GROUP_EXISTS);

		// TODO WD-39		
//		HistorizationTimelineUtils.checkValidity(person, group, validFrom, validTo);
		
		// create new link and save it
		PoPersonGroup newPg = new PoPersonGroup();
		newPg.setGroup(group);
		newPg.setPerson(person);
		newPg.setValidfrom(validFrom);
		newPg.setValidto(validTo);
		group.addPersonGroup(newPg);
		person.addMemberOfGroup(newPg);
		personDAO.savePersonGroup(newPg);
	}

	@Override
	public PoPerson findPersonByEmployeeId(String employeeId, Date date) {
		return personDAO.findPersonByEmployeeId(employeeId, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public PoPerson findPersonByTaId(String key, Date date) {
		return personDAO.findPersonByTaId(key, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public PoPerson findPersonByTaId(String key, Date from, Date to) {
		return personDAO.findPersonByTaId(key, HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	@Override
	public List<PoGroup> findPersonsLinkedGroups(PoPerson person) {
		return findPersonsLinkedGroups(person, -1);
	}

	@Override
	public List<PoGroup> findPersonsLinkedGroups(PoPerson person, int orgType) {
		return findPersonsLinkedGroups(person, orgType, DateTools.today());
	}

	@Override
	public List<PoGroup> findPersonsLinkedGroups(PoPerson person, Date effectiveDate) {
		return findPersonsLinkedGroups(person, -1, HistorizationHelper.generateUsefulValidFromDay(effectiveDate));
	}

	@Override
	public List<PoGroup> findPersonsLinkedGroups(PoPerson person, int orgType, Date effectiveDate) {
		return personDAO.findPersonsLinkedGroups(person, orgType, HistorizationHelper.generateUsefulValidFromDay(effectiveDate));
	}

	@Override
	public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, int orgType) {
		return personDAO.findPersonGroups(person, HistorizationHelper.generateUsefulValidFromDay(date), orgType);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, int orgType) {
		return personDAO.findPersonGroupsF(person, HistorizationHelper.generateUsefulValidFromDay(date), orgType);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroups(at.workflow.webdesk.po.model.PoPerson, java.util.Date, at.workflow.webdesk.po.model.PoOrgStructure)
	 */
	@Override
	public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, PoOrgStructure orgStructure) {
		return personDAO.findPersonGroups(person, HistorizationHelper.generateUsefulValidFromDay(date), orgStructure);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroupsF(at.workflow.webdesk.po.model.PoPerson, java.util.Date, at.workflow.webdesk.po.model.PoOrgStructure)
	 * @return a list of PoPersonGroup objects that are valid now or in the future, if poOrgStructure is -1, the orgStructure is not taken into account.
	 */
	@Override
	public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, PoOrgStructure poOrgStructure) {
		return personDAO.findPersonGroupsF(person, HistorizationHelper.generateUsefulValidFromDay(date), poOrgStructure);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person, PoOrgStructure poOrgStructure) {
		return personDAO.findPersonGroupsAll(person, poOrgStructure);
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person) {
		return personDAO.findPersonGroupsAll(person);
	}

	@Override
	public void removePersonFromGroup(PoPerson person, PoGroup group) {
		removePersonFromGroup(person, group, DateTools.yesterday());
	}

	@Override
	public void savePersonGroup(PoPersonGroup newPg) {
		personDAO.savePersonGroup(newPg);
	}

	@Override
	public void removePersonFromGroup(PoPerson person, PoGroup group, Date effectiveDate) {
		if (group.getOrgStructure().isHierarchy())
			throw new PoRuntimeException(PoRuntimeException.ERROR_TRY_TO_DELETE_HIERARCHICAL_GROUP);

		PoPersonGroup personGroup = personDAO.findPersonGroupObject(person, group, effectiveDate);
		if (personGroup != null) { // set validto
			personGroup.setValidto(effectiveDate);
			if (personGroup.getValidity().isPositive())
				personDAO.savePersonGroup(personGroup);
			else {
				personGroup.getPerson().getMemberOfGroups().remove(personGroup);
				personGroup.getGroup().getPersonGroups().remove(personGroup);
				personDAO.deletePersonGroupLink(personGroup);
			}
		}
	}

	@Override
	public void deleteAndFlushPerson(PoPerson person) {
		// Remove from client
		person.getClient().getPersons().remove(person);

		// delete all persongrouplinks
		for (PoPersonGroup personGroup : person.getMemberOfGroups()) {
			// Link von Group Objekt löschen
			personGroup.getGroup().getPersonGroups().remove(personGroup);
			// physisch DS löschen
			personDAO.deletePersonGroupLink(personGroup);
		}

		// Lösche Person
		personDAO.delete(person);
	}

	@Override
	public void deletePerson(PoPerson person) {
		
		Date yesterdayLastMoment = DateTools.lastMomentOfDay(DateTools.yesterday());

		if (false == HistorizationHelper.isValid(person, yesterdayLastMoment))
			// in this case we do not need to do anything as the person object is not valid anyway
			return;

		// Lasse auch alle Links zu Gruppen ablaufen
		Iterator<PoPersonGroup> groupMemberIterator = person.getMemberOfGroups().iterator();
		while (groupMemberIterator.hasNext()) {
			// lasse Link nun sofort ablaufen
			PoPersonGroup pg = groupMemberIterator.next();
			if (HistorizationHelper.isValid(pg, yesterdayLastMoment)) {
				pg.setValidto(yesterdayLastMoment);
				personDAO.savePersonGroup(pg);
			}
		}

		Iterator<PoRoleHolderLink> roleHolderIterator = roleHolderDAO.findRoleHolderWithCompetence4Person(person, yesterdayLastMoment).iterator();
		while (roleHolderIterator.hasNext()) {
			PoRoleHolderLink o = roleHolderIterator.next();

			if (o instanceof PoRoleHolderPerson) {
				PoRoleHolderPerson roleHolderPerson = (PoRoleHolderPerson) o;

				if (roleHolderPerson.getRoleCompetenceBase() instanceof PoRoleCompetencePerson) {
					PoRoleCompetencePerson rcp = (PoRoleCompetencePerson) roleHolderPerson.getRoleCompetenceBase();
					if (rcp.getCompetence4Person().getUID().equals(person.getUID()) && HistorizationHelper.isValid(rcp, yesterdayLastMoment)) {
						rcp.setValidto(yesterdayLastMoment);
						roleCompetenceDAO.save(rcp);
					}
				}
			}
		}

		Iterator<PoRoleHolderPerson> roleHolderPersonIterator = person.getReferencedAsRoleHolder().iterator();
		while (roleHolderPersonIterator.hasNext()) {
			PoRoleHolderPerson rhp = roleHolderPersonIterator.next();
			rhp.setValidto(new Date());

			if (rhp.getRoleCompetenceBase().getRole().getRoleType() == PoRole.DUMMY_ROLE) {
				PoRole role = rhp.getRoleCompetenceBase().getRole();
				if (HistorizationHelper.isValid(role, yesterdayLastMoment)) {
					role.setValidto(yesterdayLastMoment);
					roleDAO.save(role);

					Iterator<PoAPermissionRole> rolePermissions = role.getPermissions().iterator();
					while (rolePermissions.hasNext()) {
						PoAPermissionRole apr = rolePermissions.next();
						if (HistorizationHelper.isValid(apr, yesterdayLastMoment)) {
							apr.setValidto(yesterdayLastMoment);
							actionDAO.saveAPermission(apr);
						}
					}
				}
			}
			roleHolderPersonDAO.save(rhp);
		}

		Iterator<PoRoleCompetencePerson> roleCompetencePersonIterator = person.getReferencedAsCompetenceTarget().iterator();
		while (roleCompetencePersonIterator.hasNext()) {
			PoRoleCompetenceBase rhcp = roleCompetencePersonIterator.next();
			if (HistorizationHelper.isValid(rhcp, yesterdayLastMoment)) {
				rhcp.setValidto(yesterdayLastMoment);
				roleCompetenceDAO.save(rhcp);
			}
		}

		Iterator<PoAPermissionPerson> permissionPersonIterator = person.getPermissions().iterator();
		while (permissionPersonIterator.hasNext()) {
			PoAPermissionBase apb = permissionPersonIterator.next();
			if (HistorizationHelper.isValid(apb, yesterdayLastMoment)) {
				apb.setValidto(yesterdayLastMoment);
				actionDAO.saveAPermission(apb);
			}
		}

		// lasse Person ablaufen = nicht mehr gültig
		person.setValidto(yesterdayLastMoment);
		if (person.getValidity().isPositive())
			personDAO.update(person);
		else
			personDAO.delete(person);

		historicizeBankAccounts(person);
	}

	@Override
	public void deletePersonGroupLink(PoPersonGroup pgLink) {
		throw new RuntimeException("This method is deprecated!");
	}

	// TODO This function seems not to be used.
	@Override
	public void deleteAndFlushPersonGroupLink(PoPersonGroup pgLink) {
		pgLink.getPerson().getMemberOfGroups().remove(pgLink);
		// sdzuban 16-07-2013 wdhrexpert-187 reload necessary to refresh the parentGroups
		getGroup(pgLink.getGroup().getUID()).getParentGroups().remove(pgLink);
		personDAO.deletePersonGroupLink(pgLink);
	}

	// TODO if the link points to a group with hierarchical orgStructure, check
	// if the person is always linked to a hierarchical group, if not, rearrange groups!

	@Override
	public void changeValidityPersonGroupLink(PoPersonGroup personGroup, Date validFrom, Date validTo) {
		if (validFrom == null)
			validFrom = DateTools.today();
		if (validTo == null)
			validTo = new Date(DateTools.INFINITY_TIMEMILLIS);

		if (personGroup.getValidto().before(personGroup.getValidfrom()))
			throw new PoRuntimeException(
					"Link validTo is before Link validFrom");

		if (personGroup.getGroup().getOrgStructure().isHierarchy() || personGroup.getGroup().getOrgStructure().isAllowOnlySingleGroupMembership()) {
			linkPerson2Group(personGroup.getPerson(), personGroup
					.getGroup(), validFrom, validTo);
		} else {
			personGroup.setValidfrom(validFrom);
			personGroup.setValidto(validTo);
		}
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getPersonGroups(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoOrgStructure, java.util.Date)
	 */
	@Override
	public List<PoPersonGroup> getPersonGroups(PoPerson person, PoOrgStructure orgStructure, Date validAt) {
		return personDAO.getPersonGroups(person, orgStructure, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(validAt));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getPersonGroupLink(at.workflow.webdesk.po.model.PoPerson, java.lang.String, java.util.Date)
	 */
	@Override
	public PoPersonGroup getPersonGroupLink(PoPerson person, PoGroup group, Date validAt) {
		return personDAO.getPersonGroupLink(person, group, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(validAt));
	}

	@Override
	public List<PoGroup> findGroupsOfOrgStructureF(PoOrgStructure orgStructure, Date date) {
		return groupDAO.findGroupsOfOrgStructureF(orgStructure, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoGroup> findGroupsOfOrgStructure(PoOrgStructure orgStructure, Date date) {
		return groupDAO.findGroupsOfOrgStructure(orgStructure, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getOrgStructure(at.workflow.webdesk.po.model.PoPerson)
	 */
	@Override
	public PoOrgStructure getOrgStructure(PoPerson person) {
		return personDAO.getOrgStructure(person);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findChildGroupsF(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public List<PoParentGroup> findChildGroupsF(PoGroup group, Date effectiveDate) {
		return groupDAO.findChildGroupsF(group, HistorizationHelper.generateUsefulValidFromDay(effectiveDate));
	}

	@Override
	public List<PoParentGroup> findChildGroupsAll(PoGroup group) {
		return groupDAO.findChildGroupsAll(group);
	}

	@Override
	public PoGroup getPersonsHierarchicalGroup(PoPerson person, Date date) {
		return personDAO.getPersonsHierarchicalGroup(person, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(date));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsOfGroup(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date validAt) {

		return groupDAO.findPersonsOfGroup(group, HistorizationHelper.generateUsefulValidFromDay(validAt));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsOfGroup(at.workflow.webdesk.po.model.PoGroup, java.util.Date, java.util.Date)
	 */
	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date from, Date to) {
		return groupDAO.findPersonsOfGroup(group, HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsOfGroupF(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
	 */
	@Override
	public List<PoPerson> findPersonsOfGroupF(PoGroup group, Date validAt) {
		return groupDAO.findPersonsOfGroupF(group, HistorizationHelper.generateUsefulValidFromDay(validAt));
	}

	@Override
	public List<PoPerson> findPersonsOfGroup(PoGroup group, List<String> names, Date validAt) {
		return groupDAO.findPersonsOfGroup(group, names, HistorizationHelper.generateUsefulValidFromDay(validAt));
	}

	@Override
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date date) {
		return personDAO.findPersonsWithFilter(uids, names, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date from, Date to) {
		return personDAO.findPersonsWithFilter(uids, names, HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	@Override
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date date) {
		return personDAO.findPersonsWithViewPermission(viewPermissions, names, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date from, Date to) {
		return personDAO.findPersonsWithViewPermission(viewPermissions, names, 
				HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	/** 
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonInGroup(at.workflow.webdesk.po.model.PoGroup, java.util.List, java.util.Date)
	 * @deprecated replaced by findPersonsOfGroup()
	 */
	@Deprecated
	public List<PoPerson> findPersonInGroup(PoGroup group, List<String> names, Date date) {
		return findPersonsOfGroup(group, names, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getPersonsHierarchicalGroup(at.workflow.webdesk.po.model.PoPerson)
	 */
	@Override
	public PoGroup getPersonsHierarchicalGroup(PoPerson person) {
		return getPersonsHierarchicalGroup(person, new Date());
	}

	@Override
	public PoGroup getPersonsCostCenterGroup(PoPerson person) {
		return getPersonsCostCenterGroup(person, new Date());
	}

	private PoGroup getPersonsCostCenterGroup(PoPerson person, Date referenceDate) {
		List<PoGroup> linkedGroups = findPersonsLinkedGroups(person, PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS, HistorizationHelper.getEffectiveDateOrNowWithHourPrecision(referenceDate));
		if (linkedGroups.size() > 1 && allowOnlySingleCostCenterAssignment)
			throw new PoRuntimeException("Only one Costcenter allowed at a time...");

		return (linkedGroups.size() <= 0) ? null : linkedGroups.get(0);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonByUserName(java.lang.String)
	 */
	@Override
	public PoPerson findPersonByUserName(String key) {
		if (key.equals(sysAdminUserInfo.getSysAdminUser())) {
			return getSysAdminPerson();
		}
		return personDAO.findPersonByUserName(key);
	}

	@Override
	public PoPerson findPersonByWorkflowId(String key) {
		if (key.equals(sysAdminUserInfo.getSysAdminUser())) {
			return getSysAdminPerson();
		}
		return personDAO.findPersonByWorkflowId(key);
	}

	private PoPerson getSysAdminPerson() {
		// return sysadmin for workflow list
		PoPerson myPerson = new PoPerson();
		myPerson.setLastName("System");
		myPerson.setFirstName("Administrator");
		myPerson.setUserName(sysAdminUserInfo.getSysAdminUser());
		myPerson.setUID(sysAdminUserInfo.getSysAdminUser());
		return myPerson;
	}

	private int getMaxDepthOfGroupRecursive(PoGroup group, int actDepth, int max) {
		return getMaxDepthOfGroupRecursive(group.getUID(), actDepth, max);
	}

	private int getMaxDepthOfGroupRecursive(String groupUid, int actDepth, int max) {
		List<String> childGroupUids = findChildGroupUids(groupUid, null);
		if (childGroupUids != null) {
			if (actDepth > max)
				max = actDepth;

			Iterator<String> childGroupsIterator = childGroupUids.iterator();
			while (childGroupsIterator.hasNext()) {
				String uid = childGroupsIterator.next();
				max = getMaxDepthOfGroupRecursive(uid, actDepth + 1, max);
			}
		}
		return max;

	}

	/**
	 * @see at.workflow.webdesk.po.PoOrgStructureDAO#getMaxDepthOfOrgStructure(at.workflow.webdesk.po.model.PoOrgStructure)
	 */
	@Override
	public int getMaxDepthOfOrgStructure(PoOrgStructure orgStructure) {
		if (logger.isDebugEnabled())
			logger.debug("getMaxDepthOfGroupRecursive for orgStructure = " + orgStructure.getName());

		Iterator<PoGroup> groupIterator = getTopLevelGroupsOfOrgStructure(orgStructure, true).iterator();
		int max = 0;
		while (groupIterator.hasNext()) {
			PoGroup tl = groupIterator.next();
			int depth = getMaxDepthOfGroupRecursive(tl, 1, 1);
			if (depth > max)
				max = depth;
		}
		return max;
	}

	@Override
	public PoGroup findGroupByShortName(String name, PoClient client) {
		return findGroupByShortName(name, client, DateTools.today());
	}

	@Override
	public PoGroup findGroupByShortName(String name, Date referenceDate) {
		return findGroupByShortName(name, null, HistorizationHelper.generateUsefulValidFromDay(referenceDate));
	}

	@Override
	public PoGroup findGroupByShortName(String name, PoClient client, Date referenceDate) {
		return groupDAO.findGroupByShortName(name, client, HistorizationHelper.generateUsefulValidFromDay(referenceDate));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getPersonAsMap(at.workflow.webdesk.po.model.PoPerson)
	 */
	@Override
	public Map<String, Object> getPersonAsMap(PoPerson person) {
		Map<String, Object> hm = new HashMap<String, Object>();
		hm.put("UID", person.getUID());
		hm.put("client", person.getClient());
		hm.put("dateOfBirth", person.getDateOfBirth());
		hm.put("email", person.getEmail());
		hm.put("empoyeeId", person.getEmployeeId());
		hm.put("firstName", person.getFirstName());
		hm.put("lastName", person.getLastName());
		hm.put("officeCity", person.getOfficeCity());
		hm.put("officeCountry", person.getOfficeCountry());
		hm.put("officeFaxPhoneNumber", person.getOfficeFaxPhoneNumber());
		hm.put("officePhoneNumber", person.getOfficePhoneNumber());
		hm.put("officeStreetAddress", person.getOfficeStreetAddress());
		hm.put("officeZip", person.getOfficeZip());
		hm.put("referencedAsCompetenceTarget", person.getReferencedAsCompetenceTarget());
		hm.put("referencedAsRoleHolder", person.getReferencedAsRoleHolder());
		hm.put("taID", person.getTaID());
		hm.put("userName", person.getUserName());
		hm.put("fullName", person.getFullName());
		return hm;
	}

	@Override
	public List<PoGroup> findCurrentGroups() {
		return groupDAO.findCurrentGroups();
	}

	@Override
	public List<PoPerson> findPersonsOfClient(PoClient client, Date validAt) {
		return personDAO.findPersonsOfClient(client, HistorizationHelper.generateUsefulValidFromDay(validAt));
	}

	@Override
	public List<PoPerson> findPersonsOfClient(PoClient client, Date from, Date to) {
		return personDAO.findPersonsOfClient(client, HistorizationHelper.generateUsefulValidFromDay(from), 
				HistorizationHelper.generateUsefulValidToDay(to));
	}

	@Override
	public List<PoGroup> findGroupsWithoutParent(PoOrgStructure orgStructure, Date date) {
		return groupDAO.findGroupsWithoutParent(orgStructure, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public PoPerson findPersonByTaId(String key) {
		return findPersonByTaId(key, DateTools.today());
	}

	@Override
	public PoGroup findGroupByShortName(String name) {
		return findGroupByShortName(name, DateTools.today());
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsF(PoGroup group, Date date) {
		return groupDAO.findPersonGroupsF(group, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoGroup group) {
		return groupDAO.findPersonGroupsAll(group);
	}

	@Override
	public void checkUser(PoPerson person) {
		personDAO.checkUser(person);
	}

	@Override
	public void checkGroup(PoGroup group) {
		groupDAO.checkGroup(group);
	}

	@Override
	public Boolean checkNeededForSavePerson(PoPerson person, PoGroup group) {
		// TODO licence check is needed for a new person!
		return Boolean.TRUE;
	}

	@Override
	public Boolean checkNeededForUpdatePerson(PoPerson person) {
		if (person.getUID() != null && false == person.getUID().equals("")) {
			String sql = "select p.activeUser, p.CLIENT_UID from PoPerson p where p.PERSON_UID='" + person.getUID() + "'";

			PaginableQuery query = new PaginableQuery(sql);
			PoGeneralSqlService generalSqlService = (PoGeneralSqlService) WebdeskApplicationContext.getBean("PoGeneralSqlService");
			PoDataSourceService dsService = (PoDataSourceService) WebdeskApplicationContext.getBean("PoDataSourceService");
			List<Map<String, ?>> resultSet = generalSqlService.select(dsService.getDataSource(PoDataSourceService.WEBDESK), query);

			String oldPersonsClientUid = (String) resultSet.get(0).get("CLIENT_UID");
			if (false == oldPersonsClientUid.equals(person.getClient().getUID()))
				return Boolean.TRUE;

			Collection<PoPersonGroup> currentLinks = person.getMemberOfGroups();
			Collection<PoPersonGroup> personGroupLinksFromDb = findPersonGroupsAll(person);
			if (false == areCollectionsEqual(currentLinks, personGroupLinksFromDb))
				return Boolean.TRUE;

			Object activeUser = resultSet.get(0).get("activeUser");	// fri_2013-12-23: fixing failed unit tests "java.lang.Boolean cannot be cast to java.lang.Number"
			boolean oldActiveUser = (activeUser instanceof Number) ? ((Number) activeUser).intValue() == 1 : ((Boolean) activeUser).booleanValue();
			if (oldActiveUser != person.isActiveUser())
				return Boolean.TRUE;

			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private boolean areCollectionsEqual(Collection<PoPersonGroup> col1, Collection<PoPersonGroup> col2) {

		if (col1.size() != col2.size())
			return false;

		for (PoPersonGroup pg : col1) {
			if (col2.contains(pg) == false)
				return false;
		}

		return true;
	}

	@Override
	public void deleteAndFlushClient(PoClient client) {
		Iterator<PoOrgStructure> orgStructureIterator = client.getOrgStructures().iterator();
		while (orgStructureIterator.hasNext()) {
			PoOrgStructure os = orgStructureIterator.next();
			deleteAndFlushOrgStructure(os);
		}

		Iterator<? extends PoAPermissionBase> permissionsIterator = client.getPermissions().iterator();
		while (permissionsIterator.hasNext()) {
			PoAPermissionBase ap = permissionsIterator.next();
			actionDAO.deleteAPermission(ap);
		}

		Iterator<PoPerson> personIterator = client.getPersons().iterator();
		while (personIterator.hasNext()) {
			deleteAndFlushPerson(personIterator.next());
		}

		Iterator<PoRole> rolesIterator = client.getRoles().iterator();
		while (rolesIterator.hasNext()) {
			roleDAO.delete(rolesIterator.next());
		}

		clientDAO.delete(client);
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#getResolvedHeaderText()
	 */
	@Override
	public String getResolvedHeaderText(PoPerson person) {

		if (person.getUserName().equals(sysAdminUserInfo.getSysAdminUser())) {
			return "";
		}

		String ht = headerText;
		String res = "";
		while (ht != null && ht.indexOf("$") > -1) {
			res = ht.substring(0, ht.indexOf("$"));
			ht = ht.substring(ht.indexOf("$") + 1);
			if (ht.indexOf("$") > -1) {
				String function = ht.substring(0, ht.indexOf("$"));
				ht = ht.substring(ht.indexOf("$") + 1);
				Expression e = new Expression(person, function, null);
				try {
					e.execute();
					res += e.getValue();
				}
				catch (Exception e1) {
					res += "*not known*" + function + "*not known*";
				}
			} else {
				// An error occured - > every $ needs a closing $ -> break!
				break;
			}
		}
		res += ht;
		return res;
	}

	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	@Override
	public boolean isPersonActualMemberOfGroup(PoPerson person, String group) {
		PoGroup poGroup = findGroupByShortName(group, person.getClient());
		if (poGroup == null)
			return false;
		return isPersonActualMemberOfGroup(person, poGroup);
	}

	@Override
	public List<String> findUidsOfClients() {
		List<String> uids = new ArrayList<String>();
		Iterator<PoClient> clientsI = loadAllClients().iterator();
		while (clientsI.hasNext()) {
			uids.add(clientsI.next().getUID());
		}
		return uids;
	}

	@Override
	public List<String> findPersonIdsOfClients(List<String> clientUids, Date date) {
		Date referenceDate = HistorizationHelper.generateUsefulValidFromDay(date);
		Iterator<String> clientUidIterator = clientUids.iterator();
		List<String> personUids = new ArrayList<String>();
		while (clientUidIterator.hasNext()) {
			personUids.addAll(personDAO.findPersonUidsOfClient(clientUidIterator.next(), referenceDate));
		}
		return personUids;
	}

	@Override
	public List<String> findPersonUidsOfGroups(List<String> groupUids, Date date) {
		Date usefulDate = HistorizationHelper.generateUsefulValidFromDay(date);
		Iterator<String> groupUidIterator = groupUids.iterator();
		List<String> personUids = new ArrayList<String>();
		while (groupUidIterator.hasNext()) {
			personUids.addAll(groupDAO.findPersonUidsOfGroup(groupUidIterator.next(), usefulDate));
		}
		return personUids;
	}

	@SuppressWarnings("cast")
	@Override
	public PoOrgStructure findOrgStructureByNameAndClient(PoClient client, String name) {
		return (PoOrgStructure) DataAccessUtils.uniqueResult(orgStructureDAO.findOrgStructureByName(client, name));
	}

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findGroupWithOrgStructureAndPerson(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoOrgStructure, java.util.Date)
	 */
	@Override
	public List<PoGroup> findGroupWithOrgStructureAndPerson(PoPerson person, PoOrgStructure os, Date date) {
		return groupDAO.findGroupWithOrgStructureAndPerson(person, os, HistorizationHelper.generateUsefulValidFromDay(date));
	}

	@Override
	public List<PoPerson> linkPersons2Group(List<PoPerson> persons, PoGroup group) {
		List<PoPerson> groupPersons = findPersonsOfGroup(group, new Date());
		Iterator<PoPerson> itr = persons.iterator();
		while (itr.hasNext()) {
			PoPerson person = itr.next();
			if (false == groupPersons.contains(person)) {
				try {
					linkPerson2Group(person, group);
					logger.info("Added Person " + person.getFullName() + " to group " + group.getShortName() + ".");
					groupPersons.add(person);
				}
				catch (Exception e) {
					logger.error("Problems adding Person " + person.getFullName() + " to group " + group.getShortName());
				}
			}
		}

		// delete old entries that are not included in the actual person list
		Iterator<PoPerson> groupPersonIterator = groupPersons.iterator();
		while (groupPersonIterator.hasNext()) {
			PoPerson groupPerson = groupPersonIterator.next();
			if (false == persons.contains(groupPerson))
				removePersonFromGroup(groupPerson, group);
		}

		return groupPersons;
	}

	public String getHeaderText() {
		return headerText;
	}

	@Override
	public PoPerson findPersonByEmployeeId(String employeeId) {
		return findPersonByEmployeeId(employeeId, null);
	}

	@Override
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names) {
		return findPersonsWithFilter(uids, names, null);
	}

	@Override
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names) {
		return findPersonsWithViewPermission(viewPermissions, names, DateTools.today());
	}

	@Override
	public PoGroup isGroupChildGroup(PoGroup possibleChildGroup, PoGroup possibleParentGroup) {
		List<PoGroup> allChildGroups = findAllChildGroupsFlat(possibleParentGroup);
		if (allChildGroups.contains(possibleChildGroup))
			return possibleParentGroup;

		return null;
	}

	@Override
	public PoGroup getDepartment(PoGroup group, Date referenceDate) {
		
		PoOrgStructure orgStructure = group.getOrgStructure();
		List<PoGroup> roots = getTopLevelGroupsOfOrgStructureWithDate(orgStructure, referenceDate, true);
		int groupsDepth = getDepthOfGroup(group, referenceDate);
		
		if (groupsDepth == 0) // root
			return group;
		if (groupsDepth == 1) { // 2nd level
			if (roots.size() == 1)
				return group;
			else
				return getParentGroupByIndex(group, 1, referenceDate);
		} else {
			if (roots.size() == 1)
				return getParentGroupByIndex(group, groupsDepth - 1, referenceDate);
			else
				return getParentGroupByIndex(group, groupsDepth, referenceDate);
		}
	}
	
	@Override
	public User lookupUser(String userName) {
		PoPerson person = findPersonByUserName(userName);
		if (person != null && person.isActiveUser())
			return person;

		return null;
	}

	@Override
	public void deleteAndFlushParentGroup(PoParentGroup delPg) {
		groupDAO.deleteParentGroup(delPg);
	}

	@Override
	public List<PoParentGroup> findParentGroups(PoGroup parent, PoGroup child) {
		return groupDAO.findParentGroups(parent, child);
	}

	@Override
	public List<PoPerson> resolveGroupToPersons(String shortName, boolean includeChildGroups, Date date) {
		List<PoPerson> ret = new ArrayList<PoPerson>();
		PoGroup actGroup = findGroupByShortName(shortName, date);

		if (actGroup != null) {
			List<PoGroup> actGrpList = new ArrayList<PoGroup>();
			actGrpList.add(actGroup);

			if (includeChildGroups)
				actGrpList.addAll(findAllChildGroupsFlat(actGroup, date));

			Iterator<PoGroup> itr = actGrpList.iterator();
			while (itr.hasNext()) {
				PoGroup grp = itr.next();
				ret.addAll(findPersonsOfGroup(grp, date));
			}
		}
		return ret;
	}

	public void setAllowOnlySingleCostCenterAssignment(boolean allowOnlySingleCostCenterAssignment) {
		this.allowOnlySingleCostCenterAssignment = allowOnlySingleCostCenterAssignment;
	}

	@Override
	public boolean isPasswordCorrect(User user, String password) {
		PoPerson person = findPersonByUserName(user.getUserName());
		return passwordService.isPasswordCorrect(person, password);
	}

	@Override
	public List<String> findGroupIdsOfClients(List<String> clientUidList, Date date) {
		Date queryDate = HistorizationHelper.generateUsefulValidFromDay(date);
		List<String> uids = new ArrayList<String>();
		Iterator<String> i = clientUidList.iterator();
		while (i.hasNext()) {
			uids.addAll(groupDAO.findGroupUidsOfClient(i.next(), queryDate));
		}
		return uids;
	}

	/** Spring accessor */
	public void setGroupHierarchyCache(Cache groupHierarchyCache) {
		this.groupHierarchyCache = groupHierarchyCache;
	}

	@Override
	public User lookupUserByMail(String mail) {
		final PoPerson person = findPersonByEmail(mail);
		if (person != null && person.isActiveUser())
			return person;

		return null;

	}

	/** Spring accessor */
	public void setSysAdminUserInfo(SysAdminUserInfo sysAdminUserInfo) {
		this.sysAdminUserInfo = sysAdminUserInfo;
	}

	/** {@inheritDoc} */
	@Override
	public PoPersonImages getPersonImages(String uid) {
		return personImagesDAO.get(uid);
	}

	@Override
	public List<PoPersonGroup> filterPersonGroups(Collection<PoPersonGroup> personGroups, PoOrgStructure structure) {
		assert structure != null;

		List<PoPersonGroup> subList = new ArrayList<PoPersonGroup>();
		for (PoPersonGroup pg : personGroups)
			if (pg.getGroup().getOrgStructure().equals(structure))
				subList.add(pg);

		return subList;
	}

	/** Spring XML noise, do not use or call! */
	public void setPersonGroupDAO(PoPersonGroupDAOImpl personGroupDAO) {
		this.personGroupDAO = personGroupDAO;
	}
}