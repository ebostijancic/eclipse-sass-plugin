package at.workflow.webdesk.po.impl.update.v74;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * this update script 
 * - moves the GW Job "SyncCalendarDates" form GW to TA
 * - Change "SyncCalendarDates Job" Configurable flag from false to true
 * - Create a new Job Config. "TaSyncCalendarDatesDefault"
 * - Create a Config
 * 
 * @author CStastny
 *
 */
public class MigratePoConnenctorsToCfgPoConnectors extends PoAbstractUpgradeScript {
	
	private PoConnectorService connectorService;
	private PoFileService fileService;
	private String updateVersion = "v74";
	
	public void execute() {
		try {
			this.connectorService= (PoConnectorService) getBean("PoConnectorService");
			this.fileService= (PoFileService) getBean("PoFileService");

			if ( logger.isInfoEnabled() ) {
				logger.info("Migrate PoGroup- PoPersonConnector to Configurable Connector");
			}
			updateConnector ("PoPersonConnector");
			updateConnector ("PoGroupConnector");
			
		} catch (Exception e) {
			logger.error(e);
			throw new PoRuntimeException (e);
		}
	}
	
	public void updateConnector (String connectorName) {

		if ( logger.isInfoEnabled() ) {
			logger.info("Update Connector: " + connectorName);
		}
		PoConnector parentConnector = this.findConnectorByName(connectorName);
		PoConnector defaultConnector = this.findConnectorByName("default" + connectorName);

		if ( parentConnector != null && defaultConnector == null ) {
			//---Change Parameter "isConfigurable" --> true
			parentConnector.setConfigurable (true);
			this.connectorService.saveConnector(parentConnector);
			Collection destinationConnectorLinks = parentConnector.getDestConnectorLinks();
			Collection sourceConnectorLinks = parentConnector.getSrcConnectorLinks();
			//---New Connector
			if ( logger.isInfoEnabled() ) {
				logger.info("Insert new Connector: " + connectorName);
			}
			defaultConnector = new PoConnector();
			defaultConnector.setParent(parentConnector);
			defaultConnector.setClassName(null);
			defaultConnector.setConfigurable(false);
			defaultConnector.setName("default" + connectorName);
			defaultConnector.setUpdateOnVersionChange(false);
			defaultConnector.setWriteable(true);
			
			this.connectorService.saveConnector(defaultConnector);
			
			// Create 
			String defaultConfigXmlFileName = "at/workflow/webdesk/po/impl/update/" 
				+ updateVersion + "/default" + connectorName + ".xml";
			
			if ( logger.isInfoEnabled() ) {
				logger.info("Create Default Config. for Connector: " + connectorName + ", Filename " + defaultConfigXmlFileName);
			}
			Resource xmlRes = new ClassPathResource( defaultConfigXmlFileName );

			SAXBuilder builder = new SAXBuilder();
			Document jdomDoc = null;
			try {
				// Create Config. 
				jdomDoc = builder.build(xmlRes.getInputStream());
				
			} catch (JDOMException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
			org.w3c.dom.Document configDoc = XMLTools.convertToW3cDoc(jdomDoc);

			this.fileService.updateConfigurationFile(defaultConnector, configDoc, 
					"at/workflow/webdesk/po/connector/configs/default" + connectorName + ".xml");
			
			//---
			updateConnectorLink(defaultConnector, parentConnector, destinationConnectorLinks);
			updateConnectorLink(defaultConnector, parentConnector, sourceConnectorLinks);
			int i = 0;
		}
	}
	
	private void updateConnectorLink (PoConnector defaultConnector, PoConnector parentConnector, 
			Collection connectorLinksCollection) {
		
		//---Change Linked Connector to new default-Connector
		Iterator connectorLinksItr = connectorLinksCollection.iterator();
		while ( connectorLinksItr.hasNext() ) {
			PoConnectorLink connectorLink = (PoConnectorLink) connectorLinksItr.next();
			if ( connectorLink.getDestConnector().getUID().equals( parentConnector.getUID() ) ) {
				if ( logger.isInfoEnabled() ) {
					logger.info("Change LinkedConnector: " + connectorLink.getName() + ", change Destination Connector!" );
				}
				connectorLink.setDestConnector(defaultConnector);
				this.connectorService.saveConnectorLink(connectorLink);
			}
		}
		
	}
	
	private PoConnector findConnectorByName (String connectorName) {
		
		if ( logger.isInfoEnabled() ) {
			logger.info("Find Connector: " + connectorName);
		}
		PoConnector connector = null;
		List connectorList = this.connectorService.findConnectorByName(connectorName);
		Iterator connectorItr = connectorList.iterator();
		while ( connectorItr.hasNext()){
			connector = (PoConnector) connectorItr.next();

			if ( connector.getParent() == null) {
				if ( logger.isInfoEnabled() ) {
					logger.info("Found Connector: " + connectorName);
				}
				return connector;
			}
		}
		return connector;
	}
}
