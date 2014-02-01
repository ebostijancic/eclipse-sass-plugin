package at.workflow.webdesk.po.impl.helper;

import java.util.List;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * As of unit test fails due to missing imported data on 2013-12-09 I wrote this test.
 * 
 * @author fritzberger 09.12.2013
 */
public class WTestPoDataGenerator extends AbstractTransactionalSpringHsqlDbTestCase
{
	/** Globally available test data resource at "at/workflow/webdesk/po/impl/test/data/TestData.xml". */
	public static final String TESTDATA_RESOURCEPATH = "at/workflow/webdesk/po/impl/test/data/TestData.xml";
	
	/** Number of persons in TESTDATA_RESOURCEPATH. */
	public static final int PERSONS_COUNT = 99;
	
	/** Number of groups in TESTDATA_RESOURCEPATH. */
	public static final int GROUPS_COUNT = 27;
	
	/** Number of clients in TESTDATA_RESOURCEPATH. */
	public static final int CLIENTS_COUNT = 1;
	
	/** Number of organizational structures in TESTDATA_RESOURCEPATH. */
	public static final int ORGSTRUCTURES_COUNT = 1;
	
	/** Number of parent group relations in TESTDATA_RESOURCEPATH. */
	public static final int PARENTGROUPS_COUNT = 26;
	
	/** Tests if PoDataGenerator really works. */
	public void testDataGenerator() throws Exception	{
		DataGenerator generator = new PoDataGenerator(TESTDATA_RESOURCEPATH, false);
		generator.create(getApplicationContext());
		
		final PoOrganisationService orgService = (PoOrganisationService) getBean("PoOrganisationService");
		
		final List<PoPerson> persons = orgService.loadAllPersons();
		assertEquals(PERSONS_COUNT, persons.size());
		
		final List<PoGroup> groups = orgService.loadAllGroups();
		assertEquals(GROUPS_COUNT, groups.size());
		
		final List<PoClient> clients = orgService.loadAllClients();
		assertEquals(CLIENTS_COUNT, clients.size());
		
		final List<PoOrgStructure> orgStructures = orgService.loadAllOrgStructures();
		assertEquals(ORGSTRUCTURES_COUNT, orgStructures.size());
		
		final List<PoParentGroup> parentGroups = orgService.loadAllParentGroups();
		assertEquals(PARENTGROUPS_COUNT, parentGroups.size());
	}

}
