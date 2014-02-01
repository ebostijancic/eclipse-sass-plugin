package at.workflow.webdesk.po.impl.update.v110;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.util.CollectionUtils;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.impl.daos.PoParentGroupDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoPersonGroupDAOImpl;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This script changes validity to day-exact of following entities in the exact order:
 * <ul><li>PoPersonGroups</li>
 * <li>PoPersons</li>
 * <li>PoParentGroups</li>
 * <li>PoGroups</li></ul>
 * <br />
 * All the entities are evicted to prevent any unintentional flushing of not complete changes.
 * <br />
 * <br />
 * The reason for the order is following:
 * first the less-than-one-day valid assignment must be deleted,
 * than the assigned objects can be updated.
 * <br /> 
 * <br /> 
 * Deletion of PoPersons and PoGroups is left to another update script or a separate action to allow user to take control.
 * <br />
 * <br />
 * Following date-transformation is applied:
 * {@link #updateValidity(Historization, String)}
 * <br />
 * If validity of PoPersonGroup or PoParentGroup is not positive the entity is deleted.
 * <br />
 * <br />
 * Before saving following timeline checks are performed:
 * <ul><li>for every group there is no overlapping of parent groups</li>
 * <li>for every person there is strict timeline of hierarchical org. groups</li>
 * <li>for every person there is no overlapping of locations</li>
 * <li>for every person there is no overlapping of costCenters</li></ul>
 * <br /> 
 * For better tracability the processing is split into
 * - processing of PoPersonGroups and PoPersons
 * - processing of PoParentGroups and PoGroups
 * <br /> 
 * <br /> 
 *  Important fact is that Hibernate sets the date from DB to PoPerson and to PoGroup without calling the setter method.
 *  This means that after reading from DB dates are not processed to dateOnly nor to lastMomentOfDay.
 *  For PoPersonGroup and PoParentGroup the setter is called. To preserve the dates as they are in the DB
 *  select clause is used and artificial objects MyPersonGroup and MyParentGroup are created from read values.
 *  These artificial objects are processed and, when save is called, their data is copied to the real Po objects. 
 * <br />  
 * <br />  
 *  Everything is loaded into memory, than changed in the memory, than checked, and than selectively, 
 *  i.e. only what is right, is saved. For what checks failed is logged as error and not saved.
 * <br />
 * <br />
 *  Summary of the changes is logged at the end.
 * <br />
 * <br />
 *  
 * @author sdzuban 23.01.2014
 *
 */
public class PoUpdateValidityToDayExact extends PoAbstractUpgradeScript {

	private final boolean save = true; // put false for debugging of the script without saving, operation = true
	
	private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

	private class PersonComparator implements Comparator<PoPerson> {
		@Override
		public int compare(PoPerson p1, PoPerson p2) {
			int clientResult = p1.getClient().getName().compareTo(p2.getClient().getName());
			if (clientResult == 0)
				return p1.getFullName().compareTo(p2.getFullName());
			return clientResult;
		}
	}
	
	private class GroupComparator implements Comparator<PoGroup> {
		@Override
		public int compare(PoGroup g1, PoGroup g2) {
			int clientResult = g1.getClient().getName().compareTo(g2.getClient().getName());
			if (clientResult == 0)
				return g1.getShortName().compareTo(g2.getShortName());
			return clientResult;
		}
	}
	
	private class ClientsOrgStructures {

		private PoClient client;
		private PoOrgStructure orgHierarchy;
		private PoOrgStructure orgCostCenters;
		private PoOrgStructure orgLocations;
		
		public ClientsOrgStructures(PoClient client) {
			this.client = client;
			orgHierarchy = orgService.getOrgHierarchy(client);
			orgCostCenters = orgService.getOrgCostCenters(client);
			orgLocations = orgService.getOrgLocations(client);
		}

		public PoClient getClient() {
			return client;
		}
		public PoOrgStructure getOrgHierarchy() {
			return orgHierarchy;
		}
		public PoOrgStructure getOrgCostCenters() {
			return orgCostCenters;
		}
		public PoOrgStructure getOrgLocations() {
			return orgLocations;
		}
	}
	
	// default for test purpose
	class MyExactHistorization implements Historization {

		private String uid;
		private Date validfrom;
		private Date validto;
		
		public MyExactHistorization(Map<String, ?> map) {
			uid = (String) map.get("pg.UID");
			validfrom = typedValidityDate((Date) map.get("pg.validfrom"));
			validto = typedValidityDate((Date) map.get("pg.validto"));
		}
		
		@Override
		public String getUID() {
			return uid;
		}
		@Override
		public void setValidfrom(Date validfrom) {
			this.validfrom = validfrom;
		}
		@Override
		public Date getValidfrom() {
			return validfrom;
		}
		@Override
		public void setValidto(Date validto) {
			this.validto = validto;
		}
		@Override
		public Date getValidto() {
			return validto;
		}
		@Override
		public Interval getValidity() {
			return new DateInterval(validfrom, validto);
		}
		@Override
		public void historicize() {
			// not used
		}
		private Date typedValidityDate(Date validityDate) {
			if (validityDate instanceof Timestamp)
				return new Date(validityDate.getTime());
			return validityDate;
		}
	}
	
	// default for test purpose
	class MyPersonGroup extends MyExactHistorization {
		
		private PoPerson person;
		private PoGroup group;

		public MyPersonGroup(Map<String, ?> personGroupMap) {
			super(personGroupMap);
			person = (PoPerson) personGroupMap.get("pg.person");
			group = (PoGroup) personGroupMap.get("pg.group");
		}

		public final PoPerson getPerson() {
			return person;
		}
		public final PoGroup getGroup() {
			return group;
		}
	}
	
	// default for test purpose
	class MyParentGroup extends MyExactHistorization {
		
		private PoGroup parentGroup;
		private PoGroup childGroup;
		
		public MyParentGroup(Map<String, ?> parentGroupMap) {
			super(parentGroupMap);
			parentGroup = (PoGroup) parentGroupMap.get("pg.parentGroup");
			childGroup = (PoGroup) parentGroupMap.get("pg.childGroup");
		}

		public final PoGroup getParentGroup() {
			return parentGroup;
		}
		public final PoGroup getChildGroup() {
			return childGroup;
		}
	}
	
	private PoOrganisationService orgService;
	private PoGeneralDbService dbService;
	
	private PoPersonDAO personDao;
	private PoGroupDAO groupDao;
	private PoPersonGroupDAOImpl personGroupDao;
	private PoParentGroupDAOImpl parentGroupDao;
	
	private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	
	private List<PoPerson> persons;
	// following is just to prevent reading every PoPersonGroup for save/delete separately 
	private Map<String, PoPersonGroup> poPersonGroupMap;
	private Map<PoPerson, List<MyPersonGroup>> personGroupsMap;
	private List<PoGroup> groups;
	// following is just to prevent reading every PoParentGroup for save/delete separately 
	private Map<String, PoParentGroup> poParentGroupMap;
	private Map<PoGroup, List<MyParentGroup>> parentGroupsMap;
	private List<PoClient> clients;
	private Map<PoClient, ClientsOrgStructures> orgStructuresMap = new HashMap<PoClient, ClientsOrgStructures>();
	
	private Map<PoPerson, Boolean> personChanged = new HashMap<PoPerson, Boolean>();
	private Map<MyPersonGroup, Boolean> personGroupChanged = new HashMap<MyPersonGroup, Boolean>();
	private Map<PoGroup, Boolean> groupChanged = new HashMap<PoGroup, Boolean>();
	private Map<MyParentGroup, Boolean> parentGroupChanged = new HashMap<MyParentGroup, Boolean>();

	private Map<PoPerson, List<String>> failedPersonsMessagesMap = new HashMap<PoPerson, List<String>>();
	private Map<PoGroup, String> failedGroupsMessagesMap = new HashMap<PoGroup, String>();
	
	private List<PoPerson> personsToBeDeleted = new ArrayList<PoPerson>();
	private List<PoGroup> groupsToBeDeleted = new ArrayList<PoGroup>();
	
	private boolean success = true;
	
	@Override
	public void execute() {
		
		initialize();
		
		readAllEntities();
		
		processPersonsAndPersonGroups();
		
		processGroupsAndParentGroups();
		
		logSummaryAndCheckSuccess();
		
		if (success)
			logger.info("Update script ended successfully");
		else {
			String message = "The update script was not completely successful.";
			logger.error(message);
			throw new RuntimeException(message);
		}
	}

	private void initialize() {
		
		success = true;
		
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		dbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		
		personDao = (PoPersonDAO) getBean("PoPersonDAO");
		groupDao = (PoGroupDAO) getBean("PoGroupDAO");
		personGroupDao = (PoPersonGroupDAOImpl) getBean("PoPersonGroupDAO");
		parentGroupDao = (PoParentGroupDAOImpl) getBean("PoParentGroupDAO");
		
		poPersonGroupMap = new HashMap<String, PoPersonGroup>();
		poParentGroupMap = new HashMap<String, PoParentGroup>();
		
		orgStructuresMap = new HashMap<PoClient, ClientsOrgStructures>();
		
		personGroupsMap = new HashMap<PoPerson, List<MyPersonGroup>>();
		parentGroupsMap = new HashMap<PoGroup, List<MyParentGroup>>();

		personChanged = new HashMap<PoPerson, Boolean>();
		personGroupChanged = new HashMap<MyPersonGroup, Boolean>();
		groupChanged = new HashMap<PoGroup, Boolean>();
		parentGroupChanged = new HashMap<MyParentGroup, Boolean>();

		failedPersonsMessagesMap = new HashMap<PoPerson, List<String>>();
		failedGroupsMessagesMap = new HashMap<PoGroup, String>();
		
		personsToBeDeleted = new ArrayList<PoPerson>();
		groupsToBeDeleted = new ArrayList<PoGroup>();
	}
	
	private void readAllEntities() {
		
		persons = orgService.loadAllPersons(); // this reads persons with dates exactly as they are in the DB
		for (PoPerson person : persons) {
			personChanged.put(person, false);
			personGroupsMap.put(person, new ArrayList<MyPersonGroup>()); // every person has at least one group
		}
		
		loadMyPersonGroups();
		
		List<PoPersonGroup> allPoPersonGroups = personGroupDao.loadAll();
		for (PoPersonGroup personGroup : allPoPersonGroups) {
			poPersonGroupMap.put(personGroup.getUID(), personGroup);
		}
		
		groups = orgService.loadAllGroups();
		for (PoGroup group : groups) {
			groupChanged.put(group, false);
			parentGroupsMap.put(group, new ArrayList<MyParentGroup>()); // to avoid NPEs
		}
		
		loadMyParentGroups();
		
		List<PoParentGroup> allPoParentGroups = parentGroupDao.loadAll();
		for (PoParentGroup parentGroup : allPoParentGroups) {
			poParentGroupMap.put(parentGroup.getUID(), parentGroup);
		}
		
		clients = orgService.loadAllClients();
		for (PoClient client : clients)
			orgStructuresMap.put(client, new ClientsOrgStructures(client));
	}

	private void loadMyPersonGroups() {
		
		//		Collection<PoPersonGroup> personsPersonGroups = person.getMemberOfGroups(); this does not work - the dates are set by setters
		//		List<PoPersonGroup> personsPersonGroups = orgService.findPersonGroupsAll(person); this does not work either
		//		List<PoPersonGroup> personGroups = personGroupDao.loadAll(); this transforms dates via setters
		//		List<PoPersonGroup> personGroups = (List<PoPersonGroup>) dbService.getElementsAsList("from PoPersonGroup", null); this transforms dates via setteres
			
		final String query = "select pg.UID, pg.person, pg.group, pg.validfrom, pg.validto from PoPersonGroup pg";
		List<Map<String, ?>> personGroupMaps = dbService.getElementsAsListOfNamedMaps(query, null);
		for (Map<String, ?> personGroupMap : personGroupMaps) {
			MyPersonGroup personGroup = new MyPersonGroup(personGroupMap);
			personGroupChanged.put(personGroup, false);
			List<MyPersonGroup> personsPersonGroups = personGroupsMap.get(personGroup.getPerson());
			personsPersonGroups.add(personGroup);
		}
	}

	private void loadMyParentGroups() {
		
		//		Collection<PoParentGroup> groupsParentGroups = group.getParentGroups(); this does not work - the dates are set by setters
		//		List<PoParentGroup> groupsParentGroups = orgService.findParentGroupsAll(group); this does not work either
		//		List<PoParentGroup> parentGroups = parentGroupDao.loadAll(); this transforms dates via setters
		//		List<PoParentGroup> parentGroups = (List<PoParentGroup>) dbService.getElementsAsList("from PoParentGroup", null); this transforms dates via setteres

		final String query = "select pg.UID, pg.childGroup, pg.parentGroup, pg.validfrom, pg.validto from PoParentGroup pg";
		List<Map<String, ?>> parentGroupMaps = dbService.getElementsAsListOfNamedMaps(query, null);
		for (Map<String, ?> parentGroupMap : parentGroupMaps) {
			MyParentGroup parentGroup = new MyParentGroup(parentGroupMap);
			parentGroupChanged.put(parentGroup, false);
			List<MyParentGroup> groupsParentGroups = parentGroupsMap.get(parentGroup.getChildGroup());
			groupsParentGroups.add(parentGroup);
		}
	}

	private void processPersonsAndPersonGroups() {
		
		logger.info("Starting processing of PoPerson and PoPersonGroup entities");
		
		logger.info("Going to update " + persons.size() + " PoPersons and their PoPersonGroups");
		
		for (PoPerson person : persons)
			processPersonAndItsPersonGroups(person);
		
		logger.info("Ended processing of PoPerson and PoPersonGroup entities");
	}

	private void processGroupsAndParentGroups() {
		
		logger.info("Starting processing of PoGroup and PoParentGroup entities");
		
		logger.info("Going to update " + groups.size() + " PoGroups and their PoParentGroups");
		
		for (PoGroup group : groups)
			processGroupAndItsParentGroups(group);
		
		logger.info("Ended processing of PoGroup and PoParentGroup entities");
	}
	
	private void logSummaryAndCheckSuccess() {
		
		int successCount = persons.size() - failedPersonsMessagesMap.size();
		if (successCount > 0)
			logger.info("Successfully updated " + successCount + " persons and their group timelines.");
		
		successCount = groups.size() - failedGroupsMessagesMap.size();
		if (successCount > 0)
			logger.info("Successfully updated " + successCount + " groups and their parent groups.");
		
		int negativeCount = personsToBeDeleted.size();
		if (negativeCount > 0) {
			logger.warn("Found " + negativeCount + " persons with negative validity.");
			Collections.sort(personsToBeDeleted, new PersonComparator());
			for (PoPerson person : personsToBeDeleted)
				logger.warn(person.getClient().getName() + ": " + person.getFullName() + ", " + 
						StringUtils.defaultString(person.getEmployeeId()) + " valid " + person.getValidity().toString(DATE_FORMAT));
		}
		
		negativeCount = groupsToBeDeleted.size();
		if (negativeCount > 0) {
			logger.warn("Found " + negativeCount + " groups with negative validity.");
			Collections.sort(groupsToBeDeleted, new GroupComparator());
			for (PoGroup group : groupsToBeDeleted)
				logger.warn(group.getClient().getName() + ": " + group.getShortName() + " " + group.getName() +
						" valid " + group.getValidity().toString(DATE_FORMAT));
		}
		
		List<PoPerson> failedPersons = new ArrayList<PoPerson>(failedPersonsMessagesMap.keySet());
		if ( ! CollectionUtils.isEmpty(failedPersons)) {
			success = false;
			logger.error("Found errors processing " + failedPersons.size() + " persons");
			Collections.sort(failedPersons, new PersonComparator());
			for (PoPerson person : failedPersons) {
				List<String> errors = failedPersonsMessagesMap.get(person);
				logger.error("Errors processing person " + person.getFullName() +
						", " + StringUtils.defaultString(person.getEmployeeId()) +
						" of client " + person.getClient().getName() +
						" valid " + person.getValidity());
				for (String error : errors)
					logger.error(error);
			}
		}
			
		List<PoGroup> failedGroups = new ArrayList<PoGroup>(failedGroupsMessagesMap.keySet());
		if ( ! CollectionUtils.isEmpty(failedGroups)) {
			success = false;
			logger.error("Found errors processing " + failedGroups.size() + " groups");
			Collections.sort(failedGroups, new GroupComparator());
			for (PoGroup group : failedGroups) {
				String errorMessage = failedGroupsMessagesMap.get(group);
				logger.error("Error processing group " + group.getShortName() +
						" " + group.getName() + 
						" of client " + group.getClient().getName() +
						" valid " + group.getValidity());
				logger.error(errorMessage);
			}
		}		
	}
	
	private void processPersonAndItsPersonGroups(PoPerson person) {
		
		if (logger.isDebugEnabled())
			logger.debug("Processing " + person.getFullName());
		
		List<MyPersonGroup> personGroups = personGroupsMap.get(person);
		for (MyPersonGroup personGroup : personGroups) {
			personGroupChanged.put(personGroup, 
					updateValidity(personGroup, "Updating " + getPersonGroupIdentity(personGroup) + " "));
		}
		personChanged.put(person, updateValidity(person, "Updating " + person.getFullName() + " "));

		List<String> errorMessages = getTimelinesErrorMessages(person, personGroups, orgStructuresMap.get(person.getClient()));
		if (CollectionUtils.isEmpty(errorMessages)) {
			savePersonGroups(personGroups);
			savePerson(person);
		} else {
			personDao.evict(person); // to prevent flush
			failedPersonsMessagesMap.put(person, errorMessages);
			if (logger.isDebugEnabled())
				logger.debug("Timelines not preserved after update for " + person.getFullName() + ". Person and her personGroups not updated.");
		}
	}

	private List<String> getTimelinesErrorMessages(PoPerson person, List<MyPersonGroup> personGroups, ClientsOrgStructures orgStructures) {

		List<String> errorMessages = new ArrayList<String>();
		
		PoOrgStructure orgHierarchy = orgStructures.getOrgHierarchy();
		if (orgHierarchy != null) {
			String prefix = "Timeline of hierarchical organisation units ";

			List<MyPersonGroup> hierarchyTimeline = getValidPersonGroups(personGroups, orgHierarchy);
			if (HistorizationTimelineUtils.isTimelineNotComplete(hierarchyTimeline, person.getValidfrom(), person.getValidto()))
				errorMessages.add(prefix + "does not cover whole validitiy of person");
			
			List<DateInterval> gaps = HistorizationTimelineUtils.getTimelineGaps(hierarchyTimeline, person.getValidity());
			if ( ! CollectionUtils.isEmpty(gaps))
				errorMessages.add(prefix + " has following gaps: " + formatIntervals(gaps));
			
			List<DateInterval> overlaps = HistorizationTimelineUtils.getTimelineOverlaps(hierarchyTimeline);
			if ( ! CollectionUtils.isEmpty(overlaps))
				errorMessages.add(prefix + " has following overlaps: " + formatIntervals(overlaps));
			
		} else {
			errorMessages.add("Client " + orgStructures.getClient().getName() + " has no hierarchy org structure");
		}
		
		PoOrgStructure orgLocations = orgStructures.getOrgLocations();
		if (orgLocations != null) {
			List<MyPersonGroup> locationsTimeline = getValidPersonGroups(personGroups, orgLocations);
			List<DateInterval> overlaps = HistorizationTimelineUtils.getTimelineOverlaps(locationsTimeline);
			if ( ! CollectionUtils.isEmpty(overlaps))
				errorMessages.add("Locations timeline has following overlaps: " + formatIntervals(overlaps));
		}
		
		PoOrgStructure orgCostCenters = orgStructures.getOrgCostCenters();
		if (orgCostCenters != null) {
			List<MyPersonGroup> costCentersTimeline = getValidPersonGroups(personGroups, orgCostCenters);
			List<DateInterval> overlaps = HistorizationTimelineUtils.getTimelineOverlaps(costCentersTimeline);
			if ( ! CollectionUtils.isEmpty(overlaps))
				errorMessages.add("Cost centers timeline has following overlaps: " + formatIntervals(overlaps));
		}
		return errorMessages;
	}

	/** only valid persongroups must be taken. Invalid ones would distort the timeline */
	private List<MyPersonGroup> getValidPersonGroups(Collection<MyPersonGroup> personGroups, PoOrgStructure orgStructure) {
		
		List<MyPersonGroup> result = new ArrayList<MyPersonGroup>();
		
		for (MyPersonGroup personGroup : personGroups)
			if (personGroup.getValidity().isPositive() && 
					personGroup.getGroup().getOrgStructure().equals(orgStructure))
				result.add(personGroup);
		
		return result;
	}
	
	private List<String> formatIntervals(List<? extends Interval> intervals) {
		List<String> result = new ArrayList<String>();
		for (Interval interval : intervals)
			result.add(interval.toString(DATE_FORMAT));
		return result;
	}
	
	private void processGroupAndItsParentGroups(PoGroup group) {
		
		if (logger.isDebugEnabled())
			logger.debug("Processing " + group.getShortName());
		
		List<MyParentGroup> parentGroups = parentGroupsMap.get(group);
		for (MyParentGroup parentGroup : parentGroups) {
			parentGroupChanged.put(parentGroup, 
					updateValidity(parentGroup, "Updating " + getParentGroupIdentity(parentGroup) + " "));
		}
		groupChanged.put(group, updateValidity(group, "Updating " + group.getShortName() + " "));

		List<MyParentGroup> validParentGroups = getValidParentGroups(parentGroups);
		List<DateInterval> overlaps = HistorizationTimelineUtils.getTimelineOverlaps(validParentGroups); 
		if (CollectionUtils.isEmpty(overlaps)) {
			saveParentGroups(parentGroups);
			saveGroup(group);
		} else {
			groupDao.evict(group); // to prevent flush
			failedGroupsMessagesMap.put(group, "Parent groups overlap " + formatIntervals(overlaps));
			if (logger.isDebugEnabled())
				logger.debug("ParentGroup overlap after update for " + group.getShortName() + ". Group and her parentGroups not updated.");
		}
	}
	
	/** only valid parentgroups must be taken. Invalid ones would distort the timeline */
	private List<MyParentGroup> getValidParentGroups(List<MyParentGroup> parentGroups) {
		
		List<MyParentGroup> result = new ArrayList<MyParentGroup>();
		
		for (MyParentGroup parentGroup : parentGroups)
			if (parentGroup.getValidity().isPositive())
				result.add(parentGroup);
		
		return result;
	}
	
	/** deletes also unchanged personGroups with negative validity */
	private void savePersonGroups(List<MyPersonGroup> personGroups) {
		
		for (MyPersonGroup myPersonGroup : personGroups) {
			
			final boolean isValid = myPersonGroup.getValidity().isPositive();
			
			if (personGroupChanged.get(myPersonGroup) || isValid == false) {
				
				String action = isValid ? " updating " : " deleting ";
				String identity = "";
				try {
					identity = getPersonGroupIdentity(myPersonGroup);
					if (save) {
						PoPersonGroup poPersonGroup = poPersonGroupMap.get(myPersonGroup.getUID());
						
						if (isValid) {
							poPersonGroup.setValidfrom(myPersonGroup.getValidfrom());
							poPersonGroup.setValidto(myPersonGroup.getValidto());
							orgService.savePersonGroup(poPersonGroup);
						} else {
							orgService.deleteAndFlushPersonGroupLink(poPersonGroup);
						}
					} else {
						logger.info("Test run, would perform " + action + identity +
								sdf.format(myPersonGroup.getValidfrom()) + "-" + sdf.format(myPersonGroup.getValidto()));
					}
				} catch (Exception e) {
					String message = "Error while " + action + identity;
					logger.error(message, e);
				}
			}
		}
	}

	private String getPersonGroupIdentity(MyPersonGroup personGroup) {
		String fullName = personGroup.getPerson().getFullName();
		String shortName = personGroup.getGroup().getShortName();
		return fullName + " to " + shortName + " link "; 
	}
	
	private void savePerson(PoPerson person) {
		
		if (personChanged.get(person)) {
			String action = person.getValidity().isPositive() ? " updating " : " updating with negative validity ";
			if (save) {
				try {
					orgService.updatePerson(person);
				} catch (Exception e) {
					String message = "Error while " + action + person.getFullName();
					logger.error(message, e);
				}
			} else {
				logger.info("Test run, would perform " + action + person.getFullName());
			}
		}
		if ( ! person.getValidity().isPositive()) {
			personsToBeDeleted.add(person);
			if (logger.isDebugEnabled())
				logger.debug("Marking person for delete " + person.getFullName());
		}
	}

	/** deletes also unchanged parentGroups with negative validity */ 
	private void saveParentGroups(List<MyParentGroup> parentGroups) {
		
		for (MyParentGroup myParentGroup : parentGroups) {
			
			final boolean isValid = myParentGroup.getValidity().isPositive();
			
			if (parentGroupChanged.get(myParentGroup) || isValid == false) {
				
				String action = isValid ? " updating " : " deleting ";
				String identity = "";
				try {
					identity = getParentGroupIdentity(myParentGroup);
					if (save) {
						PoParentGroup poParentGroup = poParentGroupMap.get(myParentGroup.getUID());

						if (isValid) {
							poParentGroup.setValidfrom(myParentGroup.getValidfrom());
							poParentGroup.setValidto(myParentGroup.getValidto());
							orgService.saveParentGroup(poParentGroup);
						} else {
							orgService.deleteAndFlushParentGroup(poParentGroup);
						}
					} else {
						logger.info("Test run, would perform " + action + identity + " " + 
										sdf.format(myParentGroup.getValidfrom()) + "-" + sdf.format(myParentGroup.getValidto()));
					}
				} catch (Exception e) {
					String message = "Error while " + action + identity;
					logger.error(message, e);
				}
			}
		}
	}

	private String getParentGroupIdentity(MyParentGroup parentGroup) {
		String childShortName = parentGroup.getChildGroup().getShortName();
		String parentShortName = parentGroup.getParentGroup().getShortName();
		return childShortName + " to parent " + parentShortName + " link ";
	}
	
	private void saveGroup(PoGroup group) {
		
		if (groupChanged.get(group)) {
			String action = group.getValidity().isPositive() ? " updating " : " updating with negative validity ";
			if (save) {
				try {
					orgService.saveGroup(group);
				} catch (Exception e) {
					String message = "Error while " + action + group.getShortName();
					logger.error(message, e);
				}
			} else {
				logger.info("Test run, would perform " + action + group.getShortName());
			}
		}
		if ( ! group.getValidity().isPositive()) {
			groupsToBeDeleted.add(group);
			if (logger.isDebugEnabled())
				logger.debug("Marking group for delete " + group.getShortName());
		}
	}
	
	/**
	 * Following date-transformation is applied:
	 * (lastMomentOfDay is only 23:59:59 as MySQL does not support milliseconds)
	 * <ul><li>set validfrom to dateOnly</li>
	 * <li>IF validto after INFINITY validto = INFINITY (I have seen data with 1.1.3000 23:59:59)</li>
	 * <li>IF validto == lastMomentOfDay(day before INFINITY) validto = INFINITY</li> (for validto = 31.12.2999 23:59:59, the old infinity) 
	 * <li>IF validto != INFINITY AND validto != lastMomentOfDay set validto to lastMomentOfDay of previous day of validto</li></ul>
	 *
	 * This method is package just for testing. 
	 * 
	 * @param historization
	 * @param logPrefix shall contain identification of the historization, used for logging
	 * @return whether the validity changed by processing
	 */
	final boolean updateValidity(Historization dayHistorization, String logPrefix) {
		
		Date validfrom = dayHistorization.getValidfrom();
		dayHistorization.setValidfrom(DateTools.dateOnly(validfrom));
		
		Date validto = dayHistorization.getValidto();
		dayHistorization.setValidto(transformToDate(validto));
		
		boolean validityChanged = validfrom.getTime() != dayHistorization.getValidfrom().getTime();
		// on MySQL there is always change of validto from 23:59:59 to 23:59:59,900 
		// because MySQL does not deliver milliseconds and HistorizationHelper/DateTools put them in
		if (validto.getTime() == dayHistorization.getValidto().getTime() || // same time
				DateTools.isLastMomentOfDay(dayHistorization.getValidto()) && // both are last moment of the same day
				DateTools.isLastMomentOfDay(validto) &&
				DateTools.isOnSameDay(dayHistorization.getValidto(), validto))
			; // no change of validto
		else
			validityChanged = true;
		
		if (validityChanged && logger.isDebugEnabled())
			logger.debug(logPrefix + sdf.format(validfrom) + "-" + sdf.format(validto) + 
					" -> " + sdf.format(dayHistorization.getValidfrom()) +"-" + sdf.format(dayHistorization.getValidto()));
		return validityChanged;
	}
	
	private Date transformToDate(Date to) {
		
		if (to.after(DateTools.INFINITY) || 
				DateTools.isLastMomentOfDay(to) && DateTools.isOnSameDay(to, DateUtils.addDays(DateTools.INFINITY, -1))) // 31.12.2999 23:59:59
			return DateTools.INFINITY;
		
		if (to.equals(DateTools.INFINITY) || DateTools.isLastMomentOfDay(to))
			return to;
		
		return DateTools.lastMomentOfDay(DateUtils.addDays(to, -1));
	}

//	-------------------------- JUST FOR TEST PURPOSES -----------------------------
	
	/** just for test purposes */
	final Map<PoPerson, Boolean> getPersonChanged() {
		return personChanged;
	}

	/** just for test purposes */
	final Map<MyPersonGroup, Boolean> getPersonGroupChanged() {
		return personGroupChanged;
	}

	/** just for test purposes */
	final Map<PoGroup, Boolean> getGroupChanged() {
		return groupChanged;
	}

	/** just for test purposes */
	final Map<MyParentGroup, Boolean> getParentGroupChanged() {
		return parentGroupChanged;
	}

	/** just for test purposes */
	final Map<PoPerson, List<String>> getFailedPersonsMessagesMap() {
		return failedPersonsMessagesMap;
	}

	/** just for test purposes */
	final Map<PoGroup, String> getFailedGroupsMessagesMap() {
		return failedGroupsMessagesMap;
	}

	/** just for test purposes */
	final List<PoPerson> getPersonsToBeDeleted() {
		return personsToBeDeleted;
	}

	/** just for test purposes */
	final List<PoGroup> getGroupsToBeDeleted() {
		return groupsToBeDeleted;
	}

}
