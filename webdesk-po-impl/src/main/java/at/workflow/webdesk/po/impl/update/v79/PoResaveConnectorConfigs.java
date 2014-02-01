package at.workflow.webdesk.po.impl.update.v79;

import java.util.List;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

public class PoResaveConnectorConfigs extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoGeneralDbService generalDbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		
		@SuppressWarnings("unchecked")
		List<PoConnector> connectors = generalDbService.getElementsAsList("from PoConnector where module is null", null);
		
		for (PoConnector connector : connectors) {
			if (connector.getParent()!=null) {
				connector.setModule(connector.getParent().getModule());
				generalDbService.saveObject(connector);
			}
		}
	}

}
