package at.workflow.webdesk.po.impl;

import java.util.List;

import org.apache.log4j.Logger;

import at.workflow.webdesk.po.PoMultiClientService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoClientDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.util.PoClientPrefixUtil;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This class defines behavior of methods handling multi client specific situations.
 * 
 * @author sdzuban 03.09.2012
 */
public class PoMultiClientServiceImpl implements PoMultiClientService {

    private static final Logger logger = Logger.getLogger(PoMultiClientServiceImpl.class);
	
    private PoClientDAO clientDao;
	private PoOrganisationService orgService;
	
	private static final String MSG = "There is already one client using the chosen prefix. Please select another one.";
	
	/** {@inheritDoc} */
	@Override
	public void updateGroupShortNamePrefix(PoClient client, String oldPrefix, String newPrefix) {
		
		if (PoClientPrefixUtil.arePrefixesToSwap(client, oldPrefix, newPrefix)) {
			PoClient other = findClientByGroupShortNamePrefix(newPrefix);
			if (other == null || other.equals(client)) {
				if (other == null) { // client was not updated yet
					client.setGroupShortNamePrefix(newPrefix);
					orgService.saveClient(client);
				}
				List<PoGroup> groups = orgService.findGroupsFromClientF(client, DateTools.now());
				for (PoGroup group : groups) {
					group.setShortName(PoClientPrefixUtil.getUpdatedString(group.getShortName(), oldPrefix, newPrefix));
					orgService.saveGroup(group);
				}
				logger.info("Updated prefixes of " + groups.size() + " groups from " + oldPrefix + " to " + newPrefix);
			} else {
				logger.error(MSG);
				throw new RuntimeException(MSG);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void updatePersonUserNamePrefix(PoClient client, String oldPrefix, String newPrefix) {

		if (PoClientPrefixUtil.arePrefixesToSwap(client, oldPrefix, newPrefix)) {
			PoClient other = findClientByPersonUserNamePrefix(newPrefix);
			if (other == null || other.equals(client)) {
				if (other == null) { // client was not updated yet
					client.setPersonUserNamePrefix(newPrefix);
					orgService.saveClient(client);
				}
				List<PoPerson> persons = orgService.findPersonsOfClientF(client, DateTools.now());
				for (PoPerson person : persons) {
					person.setUserName(PoClientPrefixUtil.getUpdatedString(person.getUserName(), oldPrefix, newPrefix));
					orgService.updatePerson(person);
				}
				logger.info("Updated prefixes of " + persons.size() + " persons from " + oldPrefix + " to " + newPrefix);
			} else {
				logger.error(MSG);
				throw new RuntimeException(MSG);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void updatePersonEmployeeIdPrefix(PoClient client, String oldPrefix, String newPrefix) {

		if (PoClientPrefixUtil.arePrefixesToSwap(client, oldPrefix, newPrefix)) {
			PoClient other = findClientByPersonEmployeeIdPrefix(newPrefix);
			if (other == null || other.equals(client)) {
				if (other == null) { // client was not updated yet
					client.setPersonEmployeeIdPrefix(newPrefix);
					orgService.saveClient(client);
				}
				List<PoPerson> persons = orgService.findPersonsOfClientF(client, DateTools.now());
				for (PoPerson person : persons) {
					person.setEmployeeId(PoClientPrefixUtil.getUpdatedString(person.getEmployeeId(), oldPrefix, newPrefix));
					orgService.updatePerson(person);
				}
				logger.info("Updated prefixes of " + persons.size() + " persons from " + oldPrefix + " to " + newPrefix);
			} else {
				logger.error(MSG);
				throw new RuntimeException(MSG);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public PoClient findClientByGroupShortNamePrefix(String prefix) {
		return clientDao.findClientByGroupShortNamePrefix(prefix);
	}

	/** {@inheritDoc} */
	@Override
	public PoClient findClientByPersonUserNamePrefix(String prefix) {
		return clientDao.findClientByPersonUserNamePrefix(prefix);
	}

	/** {@inheritDoc} */
	@Override
	public PoClient findClientByPersonEmployeeIdPrefix(String prefix) {
		return clientDao.findClientByPersonEmployeeIdPrefix(prefix);
	}


	public void setClientDao(PoClientDAO clientDao) {
		this.clientDao = clientDao;
	}
	
	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}
}
