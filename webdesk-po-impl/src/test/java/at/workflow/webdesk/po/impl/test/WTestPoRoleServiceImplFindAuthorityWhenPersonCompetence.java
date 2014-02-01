package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * This test ensures the constellation described in (2) of Lotus ticket:
 * http://intranet/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?OpenDocument 
 * notes://Miraculix/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?EditDocument
 * 
 * @author fritzberger 12.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenPersonCompetence extends AbstractPoRoleServiceAuthorityTest {

	private PoPerson personAuer, personAnders;
	 
	private PoGroup subDepartment;
	 
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		// Gruppe anlegen
		subDepartment = createGroup("sub-department");
		// Hierarchie bilden
		organisationService.setParentGroup(subDepartment, topDepartment);
		
		// Personen anlegen
		personAuer = createPerson("auer", "Herbert", "Auer", "hauer", subDepartment);
		personAnders = createPerson("anders", "Andreas", "Anders", "aanders", subDepartment);
		
		// Role assignments
		// Auer is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, personAuer, subDepartment, null, null, 1);
		// Anders is Auer's personal chief concerning request approvals, though they are both in sub-department
		roleService.assignRoleWithPersonCompetence(chiefRole, personAnders, personAuer, null, null, 1);
	}


	public void testFindAuthorityForPersonAuer() {
		List<PoPerson> ret = roleService.findAuthority(personAuer, chiefRole);
		assertTrue(ret.size() == 2  && ret.get(0).equals(personAnders) && ret.get(1).equals(mueller));
	}
	
	public void testFindAuthorityForPersonAnders() {
		List<PoPerson> ret = roleService.findAuthority(personAnders, chiefRole);
		assertTrue(ret.size() == 2  && ret.get(0).equals(personAuer) && ret.get(1).equals(mueller));
	}
	
}
