package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * Ensure that a person that is chief of one group and deputy in another group,
 * and the chief of the other group is deputy in one group, can not approve itself.
 * 
 * It implements the test specification on
 * <pre>
 *   http://intranet/intern/ifwd_mgm.nsf/0/3183889B2B1B98E1C12578AF0029E7A3?OpenDocument
 *   notes://Miraculix/intern/ifwd_mgm.nsf/0/3183889B2B1B98E1C12578AF0029E7A3?EditDocument
 * </pre>
 * with the difference that Weiss and Müller have changed places.
 * 
 * @author fritzberger 27.06.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenCircularDeputiesInDifferentDepartments extends AbstractPoRoleServiceAuthorityTest {

	private PoGroup subDepartment1, subDepartment2;
	
	private PoPerson fasching, binder;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		subDepartment1 = createGroup("sub-department-1");
		organisationService.setParentGroup(subDepartment1, topDepartment);
		
		subDepartment2 = createGroup("sub-department-2");
		organisationService.setParentGroup(subDepartment2, topDepartment);
		
		// Fasching belongs to sub-department-1
		fasching = createPerson("fasching", "Sabrina", "Fasching", "sfasching", subDepartment1);
		// and is chief in sub-department-1
		roleService.assignRoleWithGroupCompetence(chiefRole, fasching, subDepartment1, null, null, 1);
		
		// Binder belongs to sub-department-2
		binder = createPerson("binder", "Franz", "Binder", "fbinder", subDepartment2);
		// and is chief in sub-department-2
		roleService.assignRoleWithGroupCompetence(chiefRole, binder, subDepartment2, null, null, 1);
		
		// deputies:
		// Fasching is deputy in sub-department-2
		roleService.assignRoleWithGroupCompetence(chiefRole, fasching, subDepartment2, null, null, 2);
		// Binder is deputy in sub-department-1
		roleService.assignRoleWithGroupCompetence(chiefRole, binder, subDepartment1, null, null, 2);
	}

	public void testFindAuthorityForFasching() {
		// assuming both self-approval and approval-by-deputy-of-petitioner are disallowed
		List<PoPerson> ret = roleService.findAuthority(fasching, chiefRole);
		assertTrue(ret.size() == 1 && ret.get(0).equals(mueller));
	}

}
