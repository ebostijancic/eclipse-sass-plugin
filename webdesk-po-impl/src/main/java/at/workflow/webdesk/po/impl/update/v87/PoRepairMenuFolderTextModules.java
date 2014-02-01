package at.workflow.webdesk.po.impl.update.v87;

import java.util.List;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Repairs Textmodules of Menuitem folders, which are linked to the default language
 * and start with [DefaultLangCode], where repairing means it replaces the leading
 * String '[DefaultLangCode] '
 * 
 * @author ggruber
 */
public class PoRepairMenuFolderTextModules extends PoAbstractUpgradeScript {

	@Override
	@SuppressWarnings("unchecked")
	public void execute() {
		PoGeneralDbService generalDbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		PoLanguageService languageService = (PoLanguageService) getBean("PoLanguageService");
		
		int count=0;
		List<PoMenuItem> folders = generalDbService.getElementsAsList("from PoMenuItem where parent is null", null);
		PoLanguage defLang = languageService.findDefaultLanguage();
		String searchString = "[" + defLang.getCode() + "] ";
		String replaceString = "\\[" + defLang.getCode() + "\\] ";
		
		for (PoMenuItem folder : folders) {
			PoTextModule folderTextModule = languageService.findTextModuleByNameAndLanguage(folder.getTextModuleKey(), defLang);
			if (folderTextModule !=null && folderTextModule.getValue().startsWith(searchString)) {
				folderTextModule.setValue(folderTextModule.getValue().replaceFirst(replaceString, ""));
				languageService.saveTextModule(folderTextModule);
				count++;
			}
		}
		logger.info("successfully repaired folder textmodule value on " + count + " folders.");
	}

}
