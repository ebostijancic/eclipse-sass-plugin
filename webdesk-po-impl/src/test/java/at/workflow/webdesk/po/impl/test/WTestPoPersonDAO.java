package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.impl.daos.PoImageDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoPersonImagesDAOImpl;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban 01.06.2011
 */
public class WTestPoPersonDAO extends AbstractTransactionalSpringHsqlDbTestCase {

	private Date past, beforeStart, start, afterStart, beforeEnd, end, afterEnd, future;
	
    private PoClient myClient;
    private PoGroup myGroup;
    private PoOrgStructure myStructure;

    private PoOrganisationService organisationService;
    private PoGroupDAO groupDao;
    private PoPersonDAO personDao;
    
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {

		super.onSetUpBeforeDataGeneration();

		if (organisationService==null)
			this.organisationService = (PoOrganisationService) getBean("PoOrganisationService");

		if (groupDao == null)
			this.groupDao = (PoGroupDAO) getBean("PoGroupDAO");

		if (personDao == null)
			this.personDao = (PoPersonDAO) getBean("PoPersonDAO");
		
		Calendar cal = GregorianCalendar.getInstance();
		past = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		beforeStart = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		start = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		afterStart = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		beforeEnd = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		end = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		afterEnd = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		future = cal.getTime();
		
        myClient = new PoClient();
		myClient.setName("My Client");
		myClient.setDescription("mydescription");
		organisationService.saveClient(myClient);

		myStructure = new PoOrgStructure();
		myStructure.setName("My Structure");
		myStructure.setHierarchy(true);
		myStructure.setAllowOnlySingleGroupMembership(true);
		myStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		myStructure.setClient(myClient);
		organisationService.saveOrgStructure(myStructure);
		myClient.addOrgStructure(myStructure);
		
		myGroup = new PoGroup();
		myGroup.setName("group");
		myGroup.setShortName("short");
		myGroup.setClient(myClient);
		myGroup.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup);
		myGroup.setValidfrom(past);
		myGroup.setValidto(future);
		myGroup.setDescription("g");
		groupDao.save(myGroup);

	}
	
	public void testFindPersonByTaId() {
		
		PoPerson testPerson = new PoPerson();
		testPerson.setClient(myClient);
		testPerson.setFirstName("Test");
		testPerson.setLastName("Person");
		testPerson.setUserName("tperson");
		testPerson.setEmployeeId("4711");
		testPerson.setTaID("taID");
		testPerson.setValidfrom(start);
		testPerson.setValidto(end); // so that it is valid until end 23:59:59

		organisationService.savePerson(testPerson, myGroup);
		
		PoPerson person = personDao.findPersonByTaId("taID", beforeStart);
		assertNull(person);
		
		person = personDao.findPersonByTaId("taID", start);
		assertNotNull(person);
		assertEquals("tperson", person.getUserName());
		
		person = personDao.findPersonByTaId("taID", afterStart);
		assertNotNull(person);
		assertEquals("tperson", person.getUserName());
		
		person = personDao.findPersonByTaId("taID", beforeEnd);
		assertNotNull(person);
		assertEquals("tperson", person.getUserName());
		
		person = personDao.findPersonByTaId("taID", end);
		assertNotNull(person);
		assertEquals("tperson", person.getUserName());
		
		person = personDao.findPersonByTaId("taID", afterEnd);
		assertNull(person);
	}
	
	
	public void testFindPersonByTaIdWithInterval() {
		
		PoPerson testPerson = new PoPerson();
		testPerson.setClient(myClient);
		testPerson.setFirstName("Test");
		testPerson.setLastName("Person");
		testPerson.setUserName("tperson");
		testPerson.setEmployeeId("4711");
		testPerson.setTaID("taID");
		testPerson.setValidfrom(start);
		testPerson.setValidto(end); // so that it is valid until end 23:59:59
		
		organisationService.savePerson(testPerson, myGroup);
		
		PoPerson result = personDao.findPersonByTaId("taID", past, beforeStart);
		assertNull(result);
		
		result = personDao.findPersonByTaId("taID", past, beforeStart);
		assertNull(result);
		
		result = personDao.findPersonByTaId("taID", past, start);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", past, afterStart);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", start, end);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", afterStart, beforeEnd);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", past, future);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", beforeEnd, future);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", end, future);
		assertNotNull(result);
		assertEquals("tperson", result.getUserName());
		
		result = personDao.findPersonByTaId("taID", afterEnd, future);
		assertNull(result);
	}
	
	public void testFindPersonsOfClient() {
		
		PoPerson testPerson = new PoPerson();
		testPerson.setClient(myClient);
		testPerson.setFirstName("Test");
		testPerson.setLastName("Person");
		testPerson.setUserName("tperson");
		testPerson.setEmployeeId("4711");
		testPerson.setTaID("taID");
		testPerson.setValidfrom(start);
		testPerson.setValidto(end); // so that it is valid until end 23:59:59
		
		organisationService.savePerson(testPerson, myGroup);
		
		List<PoPerson> result = personDao.findPersonsOfClient(myClient, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = personDao.findPersonsOfClient(myClient, start);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, afterStart);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, beforeEnd);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, end);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, afterEnd);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	
	public void testFindPersonsOfClientWithInterval() {
		
		PoPerson testPerson = new PoPerson();
		testPerson.setClient(myClient);
		testPerson.setFirstName("Test");
		testPerson.setLastName("Person");
		testPerson.setUserName("tperson");
		testPerson.setEmployeeId("4711");
		testPerson.setTaID("taID");
		testPerson.setValidfrom(start);
		testPerson.setValidto(end); // so that it is valid until end 23:59:59
		
		organisationService.savePerson(testPerson, myGroup);
		
		List<PoPerson> result = personDao.findPersonsOfClient(myClient, past, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = personDao.findPersonsOfClient(myClient, past, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = personDao.findPersonsOfClient(myClient, past, start);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, past, afterStart);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, start, end);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, afterStart, beforeEnd);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, past, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, beforeEnd, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, end, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = personDao.findPersonsOfClient(myClient, afterEnd, future);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	public void testPersonImagesUniqueness() {
		
		PoPersonImages imgs = new PoPersonImages();
		PoImage img = new PoImage();
		img.setImageBytes("Nonsense".getBytes());
		imgs.setOriginal(img);
		imgs.setImage(img);
		imgs.setThumbnail(img);
		PoImageDAOImpl imgDAO = (PoImageDAOImpl) getBean("PoImageDAO");
		imgDAO.save(img);
		PoPersonImagesDAOImpl imgsDAO = (PoPersonImagesDAOImpl) getBean("PoPersonImagesDAO");
		imgsDAO.save(imgs);
		assertTrue(StringUtils.isNotBlank(imgs.getUID()));
		
		PoPerson person = getNewPerson(1);
		person.setValidto(future); // same as myGroup
		person.setPersonImages(imgs);
		organisationService.savePerson(person, myGroup);
		assertTrue(StringUtils.isNotBlank(person.getUID()));
		assertTrue(StringUtils.isNotBlank(imgs.getUID()));
		
		PoPerson person2 = getNewPerson(2);
		person2.setValidto(future); // same as myGroup
		person2.setPersonImages(imgs);
		
		try {
			organisationService.savePerson(person2, myGroup);
			fail("Saved second person with same person images");
		} catch (PoRuntimeException e) { }
		
		assertTrue(StringUtils.isBlank(person2.getUID()));
		
		person2.setPersonImages(null);
		organisationService.savePerson(person2, myGroup);
		assertTrue(StringUtils.isNotBlank(person2.getUID()));
		
//		updatePerson cannot be tested because PoGeneralDAOImpl.getOriginalObject() returns null
//		because it opens new session
		
//		person2.setPersonImages(imgs);
//		try {
//			organisationService.updatePerson(person2);
//			fail("Updated second person with same person images");
//		} catch (PoRuntimeException e) { }
//		
//		PoPersonImages imgs2 = new PoPersonImages();
//		imgsDAO.save(imgs2);
//		assertTrue(StringUtils.isNotBlank(imgs2.getUID()));
//		person2.setPersonImages(imgs2);
//		organisationService.updatePerson(person2);
//		
//		assertEquals(person2.getPersonImages(), imgs2);
		
		PoPersonImages imgs2 = new PoPersonImages();
		imgs2.setOriginal(img);
		imgs2.setImage(img);
		imgs2.setThumbnail(img);
		imgsDAO.save(imgs2);
		assertTrue(StringUtils.isNotBlank(imgs2.getUID()));
		
		PoPerson person3 = getNewPerson(3);
		person3.setValidfrom(start);
		person3.setValidto(end); // so that it is valid until end 23:59:59
		person3.setPersonImages(imgs2);
		organisationService.savePerson(person3, myGroup);
		
		assertTrue(StringUtils.isNotBlank(person3.getUID()));
		assertEquals(person3.getPersonImages(), imgs2);
	}

	private PoPerson getNewPerson(int idx) {
		
		PoPerson person = new PoPerson();
		person.setClient(myClient);
		person.setUserName("userName" + idx);
		person.setEmployeeId("employeeId" + idx);
		person.setLastName("lastName" + idx);
		person.setFirstName("firstName" + idx);
		return person;
	}
}
