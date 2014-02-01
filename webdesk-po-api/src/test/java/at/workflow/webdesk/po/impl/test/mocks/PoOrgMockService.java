/*
 * Created on 24.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package at.workflow.webdesk.po.impl.test.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.OrgTree;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.User;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author ggruber
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PoOrgMockService implements PoOrganisationService {

    private Map personMapByUserId = new HashMap();
    private Map personMapByTaId = new HashMap();
    private Map personMapByEmployeeId = new HashMap();
    private PoClient client;
    private PoOrgStructure orgStructure;
    private PoGroup group;


    private void setCorrectValidity(Historization hist) {
    	hist.setValidfrom(DateTools.now());
    	hist.setValidto(DateTools.INFINITY);
    }
    
	public void init() {
		client = new PoClient();
		client.setName("workflex");
		
		orgStructure = new PoOrgStructure();
		orgStructure.setName("OrgStructure");
		orgStructure.setClient(client);

		group = new PoGroup();
		group.setName("Geschäftsführung");
		group.setShortName("G01");
		group.setOrgStructure(orgStructure);
		group.setClient(client);
		setCorrectValidity(group);
		
        PoPerson myPerson;
        myPerson = new PoPerson();
        myPerson.setUID("wef");
        myPerson.setFirstName("Florian");
        myPerson.setLastName("Weiss");
        myPerson.setUserName("wef");
        myPerson.setTaID("10");
        myPerson.setEmployeeId("0004710");
        myPerson.setClient(client);
        setCorrectValidity(myPerson);
        addToGroup(myPerson, group);
        
        this.personMapByUserId.put("wef",myPerson);
        this.personMapByTaId.put("10",myPerson);
        this.personMapByEmployeeId.put("0004710", myPerson);

        myPerson = new PoPerson();
        myPerson.setUID("bos");
        myPerson.setFirstName("Susanne");
        myPerson.setLastName("Böhm");
        myPerson.setUserName("bos");
        myPerson.setTaID("11");
        myPerson.setEmployeeId("0004711");
        myPerson.setClient(client);
        setCorrectValidity(myPerson);
        addToGroup(myPerson, group);
        
        this.personMapByUserId.put("bos",myPerson);
        this.personMapByTaId.put("11",myPerson);
        this.personMapByEmployeeId.put("0004711", myPerson);


        myPerson = new PoPerson();
        myPerson.setUID("aiw");
        myPerson.setFirstName("Wolfgang");
        myPerson.setLastName("Aigner");
        myPerson.setUserName("aiw");
        myPerson.setTaID("12");
        myPerson.setEmployeeId("0004712");
        myPerson.setClient(client);
        setCorrectValidity(myPerson);
        addToGroup(myPerson, group);
        
        this.personMapByUserId.put("aiw",myPerson);
        this.personMapByTaId.put("12",myPerson);
        this.personMapByEmployeeId.put("0004712", myPerson);


    }
	
    public PoPerson findPersonByUserName(String key) {
        // TODO Auto-generated method stub
    		return (PoPerson)this.personMapByUserId.get(key);
    }
	
    public PoPerson findPersonByEmail(String key) {
    	return null;	// implement this when needed
    }
	
	private void addToGroup(PoPerson person, PoGroup group) {
		PoPersonGroup personGroup = new PoPersonGroup();
		personGroup.setPerson(person);
		personGroup.setGroup(group);
		setCorrectValidity(personGroup);
		person.addMemberOfGroup(personGroup);
	}
    
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getClient(java.lang.String)
     */
    public PoClient getClient(String uid) {
        // TODO Auto-generated method stub
        return this.client;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#saveClient(at.workflow.webdesk.po.model.PoClient)
     */
    public void saveClient(PoClient client) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllClients()
     */
    public List loadAllClients() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findClientByName(java.lang.String)
     */
    public PoClient findClientByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteClient(at.workflow.webdesk.po.model.PoClient)
     */
    public void deleteClient(PoClient client) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#isClientExistent(java.lang.String)
     */
    public boolean isClientExistent(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#isOrgStructureExistent(java.lang.String, java.lang.String)
     */
    public boolean isOrgStructureExistent(String name, String client_uid) {
        // TODO Auto-generated method stub
        return false;
    }


    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getOrgStructure(java.lang.String)
     */
    public PoOrgStructure getOrgStructure(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#saveOrgStructure(at.workflow.webdesk.po.model.PoOrgStructure)
     */
    public void saveOrgStructure(PoOrgStructure orgStructure) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllOrgStructures()
     */
    public List loadAllOrgStructures() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllOrgStructures(at.workflow.webdesk.po.model.PoClient)
     */
    public List loadAllOrgStructures(PoClient client) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findOrgStructureByName(at.workflow.webdesk.po.model.PoClient, java.lang.String)
     */
    public PoOrgStructure findOrgStructureByName(PoClient client, String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getOrgHierarchy(at.workflow.webdesk.po.model.PoClient)
     */
    public PoOrgStructure getOrgHierarchy(PoClient client) {
        // TODO Auto-generated method stub
        return null;
    }

    public PoOrgStructure getOrgLocations(PoClient client) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    public PoOrgStructure getOrgCostCenters(PoClient client) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteOrgStructure(at.workflow.webdesk.po.model.PoOrgStructure)
     */
    public void deleteOrgStructure(PoOrgStructure orgStructure) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getGroup(java.lang.String)
     */
    public PoGroup getGroup(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroup(java.lang.String)
     */
    public PoParentGroup getParentGroup(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllGroups()
     */
    public List loadAllGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllParentGroups()
     */
    public List loadAllParentGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#saveGroup(at.workflow.webdesk.po.model.PoGroup)
     */
    public void saveGroup(PoGroup group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteGroup(at.workflow.webdesk.po.model.PoGroup)
     */
    public void deleteGroup(PoGroup group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteAndFlushGroup(at.workflow.webdesk.po.model.PoGroup)
     */
    public void deleteAndFlushGroup(PoGroup group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deletePGWithoutConstraints(at.workflow.webdesk.po.model.PoParentGroup)
     */
    public void deletePGWithoutConstraints(PoParentGroup pg) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroupLink(at.workflow.webdesk.po.model.PoGroup, at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public PoParentGroup getParentGroupLink(PoGroup childGroup,
            PoGroup parentGroup, Date validAt) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteAndFlushParentGroupLink(at.workflow.webdesk.po.model.PoParentGroup)
     */
    public void deleteAndFlushParentGroupLink(PoParentGroup parentGroup) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#removeGroupFromParentGroup(at.workflow.webdesk.po.model.PoParentGroup)
     */
    public void removeGroupFromParentGroup(PoParentGroup parentGroup) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#changeValidityParentGroupLink(at.workflow.webdesk.po.model.PoParentGroup, java.util.Date, java.util.Date)
     */
    public void changeValidityParentGroupLink(PoParentGroup parentGroup,
            Date validFrom, Date validTo) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupByName(java.lang.String)
     */
    public List findGroupByName(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#changeValidityPersonGroupLink(at.workflow.webdesk.po.model.PoPersonGroup, java.util.Date, java.util.Date)
     */
    public void changeValidityPersonGroupLink(PoPersonGroup rhpersonlink,
            Date validfrom, Date validto) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupByName(java.lang.String, at.workflow.webdesk.po.model.PoClient)
     */
    public PoGroup findGroupByName(String key, PoClient client) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupByName(java.lang.String, java.util.Date)
     */
    public List findGroupByName(String key, Date referenceDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupByName(java.lang.String, at.workflow.webdesk.po.model.PoClient, java.util.Date)
     */
    public PoGroup findGroupByName(String key, PoClient client, Date referenceDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAllChildGroupsFlat(at.workflow.webdesk.po.model.PoGroup)
     */
    public List findAllChildGroupsFlat(PoGroup group) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAllChildGroupsFlat(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public List findAllChildGroupsFlat(PoGroup group, Date effectiveDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findChildGroups(at.workflow.webdesk.po.model.PoGroup)
     */
    public List findChildGroups(PoGroup group) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findChildGroups(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public List findChildGroups(PoGroup group, Date effectiveDate) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findChildGroupUids(String groupUid, Date effectiveDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findChildGroupsF(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public List findChildGroupsF(PoGroup group, Date effectiveDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAvailableChildGroups(at.workflow.webdesk.po.model.PoGroup)
     */
    public List findAvailableChildGroups(PoGroup group) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAvailableParentGroups(at.workflow.webdesk.po.model.PoGroup)
     */
    public List findAvailableParentGroups(PoGroup group) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroup(at.workflow.webdesk.po.model.PoGroup)
     */
    public PoParentGroup getParentGroup(PoGroup group) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getParentGroup(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public PoParentGroup getParentGroup(PoGroup group, Date referenceDate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#setParentGroup(at.workflow.webdesk.po.model.PoGroup, at.workflow.webdesk.po.model.PoGroup)
     */
    public void setParentGroup(PoGroup group, PoGroup parent) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#setParentGroup(at.workflow.webdesk.po.model.PoGroup, at.workflow.webdesk.po.model.PoGroup, java.util.Date, java.util.Date)
     */
    public void setParentGroup(PoGroup group, PoGroup parent, Date validFrom,
            Date validTo) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupsLinkedPersons(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public List findGroupsLinkedPersons(PoGroup group, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getPerson(java.lang.String)
     */
    public PoPerson getPerson(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getPersonGroup(java.lang.String)
     */
    public PoPersonGroup getPersonGroup(String uid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getPersonGroups(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoOrgStructure, java.util.Date)
     */
    public List getPersonGroups(PoPerson person, PoOrgStructure orgStructure,
            Date validAt) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getPersonGroupLink(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public PoPersonGroup getPersonGroupLink(PoPerson person, PoGroup group,
            Date validAt) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getOrgStructure(at.workflow.webdesk.po.model.PoPerson)
     */
    public PoOrgStructure getOrgStructure(PoPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#loadAllPersons()
     */
    public List loadAllPersons() {
    	List persons = new ArrayList<PoPerson>();
    	for(Iterator itr=this.personMapByEmployeeId.keySet().iterator();itr.hasNext();) {
    		String key = (String) itr.next();
    		persons.add(this.personMapByEmployeeId.get(key));
    	}
        return persons;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAllPersons(java.util.Date)
     */
    public List findAllPersons(Date date) {
        return this.loadAllPersons();
    }
    
    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findAllPersons(java.util.Date)
     */
    public List findAllPersons(Date date, boolean activeUser) {
    	// TODO Auto-generated method stub
    	return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#savePerson(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup)
     */
    public void savePerson(PoPerson person, PoGroup group) {
        // TODO Auto-generated method stub
    	System.out.println("reached savePerson!");
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#updatePerson(at.workflow.webdesk.po.model.PoPerson)
     */
    public void updatePerson(PoPerson person) {
        // TODO Auto-generated method stub
    	System.out.println("reached updatePerson!");

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonByEmployeeId(java.lang.String)
     */
    public PoPerson findPersonByEmployeeId(String key) {
        return (PoPerson) this.personMapByEmployeeId.get(key);
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonByTaId(java.lang.String)
     */
    public PoPerson findPersonByTaId(String key) {
        // TODO Auto-generated method stub
        return (PoPerson)this.personMapByTaId.get(key);
    }


    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#linkPerson2Group(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup)
     */
    public void linkPerson2Group(PoPerson person, PoGroup group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#linkPerson2Group(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup, java.util.Date, java.util.Date)
     */
    public void linkPerson2Group(PoPerson person, PoGroup group,
            Date validFrom, Date validTo) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#removePersonFromGroup(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup)
     */
    public void removePersonFromGroup(PoPerson person, PoGroup group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#removePersonFromGroup(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public void removePersonFromGroup(PoPerson person, PoGroup group,
            Date effectiveDate) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#isPersonActualMemberOfGroup(at.workflow.webdesk.po.model.PoPerson, at.workflow.webdesk.po.model.PoGroup)
     */
    public boolean isPersonActualMemberOfGroup(PoPerson person, PoGroup group) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsLinkedGroups(at.workflow.webdesk.po.model.PoPerson)
     */
    public List findPersonsLinkedGroups(PoPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsLinkedGroups(at.workflow.webdesk.po.model.PoPerson, int)
     */
    public List findPersonsLinkedGroups(PoPerson person, int orgType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsLinkedGroups(at.workflow.webdesk.po.model.PoPerson, java.util.Date)
     */
    public List findPersonsLinkedGroups(PoPerson person, Date effectiveDate) {
        // TODO Auto-generated method stub
        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsLinkedGroups(at.workflow.webdesk.po.model.PoPerson, int, java.util.Date)
     */
    public List findPersonsLinkedGroups(PoPerson person, int orgStructureType,
            Date effectiveDate) {
        // TODO Auto-generated method stub
    	
    	//List ret
    	
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#getPersonsHierarchicalGroup(at.workflow.webdesk.po.model.PoPerson)
     */
    public PoGroup getPersonsHierarchicalGroup(PoPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroups(at.workflow.webdesk.po.model.PoPerson, java.util.Date, int)
     */
    public List findPersonGroups(PoPerson person, Date date, int orgType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findGroupsOfOrgStructureF(at.workflow.webdesk.po.model.PoOrgStructure, java.util.Date)
     */
    public List findGroupsOfOrgStructureF(PoOrgStructure orgStructure, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroupsF(at.workflow.webdesk.po.model.PoPerson, java.util.Date, int)
     */
    public List findPersonGroupsF(PoPerson person, Date date, int orgType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroups(at.workflow.webdesk.po.model.PoPerson, java.util.Date, at.workflow.webdesk.po.model.PoOrgStructure)
     */
    public List findPersonGroups(PoPerson person, Date date,
            PoOrgStructure orgStructure) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonGroupsF(at.workflow.webdesk.po.model.PoPerson, java.util.Date, at.workflow.webdesk.po.model.PoOrgStructure)
     */
    public List findPersonGroupsF(PoPerson person, Date date,
            PoOrgStructure orgStructure) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteAndFlushPersonGroupLink(at.workflow.webdesk.po.model.PoPersonGroup)
     */
    public void deleteAndFlushPersonGroupLink(PoPersonGroup pglink) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deletePerson(at.workflow.webdesk.po.model.PoPerson)
     */
    public void deletePerson(PoPerson person) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#deleteAndFlushPerson(at.workflow.webdesk.po.model.PoPerson)
     */
    public void deleteAndFlushPerson(PoPerson person) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsOfGroup(at.workflow.webdesk.po.model.PoGroup, java.util.Date)
     */
    public List findPersonsOfGroup(PoGroup group, Date validAt) {
        // TODO Auto-generated method stub
        return null;
    }

	public void checkGroup(PoGroup group) {
		// TODO Auto-generated method stub
		
	}

	public Boolean checkNeededForSavePerson(PoPerson person, PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public Boolean checkNeededForUpdatePerson(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public void checkUser(PoPerson person) {
		// TODO Auto-generated method stub
		
	}

	public void deleteAndFlushClient(PoClient client) {
		// TODO Auto-generated method stub
		
	}

	public void deleteAndFlushOrgStructure(PoOrgStructure orgStructure) {
		// TODO Auto-generated method stub
		
	}

	public void deleteAndFlushParentGroup(PoParentGroup delPg) {
		// TODO Auto-generated method stub
		
	}

	public void deletePersonGroupLink(PoPersonGroup pglink) {
		// TODO Auto-generated method stub
		
	}

	public List<PoPerson> findAllCurrentActivePersons() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findAllCurrentPersons() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoParentGroup> findChildGroupsAll(PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findCurrentGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup findGroupByShortName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup findGroupByShortName(String name, PoClient client) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup findGroupByShortName(String name, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup findGroupByShortName(String name, PoClient client,
			Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupWithOrgStructureAndPerson(PoPerson person,
			PoOrgStructure os, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupsFromClientF(PoClient client, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupsOfOrgStructure(PoOrgStructure orgStructure,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupsWithFilter(List<String> uids,
			List<String> names, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupsWithFilter(List<String> uids,
			List<String> names, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findGroupsWithoutParent(PoOrgStructure orgStructure,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> findNotHierarchicalGroupsOfPerson(PoPerson person,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoOrgStructure findOrgStructureByNameAndClient(PoClient client,
			String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoParentGroup> findParentGroups(PoGroup parent, PoGroup child) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoParentGroup> findParentGroupsAll(PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoPerson findPersonByEmployeeId(String employeeId, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoPerson findPersonByTaId(String key, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

    public PoPerson findPersonByWorkflowId(String key) {
        return (PoPerson) this.personMapByUserId.get(key);
    }
    
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person,
			PoOrgStructure orgStructure) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPersonGroup> findPersonGroupsAll(PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPersonGroup> findPersonGroupsF(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findPersonIdsOfClients(List<String> clientUidList,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findPersonUidsOfGroups(List<String> groupUidList,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsOfClient(PoClient client, Date validAt) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsOfClientF(PoClient client, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsOfGroup(PoGroup group,
			List<String> searchList, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsOfGroupF(PoGroup group, Date validAt) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithFilter(List<String> uids,
			List<String> names) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithFilter(List<String> uids,
			List<String> names, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithFilter(List<String> uids,
			List<String> names, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithViewPermission(
			Map<String, List<String>> viewPermissions, List<String> names,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithViewPermission(
			Map<String, List<String>> viewPermissions, List<String> names,
			Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoPerson> findPersonsWithViewPermission(
			Map<String, List<String>> viewPermissions, List<String> names) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findUidsOfClients() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDepthOfGroup(PoGroup group, Date validAt) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxDepthOfOrgStructure(PoOrgStructure orgStructure) {
		// TODO Auto-generated method stub
		return 0;
	}

	public OrgTree getOrgModel(PoOrgStructure orgS, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public OrgTree getOrgModelCached(PoOrgStructure orgS) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup getParentGroupByIndex(PoGroup group, int index, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, Object> getPersonAsMap(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup getPersonsCostCenterGroup(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup getPersonsHierarchicalGroup(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResolvedHeaderText(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> getTopLevelGroupsOfOrgStructure(PoOrgStructure os,
			boolean includeFlag) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoGroup> getTopLevelGroupsOfOrgStructureWithDate(
			PoOrgStructure os, Date date, boolean includeFlag) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup isGroupChildGroup(PoGroup possibleChildGroup,
			PoGroup possibleParentGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPersonActualMemberOfGroup(PoPerson person, String group) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<PoPerson> linkPersons2Group(List<PoPerson> persons,
			PoGroup myGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	public void refresh(Object obj) {
		// TODO Auto-generated method stub
		
	}

	public List<PoPerson> resolveGroupToPersons(String shortName,
			boolean includeChildGroups, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveParentGroup(PoParentGroup pg) {
		// TODO Auto-generated method stub
		
	}

	public void savePersonGroup(PoPersonGroup newPg) {
		// TODO Auto-generated method stub
		
	}

	public User lookupUser(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPasswordCorrect(User user, String password) {
		// TODO
		return true;
	}

	public List<String> findGroupIdsOfClients(List<String> clientUidList,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPasswordValidityDays(User user) {
		// TODO
		return 0;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public PoPerson findPersonByTaId(String key, Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findPersonsOfClient(PoClient client, Date from,
			Date to) {
		// TODO Auto-generated method stub
		return null;
	}


	public User lookupUserByMail(String mail) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public PoPersonImages getPersonImages(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PoPersonGroup> filterPersonGroups(Collection<PoPersonGroup> personGroups, PoOrgStructure structure) {
		return null;
	}

	@Override
	public void saveBankAccounts(PoPerson person) {
	}
	
	@Override
	public void historicizeBankAccounts(PoPerson person) {
	}

	@Override
	public List<PoPersonGroup> findPersonGroupsAll(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PoGroup getDepartment(PoGroup group, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}
}
