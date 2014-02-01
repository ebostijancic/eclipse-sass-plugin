package at.workflow.webdesk.po.update;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoFile;

public abstract class  PoAbstractAdjustConfigsUpdateScript extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		
		PoActionService actionService = (PoActionService) getBean("PoActionService");

		PoAction action = actionService.findActionByNameAndType(getActionNameToProcess(), PoConstants.ACTION_TYPE_ACTION);
		if (action != null) {
			List<PoAction> configs = actionService.findConfigsFromAction(action);
			Iterator<PoAction> configsIt = configs.iterator();
			while (configsIt.hasNext()) {
				PoAction config = configsIt.next();
				adjustActionConfig(config);
			}
		}

	}

	public void adjustActionConfig(PoAction actionConfig) {
		PoFileService fileService = (PoFileService) getBean("PoFileService");
		try {
			PoFile file = fileService.getFileOfConfigurable(actionConfig);
			Document w3cDoc = fileService.getFileAsXML(file);
			
			if (isAdjustmentNecessary(w3cDoc)) {
				w3cDoc = adjustActionConfigXML(w3cDoc);
				fileService.updateFileOfAction(actionConfig, w3cDoc);
				
				logger.info("Sucessfully adjusted Action-Configuration: " + actionConfig);
			}
		} catch(Exception e) {
			logger.warn("Problems occured while trying to set default values in config: "+actionConfig);
			logger.error(e);
		}
	}
	
	
	protected abstract String getActionNameToProcess();
	
	public abstract Document adjustActionConfigXML(Document actionConfigXML);
	
	public abstract boolean isAdjustmentNecessary(Document actionConfigXML);

}
