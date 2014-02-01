/**
 * 
 */
package at.workflow.webdesk.po;

import java.util.Collection;

import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoContextParameter;

/**
 * This interface defines method for getting of process variables and presenting them as
 * PoActionParameters.
 * 
 * sdzuban 18.04.2011
 */
public interface PoProcessParameterAccessor {
	
	/**
	 * Retrieves process variables and provides them as PoActionParameters.
	 * @param fullProcDefId
	 * @return
	 */
	public Collection<PoActionParameter> getProcessActionParameters(String fullProcDefId);

	/**
	 * Retrieves process context variables and provides them as PoContextParameters.
	 * @param fullProcDefId
	 * @return
	 */
	public Collection<PoContextParameter> getProcessContextParameters(String fullProcDefId);
	
}
