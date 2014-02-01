package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRoleDeputy;

/**
 * This test ensures the constellation described in:
 * http://intranet/intern/ifwd_mgm.nsf/0/59AAF7136412BB9CC1257695003D9927?OpenDocument
 * notes://Miraculix/intern/ifwd_mgm.nsf/0/59AAF7136412BB9CC1257695003D9927?EditDocument
 * 
 * @author fritzberger 13.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenOfficeHolderDeputy extends AbstractPoRoleServiceAuthorityTest {

	private PoGroup subDepartment;
	
	private PoPerson hauser, huber, haller;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
    	
		subDepartment = createGroup("sub-department");
		// Hierarchie bilden
		organisationService.setParentGroup(subDepartment, topDepartment);
		
		// Personen anlegen
		hauser = createPerson("hauser", "Johann", "Hauser", "jhauser", subDepartment);
		huber = createPerson("huber", "Franz", "Huber", "fhuber", subDepartment);
		haller = createPerson("haller", "Harald", "Haller", "hhaller", subDepartment);
		
		// Hauser is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, hauser, subDepartment, null, null, 1);
		
		// Huber is personal deputy of Hauser
		PoRoleDeputy deputy = new PoRoleDeputy();
		deputy.setRole(chiefRole);
		deputy.setOfficeHolder(hauser);  // currently active "dynamic" role holder
		deputy.setDeputy(huber);	// personal deputy
		roleService.saveDeputy(deputy);
	}


	public void testFindAuthorityForHauser() {
		List<PoPerson> ret = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret.size() == 1 && ret.get(0).equals(mueller));
	}

	public void testFindAuthorityForHuber() {
		List<PoPerson> ret = roleService.findAuthority(huber, chiefRole);
		assertTrue(ret.size() == 2 && ret.get(0).equals(hauser) && ret.get(1).equals(mueller));
	}

	public void testFindAuthorityForHaller() {
		List<PoPerson> ret = roleService.findAuthority(haller, chiefRole);
		assertTrue(ret.size() == 3 && ret.get(0).equals(hauser) && ret.get(1).equals(huber) && ret.get(2).equals(mueller));
	}

	public void testFindAuthorityWhenApprovalByDeputyForHauser() {
		final boolean disallowApprovalByDeputy = chiefRole.isDoNotAllowApprovalByDeputy();
		// allow approval by deputy, so Huber can approve for Hauser
		chiefRole.setDoNotAllowApprovalByDeputy(false);
		roleService.saveRole(chiefRole);
		
		List<PoPerson> ret = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret.size() == 2 && ret.get(0).equals(huber) && ret.get(1).equals(mueller));
		
		chiefRole.setDoNotAllowApprovalByDeputy(disallowApprovalByDeputy);
		roleService.saveRole(chiefRole);
	}

}
