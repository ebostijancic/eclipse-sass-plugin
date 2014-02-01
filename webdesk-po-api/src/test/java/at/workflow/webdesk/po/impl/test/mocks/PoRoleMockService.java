package at.workflow.webdesk.po.impl.test.mocks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

public class PoRoleMockService implements PoRoleService {

    private Map<String, PoRole> roleMapByName = new HashMap<String, PoRole>();
    private PoClient client;
    private PoOrganisationService orgService;
    
    /**
	 * @return Returns the orgService.
	 */
	public PoOrganisationService getOrgService() {
		return orgService;
	}
    
    private void init() {
        client = new PoClient();
        client.setName("workflex");
        
    	PoRole myRole; 
    	
    	myRole = new PoRole();
    	myRole.setClient(client);
    	myRole.setName("manager");
    	// speichere hier kurznamen von inhaber (quick & dirty)
    	myRole.setDescription("wef");
    	myRole.setParticipantId("manager");
    	roleMapByName.put(myRole.getName(), myRole);

    	myRole = new PoRole();
    	myRole.setClient(client);
    	myRole.setName("general_manager");
    	myRole.setParticipantId("general_manager");
    	// speichere hier kurznamen von inhaber (quick & dirty)
    	myRole.setDescription("wef");
    	
    	roleMapByName.put(myRole.getName(), myRole);

    	myRole = new PoRole();
    	myRole.setClient(client);
    	myRole.setName("hr_responsible");
    	myRole.setParticipantId("hr_responsible");
    	// speichere hier kurznamen von inhaber (quick & dirty)
    	myRole.setDescription("wef");
    	
    	roleMapByName.put(myRole.getName(), myRole);
    	    	
    }
    
	public PoRole getRole(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public void assignRole(PoRole role, PoPerson person, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRole(PoRole role, PoGroup group, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRole(PoRole role, PoPerson person, Date validFrom,
			Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRole(PoRole role, PoGroup group, Date validFrom,
			Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRoleWithGroupCompetence(PoRole role, PoPerson person,
			PoGroup group, Date validFrom, Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRoleWithGroupCompetence(PoRole role,
			PoGroup competenceGroup, PoGroup controlledGroup, Date validFrom,
			Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRoleWithPersonCompetence(PoRole role,
			PoPerson competencePerson, PoPerson controlledPerson,
			Date validFrom, Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public void assignRoleWithPersonCompetence(PoRole role,
			PoGroup competenceGroup, PoPerson controlledPerson, Date validFrom,
			Date validTo, int ranking) {
		// TODO Auto-generated method stub

	}

	public List loadAllRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public List loadAllRoles(PoClient client) {
		// TODO Auto-generated method stub
		return null;
	}

	public List loadAllRoles(Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List loadAllRolesF(Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List loadAllRoles(PoClient client, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveRole(PoRole role) {
		// TODO Auto-generated method stub

	}

	public List findRoleByName(String key) {
		List ret = new ArrayList();
		ret.add((PoRole)this.roleMapByName.get(key));
		return ret;
	}

	public PoRole findRoleByName(String key, PoClient client) {
		PoRole role =(PoRole)this.roleMapByName.get(key); 
		return role;
	}

	public List findRoleByName(String key, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRole findRoleByName(String key, PoClient client, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteRole(PoRole role, boolean force) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushRole(PoRole role) {
		// TODO Auto-generated method stub

	}

	public PoRoleCompetenceAll getRoleHolder(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson getRoleHolderPerson(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup getRoleHolderGroup(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRHCompetencePerson(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRHCompetenceGroup(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRH(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public PoPerson getRHCompetencePerson(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup getRHCompetenceGroup(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return null;
	}

	public List loadAllRoleHolders() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveRoleHolder(PoRoleCompetenceAll roleholder) {
		// TODO Auto-generated method stub

	}

	public List findRolesOfPerson(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRolesOfOrgStructureF(PoOrgStructure orgStructure, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findCompetenceGroupsOfPerson(PoPerson person, PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findCompetenceGroupsOfPerson(PoPerson person, PoRole role,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findCompetencePersonsOfPerson(PoPerson person, PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findCompetencePersonsOfPerson(PoPerson person, PoRole role,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRolesOfPerson(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRH(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRH(PoRole role, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPerson(PoPerson person, String uid, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPerson(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPersonF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPersonF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findPersonGroupsF(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroup(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroupF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroup(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroupF(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroupF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHCGroup(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHCPerson(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteRoleHolder(PoRoleCompetenceAll roleholder) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushRoleHolder(PoRoleCompetenceAll roleholder) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushRoleHolderPersonLink(
			PoRoleHolderPerson rhpersonlink) {
		// TODO Auto-generated method stub

	}

	public void deleteAndFlushRoleHolderGroupLink(PoRoleHolderGroup rhGroupLink) {
		// TODO Auto-generated method stub

	}

	public void removeGroupFromRole(PoRoleHolderGroup roleHolderGroup) {
		// TODO Auto-generated method stub

	}

	public void changeValidityRHPersonLink(PoRoleHolderPerson rhpersonlink,
			Date validfrom, Date validto) {
		// TODO Auto-generated method stub

	}

	public void changeValidityRHGroupLink(PoRoleHolderGroup rhgrouplink,
			Date validfrom, Date validto) {
		// TODO Auto-generated method stub

	}

	public List getRoleHoldersForCompetencePerson(PoRole role, PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findAuthority(PoGroup controlledGroup, PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findAuthority(PoGroup controlledGroup, PoRole role, Date date,
			int minAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findAuthority(PoPerson controlledPerson, PoRole role) {
		List ret = new ArrayList();
		ret.add(this.orgService.findPersonByUserName("wef"));
		ret.add(this.orgService.findPersonByUserName("bos"));
		return ret;
	}

	public List findAuthority(PoPerson controlledPerson, PoRole role,
			Date date, int minAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPersonWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHWithCompetenceForPerson(PoPerson controlledPerson,
			Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHWithCompetenceForGroup(PoGroup controlledGroup, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRHPersonWithCompetence4Person(PoRole role,
			PoPerson person, PoPerson controlledPerson, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRHPersonWithCompetence4Group(PoRole role,
			PoPerson person, PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRHPersonWithCompetence4All(PoRole role,
			PoPerson person, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHPersonWithCompetence4AllF(PoRole role, PoPerson person,
			Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRHGroupWithCompetence4AllF(PoRole role, PoGroup group,
			Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRHGroupWithCompetence4Person(PoRole role,
			PoGroup group, PoPerson controlledPerson, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRHGroupWithCompetence4Group(PoRole role,
			PoGroup group, PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRHGroupWithCompetence4All(PoRole role,
			PoGroup group, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findPersonsWithCompetence4Group(PoRole role,
			PoGroup controlledGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findPersonsWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removePersonFromRole(PoRoleHolderPerson roleHolderPerson) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param orgService The orgService to set.
	 */
	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoRoleService#findRoleByParticipantId(java.lang.String)
     */
    public PoRole findRoleByParticipantId(String key) {
        return this.roleMapByName.get(key);
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoRoleService#findRoleByParticipantId(java.lang.String, java.util.Date)
     */
    public PoRole findRoleByParticipantId(String key, Date effectiveDate) {
        return this.findRoleByParticipantId(key);
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoRoleService#findRoleByParticipantId(java.lang.String, at.workflow.webdesk.po.model.PoClient)
     */
    public PoRole findRoleByParticipantId(String key, PoClient client) {
    	return this.findRoleByParticipantId(key); 
    }

    /* (non-Javadoc)
     * @see at.workflow.webdesk.po.PoRoleService#findRoleByParticipantId(java.lang.String, at.workflow.webdesk.po.model.PoClient, java.util.Date)
     */
    public PoRole findRoleByParticipantId(String key, PoClient client, Date effectiveDate) {
    	return this.findRoleByParticipantId(key);
    }

    public List findRHCompetenceGroupsOfPerson(PoPerson person, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHCompetencePersonsOfPerson(PoPerson person, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHCompetencePersonsOfGroup(PoGroup group, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHCompetenceGroupsOfGroup(PoGroup group, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findCompetencePersonsOfGroup(PoGroup group, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findCompetenceGroupsOfGroup(PoGroup group, PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHPersonWithAction(PoPerson person, Date date, PoAction action) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRolesOfGroup(PoGroup group, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object findPerformerOfDummyRole(PoRole role, Date effectiveDate) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHPersonWithCompetence4Person(PoRole role, PoPerson person, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHGroupWithCompetence4Group(PoRole r, PoGroup group, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public List findAllRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    public List findRHPerson(PoRole role, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteRoleHolder(PoRoleCompetenceBase roleholder) {
        // TODO Auto-generated method stub
        
    }

	public boolean hasPersonRoleAssigned(PoPerson p, PoRole r, Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPersonRoleAssignedWithCompetence4All(PoPerson p, PoRole r, Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	public List findRHPersonsWithRoleF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteRoleCompetence(PoRoleCompetenceBase roleholder) {
		// TODO Auto-generated method stub
		
	}

	public List findRolderHolderGroupF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetenceAll(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetenceGroup(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetenceGroupWithCompetence4Group(PoRole r, PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetenceGroupsOfGroup(PoGroup group, PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetenceGroupsOfPerson(PoPerson person, PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetencePerson(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetencePersonsOfGroup(PoGroup group, PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetencePersonsOfPerson(PoPerson person, PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolder(PoRole role, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroup(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroup(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupF(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4All(PoRole role, PoGroup group, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupWithCompetence4AllF(PoRole role, PoGroup group, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4Group(PoRole role, PoGroup group, PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupWithCompetence4Group(PoRole role, PoGroup group, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4Person(PoRole role, PoGroup group, PoPerson controlledPerson, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}


	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4All(PoRole role, PoPerson person, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonWithCompetence4AllF(PoRole role, PoPerson person, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonWithCompetence4Group(PoRole role, PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonWithCompetence4Person(PoRole role, PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(PoRole role, PoPerson person, PoPerson controlledPerson, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonsWithRoleF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderWithCompetenceForGroup(PoGroup controlledGroup, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderWithCompetenceForPerson(PoPerson controlledPerson, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRoleHolerPersonWithCompetence4Group(PoRole role, PoPerson person, PoGroup controlledGroup, Date referenceDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoGroup getRoleCompetenceGroup(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoPerson getRoleCompetencePerson(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRoleCompetenceAll(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRoleCompetenceGroup(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRoleCompetencePerson(PoRoleCompetenceBase rhb) {
		// TODO Auto-generated method stub
		return false;
	}

	public void saveRoleCompetence(PoRoleCompetenceBase roleCompetence) {
		// TODO Auto-generated method stub
		
	}

	public PoRoleCompetenceAll getRoleCompetenceAll(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleCompetence(PoRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRolesForClient(PoClient client, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteDeputy(PoRoleDeputy roleDeputy) {
		// TODO Auto-generated method stub
		
	}

	public List findDirectlyLinkedRolesOfPerson(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findDirectlyLinkedRolesOfPersonF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleDeputiesOfPerson(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRolesOfPersonF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}


	public void updateDeputy(PoRoleDeputy roleDeputy) {
		// TODO Auto-generated method stub
		
	}

	public PoRoleDeputy generateDeputy(PoRole role, PoPerson officeHolder, PoPerson deputy, Date validFrom, Date validTo) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleDeputy saveDeputy(PoRoleDeputy roleDeputy) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupWithCompetence4GroupF(PoRole role, PoGroup officeHolder, PoGroup target, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderGroupWithCompetence4PersonF(PoRole role, PoGroup officeHolder, PoPerson target, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, PoGroup target, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, PoPerson target, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoleDeputiesOfPersonF(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4All(PoRole role, PoPerson person, Date referenceDate, boolean deputy) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(PoRole role, PoPerson person, PoPerson controlledPerson, Date referenceDate, boolean deputy) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderPerson findRoleHolerPersonWithCompetence4Group(PoRole role, PoPerson person, PoGroup controlledGroup, Date referenceDate, boolean deputy) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findDistinctRoleHolderGroupsWithCompetence4Group(PoRole role, PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findDistinctRoleHolderGroupsWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findAllActiveRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveRoleHolderPerson(PoRoleHolderPerson rhp) {
		// TODO Auto-generated method stub
		
	}

	public void saveRoleHolderGroup(PoRoleHolderGroup rhg) {
		// TODO Auto-generated method stub
		
	}

	public void assignDynamicRoleHolder(int dynamicType, PoRole role,
			String target, String targetId, Date validFrom, Date validTo) {
		// TODO Auto-generated method stub
		
	}

	public void deleteAndFlushRoleHolderDynamic(PoRoleHolderDynamic rhd) {
		// TODO Auto-generated method stub
		
	}

	public List findRoleHolderDynamicF(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleHolderDynamic getRoleHolderDynamic(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveRoleHolderDynamic(PoRoleHolderDynamic rhd) {
		// TODO Auto-generated method stub
		
	}

	public List findRoleCompetences(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoles(PoPerson person, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRoles(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List findRolesF(PoGroup group, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public PoRoleCompetenceBase getRoleCompetenceBase(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoRoleHolderPerson> findRoleHolderPersonAll(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoRoleHolderPerson> findRoleHolderPerson(PoRole role, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoGroup group) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public PoRole findRoleByNameAndGivenOrNullClient(String roleName, PoClient client) {
		return null;
	}

	public boolean hasPersonRole(PoPerson person, String roleName) {
		return true;
	}
	
	public List<PoPerson> findRoleHolders(PoRole role) {
		return null;
	}

	/** {@inheritDoc} */
	public void assignRoleWithClientCompetence(PoRole role, PoPerson person, PoClient client, Date validFrom, Date validTo, int ranking) {
	}

	/** {@inheritDoc} */
	public void assignRoleWithClientCompetence(PoRole role, PoGroup group, PoClient client, Date validFrom, Date validTo, int ranking) {
	}

	/** {@inheritDoc} */
	public boolean isRoleCompetenceClient(PoRoleCompetenceBase rhb) {
		return false;
	}

	/** {@inheritDoc} */
	public PoClient getRoleCompetenceClient(PoRoleCompetenceBase rhb) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleCompetenceClient> findRoleCompetenceClientsOfPerson(PoPerson person, PoRole role, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleCompetenceClient> findRoleCompetenceClientsOfGroup(PoGroup group, PoRole role, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoClient> findCompetenceClientsOfPerson(PoPerson person, PoRole role, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoClient> findCompetenceClientsOfGroup(PoGroup group, PoRole role, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleCompetenceClient> findRoleCompetenceClient(PoRole role) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findAuthority(PoClient client, PoRole role) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findAuthority(PoClient client, PoRole role, Date date, int minAmount) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Client(PoRole role, PoClient client, Date referenceDate) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleHolderLink> findRoleHolderWithCompetenceForClient(PoClient client, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Client(PoRole role, PoPerson person, PoClient client, Date referenceDate) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Client(PoRole role, PoClient client, Date referenceDate) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findPersonsWithCompetence4Client(PoRole role, PoClient client) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoPerson> findPersonsWithCompetence4Client(PoRole role, PoClient client, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleCompetenceClient> findRoleCompetenceClientWithCompetence4Client(PoRole r, PoClient client, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4ClientF(PoRole role, PoPerson officeHolder, PoClient target, Date date) {
		return null;
	}

	/** {@inheritDoc} */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4ClientF(PoRole role, PoGroup officeHolder, PoClient target, Date date) {
		return null;
	}

	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
