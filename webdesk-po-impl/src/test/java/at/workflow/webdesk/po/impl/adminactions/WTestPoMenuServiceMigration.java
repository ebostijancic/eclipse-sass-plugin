package at.workflow.webdesk.po.impl.adminactions;

import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom.JDOMException;
import org.w3c.dom.Document;

import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoMenuTreeService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class WTestPoMenuServiceMigration extends AbstractTransactionalSpringHsqlDbTestCase {
	private MigrateOldMenuToNewMenu migrationAdminAction;
	
	private PoMenuTreeService menuTreeService;
	
	private PoMenuService menuService;
	
	private PoOrganisationService organisationService;

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		migrationAdminAction = (MigrateOldMenuToNewMenu) getBean("MigrateOldMenuToNewMenu");
		menuTreeService = (PoMenuTreeService) getBean("PoMenuTreeService");
		menuService = (PoMenuService) getBean("PoMenuService");
		organisationService = (PoOrganisationService) getBean("PoOrganisationService");
		
		
	}
	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		logger.info("getDataGenerators()");
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/TestData.xml", true) };
	}

	
	public void testServicesFound() {
		assertNotNull(migrationAdminAction);
		assertNotNull(menuService);
		assertNotNull(menuTreeService);
		assertNotNull(organisationService);
	}

	public void testMigration() throws JDOMException, IOException {
		migrationAdminAction.run();
		
		Document newTreeOfPerson = menuTreeService.getMenuTreeOfPerson("wef");
		Document oldTreeOfPerson = menuService.getMenuTreeOfPerson("wef");
		
		System.out.println(XMLTools.toString(newTreeOfPerson));
		System.out.println(XMLTools.toString(oldTreeOfPerson));
		
		Diff diff = XMLUnit.compareXML(newTreeOfPerson, oldTreeOfPerson);
		
		assertTrue(diff.identical());
	}
}
