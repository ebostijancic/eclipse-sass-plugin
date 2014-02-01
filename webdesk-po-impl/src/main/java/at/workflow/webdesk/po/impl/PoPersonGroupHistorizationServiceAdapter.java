package at.workflow.webdesk.po.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.HistorizationServiceAdapter;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;

public class PoPersonGroupHistorizationServiceAdapter implements
		HistorizationServiceAdapter {

	private PoPerson person;
	private PoGroup group;
	
	public PoPersonGroupHistorizationServiceAdapter(PoPerson person,
			PoGroup group) {
		this.person = person;
		this.group = group;
	}

	@Override
	public Historization copyHistObject(Historization existingHistObject) {
		PoPersonGroup pg = (PoPersonGroup) existingHistObject;
		PoPersonGroup pgN = new PoPersonGroup();
		pgN.setGroup(pg.getGroup());
		pgN.setPerson(pg.getPerson());
		pgN.setValidfrom(pg.getValidfrom());
		pgN.setValidto(pg.getValidto());
		return pgN;
	}

	@Override
	public void deleteObject(Historization historizationObject) {
		((PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService")).
			deleteAndFlushPersonGroupLink((PoPersonGroup) historizationObject);
	}

	@Override
	public Historization generateEmptyObject(Date from, Date to) {
		PoPersonGroup pg = new PoPersonGroup();
		pg.setGroup(group);
		pg.setPerson(person);
		pg.setValidfrom(from);
		pg.setValidto(to);
		return pg;
	}

	@Override
	public boolean isStructurallyEqual(Historization existingHistObject,
			Historization newObject) {
		PoPersonGroup existPg = (PoPersonGroup) existingHistObject;
		PoPersonGroup newPg = (PoPersonGroup) newObject;
		if (existPg.getPerson().equals(newPg.getPerson()) && 
				existPg.getGroup().equals(newPg.getGroup()))
			return true;
		else
			return false;
	}

	@Override
	public void saveObject(Historization historizationObject) {
		
		PoPersonGroup personGroup = (PoPersonGroup) historizationObject;
		PoOrganisationService orgService = 
				(PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService");
		
// due to editPerson and editGroup controllers saving main object before links 
// following addition of missing linking should not cause any problems	TODO test
		
		// if this is new link update linked objects collections
		if (StringUtils.isBlank(personGroup.getUID())) {
			PoPerson person = personGroup.getPerson();
			PoGroup group = personGroup.getGroup();
			orgService.refresh(personGroup.getPerson());
			orgService.refresh(group);
			person.addMemberOfGroup(personGroup);
			group.addPersonGroup(personGroup);
		}		
		
		orgService.savePersonGroup(personGroup);
	}

}