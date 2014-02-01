package at.workflow.webdesk.po.impl.test;

import java.util.List;

import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * Ensure that a deputy that is in a sub-group is authorized like any other deputy.
 * 
 * @author fritzberger 16.05.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenDeputyInSubGroup extends AbstractPoRoleServiceAuthorityTest {

	private PoGroup subDepartment, subSubDepartment;
	
	private PoPerson steiner, seiler;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		// Gruppen anlegen
		subDepartment = createGroup("sub-department");
		subSubDepartment = createGroup("sub-sub-department");
		// Hierarchie bilden
		organisationService.setParentGroup(subDepartment, topDepartment);
		organisationService.setParentGroup(subSubDepartment, subDepartment);
		
		// Personen anlegen
		steiner = createPerson("steiner", "Andreas", "Steiner", "asteiner", subDepartment);
		seiler = createPerson("seiler", "Toni", "Seiler", "tseiler", subSubDepartment);
		
		// Role assignments
		// Steiner is first chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, steiner, subDepartment, null, null, 1);
		// Sailer is his deputy, but in sub-sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, seiler, subDepartment, null, null, 2);
	}

	public void testFindAuthorityForSteiner() {
		List<PoPerson> ret = roleService.findAuthority(steiner, chiefRole);
		assertTrue(ret.size() == 1 && ret.get(0).equals(mueller));
	}

	public void testFindAuthorityForSteiner2() {
		final boolean doNotAllowApprovalByDeputy = chiefRole.isDoNotAllowApprovalByDeputy();
		chiefRole.setDoNotAllowApprovalByDeputy(false);
		roleService.saveRole(chiefRole);
		
		List<PoPerson> ret = roleService.findAuthority(steiner, chiefRole);
		assertTrue(ret.size() == 2  && ret.get(0).equals(seiler) && ret.get(1).equals(mueller));
		
		chiefRole.setDoNotAllowApprovalByDeputy(doNotAllowApprovalByDeputy);
		roleService.saveRole(chiefRole);
	}

	public void testFindAuthorityForSeiler() {
		List<PoPerson> ret = roleService.findAuthority(seiler, chiefRole);
		assertTrue(ret.size() == 2  && ret.get(0).equals(steiner) && ret.get(1).equals(mueller));
	}

}
