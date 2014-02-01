package at.workflow.webdesk.po.impl.update.v90;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Sets the BeanProperty GlobalUserLayoutOptions.forceFullAjax to 'true' and injects it.
 * @author ggruber
 */
public class ActivateFullAjax  extends PoAbstractUpgradeScript{

	@Override
	public void execute() {
		PoBeanPropertyService bps = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		
		PoBeanProperty bp = bps.findBeanPropertyByKey("GlobalUserLayoutOptions", "forceFullAjax");
		try {
			bps.updateBeanValueAndInject(bp, "true");
		}
		catch (Exception e) {
			logger.warn("Could not activate fullAjax");
		}
		
	}

}
