package at.workflow.webdesk.po.impl.update.v91;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Deletes the old BeanPropertyValue 'PoUtilServiceTarget->timeToLiveLoginToken'
 * which is not used any more.
 */
public class RemoveTimeToLiveLoginTokenProperty extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoBeanPropertyService beanPropertyService = (PoBeanPropertyService) getBean("PoBeanPropertyService");
		PoBeanProperty bp = beanPropertyService.findBeanPropertyByKey("PoUtilServiceTarget", "timeToLiveLoginToken");
		if (bp != null) {
			beanPropertyService.deleteBeanProperty(bp);
		}
	}

}
