package at.workflow.webdesk.po;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoModule;

/**
 * Interface to be implemented by the workflow engine to tell
 * the Portal & Organisation Layer which Module is needed to execute the
 * referenced Process Definition.
 *  
 * @author ggruber
 *
 */
public interface ProcessReferenceModuleHandler extends ProcessReferenceDetachHandler {
	
	public PoModule discoverModuleOfAction(PoAction action);
	
	public void activateProcessReferences();
	
	public PoAction getCustomControllerActionForProcessDefId(String procDefId);
	
	
}
