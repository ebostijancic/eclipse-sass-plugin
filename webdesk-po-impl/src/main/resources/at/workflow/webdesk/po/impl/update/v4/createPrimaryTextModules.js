logger.info("***********************************************");
logger.info("** Update Script createPrimaryTextModules.js: Create Primary TextModules for all Current Actions and Configs.");
logger.info("***********************************************");

var PoActionService = appCtx.getBean("PoActionService");

var actions = PoActionService.loadAllCurrentActions();
var itr = actions.iterator();
while (itr.hasNext()) {
	try {
		var a = itr.next();
		PoActionService.createPrimaryTextModulesOfAction(a);
		logger.info("UpgradeScript: Create Primary TextModules for Action " + a.getName());
	} catch(e) {
		logger.warn("Problems creating Primary TextModules for Action " + a.getName());
		if (e.javaException != null)
			logger.error(e,e.javaException);
		else
			logger.error(e);
	}
}

var configs = PoActionService.findAllCurrentConfigs();
var itr = configs.iterator();
while (itr.hasNext()) {
	try {
		var c = itr.next();
		logger.info("UpgradeScript: Create Primary TextModules for Config " + c.getName());
		PoActionService.createPrimaryTextModulesOfAction(c);
	} catch(e) {
		logger.warn("Problems creating Primary TextModules for Config " + c.getName());
		if (e.javaException != null)
			logger.error(e,e.javaException);
		else
			logger.error(e);
	}
}

