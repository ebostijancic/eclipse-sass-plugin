package at.workflow.webdesk.po.impl.adminactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;

public class RepairBrokenActionCaptions extends AbstractAdminAction {

	private PoLanguageService languageService;
	private PoGeneralDbService generalDbService;
	
	@Override
	public void run() {
		
		PoLanguage language = languageService.findDefaultLanguage();
		String langCode = language.getCode();
		Object[] keys = { language };
		
		@SuppressWarnings("unchecked")
		List<PoTextModule> textModules = generalDbService.getElementsAsList("from PoTextModule where language=? and (name like '%_action_caption' or name like '%action_description' ) and substring(value,1,4) = '[" + langCode + "]'", keys);
		
		List<PoTextModule> textModulesToRepair = new ArrayList<PoTextModule>();
		
		Iterator<PoTextModule> tmItr = textModules.iterator();
		while (tmItr.hasNext()) {
			PoTextModule tm = tmItr.next();
			if (tm.getValue().startsWith("[" + langCode + "] ")) {
				tm.setValue(tm.getValue().substring(2+langCode.length()));
				this.languageService.saveTextModule(tm);
				textModulesToRepair.add(tm);
			}
			
		}

	}

	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	public void setGeneralDbService(PoGeneralDbService generalDBService) {
		this.generalDbService = generalDBService;
	}

}
