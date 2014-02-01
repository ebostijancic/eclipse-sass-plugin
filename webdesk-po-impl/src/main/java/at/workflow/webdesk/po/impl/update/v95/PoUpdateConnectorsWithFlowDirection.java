package at.workflow.webdesk.po.impl.update.v95;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import at.workflow.tools.XPathTools;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.model.FlowDirection;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * The task of this script is to update every Connector 
 * with flowDirection corresponding to its capabilities.
 * 
 * @author sdzuban 24.07.2012
 */
public class PoUpdateConnectorsWithFlowDirection extends PoAbstractUpgradeScript {

	private PoFileService fileService;
	private PoConnectorService connService;
	private XPathTools xPath = new XPathTools();
	
	/** {@inheritDoc} */
	@Override
	public void execute() {

		fileService = (PoFileService) getBean("PoFileService");
		connService = (PoConnectorService) getBean("PoConnectorService");
		
		List<PoConnector> dbConnectors = connService.findConnectorByName("dbConnector");
		List<PoConnector> separatorFileConnectors = connService.findConnectorByName("seperatorFileConnector");
		
		List<PoConnector> allConnectors = connService.loadAllConnectors();

		if (allConnectors == null || allConnectors.isEmpty())
			return;
		
		for (PoConnector connector : allConnectors) {
			
			try {
				if (dbConnectors.contains(connector.getParent()))
					updateDbConnector(connector);
				else if (separatorFileConnectors.contains(connector.getParent()))
					updateSeparatorFileConnector(connector);
				else
					updateConnector(connector);
				
				logger.info("Setting FlowDirection of " + connector.getName() + " to " + connector.getFlowDirection());
				connService.saveConnector(connector);
			} catch (Exception e) {
				logger.error("Problems occured, while setting FlowDirection of " + connector.getName(), e);
			}
			
		}
	}

	private void updateConnector(PoConnector connector) {
		connector.setFlowDirection( connService.getFlowDirectionByInspection(connector) );
	}

	@SuppressWarnings("deprecation")
	private void updateDbConnector(PoConnector connector) {
		
		if (!connector.isWriteable())
			connector.setFlowDirection(FlowDirection.SOURCE);
		else {
			PoFile file = fileService.getFileOfConfigurable(connector);
			if (file == null) 
				connector.setFlowDirection(FlowDirection.DESTINATION);
			else {
				Document configXml = fileService.getFileAsXML(file.getUID());
				String select = xPath.getTextContent(configXml, "/config/selectStatement");
				String from = xPath.getTextContent(configXml, "/config/fromStatement");
				String where = xPath.getTextContent(configXml, "/config/whereStatement");
				String mainTable = xPath.getTextContent(configXml, "/config/mainTable");
				String fromTable = null;
				if (from != null) {
					String[] fromAndTable = from.trim().split(" ");
					if (fromAndTable.length > 1)
						fromTable = fromAndTable[1]; // 0 is "from"
				}
				if (StringUtils.isBlank(select) && StringUtils.isBlank(where) &&
						(fromTable != null && fromTable.equalsIgnoreCase(mainTable) ||
						mainTable != null && mainTable.equalsIgnoreCase(fromTable)))
					connector.setFlowDirection(FlowDirection.SOURCE_AND_DESTINATION);
				else
					connector.setFlowDirection(FlowDirection.DESTINATION);
			}
		}
	}

	private void updateSeparatorFileConnector(PoConnector connector) {
		
		PoFile file = fileService.getFileOfConfigurable(connector);
		if (file == null) 
			connector.setFlowDirection(null);
		
		Document configXml = fileService.getFileAsXML(file.getUID());
		String inputFile = xPath.getTextContent(configXml, "/config/download/actualUsedFile");
		String fileOnServer = xPath.getTextContent(configXml, "/config/writeToFilePathAtServer");
		String document = xPath.getTextContent(configXml, "/config/writeToDocumentWithPattern");
		
		if (StringUtils.isNotBlank(inputFile) && 
				(StringUtils.isNotBlank(fileOnServer) || StringUtils.isNotBlank(document)))
			connector.setFlowDirection(FlowDirection.SOURCE_AND_DESTINATION);
		else if (StringUtils.isNotBlank(inputFile)) 
			connector.setFlowDirection(FlowDirection.SOURCE);
		else
			connector.setFlowDirection(FlowDirection.DESTINATION);
	}
}
