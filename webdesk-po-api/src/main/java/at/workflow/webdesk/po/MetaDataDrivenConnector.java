package at.workflow.webdesk.po;

import at.workflow.webdesk.po.model.PoConnector;

/**
 * Connectors that show behaviour driven by configurable persistent meta-data
 * implement this interface.
 */
public interface MetaDataDrivenConnector	{
    
	/**
	 * @param connectorSettings base settings of this connector as read from the webdesk database
	 * 		(is editable at runtime via UI). This contains information about peer-connectors etc.
	 * 		A PoConnector object is created automatically for every new connector, and can be
	 * 		customized by webdesk admins/users afterwards.
	 */
	public void setConnectorMetaData(PoConnector connectorSettings);

}
