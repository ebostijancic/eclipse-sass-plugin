package at.workflow.webdesk.po.util;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import junit.framework.TestCase;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * @author sdzuban 18.03.2013
 */
public class WTestPoLinkingUtils extends TestCase {

	private Date from;
	private Date to;
	
	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		from = DateTools.toDate(2013, 3, 19);
		to = DateTools.toDate(2015, 03, 19);
	}

	public void testSimpleLink() {

		// this is for illustration of simplest linking possible
		// it does not work in po module because the addXY methods follow no standard
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
//		would throw exception because there is no addPersonGroup method in PoPerson and no in PoGroup either
//		PoLinkingUtils.link(person, group); 
	}
	
	public void testLinkWithCustomMethods() {
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PoLinkingUtils.link(person, group, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		
		PoPersonGroup personsGroup = person.getMemberOfGroups().iterator().next();
		PoPersonGroup groupsPerson = group.getPersonGroups().iterator().next();
		// this is without persistence and without UID so they must be same
		assertTrue(personsGroup == groupsPerson);
		
		assertTrue(person == personsGroup.getPerson());
		assertTrue(group == personsGroup.getGroup());
	}
	
	public void testHistoricizedLink() throws Exception {
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PoLinkingUtils.link(person, group, from, to, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		
		PoPersonGroup personsGroup = person.getMemberOfGroups().iterator().next();
		PoPersonGroup groupsPerson = group.getPersonGroups().iterator().next();
		// this is without persistence and without UID so they must be same
		assertTrue(personsGroup == groupsPerson);
		
		assertTrue(person == personsGroup.getPerson());
		assertTrue(group == personsGroup.getGroup());
		assertEquals(from, personsGroup.getValidfrom());
		assertEquals(getTo(to), personsGroup.getValidto());
	}
	
	public void testHistoricizedLinkWithLinkClass() throws Exception {
		
		PoPerson person = new PoPerson();
		PoGroup group = new PoGroup();
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PoLinkingUtils.link(PoPersonGroup.class, person, group, from, to, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		
		PoPersonGroup personsGroup = person.getMemberOfGroups().iterator().next();
		PoPersonGroup groupsPerson = group.getPersonGroups().iterator().next();
		// this is without persistence and without UID so they must be same
		assertTrue(personsGroup == groupsPerson);
		
		assertTrue(person == personsGroup.getPerson());
		assertTrue(group == personsGroup.getGroup());
		assertEquals(from, personsGroup.getValidfrom());
		assertEquals(getTo(to), personsGroup.getValidto());
	}
	
	public void testHistoricizedLinkWithWrongLinkClass() throws Exception {
		
		PoPerson person = new PoPerson();
		PoGroup group = new PoGroup();

		try {
			PoLinkingUtils.link(PoParentGroup.class, person, group, from, to, "addMemberOfGroup", "addPersonGroup");
			fail("Accepted wrong link class");
		} catch (Exception e) { }
	}
	
	public void testParentChildLink() {
		
		PoGroup parent = new PoGroup();
		assertTrue(parent.getChildGroups().isEmpty());
		
		PoGroup child = new PoGroup();
		assertTrue(child.getParentGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PoLinkingUtils.link(child, parent, "addParentGroup", "addChildGroup");
		assertEquals(1, parent.getChildGroups().size());
		assertEquals(1, child.getParentGroups().size());
		
		PoParentGroup parentGroup = child.getParentGroups().iterator().next();
		PoParentGroup childGroup = parent.getChildGroups().iterator().next();
		// this is without persistence and without UID so they must be same
		assertTrue(parentGroup == childGroup);
		
		assertTrue(parent == childGroup.getParentGroup());
		assertTrue(child == parentGroup.getChildGroup());
	}
	
	public void testGetLinkObject() throws Exception {
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		Historization result = PoLinkingUtils.getLinkObject(person, group, from, to);
		assertTrue(result instanceof PoPersonGroup);
		PoPersonGroup personGroup = (PoPersonGroup) result;
		
		// the linked objects shall not know the linking object 
		assertEquals(0, person.getMemberOfGroups().size());
		assertEquals(0, group.getPersonGroups().size());
		
		assertTrue(person == personGroup.getPerson());
		assertTrue(group == personGroup.getGroup());
		assertEquals(from, personGroup.getValidfrom());
		assertEquals(getTo(to), personGroup.getValidto());
	}
	
	public void testGetLinkObjectWithLinkClass() throws Exception {
		
		PoPerson person = new PoPerson();
		PoGroup group = new PoGroup();
		
		Historization result = PoLinkingUtils.getLinkObject(PoPersonGroup.class, person, group, from, to);
		assertTrue(result instanceof PoPersonGroup);
		PoPersonGroup personGroup = (PoPersonGroup) result;
		
		// the linked objects shall not know the linking object 
		assertEquals(0, person.getMemberOfGroups().size());
		assertEquals(0, group.getPersonGroups().size());
		
		assertTrue(person == personGroup.getPerson());
		assertTrue(group == personGroup.getGroup());
		assertEquals(from, personGroup.getValidfrom());
		assertEquals(getTo(to), personGroup.getValidto());
	}
	
	public void testGetLinkObjectWithWrongClass() {
		
		try {
			PoLinkingUtils.getLinkObject(PoPerson.class, new PoPerson(), new PoGroup(), from, to);
			fail("Accepted wrong linking class");
		} catch (Exception e) {}
	}
	
	public void testRemovelink() {
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PersistentObject link = PoLinkingUtils.link(person, group, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		
		try {
			PoLinkingUtils.removeLink(link);
			fail("Did not recognize that PoPerson has no standard getPersonGroups() method");
		} catch (Exception e) { }

		PoLinkingUtils.removeLink(link, "getMemberOfGroups");
		assertEquals(0, person.getMemberOfGroups().size());
		assertEquals(0, group.getPersonGroups().size());
		
	}
	
	public void testTerminateLinkYesterdayToInfinity() {
		
		final Date now = DateTools.now();
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PersistentObject link = PoLinkingUtils.link(person, group, now, null, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		assertEquals(DateTools.INFINITY, ((Historization) link).getValidto());
		
		PoLinkingUtils.terminateLink(link);	// historicizes or removes link
		
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		
		assertEquals(getTo(DateUtils.addDays(now, -1)), ((Historization) link).getValidto());
	}
	
	public void testTerminateLinkTomorrowToInfinity() {
		
		PoPerson person = new PoPerson();
		assertTrue(person.getMemberOfGroups().isEmpty());
		
		PoGroup group = new PoGroup();
		assertTrue(group.getPersonGroups().isEmpty());
		
		// names of adder methods must be specified because they do not follow convention like 'addPersonGroup'
		PersistentObject link = PoLinkingUtils.link(person, group, DateUtils.addDays(DateTools.now(), 1), null, "addMemberOfGroup", "addPersonGroup");
		assertEquals(1, person.getMemberOfGroups().size());
		assertEquals(1, group.getPersonGroups().size());
		assertEquals(DateTools.INFINITY, ((Historization) link).getValidto());
		
		PoLinkingUtils.terminateLink(link, "getMemberOfGroups");
		// the link shall be unlinked
		assertEquals(0, person.getMemberOfGroups().size());
		assertEquals(0, group.getPersonGroups().size());
		
	}
	
	private Date getFrom(Date date) {
		return HistorizationHelper.generateUsefulValidFromDay(date);
	}
	
	private Date getTo(Date date) {
		return HistorizationHelper.generateUsefulValidToDay(date);
	}
}
