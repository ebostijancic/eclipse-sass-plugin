package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;

/**
 * Created on 05.01.2005 or 17.04.2007
 * @author DI Harald Entner logged in as: hentner
 */
public interface PoGroupDAO extends GenericDAO<PoGroup> {
    
    /**
     * @param uid
     * @return a <code>PoParentGroup</code> object with the given <code>uid</code>.
     */
    PoParentGroup getParentGroup(String uid);
    
    
    /**
     * @return a list of all <code>PoParentGroup</code> objects. Note: the objects 
     * may be invalid at the actual date.
     */
    List<PoParentGroup> loadAllParentGroups();
    
	
	/**
	 * Retrieves all the parent groups, past, present and future
	 * @param group
	 * @return
	 */
	List<PoParentGroup> findParentGroupsAll(PoGroup group);
	

	/**
	 * @param key
	 * @param referenceDate
	 * @returna  a List of <code>PoGroup</code> objects with the given 
	 * <code>name</code>. The returned objects are valid at the <code>referenceDate</code>.
	 */
	List<PoGroup> findGroupByName(String key, java.util.Date referenceDate);
	
	/**
	 * @param key
	 * @param client
	 * @param referenceDate
     * @return a  <code>PoGroup</code> object with the given 
	 * <code>key (name)</code> and <code>client</code>. The object has to be valid at the 
	 * given <code>referenceDate</code>
	 */
	PoGroup findGroupByName(String key, PoClient client, java.util.Date referenceDate);
    
    /**
     * @param name
     * @param client
     * @param referenceDate
     * @return a  <code>PoGroup</code> object with the given 
	 * <code>name</code> and <code>client</code>. The object has to be valid at the 
	 * given <code>referenceDate</code>
	 */
    PoGroup findGroupByShortName(String name, PoClient client, Date referenceDate);

    /**
     * @param orgStructure
     * @param date
     * @return a list of PoGroup objects that are assigned to the given <code>orgStructure</code>
     * at the given <code>date</code>.
     */
    List<PoGroup> findGroupsOfOrgStructureF(PoOrgStructure orgStructure, Date date);
	    
	
	/**
	 * @param uids a List of Strings representing the UID of a PoGroup object. 
	 * @param names a List of Strings objects which could match the shortName or the name
	 * property of PoPerson object. If one object has matched, it will be 
	 * added to the returned List. The * character will be replaced 
	 * with % in the hql query
	 * @param date the date on which the person has to be valid.
	 * @return a List of PoGroup objects.
	 */
	List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date date);
	
	/**
	 * @param uids a List of Strings representing the UID of a PoGroup object. 
	 * @param names a List of Strings objects which could match the shortName or the name
	 * property of PoPerson object. If one object has matched, it will be 
	 * added to the returned List. The * character will be replaced 
	 * with % in the hql query
	 * @param from	begin
	 * @param to	end
	 * @return a List of PoGroup objects.
	 */
	List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date from, Date to);
	
	/**
	 * @param group
	 * @return a list of PoParentGroup objects that are valid at the 
     * current date.
	 */
	List<PoParentGroup> findChildGroups(PoGroup group);
	
	/**
	 * @param group
	 * @param referenceDate
	 * @return a list of PoParentGroup objects that are valid at the given date.
	 */
	List<PoParentGroup> findChildGroups(PoGroup group, Date referenceDate);
	
	/**
	 * fri_2011-02-24: TODO after creating OrgTree otherwise this can be removed.
	 *  
	 * fri_2011-02-24: seems the suffix "F" stands for "Future",
	 * i.e. check only if the group's validto is bigger than passed effectiveDate.
	 * 
	 * @return a list of <code>PoParentGroup</code> objects that are valid at the given
	 * 		<code>effectiveDate</code>, or will become valid in the future.
	 */
	List<PoParentGroup> findChildGroupsF(PoGroup group, Date effectiveDate);
	
    /**
     * Retrieves all subgroups, past, current and future
     * @param group
     * @return
     */
	List<PoParentGroup> findChildGroupsAll(PoGroup group);
	
    /**
     * @param orgStructure a PoOrgStructure object
     * @param date 
     * @return all groups that have no parent assigned and corresponds to the given <code>orgStructure</code>.
     * The groups have to be valid at the given date.
     */
    List<PoGroup> findGroupsWithoutParent(PoOrgStructure orgStructure, Date date);
    
	/**
	 * @param group
	 * @param date
	 * @return a list of actions that corresponds to the given group
	 */
	List<PoAction> findActions(PoGroup group, Date referenceDate);
	
	/**
	 * @param client
	 * @param date
	 * @return a list of groups that are assigned to the given client.
	 */
	List<PoGroup> findGroupsFromClientF(PoClient client, Date date);
	
	 /**
	 * @param group
	 * @param validAt
	 * @return a list of <code>PoPerson</code> objects that are assigned to the given 
	 * <code>group</code> at the given <code>date: validAt</code>.
	 */
	List<PoPerson> findPersonsOfGroup(PoGroup group, Date validAt);
	 
	 /**
	 * @param group
	 * @param validAt
	 * @return a List of <code>PoPerson</code> objects that are assigned to the group at the 
	 * given <code>date (validAt)</code> or the assignment will become active in the future.
	 */
	List<PoPerson> findPersonsOfGroupF(PoGroup group, Date validAt);
	
	/**
	 * 
	 * @param group
	 * @param from
	 * @param to
	 * @return a List of <code>PoPerson</code> objects that are assigned
	 * to the group between <code>from</code> and <code>to</code> dates.
	 */
	List<PoPerson> findPersonsOfGroup(PoGroup group, Date from, Date to);

	
	PoParentGroup getParentGroup(PoGroup group, Date referenceDate);
	
    /**
     * get all Current PoGroups
     * @return List of PoGroups
     */
    List<PoGroup> findCurrentGroups();
    
    /**
     * @param group
     * @param date
     * @return a list of <code>PoPersonGroup</code> [pg] objects that are valid
     * at the given date. <code>pg.group==group</code>
     */
    List<PoPersonGroup> findPersonGroupsF(PoGroup group, Date date);
    
    /**
     * Retrieves all the PoPersonGroups that are, were or will be assigned to the group
     * @param group
     * @return
     */
    List<PoPersonGroup> findPersonGroupsAll(PoGroup group);
    
    /**
     * Throws a PoRuntimeException if a unique constraint is violated. The unique 
     * constraint is defined in the implementation of this interface.
     * 
     * @param group
     */
    void checkGroup(PoGroup group);
    

	/**
	 * 
	 * 
	 * @param group
	 * @param validAt
	 * @return a list of <code>PoParentGroup</code> objects, that have the given <code>group</code> as a child and 
	 * are valid at the given <code>date</code>.
	 */
	List<PoParentGroup> findParentGroupsWithDate(PoGroup group, Date validAt);
	
	
	/**
	 * Returns a list of <code>PoParentGroup</code> objects.
	 * The link has to fulfill the following equation
	 * <br><br><b>
	 * pg.validto>validFrom and pg.validfrom<=validto
	 * </b>
	 * 
	 * @param child
	 * @param validFrom
	 * @param validTo
	 * @return a List of <code>PoParentGroup</code> objects 
	 */
	List<PoParentGroup> findParentGroupsWithTimeOverlap(PoGroup child, Date validFrom, Date validTo);
	
	/**
     * @param group
     * @return a list of <code>PoParentGroup</code> objects that have the given <code>group</code>
     *  as child. The <code>group</code> as well as the <code>PoParentGroup</code> objects have to be valid
     *  at the current date.
     */
    List<PoParentGroup> findAllParentGroups(PoGroup group);
    
    
    
    /**
     * A list of valid <code>PoGroup</code> objects concerning the given <code>date</code>.
     * The given <code>person</code> has to be contained in the returned group, as well as 
     * be assigned to the given <code>orgStructure</code>.
     *
     * 
     * @param person A person contained in the groups 
     * @param os The Organisation Structure of the groups
     * @param date The date at which the groups have to be valid.
     * @return a List of <code>PoGroup</code> objects. 
     */
    List<PoGroup> findGroupWithOrgStructureAndPerson(PoPerson person, PoOrgStructure os, Date date);

	/**
	 * Save or updates the <code>PoParentGroup</code> object.
	 * 
	 * @param pg
	 */
	void saveParentGroup(PoParentGroup pg);

	/**
	 * Deletes the <code>pg</code> object.
	 * 
	 * @param pg
	 */
	void deleteParentGroup(PoParentGroup pg);
	

	List<PoGroup> findGroupsOfOrgStructure(PoOrgStructure orgStructure, Date date);

	/**
	 * Returns a list of <code>PoPerson</code> <code>uid</code>'s of the <code>PoGroup</code> with
	 * the given <code>uid</code>. Both the <code>group</code> and the <code>persons</code> have to 
	 * be active at the given <code>date</code>.
	 * 
	 * 
	 * @param uid
	 * @param date
	 * @return a list of <code>String</code> objects.
	 */
	List<String> findPersonUidsOfGroup(String uid, Date date);

	/**
	 * @param group a <code>PoGroup</code> object. Only <code>PoPerson</code> objects that 
	 * are linked with this <code>group</code> are considered.   
	 * @param date the <code>Date</code> on which the Assignment has to be valid.
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>.
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 * 
	 * 
	 * @see at.workflow.webdesk.po.dao.PoPersonDAO#findPersonsWithFilter(List uids, List names);
	 */
	List<PoPerson> findPersonsOfGroup(PoGroup group, List<String> names, Date date );


	/**
	 * <p>
	 * Use this function to get a <code>List</code> of 
	 * <code>PoParentGroup</code> objects that connect 
	 * <code>parent</code> and <code>child</code>.
	 * 
	 * @param parent
	 * @param child
	 * @return a <code>List</code> of <code>PoParentGroup</code> objects 
	 * that have <code>parent</code> as parent and <code>child</code> as 
	 * child. 
	 */
	List<PoParentGroup> findParentGroups(PoGroup parent, PoGroup child);
	
	/**
	 * Query for groups of the passed client at the passed referenceDate
	 * and return a List of the UIDs of the groups.
	 * 
	 * @param uidOfClient
	 * @param date
	 * @return
	 */
	List<String> findGroupUidsOfClient(String uidOfClient, Date date);

	/**
	 * @param viewPermissions
	 * @param date
	 * @return all groups that can be seen with view permissions
	 */
	List<PoGroup> resolveViewPermissionsToGroups(Map<String, List<String>> viewPermissions, Date date);
	
}
