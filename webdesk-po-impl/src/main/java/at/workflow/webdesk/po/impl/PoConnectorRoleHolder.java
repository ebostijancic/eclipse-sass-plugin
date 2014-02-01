package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoUtilService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.date.DateTools;

public abstract class PoConnectorRoleHolder extends PoAbstractDestinationConnector {

	protected Document configurationDocument;
	
	protected PoOrganisationService organisationService;
	protected PoRoleService roleService;
	private PoUtilService utilService;
	protected Map<PoRole, PoConnectorRoleHolderDefinition> roleHoldersDefinitionMap = new HashMap<PoRole, PoConnectorRoleHolderDefinition>();	//Defined Roles for Connector

	protected Date today = new Date();						//Standard Date for validFrom Date

	/** Spring XML noise. Do not call. */
	public void setRoleService(PoRoleService roleService) {
		this.roleService = roleService;
	}
	/** Spring XML noise. Do not call. */
	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
	/** Spring XML noise. Do not call. */
	public void setUtilService(PoUtilService utilService) {
		this.utilService = utilService;
	}

	protected PoUtilService  getUtilService() {
		return utilService;
	}
	
	/**
	 * @param group
	 * @param fieldnames	List of Fieldnames
	 * @param roleHoldersDefintionMap Definend Roles for Connecoctor
	 * 									Key...PoRole
	 * 									Value...PoConnectorRoleHolder
	 * @param valueMap				Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * @return Map					Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * Find Roleholders (Persons) for definend Fields and put the Values (Username, TaId or EmployeeId) in
	 * the valueMap
	 */
	protected Map<String, Object> findRoleHolders (PoGroup group, List<String> fieldnames, 
			Map<PoRole, PoConnectorRoleHolderDefinition> roleHoldersDefintionMap, Map<String, Object> valueMap) {
		return this.findRoleHolders (group, null, fieldnames, roleHoldersDefintionMap, valueMap);
	}
	/**
	 * @param person
	 * @param fieldnames	List of Fieldnames
	 * @param roleHoldersDefintionMap Definend Roles for Connecoctor
	 * 									Key...PoRole
	 * 									Value...PoConnectorRoleHolder
	 * @param valueMap				Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * @return ValueMap				Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * @param fieldnames
	 * @param roleHoldersDefintionMap
	 * @param valueMap
	 * @return Map					Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * Find Roleholders (Persons) for definend Fields and put the Values (Username, TaId or EmployeeId) in
	 * the valueMap
	 */
	protected Map<String, Object>  findRoleHolders (PoPerson person, List<String> fieldnames, Map<PoRole, 
			PoConnectorRoleHolderDefinition> roleHoldersDefintionMap, Map<String, Object> valueMap) {
		return this.findRoleHolders (null, person, fieldnames, roleHoldersDefintionMap, valueMap);
	}
	
	/**
	 * @param group
	 * @param person
	 * @param fieldnames	List of Fieldnames
	 * @param roleHoldersDefintionMap Definend Roles for Connecoctor
	 * 									Key...PoRole
	 * 									Value...PoConnectorRoleHolder
	 * @param valueMap				Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * @return ValueMap				Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * @param fieldnames
	 * @param roleHoldersDefintionMap
	 * @param valueMap
	 * @return Map					Values for Output
	 * 									Key...String (Fieldname)
	 * 									Value
	 * Find Roleholders (Persons) for definend Fields and put the Values (Username, TaId or EmployeeId) in
	 * the valueMap
	 */
	private Map<String, Object> findRoleHolders (PoGroup group, PoPerson person, List<String> fieldnames, 
			Map<PoRole, PoConnectorRoleHolderDefinition> roleHoldersDefintionMap, Map<String, Object> valueMap) {

		
		// I do not understand why iteration is done over parameter 
		// whereas connector definition are read from instance variable 
		for ( PoRole role : roleHoldersDefinitionMap.keySet() ) {
			
			role = this.roleService.getRole(role.getUID());
			
			PoConnectorRoleHolderDefinition connectorRoleHolder = this.roleHoldersDefinitionMap.get(role);
			List<PoRoleHolderPerson> currentRoleHoldersList = null; 
			
			// Iterate max. Roleholders (defined in the Config-File)
			for (int i = 0; i <= connectorRoleHolder.getMaximumRanking();i++) {
				String key = "$roleHolder_" + role.getName() + "_" + (i+1);
				if ( fieldnames.contains(key) ) {
					String value = "";
					if ( currentRoleHoldersList == null ) {
						if ( group != null ) {
							currentRoleHoldersList = roleService.findRoleHolderPersonWithCompetence4Group(role, group, this.today);
						}
						if ( person != null ) {
							currentRoleHoldersList = roleService.findRoleHolderPersonWithCompetence4Person(role, person, this.today);
						}
					}
					if ( i < currentRoleHoldersList.size() ) {
						PoRoleHolderPerson roleHolderPerson = currentRoleHoldersList.get(i);

						//Person found return the value 
						if ( roleHolderPerson != null ) {
							if ( connectorRoleHolder.getKey().equals(PoConnectorRoleHolderDefinition.KEY_EMPLOYEEID) ) {
								value = roleHolderPerson.getPerson().getEmployeeId();
							} else if ( connectorRoleHolder.getKey().equals( PoConnectorRoleHolderDefinition.KEY_TAID) )  {
								value = roleHolderPerson.getPerson().getTaID();
							} else if ( connectorRoleHolder.getKey().equals( PoConnectorRoleHolderDefinition.KEY_USERNAME) )  {
								value = roleHolderPerson.getPerson().getUserName();
							}
						}
					}
					valueMap.put(key, value);
				}
			}
		}

		return valueMap;
	}

	/**
	 * @param group
	 * @param roleHoldersKeyValueMap	Map of Keyvalues from Source-Connector for Roleholders
	 * 										Key...String (fieldName) ($roleHolder_<Rolename>_<Ranking 1...>
	 * Find Roleholders for person/group from Source Connector and update / delete the roleholder and ranking 
	 */
	protected void updateRoleHolder (PoGroup group, Map<String, String> roleHoldersKeyValueMap) {
		updateRoleHolder (group, null, roleHoldersKeyValueMap);
	}

	/**
	 * @param person
	 * @param roleHoldersKeyValueMap	Map of Keyvalues from Source-Connector for Roleholders
	 * 										Key...String (fieldName) ($roleHolder_<Rolename>_<Ranking 1...>
	 * Find Roleholders for person/group from Source Connector and update / delete the roleholder and ranking 
	 */
	protected void updateRoleHolder (PoPerson person, Map<String, String> roleHoldersKeyValueMap) {
		updateRoleHolder (null, person, roleHoldersKeyValueMap);
	}
	/**
	 * @param group
	 * @param person
	 * @param roleHoldersKeyValueMap	Map of Keyvalues from Source-Connector for Roleholders
	 * 										Key...String (fieldName) ($roleHolder_<Rolename>_<Ranking 1...>
	 * Find Roleholders for person/group from Source Connector and update / delete the roleholder and ranking 
	 */
	private void updateRoleHolder (PoGroup group, PoPerson person, Map<String, String> roleHoldersKeyValueMap) {
		
		String logHeader = "Update Roleholders for";

		if ( group != null ) {
			logHeader = logHeader + " group=" + group.getShortName();
		}
		if ( person != null ) {
			logHeader = logHeader + " person=" + person.getFirstName() + " " + person.getLastName();
		}
		
		for ( PoRole role : roleHoldersDefinitionMap.keySet() ) {
			StringBuffer logText = new StringBuffer(logHeader);
			
			role = this.roleService.getRole(role.getUID());
			
			PoConnectorRoleHolderDefinition connectorRoleHolder = this.roleHoldersDefinitionMap.get(role);

			try {
				List<PoPerson> newRoleHolderPersonsList = new ArrayList<PoPerson>();
				List<String> roleHoldersCheckList = new ArrayList<String>();
				boolean throwErrorWhenPersonNotFound = true;
				if ( connectorRoleHolder.isDeleteCurrentRoleHolderWhenPersonNotFound() == true ) 
					throwErrorWhenPersonNotFound = false;
				for ( int i = 1; i <= connectorRoleHolder.getMaximumRanking(); i++) {
					PoPerson newRoleHolderPerson = null;
					String key = "$roleHolder_" + role.getName() + "_" + i;
					String checkKey = null;
					
					// Is current "Roleholder-Ranking" definend in Source-Connector? 
					if ( roleHoldersKeyValueMap.containsKey(key) ) {

						// Find PoPerson and 
						// - 	put it in the "New Roleholder List", 
						//		the index in this List corresponds with the ranking of the role
						// -	"CheckList" this List definend the Roleholder to Check with the current Roleholder 
						//		and new Roleholders (when the Index has valid value (String))
						newRoleHolderPerson = findNewRoleHolderByKey ( logHeader, key, connectorRoleHolder.getKey(),
								roleHoldersKeyValueMap.get(key), throwErrorWhenPersonNotFound );
						newRoleHolderPersonsList.add( newRoleHolderPerson );
						roleHoldersCheckList.add(key + "=" + roleHoldersKeyValueMap.get(key) );
					} else {
						newRoleHolderPersonsList.add(newRoleHolderPerson);
						roleHoldersCheckList.add( checkKey );
					}
				}
				
				// Roleholders to update?
				if ( roleHoldersCheckList.size() > 0 ) {
					List<PoRoleHolderPerson> currentRoleHoldersList = new ArrayList<PoRoleHolderPerson>();
					if ( group != null ) {
						currentRoleHoldersList = roleService.findRoleHolderPersonWithCompetence4Group(role, group, this.today);
					}
					if ( person != null ) {
						currentRoleHoldersList = roleService.findRoleHolderPersonWithCompetence4Person(role, person, this.today);
					}
					
					filterOutRoleHolderDeputies(currentRoleHoldersList);
					
					updateRoleHolderUpdate (group, person, role, roleHoldersCheckList, currentRoleHoldersList, newRoleHolderPersonsList, logText);
				}
			} catch ( PoRuntimeException e) {
				logger.error (logHeader + ", Role " + role.getName() + " no updates! (Error " + e.getMessage() + ")" );
			}
		}
	}
	
	private void filterOutRoleHolderDeputies(List<PoRoleHolderPerson> rhpList) {
		List<PoRoleHolderPerson> toRemove = new ArrayList<PoRoleHolderPerson>();
		for (PoRoleHolderPerson rhp : rhpList) {
			if (rhp.getDeputy()!=null) {
				toRemove.add(rhp);
			}
		}
		rhpList.removeAll(toRemove);
	}
	
	/**
	 * @param group
	 * @param person
	 * @param role
	 * @param roleHoldersCheckList	List. This List definend the Roleholder to Check with the current Roleholder 
	 *								and new Roleholders (when the Index has valid value (String))
	 *								Value...(String) oder null (nothing to do)
	 * @param currentRoleHoldersList List Current Roleholders (from Roleservice)
	 * 								Value...PoRoleHolder
	 * @param newRoleHolderPersonsList List with new Roleholders / Persons
	 * 								Value...PoPerson
	 * @param logHeader				StringBuffer Debug Information for Logging
	 */
	private void updateRoleHolderUpdate (PoGroup group, PoPerson person, PoRole role, 
			List<String> roleHoldersCheckList, List<PoRoleHolderPerson> currentRoleHoldersList, 
			List<PoPerson> newRoleHolderPersonsList, StringBuffer logHeader) {
		
		Date toDate = new Date(DateTools.INFINITY_TIMEMILLIS);

		int i = 0;	//ranking for Role
		for( String fieldName : roleHoldersCheckList) {
			
			StringBuffer logText = new StringBuffer ( logHeader.toString() );
			if ( logger.isDebugEnabled() ) {
				logText.append( ", Role=" + role.getName() + ", ranking=" + (i+1) );
			}
			
			// Update for Roleholder needed?
			if ( fieldName != null ) {
				if ( logger.isDebugEnabled() ) {
					logText.append( ", fieldName=" + fieldName );
				}
				//
				if ( logger.isDebugEnabled() ) {
					logText.append( ", current Roleholder=" );
				}
				
				//Value for Current Roleholder
				String currentPersonUID = "";
				PoRoleHolderPerson currentRoleHolderPerson = null;
				if (i < currentRoleHoldersList.size()) {
					currentRoleHolderPerson = currentRoleHoldersList.get(i);
					currentPersonUID = currentRoleHolderPerson.getPerson().getUID();
					if ( logger.isDebugEnabled() ) {
						logText.append( currentRoleHolderPerson.getPerson().getLastName() 
								+ " " + currentRoleHolderPerson.getPerson().getFirstName() );
					}
				}


				//Value for new Roleholder
				if ( logger.isDebugEnabled() ) {
					logText.append( ", new Roleholder=" );
				}
				String newPersonUID = "";
				PoPerson newRoleHolderPerson = null;
				if (i < newRoleHolderPersonsList.size()) {
					newRoleHolderPerson = newRoleHolderPersonsList.get(i);
					if ( newRoleHolderPerson != null ) {
						newPersonUID = newRoleHolderPerson.getUID();
						if ( logger.isDebugEnabled() ) {
							logText.append( newRoleHolderPerson.getLastName() + " " + newRoleHolderPerson.getFirstName() );
						}
					}
				}				

				// Check differences
				if ( !newPersonUID.equals( currentPersonUID ) ) {
					if ( !currentPersonUID.equals("") ) {
						if ( logger.isDebugEnabled() ) {
							logText.append( ", delete Current Roleholder" );
						}
						roleService.removePersonFromRole( currentRoleHolderPerson );
					} 
					if ( !newPersonUID.equals("") ) {
						if ( logger.isDebugEnabled() ) {
							logText.append( ", update new Roleholder" );
						}
						if ( group != null ) {
							roleService.assignRoleWithGroupCompetence(role, newRoleHolderPerson, group, this.today, toDate, i+1);
						}
						if ( person != null ) {
							roleService.assignRoleWithPersonCompetence( role, newRoleHolderPerson, person, this.today, toDate, i+1);
						}
					}				
				} else {
					if ( logger.isDebugEnabled() ) {
						logText.append( ", no changes" );
					}
				}
			}
			if ( logger.isDebugEnabled() ) {
				logger.debug( logText );
			}
			i++;
		}
	}

	/**
	 * @param logHeader		Logging Text
	 * @param fieldName		Current Fieldname from Source Connector
	 * @param keyType		Keytype
	 * @param keyRoleholder	
	 * @param throwErrorWhenPersonNotFound
	 * @return
	 * Find Person
	 */
	private PoPerson findNewRoleHolderByKey (String logHeader, String fieldName, String keyType, 
			String keyRoleholder, boolean throwErrorWhenPersonNotFound) {
		
		PoPerson person = null;
		
		if ( !"".equals(keyRoleholder) && keyRoleholder!=null ) {
			if ( keyType.equals(PoConnectorRoleHolderDefinition.KEY_EMPLOYEEID) ) {
				person = this.organisationService.findPersonByEmployeeId( keyRoleholder );
			} else if ( keyType.equals( PoConnectorRoleHolderDefinition.KEY_TAID) )  {
				person = this.organisationService.findPersonByTaId( keyRoleholder );
			} else if ( keyType.equals( PoConnectorRoleHolderDefinition.KEY_USERNAME) )  {
				person = this.organisationService.findPersonByUserName( keyRoleholder );
			}
			if ( person == null ) {
				String logText = logHeader + ", fieldName=" + fieldName + ", with Key=" 
					+ keyRoleholder + " not found!";
				logger.error(logText);
				if ( throwErrorWhenPersonNotFound ) {
					throw new PoRuntimeException ( logText );
				}
			}
		}
		return person;
	}
	
	/**
	 * @param element
	 * Load Defintion from Config XML
	 */
	protected void loadRoleDefintionFromConfig (Element element ){
		this.roleHoldersDefinitionMap = new HashMap<PoRole, PoConnectorRoleHolderDefinition>();

		//---No Element found nothing to do!
		if ( element == null || element.getChild("roleHolders")==null ) return;
		List<Element> elements = element.getChild("roleHolders").getChildren("roleHolder");

		if ( elements == null ) return;

		for (Element e : elements) { 
			//element = (Element) XPath.selectSingleNode(jdomDoc,elementName);
			//elementValue = element.getAttribute("value").getValue();
			//role = this.roleService.getRole(elementValue);
			Element roleUIDElement = e.getChild("roleUID");
			String roleUID = roleUIDElement.getAttributeValue("value");
			if ( !roleUID.equals("")) {
				PoRole role = this.roleService.getRole(roleUID);
				if ( role != null) {
					PoConnectorRoleHolderDefinition connectorRoleHolder = new PoConnectorRoleHolderDefinition();
					connectorRoleHolder.setRole(role);
					connectorRoleHolder.setMaximumRanking(this.getIntValueFromConfig (e, "maximumRanking"));
					connectorRoleHolder.setKey(e.getChildText("key"));
					connectorRoleHolder.setDeleteCurrentRoleHolderWhenPersonNotFound(this.getBooleanValueFromConfig(e,"deleteCurrentRoleHolderWhenPersonNotFound"));
					this.roleHoldersDefinitionMap.put(role, connectorRoleHolder);
				} else {
					if (logger.isDebugEnabled() ) logger.debug("PoGroupConnector, role with UID " + roleUID + " not found!");
				}
			}
		}		
	}

	/**
	 * @param e
	 * @param name
	 * @return
	 */
	private int getIntValueFromConfig (Element e, String name) {
		int value = 0;
		try {
			if ( e.getChildText(name) != null ) 
				value = Integer.parseInt( e.getChildText(name));
		} catch (  NumberFormatException ne) {
			logger.error("Wrong Field-Format Name=" + name + ", Value=" + e.getChildText(name));
			logger.error(ne);
			value = 0;
		}
		return value;
	}
	/**
	 * @param e
	 * @param name
	 * @return
	 */
	private boolean getBooleanValueFromConfig (Element e, String name) {
		boolean value = false;
		if ( e.getChildText(name) != null && e.getChildText(name).equals("true") ) value = true;
		return value;
	}

	/**
	 * @param fieldNamesList
	 * @return
	 */
	protected List<String> addRoleHolderFieldNames (List<String> fieldNamesList) {
		
		if ( this.roleHoldersDefinitionMap.size() > 0 ) {
			Collection<PoRole> roles = this.roleHoldersDefinitionMap.keySet();
			for (PoRole role : roles) {
				PoConnectorRoleHolderDefinition connectorRoleHolder 
					= this.roleHoldersDefinitionMap.get(role);
				for ( int i = 1; i <= connectorRoleHolder.getMaximumRanking(); i++) {
					fieldNamesList.add("$roleHolder_" + role.getName() + "_" + i);
				}
			}
		}
		return fieldNamesList;
	}
}
