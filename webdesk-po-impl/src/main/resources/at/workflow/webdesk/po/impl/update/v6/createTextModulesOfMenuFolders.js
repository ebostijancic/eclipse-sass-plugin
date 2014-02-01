logger.info("***********************************************");
logger.info("** Update Script createTextModulesOfMenuFolders.js: Create TextModules of Folderitems in the Menutree. (Action: editMenuTree.act) **");
logger.info("***********************************************");


var PoActionService = appCtx.getBean("PoActionService");
var PoMenuService = appCtx.getBean("PoMenuService");
var PoLanguageService = appCtx.getBean("PoLanguageService");
var PoModuleService = appCtx.getBean("PoModuleService");

var languages = PoLanguageService.loadAllLanguages();
var defLanguage = PoLanguageService.findDefaultLanguage();

var action = PoActionService.findActionByNameAndType("po_editMenuTree", Packages.at.workflow.webdesk.po.PoConstants.ACTION_TYPE_ACTION);
var module = PoModuleService.getModuleByName(action.getActionFolder());

var menuItems = PoMenuService.findAllCurrentMenuItems();
var miIt = menuItems.iterator();
while (miIt.hasNext()) {
	var mi = miIt.next();
	if (mi.getAction() == null) {
		//MenuItem is a Folder
		try {
			var textModuleKey = mi.getTextModuleKey();
			if (textModuleKey == null) {
				textModuleKey = "po_editMenuTree.act_menufolder_"+mi.getClient().getName()+"_"+mi.getUID();
				mi.setTextModuleKey(textModuleKey);
			}
			var tm = PoLanguageService.findTextModuleByNameAndLanguage(textModuleKey, defLanguage);
			if (tm == null) {
				var lngIt = languages.iterator();
				while (lngIt.hasNext()) {
					var lng = lngIt.next();
					var newTm = PoLanguageService.findTextModuleByNameAndLanguage(mi.getTextModuleKey(), lng);
					if (newTm == null) {
						var newTm = new Packages.at.workflow.webdesk.po.model.PoTextModule();
						
				    	newTm.setAction(action);
				    	newTm.setLanguage(lng);
				    	newTm.setModule(module);
				    	
				    	//var tmKey = "po_editMenuTree.act_menufolder_"+mi.getClient().getName()+"_"+mi.getName();
				    	newTm.setName(textModuleKey);
				    	//newTm.setName(mi.getTextModuleKey());
					}
			    	if (lng == defLanguage) {
			    		newTm.setValue(mi.getName());
			    	} else {
			    		newTm.setValue("["+lng.getCode()+"] "+mi.getName());	    		
			    	}
			    	PoLanguageService.saveTextModule(newTm);
			    	logger.info("Sucessfully created TextModule of Folderitem: " + newTm);
				}
			}
		} catch(e) {
			logger.warn("Problems creating TextModules of Folderitems: "+mi);
			if (e.javaException != null)
				logger.error(e,e.javaException);
			else
				logger.error(e);
		}
	}
}
