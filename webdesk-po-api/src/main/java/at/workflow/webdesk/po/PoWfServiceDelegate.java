package at.workflow.webdesk.po;

import java.util.Collection;
import java.util.List;

import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoContextParameter;

/**
 * delegate interface for access to selected WfProcessDefinition infos via PoProcessDefinition
 * 
 * @author sdzuban
 *
 */
public interface PoWfServiceDelegate {
	
	public class PoProcessDefinition implements Comparable<PoProcessDefinition> {
		
		private String name;
		private String pckId;
		private String procDefId;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPckId() {
			return pckId;
		}
		public void setPckId(String pckId) {
			this.pckId = pckId;
		}
		public String getProcDefId() {
			return procDefId;
		}
		public void setProcDefId(String procDefId) {
			this.procDefId = procDefId;
		}
		
		@Override
		public int compareTo(PoProcessDefinition o) {
			if (name == null && o.getName() == null)
				return 0;
			else if (name == null)
				return -1;
			else 
				return name.compareToIgnoreCase(o.getName());
		}
	}

	public List<PoProcessDefinition> loadAllCurrentProcessDefinitions();

    /**
     * @param fullProcDefId
     * @return Collection of PoActionParameter
     * 
     * Loads processdefinition for given id. Id must be provided in the form of
     * the unique id, with which the definition is identified inside the workflow
     * system. f.i. for Shark the id looks like   PckId#VersNo.#ProcessDefId
     * OR the procDefName which is the Id without a version, f.i. PckId$ProcessDefId
     * - this will return the collection of PoActionParameter of latest corresponding Processdefinition.
     */
	public Collection<PoActionParameter> getProcessActionParameters(String fullProcDefId);

	
	/**
	 * @param fullProcDefId
	 * @return Collection of PoContextParameter
	 * 
	 * Loads processdefinition for given id. Id must be provided in the form of
	 * the unique id, with which the definition is identified inside the workflow
	 * system. f.i. for Shark the id looks like   PckId#VersNo.#ProcessDefId
	 * OR the procDefName which is the Id without a version, f.i. PckId$ProcessDefId
	 * - this will return the collection of PoContextParameter of latest corresponding Processdefinition.
	 */
	public Collection<PoContextParameter> getProcessContextParameters(String fullProcDefId);
}
