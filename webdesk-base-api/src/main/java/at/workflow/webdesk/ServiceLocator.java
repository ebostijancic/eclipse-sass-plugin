package at.workflow.webdesk;

/**
 * Locate a Spring service. Use this for loose coupled service bindings
 * where the caller can not be sure that the service is actually available. 
 *  
 * @author ggruber
 */
public interface ServiceLocator {

	/** @return the service, or null if it is not available. */
	public abstract Object lookupService();
	
	/** @return true if the service is available (installation-specific). */
	public abstract boolean isServiceAvailable();

}
