package at.workflow.webdesk.po.impl.test;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoConnectorInterface;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.PoAbstractDomainObjectReadingConnector;
import at.workflow.webdesk.po.impl.PoAbstractDomainObjectWritingConnector;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * Tests the abstraction of a domain-object oriented source-connector
 * with persons defined in MinTestData.xml.
 * 
 * @author fritzberger 24.02.2012
 */
public class WTestAbstractDomainSourceConnector extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoOrganisationService organisationService;
	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		organisationService = (PoOrganisationService) getBean("PoOrganisationService");
	}

	/** Reads all persons from service, reads all persons via connector, and compares both results. */
	public void testActionFieldsContainAlsoThoseFromSuperclass() throws Exception {
		PoConnectorInterface connector = new PoAbstractDomainObjectWritingConnector() {
			@Override
			protected Class<? extends PersistentObject> getDomainObjectClass() {
				return PoAction.class;
			}
			@Override
			protected PersistentObject resolveNaturalKeyValueFrom(String beanReferencePropertyName, Object naturalKeyValue) {
				return null;
			}
			@Override
			protected Object getNaturalKeyValueFor(String beanReferencePropertyName, PersistentObject bean) {
				return null;
			}
			@Override
			protected void saveDomainObject(PersistentObject domainObject) {
			}
		};
		
		initConnector(connector);
		
		List<String> fieldNames = connector.getFieldNames();
		assertTrue("The connector did not find inherited properties!", fieldNames.contains("validfrom") && fieldNames.contains("validto"));
	}
	
	/** Reads all persons from service, reads all persons via connector, and compares both results. */
	public void testPersonsAndTheirProperties() throws Exception {
		// reads all persons from service
		final Date now = DateTools.now();
		final List<PoPerson> persons = findAllPersons(now);
		assert persons != null && persons.size() > 0 : "No persons in test data?";
		
		PoAbstractDomainObjectWritingConnector connector = new PoAbstractDomainObjectWritingConnector() {
			@Override
			protected Class<? extends PersistentObject> getDomainObjectClass() {
				return PoPerson.class;
			}
			@Override
			protected PersistentObject resolveNaturalKeyValueFrom(String beanReferencePropertyName, Object naturalKeyValue) {
				return null;
			}
			@Override
			protected Object getNaturalKeyValueFor(String beanReferencePropertyName, PersistentObject bean) {
				return null;
			}
			@Override
			protected void saveDomainObject(PersistentObject domainObject) {
			}
		};
		
		initConnector(connector);
		
		// read all persons via connector
		List<String> fieldNames = connector.getFieldNames();
		List<Map<String,Object>> records = connector.findAllObjects(fieldNames, null);
		assertEquals(persons.size(), records.size());
		
		// compare both results
		for (Map<String,Object> record : records)
			assertInPersons(record, persons);
	}

	private List<PoPerson> findAllPersons(final Date now) {
		return organisationService.findAllPersons(now);
	}

	/** Asserts that the person in passed record is in List of persons and has the same property values as that in List. */
	private void assertInPersons(Map<String,Object> record, List<PoPerson> persons) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String userName = (String) record.get("userName");
		PoPerson person = null;
		for (PoPerson p : persons)	{
			if (userName.equals(p.getUserName()))	{
				if (person != null)
					throw new IllegalStateException("Duplicate userName in test data: "+userName);
				person = p;
			}
		}
		
		assertNotNull("Person not found: "+userName, person);
		
		Map<String,Object> properties = ReflectionUtils.getPropertyValues(person);
		
		for (Map.Entry<String,Object> e : record.entrySet())	{
			String propertyName = e.getKey();
			Object propertyValue = e.getValue();
			
			assertTrue("Person's properties must contain "+propertyName, PoAbstractDomainObjectReadingConnector.SELF_REFERENCE.equals(propertyName) || properties.containsKey(propertyName) || properties.containsKey(propertyName.substring("$".length())));
			
			if ( PoAbstractDomainObjectReadingConnector.SELF_REFERENCE.equals(propertyName)== false) {
				Object value = properties.get(propertyName);
				assertEquals("Person's property '"+propertyName+"' must match that in record of person '"+userName, value, propertyValue);
			}
		}
	}

	private void initConnector(PoConnectorInterface connector) {
		connector.setApplicationContext(getApplicationContext());
		connector.init();
	}

}
