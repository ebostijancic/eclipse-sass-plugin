package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;

/**
 * Created on 05.01.2005
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          wdDEV<br>
 * refactored at:    08.06.2007<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoPersonDAO.java<br><br>
 *
 * The DataAccess Object for <code>PoPerson, PoPersonGroup</code> objects.
 * 
 */
public interface PoPersonDAO extends GenericDAO<PoPerson>{

	/**
	 * @param person
	 * Saves the person object to the database
	 */
	public void save(PoPerson person, PoGroup group);
	
	/**
	 * @param uid
	 * @return the PoPersonGroup object with the given uid.
	 */
	public PoPersonGroup getPersonGroup(String uid);
	
	/**
	 * <p>
	 * Use this function to get a <code>List</code> of <code>PoPersonGroup</code> objects
	 * that are assigned to the <code>person</code> via the <code>orgStructure</code>. If 
	 * the <code>orgStructure</code> is <i>hierarchical</i>, exactly <i>one entry</i> 
	 * should be returned. 
	 * 
	 * 
	 * @param person a <code>PoPerson</code>
	 * @param orgStructure <code>PoOrgStructure</code>
	 * @param validAt the <code>Date</code> on which all assignments 
	 * must be valid.
	 * @return a List of <code>PoPersonGroup</code> objects.
	 */
	public List<PoPersonGroup> getPersonGroups(PoPerson person, PoOrgStructure orgStructure, Date validAt);
	
	/**
	 * @param person
	 * @param group
	 * @param validAt
	 * @return a <code>PoPersonGroup</code> object, or null if none was found.
	 */
	public PoPersonGroup getPersonGroupLink(PoPerson person, PoGroup group, Date validAt);		
	
	/**
	 * @param person
	 * 
	 * updates the person.
	 */
	public void update(PoPerson person);
    
	/**
	 * @param key
	 * @param date
	 * @return a <code>PoPerson</code> object with the given <code>key</code> equals its <code>taId</code>.
	 */
	public PoPerson findPersonByTaId(String key, Date date);
	
	/**
	 * @param key
	 * @param from
	 * @param to
	 * @return the most recent <code>PoPerson</code> object 
	 * with the given <code>key</code> equals its <code>taId</code>.
	 */
	public PoPerson findPersonByTaId(String key, Date from, Date to);
	
	/**
	 * finds PoPerson given the UserName of the Person
	 * throws an Exception if more than one Persons match
	 * returns null if no match was found.
	 * 
	 * @param userName
	 * @return a PoPerson Object, which matches the Username
	 */
	public PoPerson findPersonByUserName(String userName);
	
	/**
	 * @param email the mail address of the person to find.
	 * @return the person with passed mail address, or null if none found.
	 */
	public PoPerson findPersonByEmail(String email);
	
	/**
	 * finds PoPerson with a given workflowId
	 * 
	 * @param workflowId
	 * @return PoPerson
	 */
	public PoPerson findPersonByWorkflowId(String workflowId);
	
	/**
	 * @param employeeId the <code>employeeId</code> of the <code>PoPerson</code>.
	 * @param date the date on which the <code>PoPerson</code> has to be valid.
	 * @return a <code>PoPerson</code> if one with the given <code>employeeId</code> was found, 
	 * <code>null</code> otherwise.
	 */
	public PoPerson findPersonByEmployeeId(String employeeId, Date date);
	
	
	/**
	 * @param date
	 * @return a list of persons that are already valid
	 */
	public List<PoPerson> findAllPersons(Date date);

	/**
	 * @param date
	 * @param activeUser flag
	 * 
	 * @return a list of persons that are valid and have required activation state
	 */
	public List<PoPerson> findAllPersons(Date date, boolean activeUser);
	// etc, etc...
	

    /**
     * @param person
     * @param group
     * @return if the given person is a member of the given group. 
     */
    public boolean isPersonActualMemberOfGroup(PoPerson person, PoGroup group);
    
    /**
     * @param person
     * @param orgStructureType
     * @param date
     * @return a <code>List</code> containing <code>PoGroup</code> objects.
     * <p>
     * This function can be used to find a <code>List</code> of <code>PoGroup</code> objects, 
     * which have to be valid at the <code>date</code>. 
     * <p>
     * The <code>orgStructureType</code> defines, to which <code>PoOrgstructure</code> the 
     * <code>PoGroup</code> belongs. If it is <code>-1</code> it is ommitted. 
     */
    public List<PoGroup> findPersonsLinkedGroups(PoPerson person, int orgStructureType, Date date);
       
    
    /**
     * @param client
     * @param date
     * @return a list of persons that are assigned to the given client. 
     */
    public List<PoPerson> findPersonsOfClientF(PoClient client, Date date); 
    
    
        /**
     * @param person
     * @param date
     * @param poOrgStructure
     * @return a list of PoPersonGroup objects that are assigned to the passed person. 
     * The appropriate Groups or rather the PoPersonGroup objects must be valid at the passed date.
     * If an integer that is >=0 is passed as the poOrgStructure, the corresponding groups have to 
     * belong to this orgStructure, otherwise the orgStructure is omitted.
     * 
     */
    public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, int poOrgStructure);
    	
    
    /**
     * @param person
     * @param date
     * @param poOrgStructure
     * @return a list of PoPersonGroup objects that are valid now or in the future.
     * If poOrgStructure is -1, the orgStructure is not taken into account.
     */
    public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, int poOrgStructure);
    	
	/**
	 * @param person
	 * @param date
	 * @param orgStructure
	 * @return a list of PoPersonGroup objects that are assigned to the given person and that are valid at the
	 * given date. Also, the given orgstructure has to be assigned to the group. 
	 */
	public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, PoOrgStructure orgStructure);

	/**
	 * @param person
	 * @return the orgStructure of the given Person. The orgStructure 
	 * has orgType = 1 (organisationHierarchy see. Poconstants for more information)
	 */
	public PoOrgStructure getOrgStructure(PoPerson person);
		
    /**
     * @param person
     * @param date
     * @param poOrgStructure
     * @return a list of PoPersonGroup objects that are valid now or in the future.
     */
    public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, PoOrgStructure poOrgStructure);
    
    /**
     * @param person
     * @param poOrgStructure
     * @return a list of all PoPersonGroup objects with poOrgStructure of the person
     */
    public List<PoPersonGroup> findPersonGroupsAll(PoPerson person, PoOrgStructure poOrgStructure);
    
    /**
     * @param person
     * @return a list of all PoPersonGroup objects of the person
     */
    public List<PoPersonGroup> findPersonGroupsAll(PoPerson person);
    
    /**
     * @param person
     * @param date
     * @return a list of PoActions // only Parent objects so far. 
     */
    public List<PoAction> findActions(PoPerson person, Date date);
    

    /**
     * re-attaches detached hibernate object to the session.
     * uses lock-mode NONE -> can be called on any detached object which
     * is stored for instance inside the user-session.
     * 
     * @param obj (hibernate detached object)
     */
    public void refresh(Object obj);
    
    
    public List<PoPerson> findPersonsOfClient(PoClient client, Date validAt);

    
    public List<PoPerson> findPersonsOfClient(PoClient client, Date from, Date to);
    
    
    /**
     * This should have been named checkUniqueness(), or checkTemporalUniqueness().
     * Checks if it is valid to store the given person in the database.
     * Throws a PoRuntimeException if a unique constraint is violated.
     */
	public void checkUser(PoPerson person);

	/**
	 * <p>
	 * Saves or updates the <code>PoPersonGroup</code> instance, 
	 * depending on the <code>uid</code> of the object. 
	 * 
	 * 
	 * @param newPg a <code>PoPersonGroup</code>
	 * 
	 * 
	 */
	public void savePersonGroup(PoPersonGroup newPg);

	
	/**
	 * Returns a list of <code>PoPersonGroup</code> objects that 
	 * will become active in the future. The returned objects are assigned
	 * to the given <code>person</code> and the given <code>orgStructure</code>
	 * 
	 * @param person
	 * @param orgStructure
	 * @param validFrom
	 * @return a list of <code>PoPersonGroup</code> objects.
	 */
	public List<PoPersonGroup> getFutureGroups(PoPerson person, PoOrgStructure orgStructure,
            Date validFrom);

	/**
	 * This function helps to find <code>PoPersonGroup</code> objects, with the 
	 * given <code>person</code> and <code>group</code> assigned. Both objects 
	 * have to be valid at the given date.
	 * 
	 * @param person
	 * @param group
	 * @param effectiveDate
	 * @return a <code>PoPersonGroup</code> object, or null if none was found.
	 * 
	 * Throws a PoRuntimeException, if more than one <code>PoPersonGroup</code> object
	 * was found.
	 */
	public PoPersonGroup findPersonGroupObject(PoPerson person, PoGroup group, Date effectiveDate);
   
	
	/**
	 * This function can be used to find the <code>PoPersonGroup</code> object that 
	 * will become valid after the given <code>personGroup</code> object.
	 * 
	 * @param personGroup
	 * @return a <code>PoPersonGroup</code> object or null
	 */
	public PoPersonGroup getGroupAfter(PoPersonGroup personGroup);

	/**
	 * This function can be used to find the <code>PoPersonGroup</code> object that 
	 * was valid before the given <code>personGroup</code> object.
	 * 
	 * @param personGroup
	 * @return a <code>PoPersonGroup</code> object or null
	 */
    public PoPersonGroup getGroupBefore(PoPersonGroup personGroup);

	/**
	 * Deletes the given <code>PoPersonGroup</code> object
	 * from the database.
	 * 
	 * @param futPg
	 */
	public void deletePersonGroupLink(PoPersonGroup futPg);
	
	/**
	 * Returns the hierarchical group of the given 
	 * <code>person/code> at the given <code>date</code>.
	 * 
	 * @param person
	 * @param date
	 * 
	 * @return a <code>PoGroup</code> object.
	 */
	public PoGroup getPersonsHierarchicalGroup(PoPerson person, Date date);

	/**
	 * Returns a list of <code>PoPerson</code> <code>uid</code>'s of the <code>PoClient</code> with
	 * the given <code>uid</code>. Both the <code>client</code> and the <code>persons</code> have to 
	 * be active at the given <code>date</code>.
	 * 
	 * 
	 * @param uid
	 * @param date
	 * @return a list of <code>String</code> objects.
	 */
	public List<String> findPersonUidsOfClient(String uid, Date date);
	
	
	
	/**
	 * @param uids a <code>List</code> of <code>String</code> objects, representing
	 * the <code>UID</code> of a <code>PoPerson</code> object. 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * @param date the date on which the person has to be valid.
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 * 
	 * 
	 * @see at.workflow.webdesk.po.dao.PoGroupDAO#findPersonsOfGroup(PoGroup group, List names, Date date );
	 */
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date date);
	
	/**
	 * @param uids a <code>List</code> of <code>String</code> objects, representing
	 * the <code>UID</code> of a <code>PoPerson</code> object. 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * @param from	begin
	 * @param to	end
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 * 
	 * 
	 * @see at.workflow.webdesk.po.dao.PoGroupDAO#findPersonsOfGroup(PoGroup group, List names, Date date );
	 */
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date from, Date to);
	
	
	/**
	 * @param viewPermissions a <code>Map</code> M(x,y) where x is a <code>String</code>
	 * object [<code>clients</code>, <code>groups</code>, <code>persons</code> are actually
	 * considered] and y is a List of <code>uid</code>'s. 
	 * 
	 * 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * 
	 * @param date
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date date);

	/**
	 * @param viewPermissions a <code>Map</code> M(x,y) where x is a <code>String</code>
	 * object [<code>clients</code>, <code>groups</code>, <code>persons</code> are actually
	 * considered] and y is a List of <code>uid</code>'s. 
	 * 
	 * 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * 
	 * @param from	beginning
	 * @param to	end
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date from, Date to);
	
	/**
	 * <p>
	 * Returns a <code>List</code> of <code>PoPersonGroup</code>'s, which 
	 * connects the given <code>person</code> and <code>group</code>. The assigment
	 * has to be valid at <code>validFrom</code> or become valid afterwards.
	 * 
	 * @param person <code>PoPerson</code>
	 * @param group <code>PoGroup</code>
	 * @param validFrom <code>Date</code>
	 */
	public List<PoPersonGroup> findPersonGroupsF(PoPerson person, PoGroup group, Date validFrom);
}
