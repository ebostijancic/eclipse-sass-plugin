package at.workflow.webdesk.po.impl.update.v86;

import java.util.List;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * This Updatescript processes all PoMenuItem objects which represent folders and have
 * an empty 'textModuleKey' property. This property will be set with a useful value
 * in this script and persisted afterwards.
 * 
 * @author ggruber
 */
public class PoSetMenuFolderTextModuleKeys extends PoAbstractUpgradeScript {

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		PoGeneralDbService generalDbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		PoMenuService menuService = (PoMenuService) getBean("PoMenuService");
		List<PoMenuItem> folders = (List<PoMenuItem>) generalDbService.getElementsAsList("from PoMenuItem where parent is null and textModuleKey is null", null);
		for (PoMenuItem folder : folders) {
			folder.setTextModuleKey(menuService.getMenuFolderTextModuleKey(folder));
			menuService.saveMenuItem(folder);
		}
		this.logger.info("successfully set textmoduleKey on " + folders.size() + " folders.");
	}

}
