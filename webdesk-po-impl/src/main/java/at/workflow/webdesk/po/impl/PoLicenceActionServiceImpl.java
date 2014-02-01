package at.workflow.webdesk.po.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springmodules.cache.util.Reflections;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLicenceActionService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.model.PoAPermissionAdapter;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAPermissionVisitor;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * 
 *  TODO: add some more caching in order to make all database queries as fast as possible
 *  maybee we should cache all persons of all groups?
 *  
 *	@author hentner, ggruber
 */
public class PoLicenceActionServiceImpl implements PoLicenceActionService, ApplicationContextAware {

	private PoActionDAO actionDAO;
	private PoGeneralDAO generalDAO;
	private ApplicationContext appCtx;
	
	private Cache objectCategoryMembershipCache;
	private Cache actionCache;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	public PoActionDAO getActionDAO() {
		return actionDAO;
	}

	public void setActionDAO(PoActionDAO actionDAO) {
		this.actionDAO = actionDAO;
	}

	@Override
	public PoAction findActionByName(String name) {
		// use some caching, as this method is often called
		// when doing a full licence check!
		String cacheKey = name;
		if (actionCache.isKeyInCache(cacheKey) && actionCache.get(cacheKey)!=null ) {
			PoAction action = (PoAction) actionCache.get(cacheKey).getValue();
			
			if (action!=null)
				return actionDAO.get(action.getUID());
		}
		PoAction action = actionDAO.findActionByNameAndType(name, PoConstants.ACTION_TYPE_ACTION);
		if (action!=null)
			actionCache.put( new Element(cacheKey, action));
		
		return action;
	}

	@Override
	public List<String> findActionNamesOfModule(String name) {
		return this.actionDAO.findActionNamesOfModule(name);
	}

	@Override
	public List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date) {
		return this.actionDAO.findAllPermissionsForActionF(action,date);
	}
	
	/**
	 * central method for making a fullcheck for an action. it expands a particular permission
	 * to the UIDs of the assigned persons. We have to expand group, role and client permissions
	 * here. In order to make it real fast, we are caching those relations in an external cache, which
	 * lasts for 60 minutes.
	 */
	@Override
	public Set<String> findAssignedPersonsOfAction(PoAPermissionBase apb) {
		
		Set<String> uidSet = new HashSet<String>();
        PoAPermissionVisitor apV = new PoAPermissionAdapter();
        apb.accept(apV);
        
        this.logger.trace("    findAssignedPersonsOfAction -> type=" + apV.getWhichClassVisited());
        
        if (apV.getWhichClassVisited().equals("AP")) {
        	// Permission for Person
            PoAPermissionPerson app = (PoAPermissionPerson) apV.getVisitedObject();
            addIfActiveUser(uidSet, app.getPerson());
        } else if (apV.getWhichClassVisited().equals("AR")) {
        	// Permission for Role (REAL or dummy)
        	PoAPermissionRole apr = (PoAPermissionRole) apV.getVisitedObject();
        	addPersonsOfRoleToSet(uidSet, apr.getRole());
        	
        } else if (apV.getWhichClassVisited().equals("AG")) {
        	// Permission for group
            PoAPermissionGroup apg  = (PoAPermissionGroup) apV.getVisitedObject();
            addPersonsOfGroupToSet(uidSet,apg.getGroup());
            // this is the performance killer!
            if (apg.isInheritToChilds()) {
                List<PoGroup> childs = getOrganisationService().findAllChildGroupsFlat(apg.getGroup());
                for (PoGroup group : childs) {
                	addPersonsOfGroupToSet(uidSet, group);
                }
            }
        } else if (apV.getWhichClassVisited().equals("AC")) {
        	// Permission For client
            PoAPermissionClient apc = (PoAPermissionClient) apV.getVisitedObject();
            addPersonsOfClientToSet(uidSet, apc.getClient());
        }
        return uidSet;
	}
	
	private void addPersonsOfClientToSet(Set<String> uidSet, PoClient client) {
		
		ObjectCategoryMembership personClientMembership = buildPersonClientMembership();
    	addObjectsOfCategoryToSet(uidSet, personClientMembership, client.getUID());
		
	}

	private void addPersonsOfRoleToSet(Set<String> uidSet, PoRole role) {
		
		ObjectCategoryMembership personRoleMembership = buildPersonRoleMembership();
    	addObjectsOfCategoryToSet(uidSet, personRoleMembership, role.getUID());
		
	}
	
	private void addPersonsOfGroupToSet(Set<String> l, PoGroup g) {
		
		ObjectCategoryMembership personGroupMembership = buildPersonGroupMembership();
    	addObjectsOfCategoryToSet(l, personGroupMembership, g.getUID());
    }

	private void addObjectsOfCategoryToSet(Set<String> l, ObjectCategoryMembership objCatMembership, String categoryUid) {
		@SuppressWarnings("unchecked")
		List<CategoryObjectRelation> relations = (List<CategoryObjectRelation>) objCatMembership.get(categoryUid);
    	if (relations != null) {
	    	for(CategoryObjectRelation relation : relations) {
	    		l.add(relation.objectUid);
	    	}
    	}
	}

	private void addIfActiveUser(Set<String> uidSet, PoPerson person) {
		if (person.isActiveUser()) {
			uidSet.add(person.getUID());
		}
	}
	
    @SuppressWarnings("serial")
	private static class RelationBase implements Serializable 
    {
    	String relationUid;
    }
    
    /**
     * Helper class that holds the categoryUid with the relation UID,
     * as sometimes the relation records of the hierarchy are needed.
     */
    @SuppressWarnings("serial")
	private static class ObjectCategoryRelation extends RelationBase implements Serializable
    {
    	final String categoryUid;
    	
		public ObjectCategoryRelation(String relationUid, String categoryUid) {
			this.relationUid = relationUid;
			this.categoryUid = categoryUid;
		}
    }
    
    /**
     * Helper class that holds the objectUid with the relation UID,
     * as sometimes the relation records of the hierarchy are needed.
     */
    @SuppressWarnings("serial")
	private static class CategoryObjectRelation extends RelationBase implements Serializable
    {
    	final String objectUid;
    	
    	public CategoryObjectRelation(String relationUid, String objectUid) {
    		this.relationUid = relationUid;
    		this.objectUid = objectUid;
    	}
    }
    
    /**
     * Helper with key = Object UID and value List of category UIDs where the object is member
     * that represents f.i. a person/group membership hierarchy (in effect a M:N relationship)
     */
    @SuppressWarnings("serial")
	private static class ObjectCategoryMembership extends Hashtable<String,List<? extends RelationBase>>
    {
    	public void putRelation(String relationUid, String objectUid, String categoryUid)	{
    		@SuppressWarnings("unchecked")
			List<ObjectCategoryRelation> categories = (List<ObjectCategoryRelation>) get(objectUid);
    		if (categories == null)	{
    			categories = new ArrayList<ObjectCategoryRelation>();
    			put(objectUid, categories);
    		}
    		categories.add(new ObjectCategoryRelation(relationUid, categoryUid));
    		
    		@SuppressWarnings("unchecked")
			List<CategoryObjectRelation> objects = (List<CategoryObjectRelation>) get(categoryUid);
    		if (objects == null)	{
    			objects = new ArrayList<CategoryObjectRelation>();
    			put(categoryUid, objects);
    		}
    		objects.add(new CategoryObjectRelation(relationUid, objectUid));
    	}
    }
    
    private ObjectCategoryMembership buildPersonGroupMembership() {
    	Date queryDate = HistorizationHelper.getNowWithHourPrecision();
    	String cacheKey = "PGMS_" + Reflections.reflectionHashCode(queryDate);
    	ObjectCategoryMembership personMembership = null;
    	
    	// FIXME: this caching approach might not work, if we intercept service calls
    	// where person group membership changes... As there is not flushing algorythm...
    	if (objectCategoryMembershipCache.isKeyInCache(cacheKey) && objectCategoryMembershipCache.get(cacheKey).getValue() instanceof ObjectCategoryMembership) {
    		personMembership = (ObjectCategoryMembership) objectCategoryMembershipCache.get(cacheKey).getValue();
    		return personMembership;
    	}
    	
    	if (logger.isDebugEnabled())
    		logger.debug("Call buildPersonGroupMembership with date: "+queryDate);
    	
		@SuppressWarnings({ "cast", "unchecked" })
		List<Object[]> personGroupMembershipRelations = (List<Object[]>) generalDAO.getElementsAsList(
				"select pg.UID, pg.person.UID, pg.group.UID " +
				" from PoPersonGroup pg where pg.person.activeUser is true " +
				" and pg.validfrom <= ? and pg.validto > ? " +
				" and pg.person.validfrom <= ? and pg.person.validto > ? " +
				" and pg.group.validfrom <=? and pg.group.validto > ? " +
				" order by pg.person.UID, pg.group.shortName asc",
				new Object[] { queryDate, queryDate, queryDate, queryDate, queryDate, queryDate});
		
		personMembership = new ObjectCategoryMembership();
		for (Object[] relation : personGroupMembershipRelations)	{
			personMembership.putRelation((String)relation[0], (String)relation[1], (String)relation[2]);
		}
		
		objectCategoryMembershipCache.put(new Element(cacheKey, personMembership));
		
		return personMembership;
    }
    
    private ObjectCategoryMembership buildPersonRoleMembership() {
    	
    	Date queryDate = HistorizationHelper.getNowWithHourPrecision();
    	String cacheKey = "PRMS_" + Reflections.reflectionHashCode(queryDate);
    	ObjectCategoryMembership personRoleMembership = null;
    	
    	if (objectCategoryMembershipCache.isKeyInCache(cacheKey) && objectCategoryMembershipCache.get(cacheKey).getValue() instanceof ObjectCategoryMembership) {
    		personRoleMembership = (ObjectCategoryMembership) objectCategoryMembershipCache.get(cacheKey).getValue();
    		return personRoleMembership;
    	}
    	
    	if (logger.isDebugEnabled())
    		logger.debug("Call buildRoleRoleholderMembership with date: "+queryDate);
    	
    	// ggruber 2012-10-04 query for UIDs instead of objects to avoid hibernate exceptions like:
    	// Found two representations of same collection: at.workflow.webdesk.po.model.PoPerson.deputies
    	// see: Notes://Miraculix/C1256B300058B5FC/4019CC1110A8FCFBC1256D5D00502822/2F2D51928A4BF054C1257A8A0059971C    	
    	
    	@SuppressWarnings({ "cast", "unchecked" })
    	List<Object[]> roleHolderPersonRelations = (List<Object[]>) generalDAO.getElementsAsList(
    					"select rhp.UID, p.UID, r.UID from PoRoleCompetenceBase as rh"
    							+ " join rh.roleHolderPersons as rhp "
    							+ " join rhp.person as p "
    							+ " join rh.role as r "
    							+ " where "
    							+ " p.activeUser is true "
    							+ " and rhp.validfrom<=? and rhp.validto>? "
    							+ " and rh.validfrom<=? and rh.validto>? "
    							+ " and p.validfrom<=? and p.validto>? " 
    							+ " and r.validfrom<=? and r.validto>?",
    					new Object[] { queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate });
    	
    	personRoleMembership = new ObjectCategoryMembership();
    	for (Object[] relation : roleHolderPersonRelations)	{
    		personRoleMembership.putRelation((String)relation[0], (String)relation[1], (String)relation[2]);
    	}
    	
    	// ggruber 2012-10-04 query for UIDs instead of objects to avoid hibernate exceptions like:
    	// Found two representations of same collection: at.workflow.webdesk.po.model.PoPerson.deputies
    	// see: Notes://Miraculix/C1256B300058B5FC/4019CC1110A8FCFBC1256D5D00502822/2F2D51928A4BF054C1257A8A0059971C
    	
    	@SuppressWarnings({ "cast", "unchecked" })
    	List<Object[]> rolePersonRelations = (List<Object[]>) generalDAO.getElementsAsList(
				"select distinct p.UID, r.UID, rhg.UID from PoRoleHolderGroup as rhg "
					+ " join rhg.roleHolder as rh " 
					+ " join rh.role as r " 
					+ " join rhg.group as g " 
					+ " join g.personGroups as pg " 
					+ " join pg.person as p " 
					+ " where "
					+ " p.activeUser is true "
					+ " and r.validfrom<=? and r.validto >? "
					+ " and rhg.validfrom<=? and rhg.validto >? "
					+ " and rh.validfrom<=? and rh.validto>? "
					+ " and p.validfrom<=? and p.validto>? "
					+ " and pg.validfrom<=? and pg.validto>? "
					+ " and g.validfrom<=? and g.validto>? "
					,new Object[] { queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate, queryDate,});
    	
    	for (Object[] relation : rolePersonRelations)	{
    		String personUID = (String) relation[0];
    		String roleUID = (String) relation[1];
    		String rhgUID = (String) relation[2];
    		personRoleMembership.putRelation(rhgUID, personUID, roleUID);
    	}
    	
    	objectCategoryMembershipCache.put(new Element(cacheKey, personRoleMembership));
    	
    	return personRoleMembership;
    }
    
    private ObjectCategoryMembership buildPersonClientMembership() {
    	Date queryDate = HistorizationHelper.getNowWithHourPrecision();
    	String cacheKey = "PCMS_" + Reflections.reflectionHashCode(queryDate);
    	ObjectCategoryMembership personClientMembership = null;
    	
    	if (objectCategoryMembershipCache.isKeyInCache(cacheKey) && objectCategoryMembershipCache.get(cacheKey).getValue() instanceof ObjectCategoryMembership) {
    		personClientMembership = (ObjectCategoryMembership) objectCategoryMembershipCache.get(cacheKey).getValue();
    		return personClientMembership;
    	}
    	
    	if (logger.isDebugEnabled())
    		logger.debug("Call buildPersonClientMembership with date: "+queryDate);
    	
		@SuppressWarnings({ "cast", "unchecked" })
		List<PoPerson> persons = (List<PoPerson>) generalDAO.getElementsAsList(
				"from PoPerson p where p.activeUser is true " +
				" and p.validfrom <= ? and p.validto > ? ",
				new Object[] { queryDate, queryDate } );
		
		personClientMembership = new ObjectCategoryMembership();
		for (PoPerson person : persons)	{
			personClientMembership.putRelation(person.getUID(), person.getUID(), person.getClient().getUID());
		}
		
		objectCategoryMembershipCache.put(new Element(cacheKey, personClientMembership));
		
		return personClientMembership;
    }
    

	public PoOrganisationService getOrganisationService() {
		return (PoOrganisationService) this.appCtx.getBean("PoOrganisationService");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appCtx = applicationContext;
	}

	public void setObjectCategoryMembershipCache(Cache objectCategoryMembershipCache) {
		this.objectCategoryMembershipCache = objectCategoryMembershipCache;
	}
	
	public void setActionCache(Cache actionCache) {
		this.actionCache = actionCache;
	}

	public void setGeneralDAO(PoGeneralDAO generalDAO) {
		this.generalDAO = generalDAO;
	}

	@Override
	public List<PoAction> findAllCurrentActions() {
		return this.actionDAO.findAllActionsOfType(new Date(), PoConstants.ACTION_TYPE_ACTION);
	}

}
