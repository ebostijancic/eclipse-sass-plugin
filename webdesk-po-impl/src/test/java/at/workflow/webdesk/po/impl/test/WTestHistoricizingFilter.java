package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.daos.PoPersonGroupDAOImpl;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.HistoricizingFilter;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * @author sdzuban 16.05.2013
 */
public class WTestHistoricizingFilter extends AbstractTransactionalSpringHsqlDbTestCase {
	
	private PoPersonGroupDAOImpl dao;
	private PoOrganisationService orgService;
	
	private PoClient client;
	private PoOrgStructure orgStructure;
	private PoPerson wef;
	
	private List<PoPersonGroup> fromDB = new ArrayList<PoPersonGroup>();
	private List<PoPersonGroup> tested = new ArrayList<PoPersonGroup>();
	
	private HistoricizingFilter<PoPersonGroup> filter;
	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}

	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (dao == null) {
			dao = (PoPersonGroupDAOImpl) getBean("PoPersonGroupDAO");
			filter = new HistoricizingFilter<PoPersonGroup>(dao);
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
		}
		
		wef = orgService.findPersonByUserName("wef");
		client = wef.getClient();
		orgStructure = orgService.getOrgHierarchy(client);
		
		fromDB = orgService.findPersonGroupsAll(wef, orgStructure);
		tested.clear();
	}
	
	public void testSame() {

		tested.addAll(fromDB);
		
		filter.historicizeDeleted(fromDB, tested);
		
		fromDB = orgService.findPersonGroupsAll(wef, orgStructure);
		assertEquals(1, fromDB.size());
		assertEquals(DateTools.INFINITY, fromDB.get(0).getValidto());
	}

	public void testSamePlusNew() {
		
		tested.addAll(fromDB);
		tested.add(new PoPersonGroup());
		
		filter.historicizeDeleted(fromDB, tested);
		
		fromDB = orgService.findPersonGroupsAll(wef, orgStructure);
		assertEquals(1, fromDB.size());
		assertEquals(DateTools.INFINITY, fromDB.get(0).getValidto());
	}
	
	public void testDeleted() throws Exception {
		
		// the link will be deleted now because it started on today() and is ended yesterday()
		filter.historicizeDeleted(fromDB, tested);
		
		fromDB = orgService.findPersonGroupsAll(wef, orgStructure);
		assertEquals(0, fromDB.size());
	}
	
}
