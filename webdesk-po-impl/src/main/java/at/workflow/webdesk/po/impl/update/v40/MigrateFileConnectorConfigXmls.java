package at.workflow.webdesk.po.impl.update.v40;

import java.util.Iterator;

import org.jdom.transform.XSLTransformer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.tools.XMLTools;

public class MigrateFileConnectorConfigXmls extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoConnectorService connectorService = (PoConnectorService) getBean("PoConnectorService");
		PoFileService fileService = (PoFileService) getBean("PoFileService");
		PoConnector fileConnector = connectorService.findConnectorByName("seperatorFileConnector").get(0);
		
		
		if (fileConnector!=null) {
			Iterator<PoConnector> childConnItr = fileConnector.getChilds().iterator();
			while (childConnItr.hasNext()) {
				PoConnector conn = childConnItr.next();
				
				Document configXml = fileService.getFileAsXML(fileService.getFileOfConfigurable(conn).getUID());
				
				Resource res = new ClassPathResource(
						"at/workflow/webdesk/po/impl/update/v40/updateFileConnectorConfigs.xsl");
				
				org.jdom.Document resDoc;
				try {
					XSLTransformer transformer = new XSLTransformer(res.getInputStream());
					resDoc = transformer.transform(XMLTools.convertToJdomDoc(configXml));
				} catch (Exception e) {
					throw new PoRuntimeException("failed to update fileconnector config!");
				}
				
				fileService.updateConfigurationFile(conn, XMLTools.convertToW3cDoc(resDoc), "at/workflow/webdesk/" + conn.getParent().getModule().getName() + "/connector/configs/" + conn.getName() + ".xml");
				
			}
		}
		
	}

}
