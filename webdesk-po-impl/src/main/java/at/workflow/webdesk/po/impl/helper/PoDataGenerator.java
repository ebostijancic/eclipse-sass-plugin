package at.workflow.webdesk.po.impl.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoImportExportService;
import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class PoDataGenerator implements DataGenerator {
	Logger logger = Logger.getLogger(this.getClass());

	private PoOrganisationService poOrganisationService;
	private PoActionService poActionService;
	private PoActionPermissionService permissionService;
	private PoRoleService poRoleService;
	private PoImportExportService poImportExportService;
	private PoMenuService poMenuService;
	private PoPasswordService passwordService;

	private String classPath;
	private boolean createMenus;

	public PoDataGenerator(String classPath) {
		this(classPath, false);
	}

	public PoDataGenerator(String classPath, boolean createMenus) {
		this.classPath = classPath;
		this.createMenus = createMenus;
	}

	@Override
	public void create(ApplicationContext appCtx) throws IOException {
		// get Services 
		poOrganisationService = (PoOrganisationService) appCtx.getBean("PoOrganisationService");
		poActionService = (PoActionService) appCtx.getBean("PoActionService");
		permissionService = (PoActionPermissionService) appCtx.getBean("PoActionPermissionService");
		poRoleService = (PoRoleService) appCtx.getBean("PoRoleService");
		poMenuService = (PoMenuService) appCtx.getBean("PoMenuService");
		poImportExportService = (PoImportExportService) appCtx.getBean("PoImportExportService");
		passwordService = (PoPasswordService) appCtx.getBean("PoPasswordService");

		// dokument laden
		Resource importFileRes = new ClassPathResource(classPath);
		URL url = importFileRes.getURL();
		InputStream in = url.openStream();
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		logger.info("Reading XML data from " + url);

		try {
			Document doc = builder.build(in);

			// mandant anlegen
			createClients(doc);

			// orgstructure anlegen
			createOrgStructures(doc);

			// create groups
			createGroups(doc);

			// create Persons
			createPersons(doc);

			// create actions
			createActions(doc);

			// create roles
			createRoles(doc);

			// create Menus
			if (createMenus) {
				this.createMenus(doc);
			}

			// Permissions anlegen
			createActionPermissions(doc);

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Generation of Testdata was not successful", e);
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) { /* stream might have been closed by SAXBuilder. */
			}
		}
		logger.info("**   Creation has finished.");
	}

	public void createGroups(Document doc) throws Exception {
		logger.info("**   Create Groups");

		poImportExportService.importOrganisationStructure(XMLTools.convertToW3cDoc(doc), null);
	}

	@SuppressWarnings("rawtypes")
	public void createActions(Document doc) throws Exception {
		// actions anlegen
		logger.info("**   Create Actions");

		XPath xpath = XPath.newInstance("//actions/action");
		List results = xpath.selectNodes(doc);
		Iterator myItr = results.iterator();
		while (myItr.hasNext()) {
			Element myElem = (Element) myItr.next();

			String actionName = myElem.getChildText("name");
			int actionType = Integer.parseInt(myElem.getChildText("actiontype"));
			PoAction action = poActionService.findActionByNameAndType(actionName, actionType);

			if (action == null) {
				action = new PoAction();
				PoAction parent = null;
				if (actionType == PoConstants.ACTION_TYPE_CONFIG) {
					// set parent
					String parentActionName = myElem.getChildText("parent");
					parent = poActionService.findActionByNameAndType(parentActionName, PoConstants.ACTION_TYPE_ACTION);
					parent.addChild(action);
				}
				action.setActionType(actionType);
				action.setName(actionName);
				action.setCaption(myElem.getChildText("caption"));
				if (myElem.getChild("defaultpermissiontype") != null) {
					action.setDefaultViewPermissionType(Integer.parseInt(myElem.getChildText("defaultpermissiontype")));
				}
				action.setActionFolder(myElem.getChildText("actionfolder"));
				action.setProcessDefId(myElem.getChildText("processdefid"));
				action.setImageSet(myElem.getChildText("imageset"));
				action.setImage(myElem.getChildText("image"));
				action.setDescription(myElem.getChildText("description"));
				try {
					logger.debug("create Action=" + action);
					poActionService.saveAction(action);
					poActionService.createPrimaryTextModulesOfAction(action);
				}
				catch (Exception e) {
					logger.error(e, e);
				}
			} else {
				logger.debug("action = " + action + " already present!");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void createOrgStructures(Document doc) throws Exception {
		logger.info("**   Create Orgstructure");

		List<PoOrgStructure> orgStructures = new ArrayList<PoOrgStructure>();
		PoOrgStructure myStructure = null;
		XPath xpath = XPath.newInstance("//structures/structure");
		List results = xpath.selectNodes(doc);
		Iterator myItr = results.iterator();
		while (myItr.hasNext()) {
			Element myElem = (Element) myItr.next();
			String structureName = myElem.getAttributeValue("name");
			String clientName = myElem.getChildText("client");
			PoClient myClient = poOrganisationService.findClientByName(clientName);

			if (myClient != null) {

				myStructure = poOrganisationService.findOrgStructureByName(myClient, structureName);
				if (myStructure == null) {
					myStructure = new PoOrgStructure();
					myStructure.setName(structureName);
					if (myElem.getChildText("hierarchy").equals("true")) {
						myStructure.setHierarchy(true);
					} else
						myStructure.setHierarchy(false);

					myStructure.setDescription(myElem.getChildText("description"));

					myStructure.setOrgType(new Integer(myElem.getChildText("orgtype")).intValue());
					myStructure.setClient(myClient);

					logger.debug("create structure=" + myStructure);
					orgStructures.add(myStructure);
				} else {
					logger.debug("structure=" + myStructure + " already present!");
				}
			}
		}
		// store first the org. hierarchy
		for (PoOrgStructure structure : orgStructures)
			if (structure.getOrgType() == PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY) {
				structure.setHierarchy(true);
				structure.setAllowOnlySingleGroupMembership(true);
				poOrganisationService.saveOrgStructure(structure);
			}
		// than the others
		for (PoOrgStructure structure : orgStructures)
			if (structure.getOrgType() != PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY)
				poOrganisationService.saveOrgStructure(structure);
	}

	@SuppressWarnings("rawtypes")
	public void createPersons(Document doc) throws Exception {
		// personen anlegen
		logger.info("**   Create Persons");

		XPath xpath = XPath.newInstance("/exportdata/persons/person");
		List results = xpath.selectNodes(doc);
		Iterator myItr = results.iterator();
		while (myItr.hasNext()) {
			Element myElem = (Element) myItr.next();

			String employeeId = myElem.getChildText("employeeid");
			PoPerson person = poOrganisationService.findPersonByEmployeeId(employeeId, new Date());

			if (person == null) {
				person = new PoPerson();

				person.setFirstName(myElem.getChildText("firstname"));
				person.setLastName(myElem.getChildText("lastname"));
				person.setTaID(myElem.getChildText("taId"));
				person.setEmployeeId(employeeId);
				person.setUserName(myElem.getChildText("username"));

				String eMail = myElem.getChildText("mailaddress");
				if (eMail != null && eMail.length() > 0)
					person.setEmail(eMail);

				PoGroup group = poOrganisationService.findGroupByShortName(myElem.getChildText("group"));

				String clientName = myElem.getChildText("client");
				PoClient client;
				if (clientName != null && clientName.length() > 0) {
					client = poOrganisationService.findClientByName(clientName);
				} else {
					logger.debug("client was not provided, so take client from provided group!");
					client = poOrganisationService.getClient(group.getClient().getUID());
				}
				person.setClient(client);

				logger.debug("create Person=" + person);
				poOrganisationService.savePerson(person, group);

				final String password = myElem.getChildText("password");
				if (password != null && password.length() > 0) {
					passwordService.setNewPassword(person, null, password);
				}

			} else {
				// 
				logger.debug("Person=" + person + " already present!");
			}

		}
	}

	public void createRoles(Document doc) throws Exception {
		logger.info("**   Create Roles");

		poImportExportService.importRoles(XMLTools.convertToW3cDoc(doc));
	}

	@SuppressWarnings("rawtypes")
	public void createMenus(Document doc) throws Exception {
		// Menu anlegen
		logger.info("**   Create Menus");

		PoClient client = null;
		XPath xpath = XPath.newInstance("//menus/menuitem");
		List results = xpath.selectNodes(doc);
		Iterator menuIterator = results.iterator();
		while (menuIterator.hasNext()) {
			try {
				Element item = (Element) menuIterator.next();
				PoMenuItem mi = new PoMenuItem();
				String clientName = item.getChildText("client");
				client = poOrganisationService.findClientByName(clientName);
				mi.setClient(client);
				PoAction act = poActionService.findActionByNameAndType(
						item.getChildText("name"),
						new Integer(item.getChildText("actiontype")).intValue());
				
				if (act == null) {
					mi.setName(item.getChildText("name"));
				} else {
					mi.setAction(act);
					mi.setName(act.getName());
				}
				mi.setRanking(new Integer(item.getChildText("ranking")).intValue());
				PoMenuItem mip = poMenuService.findMenuItemByName(item.getChildText("parent"));
				if (mip != null)
					mi.setParent(mip);
				poMenuService.saveMenuItem(mi);

				if (act == null)
					mi.setTextModuleKey("po_editMenuTree.act_menufolder_" + client.getName() + "_" + mi.getUID());

				poMenuService.saveMenuItem(mi);
			}
			catch (Exception e) {
				logger.error("Menüeintrag konnte nicht angelegt werden. ", e);
			}
		}

		// this is necessary to create the i18ns for all folders!
		poMenuService.refreshTextModulesOfFolders(client);
	}

	@SuppressWarnings("rawtypes")
	public void createClients(Document doc) throws Exception {
		// mandant anlegen

		XPath xpath = XPath.newInstance("//clients/client");
		List results = xpath.selectNodes(doc);
		String myClientName;
		PoClient myClient = null;
		Iterator itr = results.iterator();
		Element elem;
		logger.info("**   Create Clients");

		while (itr.hasNext()) {
			elem = (Element) itr.next();

			myClientName = elem.getAttributeValue("name");
			myClient = poOrganisationService.findClientByName(myClientName);
			if (myClient == null) {
				myClient = new PoClient();
				myClient.setName(myClientName);

				logger.debug("create client=" + myClient);
				poOrganisationService.saveClient(myClient);
			} else {
				logger.error("Client already present!");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void createActionPermissions(Document doc) throws Exception {

		logger.info("**   Creating Action Permissions");

		PoClient client = null;

		PoRole role = null;
		XPath xpath = XPath.newInstance("//actionpermissions/actionpermission");
		List results = xpath.selectNodes(doc);
		Iterator apIterator = results.iterator();
		Element item = null;
		while (apIterator.hasNext()) {
			try {
				item = (Element) apIterator.next();

				String clientName = item.getChildText("client");
				String groupName = item.getChildText("group");
				String roleName = item.getChildText("role");
				String actionName = item.getChildText("action");
				int actionType = new Integer(item.getChildText("actiontype")).intValue();

				// actionpermission for group
				if (groupName != null && !groupName.equals("")) {

					PoAction which_action = poActionService.findActionByNameAndType(actionName, actionType);
					if (which_action != null) {
						PoGroup group = poOrganisationService.findGroupByShortName(groupName);
						boolean itc = false;
						if (item.getChild("inherittochilds") != null)
							itc = new Boolean(item.getChildText("inherittochilds")).booleanValue();
						int vpt = new Integer(item.getChildText("viewpermissiontype")).intValue();
						permissionService.assignPermission(which_action, group, new Date(), PoConstants.getInfDate(), itc, vpt);
					} else {
						logger.error("Action vom Typ " + actionType + " mit Namen " + actionName + " konnte nicht gefunden werden!");
					}
				}

				// actionpermission for client
				if (clientName != null && !clientName.equals("")) {

					PoAction which_action = poActionService.findActionByNameAndType(actionName, actionType);
					if (which_action != null) {
						client = poOrganisationService.findClientByName(clientName);
						int vpt = new Integer(item.getChildText("viewpermissiontype")).intValue();
						permissionService.assignPermission(which_action, client, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), vpt);
					} else {
						logger.error("Action vom Typ " + actionType + " mit Namen " + actionName + " konnte nicht gefunden werden!");
					}
				}

				// actionpermission for role
				if (roleName != null && !roleName.equals("")) {

					PoAction which_action = poActionService.findActionByNameAndType(actionName, actionType);
					if (which_action != null) {
						role = poRoleService.findRoleByName(roleName).get(0);
						int vpt = new Integer(item.getChildText("viewpermissiontype")).intValue();
						boolean itc = false;
						if (item.getChild("viewinherittochilds") != null)
							itc = new Boolean(item.getChildText("viewinherittochilds")).booleanValue();
						permissionService.assignPermission(which_action, role, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), vpt, false, itc);
					} else {
						logger.error("Action vom Typ " + actionType + " mit Namen " + actionName + " konnte nicht gefunden werden!");
					}
				}

			}
			catch (Exception e) {
				logger.error("Permission " + item.getChildText("action") + " konnte nicht angelegt werden. ", e);
			}
		}
	}

}
