package at.workflow.webdesk.po.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoImportExportService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.tools.XMLTools;

public class PoImportExportServiceImpl implements PoImportExportService {
	public final static Logger logger = Logger.getLogger(PoImportExportServiceImpl.class.getName());
	
	private PoOrganisationService orgService;
	private PoRoleService roleService;
	
	@Override
	public void importOrganisationStructure(Document w3cDoc) {
		try {
			org.jdom.Document doc = XMLTools.convertToJdomDoc(w3cDoc);
			XPath xpath = XPath.newInstance("/exportdata/clients/client");
	        List results = xpath.selectNodes(doc);
	        String clientName = ((Element)results.get(0)).getAttributeValue("name");
	        
	        PoClient myClient = orgService.findClientByName(clientName);
	        if (myClient==null) {
	        	myClient = (PoClient) orgService.loadAllClients().get(0);
	        }
	        importOrganisationStructure(XMLTools.convertToW3cDoc(doc), myClient);
	        		
	        
		} catch (Exception e) {
			this.logger.error(e);
			throw new PoRuntimeException(e);
		}
	}

	@Override
	public void importOrganisationStructure(Document w3cDoc, PoClient client) {
		XPath xpath;
        List results;        
        org.jdom.Document doc = XMLTools.convertToJdomDoc(w3cDoc);
        
        try {
	        xpath = XPath.newInstance("/exportdata/groups/group");
	        results = xpath.selectNodes(doc);
	        Iterator myItr = results.iterator();
	        
	        while(myItr.hasNext()) {
	            Element myElem = (Element)myItr.next();
	            
	            String groupShortName = myElem.getAttributeValue("name");
	            String clientName = myElem.getChildText("client");
	            PoClient actClient = client == null ? orgService.findClientByName(clientName):client;
	            
	            PoGroup myGroup = orgService.findGroupByShortName(groupShortName, actClient);
	            if (myGroup == null) { 
	            	logger.debug("New Group = " + groupShortName);
	            	myGroup = new PoGroup();
		            myGroup.setShortName(groupShortName.trim());
	            }
	            
	            // fri_2010-11-22: put description into name when name sub-element does not exist
	            String name = myElem.getChildText("name");
	            name = name != null ? name.trim() : null;
	            String desc = myElem.getChildText("description");
	            desc = desc != null ? desc.trim() : null;
	            myGroup.setName(name != null && name.length() > 0 ? name : desc);
	            myGroup.setDescription(desc);
	            
	            String orgStructureName = myElem.getChildText("structure");
	            PoOrgStructure structure = orgService.findOrgStructureByNameAndClient(actClient, orgStructureName);
	            myGroup.setOrgStructure(structure);
	            myGroup.setClient(actClient);
	            logger.debug("try to update group=" + myGroup);
	            
	            final String parentGroupShortName = myElem.getChildText("parent");
	            
	            String topLevel = myElem.getChildText("toplevel");
	            if (topLevel == null)	{
		            if (parentGroupShortName == null)
		            	myGroup.setTopLevel(true);
	            }
	            else	{
		            boolean topLevelValue = Boolean.valueOf(topLevel);
	            	myGroup.setTopLevel(topLevelValue);
	            }
	            
	            orgService.saveGroup(myGroup);
	            
	            if (parentGroupShortName != null && parentGroupShortName != "") {
	            	
	            	// check if group has already parentgroup
	            	PoParentGroup myParentGroup = orgService.getParentGroup(myGroup);
	            	if (myParentGroup == null || ! myParentGroup.getParentGroup().getShortName().equals(parentGroupShortName)) {
	            		logger.debug("set parent group=" + parentGroupShortName);
		            	PoGroup parentGroup = orgService.findGroupByShortName(parentGroupShortName, actClient);
		            	if (parentGroup!=null) {
		            		orgService.setParentGroup(myGroup,parentGroup);
		            	}
	            	} else {
	            		logger.debug("parent group=" + parentGroupShortName + " already set!");
	            	}
	            } 
	        }
        } catch (Exception e) {
        	this.logger.error(e);
        	throw new RuntimeException(e);
        }
		

	}
	
	private String cleanRoleName(String roleName) {
		
		// replace '_' and whitespace with an underscore!
		roleName = roleName.replaceAll("-", "_");
		roleName = roleName.replaceAll(" ", "_");
		
		return roleName;
		
	}

	@Override
	public void importRoles(Document w3cdoc) {
		
		org.jdom.Document doc = XMLTools.convertToJdomDoc(w3cdoc);
        XPath xpath;
        List results;
		try {
			xpath = XPath.newInstance("/exportdata/roles/role");
			results = xpath.selectNodes(doc);
        
	        PoRole role;
	        Iterator myItr2;
	        Iterator myItr3;
	        List rhPersons;
	        List rhGroups;
	        Element personElement;
	        Element groupElement;
	        PoPerson myPerson;
	        PoPerson roPerson;
	        PoGroup roGroup;
	        PoGroup myGroup;
	        List compGroups;
	        List compPersons;
	        int i;
	        Iterator myItr = results.iterator();
	        while(myItr.hasNext()) {
	            Element myElem = (Element)myItr.next();
	            
	            String roleParticipantId = myElem.getAttributeValue("participantid");
	            if (roleParticipantId == null)
	            	roleParticipantId = myElem.getAttributeValue("name");
	            
	            role = roleService.findRoleByParticipantId(roleParticipantId);
	            
	            if (role==null) {
	            	// try to find by name
	            	List roles = roleService.findRoleByName(roleParticipantId);
	            	if (roles.size()==1)
	            		role = (PoRole) roles.get(0);
	            	
	            	// is role still null ???
	            	// then we have to create one!
	            	if (role==null) {
	            		role = new PoRole();
		            	role.setParticipantId(cleanRoleName(roleParticipantId));
	            	}
	            }
	            
	            
	            role.setName(myElem.getAttributeValue("name"));
	            
	            
	            if(myElem.getAttribute("donotallowselfapproval")!=null)
	            	role.setDoNotAllowSelfApproval(Boolean.valueOf(myElem.getAttributeValue("donotallowselfapproval")));
	            
	            if (role.getParticipantId()==null || role.getParticipantId().equals(""))
	                role.setParticipantId(role.getName());
	            
	            role.setClient(null);
	            role.setOrgType(new Integer(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY));
	            logger.debug("create role=" + role);
	            roleService.saveRole(role);
	            
	            xpath = XPath.newInstance("/exportdata/roles/role[@name='" + role.getName() + "']/roleholder");
	            List childResults = xpath.selectNodes(doc);
	            
	            myItr2 = childResults.iterator();
	            while (myItr2.hasNext()) {
	                myElem = (Element)myItr2.next();
	                
	                if (myElem.getChild("competencetarget").getAttributeValue("type").equals("group")) {
	                    // competencetarget = group
	                    	
	                	myGroup = (PoGroup)orgService.findGroupByShortName(
	                			myElem.getChild("competencetarget").getTextTrim());
	                    if (myGroup!=null) {
		                    rhPersons = myElem.getChildren("person");
		                    myItr3 = rhPersons.iterator();
		                    i=1;
		                    while (myItr3.hasNext()) {
		                        personElement = (Element) myItr3.next();
		                        // search by employeeid if available
		                        if (personElement.getAttribute("employeeid")!=null) {
		                        	roPerson = orgService.findPersonByEmployeeId(personElement.getAttributeValue("employeeid"), new Date());
		                        } else {
		                        	roPerson = orgService.findPersonByUserName(personElement.getAttributeValue("id").replace('/',','));
		                        }
		                        if (roPerson!=null) {
		                        	// check if Person has already competence for group
		                        	compGroups = roleService.findCompetenceGroupsOfPerson(roPerson, role, new Date());
		                        	if (!compGroups.contains(myGroup)) {
		                        		// no competence is yet present -> link person
			                            roleService.assignRoleWithGroupCompetence(role, roPerson,myGroup, new java.util.Date(),null,i );                            
			                            i=i+1;
		                        	}
		                        }
		                    }
		                    rhGroups = myElem.getChildren("group");
		                    myItr3 = rhGroups.iterator();
		                    i=1;
		                    while (myItr3.hasNext()){
		                    	groupElement=(Element) myItr3.next();
		                    	roGroup = orgService.findGroupByShortName(groupElement.getAttributeValue("name"));
		                    	if (roGroup!=null){
		                    		// check if Groups has already competence for group
		                    		compGroups = roleService.findCompetenceGroupsOfGroup(roGroup, role, new Date());
		                    		if (!compGroups.contains(myGroup)) {
		                    			// no competence is yet present -> link group
		                    			roleService.assignRoleWithGroupCompetence(role, roGroup, myGroup, new java.util.Date(), null, i);
		                    			i++;
		                    		}
		                    	}
		                    }
	                    }
	                }
	                if (myElem.getChild("competencetarget").getAttributeValue("type").equals("person")) {
	                    // competencetarget = person
	                    myPerson = (PoPerson)orgService.findPersonByUserName(myElem.getChild("competencetarget").getTextTrim().replace('/',','));     
	                    
	                    if (myPerson!=null) {
		                    rhPersons = myElem.getChildren("person");
		                    myItr3 = rhPersons.iterator();
		                    i=1;
		                    while (myItr3.hasNext()) {
		                    	personElement = (Element) myItr3.next();
		                        // search by employeeid if available
		                        if (personElement.getAttribute("employeeid")!=null) {
		                        	roPerson = orgService.findPersonByEmployeeId(personElement.getAttributeValue("employeeid"), new Date());
		                        } else {
		                        	roPerson = orgService.findPersonByUserName(personElement.getAttributeValue("id").replace('/',','));
		                        }
		                        if (roPerson!=null) {
		                        	compPersons = roleService.findCompetencePersonsOfPerson(roPerson, role, new Date());
		                        	if (!compPersons.contains(roPerson)) {
			                            roleService.assignRoleWithPersonCompetence(role,roPerson, myPerson, new java.util.Date(),null,i );
			                            i=i+1;
		                        	}
		                        }
		                    }
	                    }
	                }
	                if (myElem.getChild("competencetarget").getAttributeValue("type").equals("all")) {
	                    // competencetarget = all
	                    rhPersons = myElem.getChildren("person");
	                    myItr3 = rhPersons.iterator();
	                    i=1;
	                    while (myItr3.hasNext()) {
	                        personElement = (Element) myItr3.next();
	                        // search by employeeid if available
	                        if (personElement.getAttribute("employeeid")!=null) {
	                        	roPerson = orgService.findPersonByEmployeeId(personElement.getAttributeValue("employeeid"), new Date());
	                        } else {
	                        	roPerson = orgService.findPersonByUserName(personElement.getAttributeValue("id").replace('/',','));
	                        }
	                        if (roPerson!=null) {
	                        	if (!roleService.hasPersonRoleAssignedWithCompetence4All(roPerson, role, new Date())) {
		                            roleService.assignRole(role, roPerson, new java.util.Date(),null,i );
		                            i=i+1;
	                        	}
	                        }
	                    }
	                }
	            }
	        }
	        logger.info("finished creating roles");

		} catch (JDOMException e) {
			this.logger.error(e);
			throw new PoRuntimeException(e);
		}
	}

	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}

	public void setRoleService(PoRoleService roleService) {
		this.roleService = roleService;
	}

}
