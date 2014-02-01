package at.workflow.webdesk.po.impl;

import java.util.Date;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.HistorizationServiceAdapter;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.po.PoOrganisationService;


/**
 * 
 * The implementation of the {@link at.workflow.webdesk.po.HistorizationServiceAdapter}
 * See also {@link at.workflow.webdesk.po.HistorizationTimelineHelper}
 * 
 * 
 * @author hentner
 *
 */
public class PoParentGroupHistorizationServiceAdapter implements
		HistorizationServiceAdapter {

	private PoGroup parent;
	private PoGroup child;
	
	public PoParentGroupHistorizationServiceAdapter(PoGroup parent,
			PoGroup child) {
		this.parent = parent;
		this.child = child;
	}

	@Override
	public Historization copyHistObject(Historization existingHistObject) {
		PoParentGroup existingPg = (PoParentGroup) existingHistObject;
		PoParentGroup pg = new PoParentGroup();
		pg.setChildGroup(existingPg.getChildGroup());
		pg.setParentGroup(existingPg.getParentGroup());
		// we don't set the ranking -> default
		pg.setValidfrom(existingHistObject.getValidfrom());
		pg.setValidto(existingHistObject.getValidto());
		return pg;
	}

	@Override
	public void deleteObject(Historization historizationObject) {
		((PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService")).
					deleteAndFlushParentGroup((PoParentGroup) historizationObject);
	}

	@Override
	public Historization generateEmptyObject(Date from, Date to) {
		PoParentGroup pg = new PoParentGroup();
		pg.setChildGroup(child);
		pg.setParentGroup(parent);
		pg.setValidfrom(from);
		pg.setValidto(to);
		return pg;
	}

	@Override
	public boolean isStructurallyEqual(Historization existingHistObject,
			Historization newObject) {
		PoParentGroup existingPg = (PoParentGroup) existingHistObject;
		PoParentGroup newPg = (PoParentGroup) newObject;
		if (existingPg.getParentGroup().equals(newPg.getParentGroup()))
			return true;
		return false;
	}

	@Override
	public void saveObject(Historization historizationObject) {
		PoParentGroup pg = (PoParentGroup) historizationObject;
		
		PoOrganisationService poOrganisationService = (PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService");
		assert poOrganisationService != null : "Global WebdeskApplicationContext.applicationContext is null, or contained no bean named 'PoOrganisationService'";
		poOrganisationService.saveParentGroup((PoParentGroup) historizationObject);
		
		if (!pg.getParentGroup().getChildGroups().contains(pg))
			pg.getParentGroup().getChildGroups().add(pg);
		
		if (!pg.getChildGroup().getParentGroups().contains(pg))
			pg.getChildGroup().getParentGroups().add(pg);
		
	}
}
