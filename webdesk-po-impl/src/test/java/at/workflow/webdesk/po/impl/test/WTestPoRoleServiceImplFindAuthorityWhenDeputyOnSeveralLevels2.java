package at.workflow.webdesk.po.impl.test;

import java.util.List;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * Ensure that a person that is deputy of the chief of the group above him can not approve itself.
 * This test has one department more than its sibling test.
 * 
 * It implements the test specification on
 * <pre>
 *   http://intranet/intern/ifwd_mgm.nsf/0/3183889B2B1B98E1C12578AF0029E7A3?OpenDocument
 *   notes://Miraculix/intern/ifwd_mgm.nsf/0/3183889B2B1B98E1C12578AF0029E7A3?EditDocument
 * </pre>
 * with the difference that Weiss and Müller have changed places.
 * 
 * @author fritzberger 14.06.2011
 */
public class WTestPoRoleServiceImplFindAuthorityWhenDeputyOnSeveralLevels2 extends AbstractPoRoleServiceAuthorityTest {

	private PoGroup subDepartment, bottomDepartment;
	
	private PoPerson haider, huber, weiss;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		subDepartment = createGroup("sub-department");
		organisationService.setParentGroup(subDepartment, topDepartment);
		
		bottomDepartment = createGroup("bottom-department");
		organisationService.setParentGroup(bottomDepartment, subDepartment);
		
		// Haider belongs to sub-department
		haider = createPerson("haider", "Hans", "Haider", "hhaider", subDepartment);
		
		// Huber belongs to sub-department
		huber = createPerson("huber", "Johann", "Huber", "huber", subDepartment);
		// and is chief in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, huber, subDepartment, null, null, 1);
		
		// Weiss belongs to bottom-department
		weiss = createPerson("weiss", "Florian", "Weiss", "fweiss", bottomDepartment);
		// and is chief in bottom-department
		roleService.assignRoleWithGroupCompetence(chiefRole, weiss, bottomDepartment, null, null, 1);
		
		// deputies:
		// Haider is deputy chief in top-department
		roleService.assignRoleWithGroupCompetence(chiefRole, haider, topDepartment, null, null, 2);
		// Weiss (chief in bottom-department) is deputy in sub-department
		roleService.assignRoleWithGroupCompetence(chiefRole, weiss, subDepartment, null, null, 2);
		// Huber (chief in sub-department) is deputy in bottom-department
		roleService.assignRoleWithGroupCompetence(chiefRole, huber, bottomDepartment, null, null, 2);
	}

	public void testFindAuthorityForWeiss() {
		// assuming both self-approval and approval-by-deputy-of-petitioner are disallowed
		List<PoPerson> ret = roleService.findAuthority(weiss, chiefRole);
		assertTrue(ret.size() == 3 && ret.get(0).equals(huber) && ret.get(1).equals(mueller) && ret.get(2).equals(haider));
	}

}
