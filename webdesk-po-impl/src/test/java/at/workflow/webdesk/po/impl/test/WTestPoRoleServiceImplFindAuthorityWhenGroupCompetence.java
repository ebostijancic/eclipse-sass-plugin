package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * This test ensures the constellation described in (1) of Lotus ticket:
 * http://intranet/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?OpenDocument 
 * notes://Miraculix/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?EditDocument
 * 
 * @author ggruber 12.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenGroupCompetence extends AbstractPoRoleServiceAuthorityTest {

	private PoPerson personMaier, personMader;
	 
	private PoGroup subDepartment;
	 
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		// Gruppe anlegen
		subDepartment = createGroup("sub-department");
		// Hierarchie bilden
		organisationService.setParentGroup(subDepartment, topDepartment);
		
		// Personen anlegen
		personMaier = createPerson("maier", "Tim", "Maier", "tmaier", subDepartment);
		personMader = createPerson("mader", "Martin", "Mader", "mmader", subDepartment);
		
		// Role assignments
		// Maier is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, personMaier, subDepartment, null, null, 1);
		// Mueller, besides being chief in top-department, is deputy-chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, mueller, subDepartment, null, null, 2);
	}


	public void testFindAuthorityForPersonMader() {
		List<PoPerson> ret = roleService.findAuthority(personMader, chiefRole);
		assertTrue(ret.size() == 2  && ret.get(0).equals(personMaier) && ret.get(1).equals(mueller));
	}
	
	public void testFindAuthorityForPersonMaier() {
		List<PoPerson> ret = roleService.findAuthority(personMaier, chiefRole);
		assertTrue(ret.size() == 1  && ret.get(0).equals(mueller));
	}

}
