package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * @author fritzberger 27.10.2010
 */
abstract class AbstractPoPasswordTestCase extends AbstractTransactionalSpringHsqlDbTestCase {
	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}
	
	protected final PoPerson readPerson()	{
		PoPersonDAO personDAO = (PoPersonDAO) getBean("PoPersonDAO");
		final String personUsername = "wef";
		PoPerson person = personDAO.findPersonByUserName(personUsername);
		assertTrue("Person '"+personUsername+"' was not loaded from XML!", person != null);
		return person;
	}
	
	protected final PoPasswordService getPasswordService()	{
		return (PoPasswordService) getBean("PoPasswordService");
	}


}
