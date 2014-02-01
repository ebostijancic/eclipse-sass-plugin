package at.workflow.webdesk.po.impl.update.v93;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Sets the BeanProperty GlobalUserLayoutOptions.onlyShowCurrentPathInMenu to 'true' and injects it.
 * @author ggruber
 */
public class ActivateShowOnlyCurrentPathInMenu  extends PoAbstractUpgradeScript{

	@Override
	public void execute() {
		PoBeanPropertyService bps = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		
		PoBeanProperty bp = bps.findBeanPropertyByKey("GlobalUserLayoutOptions", "onlyShowCurrentPathInMenu  ");
		try {
			bps.updateBeanValueAndInject(bp, "true");
		}
		catch (Exception e) {
			logger.warn("Could not activate onlyShowCurrentPathInMenu  ");
		}
		
	}

}
