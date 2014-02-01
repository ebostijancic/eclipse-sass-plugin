package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * Ensure that a person that is deputy of the chief of the group above him can not approve itself.
 * 
 * @author fritzberger 16.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenDeputyOnSeveralLevels extends AbstractPoRoleServiceAuthorityTest {

	private PoGroup subDepartment;
	
	private PoPerson hauser;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		subDepartment = createGroup("sub-department");
		organisationService.setParentGroup(subDepartment, topDepartment);
		
		hauser = createPerson("hauser", "Hans", "Hauser", "hhauser", subDepartment);
		
		// Hauser is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, hauser, subDepartment, null, null, 1);
		// Hauser is deputy in top-department
		roleService.assignRoleWithGroupCompetence(chiefRole, hauser, topDepartment, null, null, 2);
	}

	public void testFindAuthorityForHauser() {
		// assuming both selfApproval and deputyApproval are disallowed
		List<PoPerson> ret = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret.size() == 1 && ret.get(0).equals(mueller));
		
		final boolean disallowApprovalByDeputy = chiefRole.isDoNotAllowApprovalByDeputy();
		final boolean disallowSelfApproval = chiefRole.isDoNotAllowSelfApproval();
		
		chiefRole.setDoNotAllowApprovalByDeputy(false);
		roleService.saveRole(chiefRole);
		// ... nevertheless Hauser can not approve himself because even if he
		// is present as deputy, the stronger self-approval flag removes him
		List<PoPerson> ret1 = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret1.size() == 1 && ret1.get(0).equals(mueller));
		
		chiefRole.setDoNotAllowSelfApproval(false);
		roleService.saveRole(chiefRole);
		// ... now Hauser can approve himself because both flags are false 
		List<PoPerson> ret2 = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret2.size() == 2 && ret2.get(0).equals(hauser) && ret2.get(1).equals(mueller));
		
		// when self-approval is allowed but approval as deputy not ...
		/*
		 * This is a case that is prevented be the UI, so no test is needed for it.
		 * 
		chiefRole.setDoNotAllowApprovalByDeputy(true);
		roleService.saveRole(chiefRole);
		List<PoPerson> ret3 = roleService.findAuthority(hauser, chiefRole);
		assertTrue(ret3.size() == 2 && ret3.get(0).equals(hauser) && ret3.get(0).equals(mueller));
		 */
		
		chiefRole.setDoNotAllowApprovalByDeputy(disallowApprovalByDeputy);
		chiefRole.setDoNotAllowSelfApproval(disallowSelfApproval);
		roleService.saveRole(chiefRole);
	}

}
