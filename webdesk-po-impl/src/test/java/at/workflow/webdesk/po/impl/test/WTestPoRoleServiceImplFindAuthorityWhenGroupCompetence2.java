package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * This test ensures the constellation described in (3) of Lotus ticket:
 * http://intranet/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?OpenDocument 
 * notes://Miraculix/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?EditDocument
 * 
 * @author fritzberger 12.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenGroupCompetence2 extends AbstractPoRoleServiceAuthorityTest {

	private PoPerson berger, bayer, bauer;
	 
	private PoGroup middleDepartment, subDepartment;
	 
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		// Gruppe anlegen
		middleDepartment = createGroup("middle-department");
		subDepartment = createGroup("sub-department");
		// Hierarchie bilden
		organisationService.setParentGroup(middleDepartment, topDepartment);
		organisationService.setParentGroup(subDepartment, middleDepartment);
		
		// Personen anlegen
		berger = createPerson("berger", "Hans", "Berger", "hberger", subDepartment);
		bayer = createPerson("bayer", "Ernst", "Bayer", "ebayer", subDepartment);
		bauer = createPerson("bauer", "Otto", "Bauer", "obauer", subDepartment);
		
		// Role assignments
		// Berger is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, berger, subDepartment, null, null, 1);
		// Bayer is deputy-chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, bayer, subDepartment, null, null, 2);
	}


	public void testFindAuthorityForPersonBerger() {
		List<PoPerson> ret = roleService.findAuthority(berger, chiefRole);
		assertTrue(ret.size() == 1 && ret.get(0).equals(mueller));
	}
	
	public void testFindAuthorityForPersonBayer() {
		List<PoPerson> ret = roleService.findAuthority(bayer, chiefRole);
		assertTrue(ret.size() == 2 && ret.get(0).equals(berger) && ret.get(1).equals(mueller));
	}
	
	public void testFindAuthorityForPersonBauer() {
		List<PoPerson> ret = roleService.findAuthority(bauer, chiefRole);
		assertTrue(ret.size() == 3 && ret.get(0).equals(berger) && ret.get(1).equals(bayer) && ret.get(2).equals(mueller));
	}
	
}
