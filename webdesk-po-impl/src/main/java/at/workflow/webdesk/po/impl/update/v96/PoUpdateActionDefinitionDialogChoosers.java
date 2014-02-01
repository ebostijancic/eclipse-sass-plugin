package at.workflow.webdesk.po.impl.update.v96;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * Ensure that defaultviewpermissiontype for po_choosePersonDialog.act and po_chooseGroupDialog.act is 0!
 * @author ggruber
 */
public class PoUpdateActionDefinitionDialogChoosers extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		
		resetDefaultViewPermissionType("po_choosePersonDialog.act");
		resetDefaultViewPermissionType("po_chooseGroupDialog.act");
	}

	private void resetDefaultViewPermissionType(String actionToChange) {
		
		PoActionService actionService = (PoActionService) getBean("PoActionService");
		PoAction chooser = actionService.findActionByURL(actionToChange);
		if (chooser!=null) {
			chooser.setDefaultViewPermissionType( PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON );
			actionService.saveAction(chooser);
		}
		
	}

}
