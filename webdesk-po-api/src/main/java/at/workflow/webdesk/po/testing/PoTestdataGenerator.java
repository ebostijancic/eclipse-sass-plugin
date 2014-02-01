package at.workflow.webdesk.po.testing;

import java.util.Random;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.daos.PoClientDAO;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.daos.PoOrgStructureDAO;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * Provides basic unit-test data for PO tables like client, org-structure, groups.
 * Getter methods will cahce their firstly created value, create methods will always create a new instance.
 * <p />
 * Can not do this with <code>GroupAndPersonGenerator</code> because this uses services to access the database,
 * which includes a transaction that would not be initialized correctly in case of a unit-test.
 * 
 * @author fritzberger 18.10.2010
 */
public class PoTestdataGenerator {
	
	private Random random = new Random();
	
	public final PoClient createClient()	{
		PoClient client = new PoClient();
		client.setName(getRandomString());
		client.setDescription(getRandomString());
		client.setShortName(getRandomString());
		final PoClientDAO dao = (PoClientDAO) WebdeskApplicationContext.getBean("PoClientDAO");
		dao.save(client);
		return client;
	}
	
	public final PoOrgStructure createOrgStructure()	{
		PoOrgStructure orgStructure = new PoOrgStructure();
		orgStructure.setClient(createClient());
		orgStructure.setHierarchy(true);
		orgStructure.setName(getRandomString());
		orgStructure.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		final PoOrgStructureDAO dao = (PoOrgStructureDAO) WebdeskApplicationContext.getBean("PoOrgStructureDAO");
		dao.save(orgStructure);
		return orgStructure;
	}
	
	public final PoGroup createGroup()	{
		final PoGroup group = new PoGroup();
		group.setName(getRandomString());
		group.setDescription(getRandomString());
		group.setShortName(getRandomString());
		group.setClient(createClient());
		group.setOrgStructure(createOrgStructure());
		final PoGroupDAO dao = (PoGroupDAO) WebdeskApplicationContext.getBean("PoGroupDAO");
		dao.save(group);
		return group;
	}
	
	public final PoPerson createPerson()	{
		final PoPerson person = new PoPerson();
		person.setFirstName(getRandomString());
		person.setLastName(getRandomString());
		person.setUserName(getRandomString());
		person.setClient(createClient());
		person.setEmployeeId(getRandomString());
		final PoPersonDAO dao = (PoPersonDAO) WebdeskApplicationContext.getBean("PoPersonDAO");
		dao.save(person);
		return person;
	}

	public String getRandomString() {
		return "s"+random.nextInt();
	}	

}
