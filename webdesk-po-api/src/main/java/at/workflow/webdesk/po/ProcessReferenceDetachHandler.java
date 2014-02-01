package at.workflow.webdesk.po;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * This Interface ensures that there are not direct dependencies from PO to the WF module. As
 * the Portal & Organisation core also has a notion of PoAction objects with type 'ProcessReference'
 * it needs an implementation for the case, where the Workflow Module (wf) was - for whatever reason -
 * detached (maybe due to the removal of the needed licence). Then of course we have to detach *ALL*
 * actions of that type. If the workflow module has not been loaded, we have a default implemenation
 * which is right within the interface.
 * 
 * @author ggruber
 *
*/
public interface ProcessReferenceDetachHandler {
	
	public void detachProcessReferences();
	
	public class ProcessReferenceDetachHandlerImpl implements ProcessReferenceDetachHandler {

		protected PoActionService actionService;
		
		protected List<PoAction> findCurrentProcessReferences() {
			List<PoAction> ret = new ArrayList<PoAction>();
			List<PoAction> allActions = this.actionService.loadAllActions();
			
			for (PoAction action : allActions) {
				if (action.getActionType() == PoConstants.ACTION_TYPE_PROCESS && HistorizationHelper.isValid(action)) {
					ret.add(action);
				}
			}
			
			return ret;
		}

		@Override
		public void detachProcessReferences() {

			List<PoAction> procRefs = findCurrentProcessReferences();
			for (PoAction action : procRefs) {
				action.setDetached(true);
				this.actionService.saveAction(action);
			}
			
		}

		public void setActionService(PoActionService actionService) {
			this.actionService = actionService;
		}
		
	}

}
