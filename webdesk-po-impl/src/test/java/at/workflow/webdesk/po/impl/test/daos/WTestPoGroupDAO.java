/**
 * 
 */
package at.workflow.webdesk.po.impl.test.daos;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban
 *
 */
public class WTestPoGroupDAO extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoOrganisationService orgService;
	private PoGroupDAO groupDao;
	private PoClient client;
	private PoGroup group;
	private Date now;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (orgService == null) {
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
			groupDao = (PoGroupDAO) getBean("PoGroupDAO");
		}
		
		now = new Date();
		
		client = new PoClient();
		client.setName("client");
		orgService.saveClient(client);
		
		PoOrgStructure orgStructure = new PoOrgStructure();
		orgStructure.setClient(client);
		orgStructure.setHierarchy(true);
		orgStructure.setAllowOnlySingleGroupMembership(true);
		orgStructure.setName("orgStructure");
		orgStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		orgService.saveOrgStructure(orgStructure);
		
		group = new PoGroup();
		group.setClient(client);
		group.setOrgStructure(orgStructure);
		group.setName("group");
		group.setShortName("g");
		group.setValidfrom(DateTools.dateOnly(now));
		group.setValidto(DateTools.lastMomentOfDay(now));
		orgService.saveGroup(group);
	}
	
	public void testResolveViewPermissionsToGroups() {
		
		Map<String, List<String>> permissions = new HashMap<String, List<String>>();
		
		List<PoGroup> result = groupDao.resolveViewPermissionsToGroups(permissions, now);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		permissions.put(PoActionPermissionService.CLIENTS, Arrays.asList("nonsense"));
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, now);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		permissions.put(PoActionPermissionService.CLIENTS, Arrays.asList(client.getUID()));
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, now);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(group, result.get(0));

		permissions.clear();
		permissions.put(PoActionPermissionService.GROUPS, Arrays.asList("nonsense"));
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, now);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		permissions.put(PoActionPermissionService.GROUPS, Arrays.asList(group.getUID()));
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, now);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(group, result.get(0));
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, DateTools.yesterday());
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = groupDao.resolveViewPermissionsToGroups(permissions, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(0, result.size());
		
	}
}
