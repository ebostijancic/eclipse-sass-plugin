package at.workflow.webdesk.po.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.HistoricizableValidFromComparator;
import at.workflow.webdesk.tools.date.HistorizationUtils;

/**
 * This small utility should repair inconsistent person-orgunit relations
 * 
 * @author ggruber
 *
 */
public class OrgAdminHelper {

	private PoOrganisationService organisationService;
	
	public void enforceConsistentGroupsOverTime(PoPerson person, int orgType) {
		
		// get overlapping intervals and
		// delete historization objects with same validfrom
		
		List<PoPersonGroup> overlappings = new ArrayList<PoPersonGroup>();
		overlappings.addAll(getOverlappingGroupLinks(person, orgType));
		
		if (overlappings.size() < 2)
			return;
		
		Collections.sort(overlappings, new HistoricizableValidFromComparator());
		
		PoPersonGroup lastPg=null;
		for (int i=0; i<overlappings.size();i++) {
			PoPersonGroup pg = overlappings.get(i);
			if (lastPg!=null && pg.getValidfrom().equals(lastPg.getValidfrom())) {
				organisationService.deleteAndFlushPersonGroupLink(lastPg);
			}
			lastPg = pg;
		}
		
		// now concat all the intervalls of the remaining hierarchical group links
		List<PoPersonGroup> remainingLinks = getHierarchicalGroupLinks(person, orgType);
		Collections.sort(remainingLinks, new HistoricizableValidFromComparator());
		lastPg = null;
		for (PoPersonGroup pg : remainingLinks ) {
			if (lastPg!=null && !(lastPg.getValidto().equals(pg.getValidfrom()))) {
				lastPg.setValidto(pg.getValidfrom());
				organisationService.savePersonGroup(lastPg);
			}
			lastPg = pg;
		}
	}
	
	private List<PoPersonGroup> getHierarchicalGroupLinks(PoPerson person, int orgType) {
		List<PoPersonGroup> hierarchicalGroupLinks =new ArrayList<PoPersonGroup>();
		Iterator<PoPersonGroup> itr = person.getMemberOfGroups().iterator();
		while (itr.hasNext()) {
			PoPersonGroup pg = itr.next();
			if (pg.getGroup().getOrgStructure().getOrgType() == orgType) {
				hierarchicalGroupLinks.add(pg);
			}
		}
		return hierarchicalGroupLinks;
	}
	
	@SuppressWarnings("unchecked")
	private Set<PoPersonGroup> getOverlappingGroupLinks(PoPerson person, int orgType) {
		
		List<PoPersonGroup> hierarchicalGroupLinks = getHierarchicalGroupLinks(person, orgType);
		
		if (hierarchicalGroupLinks.size()<2)
			return new HashSet<PoPersonGroup>();
		
		// check overlaps
		Set<PoPersonGroup> overlappings = new HashSet<PoPersonGroup>();
		List<PoPersonGroup> compareList = new ArrayList<PoPersonGroup>();
		compareList.addAll(hierarchicalGroupLinks);
		for (PoPersonGroup pg : hierarchicalGroupLinks) {
			compareList.remove(pg);
			List<? extends Historization> overlaps = HistorizationUtils.getOverlappingEntriesIncludingPast(compareList, pg.getValidfrom(), pg.getValidto(), false);
			
			if (overlaps.size()>0) {
				overlappings.addAll((Collection<? extends PoPersonGroup>) overlaps);
				overlappings.add(pg);
			}
			
		}
		return overlappings;
	}
	
	public void enforceConsistentHierarchicalGroupsForAll() {
		Iterator<PoPerson> itr = organisationService.loadAllPersons().iterator();
		while (itr.hasNext()) {
			PoPerson reloadedPerson = organisationService.getPerson(itr.next().getUID()); // be sure to avoid lazy loading exceptions
			enforceConsistentGroupsOverTime(reloadedPerson, PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		}
	}
	
	public void enforceConsistentCostCenterGroupsForAll() {
		Iterator<PoPerson> itr = organisationService.loadAllPersons().iterator();
		while (itr.hasNext()) {
			PoPerson reloadedPerson = organisationService.getPerson(itr.next().getUID()); // be sure to avoid lazy loading exceptions
			enforceConsistentGroupsOverTime(reloadedPerson, PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS);
		}
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
	
}
