package at.workflow.webdesk.po.impl.test;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;

public class TestPoPersonGroupServiceAdapter implements TestServiceAdapter {

	private PoOrganisationService orgService;
	private PoPerson person;
	private PoOrgStructure orgStructure;
	
	
	public TestPoPersonGroupServiceAdapter(PoPerson person) {
		orgService = (PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService");
		this.person = person;
		orgStructure = orgService.getOrgHierarchy(person.getClient());
	}

	public List<? extends Historization> findEntries() {
		return orgService.findPersonGroupsF(person, new Date(), PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
	}
	
	
	public Historization generateNew(String shortName) {
		PoGroup group = new PoGroup();
		group.setShortName(shortName);
		group.setClient(person.getClient());
		group.setName(shortName);
		group.setOrgStructure(orgStructure);
		group.setValidfrom(new Date());
		group.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		return group;
	}

	
	public void save(Historization newObject) {
		orgService.saveGroup((PoGroup) newObject);
	}

	public void doAssignment(Historization newObject, Date from, Date to) {
		orgService.linkPerson2Group(person, (PoGroup) newObject, from, to);
	}

	

}
