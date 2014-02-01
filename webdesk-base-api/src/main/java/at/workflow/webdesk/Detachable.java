package at.workflow.webdesk;

/** 
 * Marks a config object to be detachable. Means that it is still there but cannot be 
 * used by the user. While the configuration is not lost, it is not activated.
 *  
 * @author ggruber
 *
 */
public interface Detachable {
	
	public boolean isDetached();
	
	public void reactivate();

}
