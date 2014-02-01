package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.testing.PoTestdataGenerator;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataHibernate;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Tests some PO fields for maximum length and nullability.
 * 
 * @author fritzberger 20.10.2010
 */
public class WTestPersistenceMetadata extends AbstractTransactionalSpringHsqlDbTestCase {

	private PersistenceMetadata metaData;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		new PoTestdataGenerator().createPerson();
		metaData = new PersistenceMetadataHibernate();
	}
	
	public void testColumnLength()	{
		assertEquals(80, metaData.getMaximumLength(PoPerson.class, "lastName"));
	}
	
	public void testNullable()	{
		assertFalse(metaData.isNullable(PoPerson.class, "lastName"));
		assertTrue(metaData.isNullable(PoPerson.class, "email"));
	}
	
	public void testTransient()	{
		assertTrue(metaData.isMapped(PoPerson.class, "lastName"));
		assertFalse(metaData.isMapped(PoPerson.class, "workflowId"));
	}
	
	public void testColumnNameToPropertyName()	{
		assertEquals("CLIENT_UID", metaData.getAttributeName(PoPerson.class, "client"));
		assertEquals("client", metaData.getPropertyName(PoPerson.class, "CLIENT_UID"));
	}

	public void testGetMappedByPropertyName()	{
		assertEquals("person", metaData.getMappedByPropertyName(PoPerson.class, "memberOfGroups"));
		assertEquals("officeHolder", metaData.getMappedByPropertyName(PoPerson.class, "deputies"));
	}
	
	public void testUniqueProperty()	{
		assertTrue(metaData.isUniqueProperty(PoClient.class, "name"));
		
		assertFalse(metaData.isUniqueProperty(PoClient.class, "description"));
		assertFalse(metaData.isUniqueProperty(PoPerson.class, "userName"));	// this is unique, but checked only by service.save() because it's temporal
		assertFalse(metaData.isUniqueProperty(PoPerson.class, "lastName"));
		
		try	{
			metaData.isUniqueProperty(PoPerson.class, "xxx");
			fail("There is no property 'xxx' in PoPerson, this should throw an exception!");
		}
		catch (IllegalArgumentException e)	{
			// is expected here
		}
	}
	
}
