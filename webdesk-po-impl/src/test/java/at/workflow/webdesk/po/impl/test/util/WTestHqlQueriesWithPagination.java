package at.workflow.webdesk.po.impl.test.util;

import java.util.Collections;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class WTestHqlQueriesWithPagination extends
		AbstractTransactionalSpringHsqlDbTestCase {

	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/testdata.xml", false) };
	}
	
	public void testPersonQueryContainedInClient() {
		
		PoOrganisationService orgService = (PoOrganisationService) getBean("PoOrganisationService");
		PoUtilService utilService = (PoUtilService) getBean("PoUtilService");
		
		String hql = "select distinct p.UID, p.lastName, p.firstName, p.client.name, pg.group.shortName, " + 
				" p.userName, p.taID, p.employeeId, p.activeUser, p.validfrom, p.email  from PoPerson p "+
				" join p.memberOfGroups as pg " + 
				" where " + 
				" p.validfrom<=current_timestamp() " + 
				" and p.validto>current_timestamp() " +
				" and pg.validfrom<=current_timestamp() " + 
				" and pg.validto>current_timestamp() " +
				" and pg.group.validfrom<=current_timestamp() " + 
				" and pg.group.validto>current_timestamp() " +                         
				" and pg.group.orgStructure.orgType=1 " +
				" and (p.UID in (:PERSONUIDS) or " + 
				" pg.group.UID in (:GROUPUIDS) or " + 
				" p.client.UID in (:CLIENTUIDS))";
		
		
		String[] params = {"PERSONUIDS", "GROUPUIDS", "CLIENTUIDS" };
		Object[] values = new Object[] { Collections.EMPTY_LIST, Collections.EMPTY_LIST, orgService.loadAllClients().get(0).getUID() };
		
		FilterAndSortPaginationCursor cursor = utilService.createQueryPaginationCursor(hql, params, values, 2);
		
		assertTrue(cursor.getElements().size()==2);
	}
	
}
