package at.workflow.webdesk.po.impl.update.v94;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.workflow.tools.XPathTools;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * The task of this script is to download every dbConnector configuration
 * and set dataSourceType = manual
 * 
 * @author sdzuban 24.07.2012
 */
public class PoUpdateDBConnectorConfigs extends PoAbstractUpgradeScript {

	/** {@inheritDoc} */
	@Override
	public void execute() {

		XPathTools xPath = new XPathTools();
		PoFileService fileService = (PoFileService) getBean("PoFileService");
		PoConnectorService connService = (PoConnectorService) getBean("PoConnectorService");
		List<PoConnector> dbConnectors = connService.findConnectorByName("dbConnector");

		if (dbConnectors == null || dbConnectors.isEmpty())
			return;
		
		List<PoConnector> allConnectors = connService.loadAllConnectors();
		List<PoConnector> dbConfigs = new ArrayList<PoConnector>();
		// collect configurations of dbConnector
		for (PoConnector connector : allConnectors)
			if (dbConnectors.contains(connector.getParent()))
				dbConfigs.add(connector);
	
		for (PoConnector connector : dbConfigs) {
			
			PoFile file = fileService.getFileOfConfigurable(connector);
			if (file == null) 
				continue;
			
			Document configXml = fileService.getFileAsXML(file.getUID());
			// check, if the node is already there
			Node dataSourceTypeNode = xPath.getNodeWithXPath(configXml, "/config/dataSourceType");
			if (dataSourceTypeNode != null)
				continue;
			
			// add "manual" to display the old configuration
			Node node = xPath.getNodeWithXPath(configXml, "/config");
			Element dataSourceType = configXml.createElement("dataSourceType");
			dataSourceType.setTextContent("manual");
			node.appendChild(dataSourceType);
//			String configAsString = XMLTools.createStringFromW3cDoc(configXml);
//			System.out.println(configAsString);
			fileService.updateConfigurationFile(connector, configXml, file.getPath());
		}
	}
}
