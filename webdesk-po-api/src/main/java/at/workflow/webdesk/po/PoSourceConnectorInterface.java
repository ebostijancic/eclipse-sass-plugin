package at.workflow.webdesk.po;

import java.util.List;
import java.util.Map;

/**
 * A source-connector reads data from some medium like database or CSV-file.
 */
public interface PoSourceConnectorInterface extends PoConnectorInterface {

	/**
	 * This function will be called after all objects have been imported
	 * (and the destination-connector's post-process has been called).
	 * It can be used for functionality that needs to be performed upon the
	 * full collection of imported records, e.g. to set some "wasTransferred"
	 * flags in exports source objects.
	 *
	 * @param records list of exported data-sets (records) that really have
	 * 		been imported by the destination-connector.
	 */
	public void postProcessImportedRecords(List<Map<String,Object>> records);

}
