package at.workflow.webdesk.po;


import at.workflow.webdesk.po.model.PoAction;

/**
 * This interface has to be implemented by action configs, where additional processing is necessary after registration of 
 * the corresponding ActionConfig. 
 * 
 * @author ggruber
 */
public interface ActionConfigRegistrationPostProcessor {

	/** implement this method to do further processing of the action configuration, after it was registered */
	public void afterRegistration(PoAction action);
	
	/** return the Name of the action without postfix, but with module prefix, f.i. ta_getJournal */
	public String appliesToAction();
}
