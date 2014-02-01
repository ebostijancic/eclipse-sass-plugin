package at.workflow.webdesk.po.impl.update.v27;

import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * This update Script supports referencing from <code>PoBeanProperty</code> objects to
 * <code>PoModule</code> objects. So far, this only worked with Strings.
 * 
 * @author DI Harald Entner (hentner) 19.07.2007
 */
public class PoUpdateBeanPropertySetModule extends PoAbstractUpgradeScript {
	
	@Deprecated
	public void execute() {
		logger.warn("PoUpdateBeanPropertySetModule which updates PoBeanProperties is not running any more. Due to Deprecation of PoModule.setPck");
	}

}
